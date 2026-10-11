package com.tolgazorlu.lab.catalog;

import com.tolgazorlu.lab.common.NotFoundException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Price list endpoints. Every dealer opens the app at 09:00 and calls these.
 * LAB: no cache. Every request goes to Postgres.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping
    public List<ProductView> list(@RequestParam String category,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "50") int size) {
        return products.findByCategoryOrderByNameAsc(category, PageRequest.of(page, size))
                .stream()
                .map(ProductView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductView get(@PathVariable Long id) {
        return products.findById(id)
                .map(ProductView::from)
                .orElseThrow(() -> new NotFoundException("product " + id));
    }
}
