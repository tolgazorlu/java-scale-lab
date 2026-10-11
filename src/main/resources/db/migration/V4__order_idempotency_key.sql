-- Mission 10: the same Idempotency-Key can create at most one order.
ALTER TABLE orders ADD COLUMN idempotency_key TEXT;
CREATE UNIQUE INDEX uq_orders_idempotency_key ON orders (idempotency_key);
