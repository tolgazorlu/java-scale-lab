package com.tolgazorlu.lab.order;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // LAB: orders.dealer_id has no index. Run EXPLAIN on this query.
    List<Order> findByDealerIdOrderByCreatedAtDesc(Long dealerId, Pageable pageable);
}
