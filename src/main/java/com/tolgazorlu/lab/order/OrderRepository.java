package com.tolgazorlu.lab.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // LAB: orders.dealer_id has no index. Run EXPLAIN on this query.
    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByDealerIdOrderByCreatedAtDesc(Long dealerId, Pageable pageable);
}
