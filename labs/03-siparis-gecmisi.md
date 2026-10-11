# Görev 03 · Bayinin sipariş geçmişi

**Süre:** 60 dk · **Kavram:** EXPLAIN, seq scan, index, Flyway migration
**Bağlantı:** system-design müfredatı 1.4(a). Kartlarla anlattığımız seq scan'i bugün kendi DB'nde göreceksin.

## Senaryo

Bayiler "son siparişlerim" ekranını açıyor. 400 bin siparişlik tabloda her bayinin ~200 siparişi
var. Şikâyet: "ekran bazen hiç açılmıyor".

## 1. Önce ölç

```bash
./lab load dealer-history 50
```

Not: bu test sırasında sistem ağır yavaşlayabilir. Bu normal, görmen gereken şey bu.

Grafana'da **p95 per endpoint** ve **DB connection pool** panellerine bak. Sonra:

```bash
./lab topsql
```

En çok toplam süre harcayan sorgu hangisi? Kaç kez çağrılmış, ortalaması kaç ms?

## 2. Sorguyu aç

```bash
./lab explain "SELECT * FROM orders WHERE dealer_id = 42 ORDER BY created_at DESC LIMIT 20"
```

Çıktıda şunları bul:
- `Seq Scan` mı yoksa `Index Scan` mı yazıyor?
- `Execution Time` kaç ms?
- `rows=` 20 satır istedin, Postgres kaç satıra bakmış?

## 3. 4 adım (kağıda)

1. **Ne bozuk:** tek cümle.
2. **Sayılar:** sorgu süresi `W_sql`, istek başına kaç sorgu (topsql'deki `calls` / istek sayısı),
   50 istek/sn × ... → pool talebi `L` kaç? Pool 10.
3. **Yol:** istek → controller → `findByDealerIdOrderByCreatedAtDesc` → Postgres → ...
4. **Ne bozulabilir:** hangi kalem süreyi yiyor?

## 4. Düzelt

Index'i **Flyway migration** ile ekle. Asla elle `CREATE INDEX` çalıştırıp bırakma: production'da
migration olmayan değişiklik yoktur.

`src/main/resources/db/migration/V2__...sql` dosyasını oluştur. Index'in **hangi kolonlarda** ve
**hangi sırada** olması gerektiğine sen karar ver. İpucu: sorgu hem filtreliyor (`WHERE`) hem
sıralıyor (`ORDER BY`).

Uygulamayı yeniden başlat (Flyway migration'ı çalıştırır), sonra:

```bash
./lab explain "SELECT * FROM orders WHERE dealer_id = 42 ORDER BY created_at DESC LIMIT 20"
./lab load dealer-history 50
./lab compare dealer-history
```

## 5. Hâlâ yavaşsa

Index'ten sonra p95 düzeldi ama hedefe (500 ms) gelmediyse, `./lab topsql` tekrar çalıştır.
Artık en üstte hangi sorgular var? Kaç kez çağrılıyorlar? Bunu fark etmen yeterli, düzeltme
Görev 04'te. Sadece `calls` sayısını kağıda yaz.

## 6. Kanıt

| | Önce | Index sonrası |
|---|---|---|
| EXPLAIN: scan tipi | | |
| Execution Time | | |
| dealer-history p95 | | |
| dropped iterations | | |
| topsql 1. sıradaki sorgu | | |

## 7. Kendi cümlenle

```bash
./lab done 03 "..."
git add -A && git commit -m "perf: index orders by dealer and date"
```

## Kontrol sorusu

Index'i `(created_at, dealer_id)` sırasıyla açsaydın aynı hızı alır mıydın? 52 kartlık deste
örneğiyle açıkla: önce renge göre mi sıraladın, önce sayıya göre mi?

## Java köşesi

`OrderRepository.findByDealerIdOrderByCreatedAtDesc` metodunun gövdesi yok. Spring Data SQL'i
metot adından üretiyor. `application.yml` içinde `show_sql: true` yap, isteği bir kez at, log'da
üretilen SQL'i bul. Adın her parçası (`findBy`, `DealerId`, `OrderBy`, `CreatedAt`, `Desc`)
SQL'in hangi parçasına dönüştü?
