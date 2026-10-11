# İlerleme

`./lab today` bu tablodaki ilk `todo` satırını açar. `./lab done NN "tek cümle"` satırı kapatır
ve aşağıdaki günlüğe ekler. Görev dosyası henüz yazılmadıysa Hermes'e
"scale lab: sıradaki görevi yaz" de.

| # | Görev | Kavram | Durum | Tarih |
|---|---|---|---|---|
| 01 | Gözlerini aç: dashboard turu | RED, p50/p95/p99, ortalama neden yalan söyler | todo | - |
| 02 | Sabah 09:00: kırılma noktası | Little's Law, connection pool, saturation | todo | - |
| 03 | Bayinin sipariş geçmişi | EXPLAIN, seq scan, index, Flyway migration | todo | - |
| 04 | 1 istek, 61 sorgu | N+1, Hibernate lazy loading, fetch join | todo | - |
| 05 | Ödeme yavaşlayınca her şey yavaşlar | transaction sınırı, pool tükenmesi | todo | - |
| 06 | Son 10 ürün | race condition, lost update, atomic UPDATE, optimistic lock | todo | - |
| 07 | Sağlayıcı cevap vermiyor | timeout (connect/read), fail fast | todo | - |
| 08 | Tekrar dene ama akıllıca | retry, exponential backoff, jitter (Resilience4j) | todo | - |
| 09 | Devreyi kes | circuit breaker, bulkhead, fallback | todo | - |
| 10 | Aynı sipariş iki kez | idempotency key, unique constraint | todo | - |
| 11 | Fiyat listesi cache'i | Redis, cache-aside, TTL | todo | - |
| 12 | Cache süresi doldu, herkes DB'ye koştu | thundering herd, request coalescing | todo | - |
| 13 | Bir bayi herkesi yavaşlatıyor | rate limiting, token bucket, noisy neighbor | todo | - |
| 14 | 3 instance + load balancer | stateless service, horizontal scaling, nginx | todo | - |
| 15 | Virtual threads | Java 21+ Loom, thread-per-request, pinning | todo | - |
| 16 | Sipariş event'i kaybolmasın | dual write, outbox pattern, Kafka | todo | - |
| 17 | Consumer çöktü | at-least-once, consumer idempotency, DLQ | todo | - |
| 18 | SLO ve alarm | SLI/SLO, error budget, burn rate alert | todo | - |
| 19 | Neden yavaş: flame graph | profiling (Pyroscope), allocation, GC | todo | - |
| 20 | Okumaları replica'ya | read replica, replication lag, read-your-writes | todo | - |
| 21 | 400 bin → 40 milyon sipariş | partitioning, archive, vacuum | todo | - |
| 22 | Chaos günü | instance öldürme, DB restart, graceful shutdown | todo | - |
| 23 | Kapasite raporu | capacity planning, darboğaz zinciri | todo | - |

## Günlük

