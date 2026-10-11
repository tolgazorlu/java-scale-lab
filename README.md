# Scale Lab

A Java learning lab. A realistic dealer ordering system, real load, real monitoring.
The system is **deliberately** naive: missing indexes, N+1 queries, a race condition, an external
call inside a database transaction, an HTTP client with no timeouts. Each mission measures one of
them, fixes it, and proves the fix.

No memorizing: every mission is **predict → measure → 4 steps → change → measure again → one sentence**.

The daily flow (lesson first, then the lab) lives in **dacospace** (http://localhost:4120), which
reads this repo, runs `./lab` commands with live output and embeds the Grafana panels. Everything
also works from the terminal.

![Scale Lab dashboard under 70 orders/s](docs/dashboard.png)

## Daily loop

```bash
./lab today              # today's mission
./lab up && ./lab run    # infrastructure + app (run in a separate terminal)
# ... do the mission ...
./lab done 03 "an index's column order must follow the query's WHERE, then ORDER BY"
git add -A && git commit -m "perf: ..."
```

`./lab help` lists every command.

## What's inside

| Part | Purpose | Address |
|---|---|---|
| Spring Boot 4.1 + Java 25 | the app | http://localhost:4180 |
| Postgres 18 + pg_stat_statements | data, slow query analysis | localhost:4184 |
| Grafana + Prometheus + Tempo + Loki + Pyroscope | metrics, traces, logs, profiles | http://localhost:4182 |
| WireMock | fake payment provider, can be slowed down or killed | localhost:4189 |
| k6 | load tests (in Docker) | `./lab load ...` |

Grafana ships a ready dashboard: **Scale Lab** (RED, percentiles, connection pool, Tomcat threads,
outbound HTTP, JVM, k6). Every request is traced in Tempo.

Compose profiles that later missions unlock: `cache` (Redis), `events` (Kafka), `scale` (nginx + 3 instances).

## Content

- `lessons/NN-*.md`: the 10-15 minute reading for mission NN (the concept).
- `labs/NN-*.md`: the hands-on mission.
- `PROGRESS.md`: the roadmap (23 missions) and your one-line log.

## Domain

- **dealers** (2,000), **products** (20,000, the first 20 are campaign products), **orders**
  (400k), **order_items** (1.2M)
- `GET /api/products?category=...` price list
- `GET /api/products/{id}`
- `POST /api/orders` lower stock, charge payment, write the order
- `GET /api/dealers/{id}/orders` recent orders

## Load scenarios (`load/`)

| Scenario | Simulates |
|---|---|
| `smoke` | does every endpoint answer |
| `morning-rush [RPS]` | 09:00, every dealer opens the price list |
| `orders [RPS]` | steady order traffic, 80% campaign products (hot rows) |
| `dealer-history [RPS]` | the "my recent orders" screen |
| `last-item` (`./lab race`) | 100 dealers try to buy the last 10 items at once |

Results go to `results/` as JSON; `./lab compare <scenario>` compares the last two runs.

## Chaos

```bash
./lab chaos payment slow        # 2 s delay
./lab chaos payment very-slow   # 9 s
./lab chaos payment hang        # 120 s
./lab chaos payment errors      # 503
./lab chaos payment down        # connection reset
./lab chaos payment normal
```

## Requirements

Docker Desktop, Java 25 (SDKMAN), the Maven wrapper is in the repo. Ports: cortex
`playbooks/local-ports.md`, block 4180-4189.

## Rules

- Don't call a fix "fixed" without measuring it. `results/` is the evidence.
- Schema changes only through Flyway migrations.
- One mission = one commit or a few small ones. English, `type: subject`.
