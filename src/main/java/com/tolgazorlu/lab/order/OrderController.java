package com.tolgazorlu.lab.order;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderView> place(@Valid @RequestBody PlaceOrderRequest request,
                                           @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        OrderService.Placed placed = service.place(request, key);
        return ResponseEntity.status(placed.replay() ? HttpStatus.OK : HttpStatus.CREATED).body(placed.order());
    }

    @GetMapping("/api/dealers/{dealerId}/orders")
    public List<OrderView> recent(@PathVariable Long dealerId,
                                  @RequestParam(defaultValue = "20") int limit) {
        return service.recentOrders(dealerId, limit);
    }
}
