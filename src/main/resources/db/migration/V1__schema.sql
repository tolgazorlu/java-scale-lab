-- Bayi (dealer) ordering system, version 0.
-- This schema is naive ON PURPOSE. Labs will find and fix its problems.

CREATE TABLE dealers (
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT        NOT NULL,
    city        TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id           BIGSERIAL PRIMARY KEY,
    sku          TEXT        NOT NULL UNIQUE,
    name         TEXT        NOT NULL,
    category     TEXT        NOT NULL,          -- LAB: no index on category
    price_cents  BIGINT      NOT NULL CHECK (price_cents > 0),
    stock        INT         NOT NULL,          -- LAB: no CHECK (stock >= 0)
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    dealer_id    BIGINT      NOT NULL REFERENCES dealers(id),  -- LAB: FK without index
    status       TEXT        NOT NULL,
    total_cents  BIGINT      NOT NULL,
    payment_ref  TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT NOT NULL REFERENCES orders(id),    -- LAB: FK without index
    product_id        BIGINT NOT NULL REFERENCES products(id),
    quantity          INT    NOT NULL CHECK (quantity > 0),
    unit_price_cents  BIGINT NOT NULL
);
