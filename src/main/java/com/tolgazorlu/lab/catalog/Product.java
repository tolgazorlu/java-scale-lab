package com.tolgazorlu.lab.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One row of the "products" table.
 * An @Entity is a Java class that Hibernate maps to a database table.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;
    private String name;
    private String category;
    private long priceCents;
    private int stock;
    private Instant updatedAt;

    protected Product() {
        // Hibernate needs an empty constructor.
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public long getPriceCents() { return priceCents; }
    public int getStock() { return stock; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStock(int stock) {
        this.stock = stock;
        this.updatedAt = Instant.now();
    }
}
