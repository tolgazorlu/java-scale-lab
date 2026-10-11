package com.tolgazorlu.lab.order;

import com.tolgazorlu.lab.payment.PaymentClient;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Mission 07: settles orders whose payment outcome we do not know, by asking the provider for the
 * charge with the order's reference. Found: PAID. Not found: FAILED and the stock goes back.
 * Provider unreachable: try again on the next run.
 */
@Component
class PaymentReconciler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciler.class);

    private final OrderRepository orders;
    private final OrderService service;
    private final PaymentClient payments;
    private final TransactionTemplate tx;
    private final MeterRegistry meters;

    PaymentReconciler(OrderRepository orders, OrderService service, PaymentClient payments,
                      TransactionTemplate tx, MeterRegistry meters) {
        this.orders = orders;
        this.service = service;
        this.payments = payments;
        this.tx = tx;
        this.meters = meters;
    }

    @Scheduled(initialDelay = 5_000, fixedDelay = 5_000)
    void reconcile() {
        Instant now = Instant.now();
        List<Long> ids = orders.findToReconcile(now.minusSeconds(10), now.minusSeconds(60), PageRequest.of(0, 200));
        for (Long id : ids) {
            String outcome;
            try {
                Optional<String> paymentRef = payments.lookup(id);
                tx.executeWithoutResult(status -> service.settle(id, paymentRef));
                outcome = paymentRef.isPresent() ? "paid" : "failed";
            } catch (RuntimeException e) {
                log.warn("could not reconcile order {} yet: {}", id, e.getMessage());
                outcome = "retry";
            }
            meters.counter("lab.payments.reconciled", "outcome", outcome).increment();
        }
    }
}
