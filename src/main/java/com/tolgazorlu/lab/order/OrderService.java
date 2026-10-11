package com.tolgazorlu.lab.order;

import com.tolgazorlu.lab.catalog.Product;
import com.tolgazorlu.lab.catalog.ProductRepository;
import com.tolgazorlu.lab.common.NotFoundException;
import com.tolgazorlu.lab.common.OutOfStockException;
import com.tolgazorlu.lab.dealer.Dealer;
import com.tolgazorlu.lab.dealer.DealerRepository;
import com.tolgazorlu.lab.payment.PaymentClient;
import java.util.List;
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
            paymentRef = payments.charge(request.dealerId(), pending.getTotalCents());
        } catch (RuntimeException e) {
            tx.executeWithoutResult(status -> release(orderId));
            throw e;
        }
        return tx.execute(status -> {
            Order order = orders.findById(orderId).orElseThrow();
            order.markPaid(paymentRef);
            return OrderView.from(order);
        });
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

    private void release(Long orderId) {
        Order order = orders.findById(orderId).orElseThrow();
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
