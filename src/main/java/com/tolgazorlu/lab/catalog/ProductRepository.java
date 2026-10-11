package com.tolgazorlu.lab.catalog;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data writes the SQL for these methods from their names.
 * findByCategory -> SELECT ... FROM products WHERE category = ?
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryOrderByNameAsc(String category, Pageable pageable);
}
