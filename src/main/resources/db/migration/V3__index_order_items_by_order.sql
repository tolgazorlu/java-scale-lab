-- Mission 04: Postgres does not index foreign keys. Every items lookup was a full scan.
CREATE INDEX idx_order_items_order ON order_items (order_id);
