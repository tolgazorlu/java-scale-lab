# Scale Lab

Java öğrenme laboratuvarı. Gerçekçi bir bayi sipariş sistemi, gerçek yük, gerçek monitoring.
Sistem **bilerek** saf yazıldı: içinde index eksiği, N+1, race condition, transaction içinde dış
çağrı, timeout'suz HTTP client var. Her görev birini ölçer, düzeltir, düzeldiğini kanıtlar.

Ezber yok: her görev **tahmin et → ölç → 4 adım → değiştir → tekrar ölç → tek cümle** döngüsüdür.

## Günlük döngü

```bash
./lab today              # bugünün görevi
./lab up && ./lab run    # altyapı + uygulama (run ayrı terminalde)
# ... görevi yap ...
./lab done 03 "index'in kolon sırası sorgunun WHERE + ORDER BY sırasını izlemeli"
git add -A && git commit -m "perf: ..."
```

`./lab help` bütün komutları listeler.

## Ne var

| Parça | Ne işe yarar | Adres |
|---|---|---|
| Spring Boot 4.1 + Java 25 | uygulama | http://localhost:4180 |
| Postgres 18 + pg_stat_statements | veri, yavaş sorgu analizi | localhost:4184 |
| Grafana + Prometheus + Tempo + Loki + Pyroscope | metrik, trace, log, profil | http://localhost:4182 |
| WireMock | sahte ödeme sağlayıcısı, yavaşlatılabilir, düşürülebilir | localhost:4189 |
| k6 | yük testi (Docker içinde) | `./lab load ...` |

Grafana'da hazır dashboard: **Scale Lab** (RED, percentile'lar, connection pool, Tomcat thread,
dış HTTP, JVM, k6). Tempo'da her isteğin trace'i var.

Sonraki görevlerde açılan profiller: `cache` (Redis), `events` (Kafka), `scale` (nginx + 3 instance).

## Domain

- **dealers** (2.000 bayi), **products** (20.000 ürün, ilk 20'si kampanya ürünü), **orders**
  (400 bin), **order_items** (1,2 milyon)
- `GET /api/products?category=...` fiyat listesi
- `GET /api/products/{id}`
- `POST /api/orders` stok düş, ödeme al, sipariş yaz
- `GET /api/dealers/{id}/orders` son siparişler

## Yük senaryoları (`load/`)

| Senaryo | Ne simüle eder |
|---|---|
| `smoke` | her endpoint cevap veriyor mu |
| `morning-rush [RPS]` | 09:00, bütün bayiler fiyat listesini açıyor |
| `orders [RPS]` | sabit sipariş trafiği, %80 kampanya ürünü (hot rows) |
| `dealer-history [RPS]` | "son siparişlerim" ekranı |
| `last-item` (`./lab race`) | 100 bayi aynı anda son 10 ürünü almaya çalışıyor |

Sonuçlar `results/` altına JSON olarak yazılır, `./lab compare <senaryo>` son iki koşuyu kıyaslar.

## Kaos

```bash
./lab chaos payment slow        # 2 sn gecikme
./lab chaos payment very-slow   # 9 sn
./lab chaos payment hang        # 120 sn
./lab chaos payment errors      # 503
./lab chaos payment down        # connection reset
./lab chaos payment normal
```

## Gereksinimler

Docker Desktop, Java 25 (SDKMAN), Maven wrapper repo'da. Portlar cortex
`playbooks/local-ports.md` 4180-4189 bloğunda.

## Kurallar

- Bir düzeltmeyi ölçmeden "düzeldi" deme. `results/` kanıttır.
- Şema değişikliği sadece Flyway migration ile.
- Her görev tek commit ya da birkaç küçük commit. İngilizce, `type: subject`.
