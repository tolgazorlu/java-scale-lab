package com.tolgazorlu.lab.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // LAB: orders.dealer_id has no index. Run EXPLAIN on this query.
    List<Order> findByDealerIdOrderByCreatedAtDesc(Long dealerId, Pageable pageable);

    /** Mission 07: the place() thread and the reconciler may settle the same order; one waits for the other. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findForUpdate(@Param("id") Long id);

    /**
     * Mission 07: orders whose payment outcome we do not know. PAYMENT_UNKNOWN after a short grace
     * period, and NEW orders old enough that the request that created them must have died.
     */
    @Query("""
            select o.id from Order o
            where (o.status = 'PAYMENT_UNKNOWN' and o.createdAt < :unknownBefore)
               or (o.status = 'NEW' and o.createdAt < :staleBefore)
            order by o.createdAt""")
    List<Long> findToReconcile(@Param("unknownBefore") Instant unknownBefore,
                               @Param("staleBefore") Instant staleBefore, Pageable pageable);
}
