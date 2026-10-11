package com.tolgazorlu.lab.order;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderView> place(@Valid @RequestBody PlaceOrderRequest request) {
        OrderView order = service.place(request);
        // Mission 07: 202 Accepted when we could not confirm the payment; the client polls the order.
        HttpStatus status = "PAYMENT_UNKNOWN".equals(order.status()) ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(order);
    }

    @GetMapping("/api/dealers/{dealerId}/orders")
    public List<OrderView> recent(@PathVariable Long dealerId,
                                  @RequestParam(defaultValue = "20") int limit) {
        return service.recentOrders(dealerId, limit);
    }
}
