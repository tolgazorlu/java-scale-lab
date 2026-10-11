-- Fills the database with realistic volume so slow queries actually look slow.
-- Run with: ./lab seed   (takes 30-90 s)
-- Sizes: 2.000 dealers, 20.000 products, 400.000 orders, ~1.2M order items.

BEGIN;

TRUNCATE order_items, orders, products, dealers RESTART IDENTITY CASCADE;

INSERT INTO dealers (name, city)
SELECT 'Bayi ' || g,
       (ARRAY['Istanbul','Ankara','Izmir','Bursa','Antalya','Konya','Adana','Kayseri'])[1 + (g % 8)]
FROM generate_series(1, 2000) g;

INSERT INTO products (sku, name, category, price_cents, stock)
SELECT 'SKU-' || lpad(g::text, 6, '0'),
       'Urun ' || g,
       (ARRAY['icecek','atistirmalik','temizlik','kisisel-bakim','kirtasiye','sut-urunleri',
              'konserve','dondurulmus','bebek','evcil-hayvan'])[1 + (g % 10)],
       500 + (random() * 50000)::int,
       100 + (random() * 900)::int
FROM generate_series(1, 20000) g;

-- A few products are "hot": every dealer orders them (campaign items).
UPDATE products SET name = 'Kampanya ' || id, stock = 1000000 WHERE id <= 20;

INSERT INTO orders (dealer_id, status, total_cents, payment_ref, created_at)
SELECT 1 + (random() * 1999)::int,
       'PAID',
       0,
       'seed-' || g,
       now() - (random() * interval '365 days')
FROM generate_series(1, 400000) g;

INSERT INTO order_items (order_id, product_id, quantity, unit_price_cents)
SELECT o.id,
       1 + (random() * 19999)::int,
       1 + (random() * 9)::int,
       500 + (random() * 50000)::int
FROM orders o, generate_series(1, 3) n;

UPDATE orders o
SET total_cents = s.total
FROM (SELECT order_id, sum(quantity * unit_price_cents) AS total
      FROM order_items GROUP BY order_id) s
WHERE o.id = s.order_id;

COMMIT;

ANALYZE;
