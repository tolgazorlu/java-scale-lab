package com.tolgazorlu.lab.catalog;

/** What the API returns for a product. A record is a small immutable data class. */
public record ProductView(Long id, String sku, String name, String category, long priceCents, int stock) {

    public static ProductView from(Product p) {
        return new ProductView(p.getId(), p.getSku(), p.getName(), p.getCategory(), p.getPriceCents(), p.getStock());
    }
}
