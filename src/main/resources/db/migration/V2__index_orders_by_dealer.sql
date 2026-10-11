-- Mission 03: the recent-orders query filters by dealer and sorts by date.
CREATE INDEX idx_orders_dealer_created ON orders (dealer_id, created_at DESC);
