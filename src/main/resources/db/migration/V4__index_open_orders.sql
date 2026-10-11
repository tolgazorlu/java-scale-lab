-- Mission 07: the reconciler looks for orders still waiting for a payment outcome every 5 seconds.
-- A partial index keeps that cheap: it only contains the few open orders, not all 400k.
CREATE INDEX idx_orders_open ON orders (created_at) WHERE status IN ('NEW', 'PAYMENT_UNKNOWN');
