package com.tolgazorlu.lab.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** JSON body of POST /api/orders. */
public record PlaceOrderRequest(@NotNull Long dealerId, @NotEmpty @Valid List<Line> lines) {

    public record Line(@NotNull Long productId, @Min(1) int quantity) {
    }
}
