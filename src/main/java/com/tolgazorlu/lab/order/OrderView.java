package com.tolgazorlu.lab.order;

import java.time.Instant;
import java.util.List;

public record OrderView(Long id, Long dealerId, String status, long totalCents, String paymentRef,
                        Instant createdAt, List<LineView> lines) {

    public record LineView(Long productId, String productName, int quantity, long unitPriceCents) {
    }

    public static OrderView from(Order o) {
        List<LineView> lines = o.getItems().stream()
                .map(i -> new LineView(i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getUnitPriceCents()))
                .toList();
        return new OrderView(o.getId(), o.getDealer().getId(), o.getStatus(), o.getTotalCents(),
                o.getPaymentRef(), o.getCreatedAt(), lines);
    }
}
