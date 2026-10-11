package com.tolgazorlu.lab.order;

import com.tolgazorlu.lab.catalog.Product;
import com.tolgazorlu.lab.catalog.ProductRepository;
import com.tolgazorlu.lab.common.NotFoundException;
import com.tolgazorlu.lab.common.OutOfStockException;
import com.tolgazorlu.lab.dealer.Dealer;
import com.tolgazorlu.lab.dealer.DealerRepository;
import com.tolgazorlu.lab.payment.PaymentClient;
import java.util.List;
import java.util.Optional;
import com.tolgazorlu.lab.payment.PaymentOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The heart of the system: placing an order.
 *
 * Version 0 has several real-world bugs on purpose. Each one is a lab:
 *  - stock check + update is a race (two dealers can buy the last item)
 *  - the payment call runs INSIDE the database transaction (holds a pool connection)
 *  - the payment call has no timeout, no retry policy, no circuit breaker
 *  - a client retry creates a second order (no idempotency key)
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final ProductRepository products;
    private final DealerRepository dealers;
    private final PaymentClient payments;
    private final TransactionTemplate tx;

    public OrderService(OrderRepository orders, ProductRepository products,
                        DealerRepository dealers, PaymentClient payments, TransactionTemplate tx) {
        this.tx = tx;
        this.orders = orders;
        this.products = products;
        this.dealers = dealers;
        this.payments = payments;
    }

    /**
     * Mission 05: three short steps instead of one long transaction.
     * 1. tx: reserve stock and write a PENDING order (holds a connection for milliseconds)
     * 2. no tx: charge the payment provider (may take seconds, holds no connection)
     * 3. tx: mark PAID, or FAILED and give the stock back
     */
    public OrderView place(PlaceOrderRequest request) {
        Long orderId = tx.execute(status -> reserve(request));
        Order pending = tx.execute(status -> orders.findById(orderId).orElseThrow());
        String paymentRef;
        try {
            paymentRef = payments.charge(orderId, request.dealerId(), pending.getTotalCents());
        } catch (RuntimeException e) {
            if (PaymentOutcome.definitelyNotCharged(e)) {
                tx.executeWithoutResult(status -> settle(orderId, Optional.empty()));
                throw e;
            }
            // Mission 07: the provider may have charged. Keep the stock reserved, do not call it FAILED,
            // and let PaymentReconciler ask the provider what really happened.
            log.warn("payment outcome unknown for order {}: {}", orderId, e.getMessage());
            return tx.execute(status -> {
                Order order = orders.findForUpdate(orderId).orElseThrow();
                if (order.isOpen()) order.markPaymentUnknown();
                return OrderView.from(order);
            });
        }
        return tx.execute(status -> settle(orderId, Optional.of(paymentRef)));
    }

    /**
     * Records a payment outcome exactly once: PAID with the provider's id, or FAILED with the stock
     * returned. Called by place() and by PaymentReconciler, so it locks the order row first.
     * Must run inside a transaction.
     */
    public OrderView settle(Long orderId, Optional<String> paymentRef) {
        Order order = orders.findForUpdate(orderId).orElseThrow();
        if (!order.isOpen()) {
            if (paymentRef.isPresent() && !"PAID".equals(order.getStatus())) {
                // Money moved for an order we already gave up on. A human (or a refund job) must act.
                log.error("order {} is {} but the provider charged it ({}): refund needed",
                        orderId, order.getStatus(), paymentRef.get());
            }
            return OrderView.from(order);
        }
        if (paymentRef.isPresent()) {
            order.markPaid(paymentRef.get());
        } else {
            release(order);
        }
        return OrderView.from(order);
    }

    private Long reserve(PlaceOrderRequest request) {
        Dealer dealer = dealers.findById(request.dealerId())
                .orElseThrow(() -> new NotFoundException("dealer " + request.dealerId()));
        Order order = new Order(dealer);
        for (PlaceOrderRequest.Line line : request.lines()) {
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("product " + line.productId()));
            if (products.reserveStock(product.getId(), line.quantity()) == 0) {
                throw new OutOfStockException(product.getId());
            }
            order.addItem(new OrderItem(order, product, line.quantity()));
        }
        return orders.save(order).getId();
    }

    private void release(Order order) {
        for (OrderItem item : order.getItems()) {
            products.releaseStock(item.getProduct().getId(), item.getQuantity());
        }
        order.markFailed();
    }

    @Transactional(readOnly = true)
    public List<OrderView> recentOrders(Long dealerId, int limit) {
        // LAB (N+1): 1 query for orders, then 1 per order for items, then 1 per item for product.
        return orders.findByDealerIdOrderByCreatedAtDesc(dealerId, PageRequest.of(0, limit))
                .stream()
                .map(OrderView::from)
                .toList();
    }
}
