package com.tolgazorlu.lab.catalog;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data writes the SQL for these methods from their names.
 * findByCategory -> SELECT ... FROM products WHERE category = ?
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryOrderByNameAsc(String category, Pageable pageable);

    /** Mission 06: check and decrement in one statement. Returns 0 when there is not enough stock. */
    @Modifying
    @Query("update Product p set p.stock = p.stock - :qty where p.id = :id and p.stock >= :qty")
    int reserveStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("update Product p set p.stock = p.stock + :qty where p.id = :id")
    int releaseStock(@Param("id") Long id, @Param("qty") int qty);
}
