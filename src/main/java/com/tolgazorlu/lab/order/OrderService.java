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

    public OrderService(OrderRepository orders, ProductRepository products,
                        DealerRepository dealers, PaymentClient payments) {
        this.orders = orders;
        this.products = products;
        this.dealers = dealers;
        this.payments = payments;
    }

    @Transactional
    public OrderView place(PlaceOrderRequest request) {
        Dealer dealer = dealers.findById(request.dealerId())
                .orElseThrow(() -> new NotFoundException("dealer " + request.dealerId()));

        Order order = new Order(dealer);
        for (PlaceOrderRequest.Line line : request.lines()) {
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("product " + line.productId()));

            // LAB (race condition): read, check, write. Not atomic.
            if (product.getStock() < line.quantity()) {
                throw new OutOfStockException(product.getId());
            }
            product.setStock(product.getStock() - line.quantity());

            order.addItem(new OrderItem(order, product, line.quantity()));
        }

        // LAB (slow dependency): external HTTP call while the DB transaction is open.
        String paymentRef = payments.charge(dealer.getId(), order.getTotalCents());
        order.markPaid(paymentRef);

        return OrderView.from(orders.save(order));
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
