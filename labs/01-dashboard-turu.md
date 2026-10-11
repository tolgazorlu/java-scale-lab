# Görev 01 · Gözlerini aç: dashboard turu

**Süre:** 45-60 dk · **Kavram:** RED metrikleri, p50/p95/p99, ortalama neden yalan söyler
**Bağlantı:** system-design müfredatı 2.7 Observability

## Senaryo

Bir bayi sipariş sistemi işletiyorsun. 2.000 bayi, 20.000 ürün, geçen yıldan 400 bin sipariş.
Bayiler sabah fiyat listesini açıyor, gün içinde sipariş veriyor, ara ara "son siparişlerim"
ekranına bakıyor. Henüz hiçbir şey optimize edilmedi.

Bugün hiçbir kodu değiştirmiyorsun. Sadece **görmeyi** öğreniyorsun. Sonraki 22 görevin hepsi
"şu grafiğe bak, şu sayı değişti mi" diye ilerleyecek. Grafiği okuyamazsan hiçbiri işe yaramaz.

## Hazırlık

```bash
./lab up          # Postgres, Grafana stack, sahte ödeme sağlayıcısı
./lab run         # ayrı bir terminalde, uygulama :4180
./lab seed        # sadece ilk sefer, ~15 sn
```

Grafana: http://localhost:4182 → Dashboards → Scale Lab → **Scale Lab**

## 1. Önce tahmin et (kağıda yaz)

Testi çalıştırmadan önce yaz:

1. Saniyede 100 istek gelirse fiyat listesi endpoint'inin p95'i kaç ms olur?
2. Ortalama ile p99 arasında kaç kat fark olur?

Yanlış tahmin sorun değil. Tahmin etmeden ölçmek, sonucu "evet, beklediğim gibi" diye okumana
yol açar. Tahmin, ölçümü dikkatle okumaya zorlar.

## 2. Ölç

```bash
./lab load morning-rush 100
```

Test 100 sn sürer. Bu sürede Grafana'yı aç ve şu dört paneli izle:

| Panel | Ne söyler |
|---|---|
| Rate | Saniyede gelen istek. 100'e çıkıyor mu? |
| Errors | 4xx/5xx. Şu an boş olmalı. |
| Duration p50/p95/p99 + average | Asıl ders burada |
| DB connection pool | `active`, `idle`, `pending` |

## 3. Kavram: RED ve percentile

**RED**, bir servisin kullanıcı gözünden sağlığıdır:
- **Rate**: saniyede kaç istek
- **Errors**: kaçı hata
- **Duration**: ne kadar sürdü

**p95 = 120 ms** demek: "isteklerin %95'i 120 ms'den kısa sürdü, %5'i daha uzun."
100 istekten en yavaş 5'i p95'in üstündedir.

**Ortalama neden yalan söyler:** 99 istek 10 ms, 1 istek 5.000 ms sürsün.
Ortalama = (99×10 + 5000) / 100 = **59,9 ms**. Dashboard "her şey yolunda" der.
Ama o 1 kişi 5 saniye bekledi. Bayi günde 50 istek atıyorsa, her bayi günde yarım kez bu
5 saniyeyi yaşar. Şikâyet gelir ama ortalama yeşildir.

## 4. Kurcala

1. Dashboard'da "average (lies)" çizgisini p99 ile karşılaştır. Kaç kat fark var?
2. Grafana → Explore → **Tempo** → Search → en uzun süren trace'i aç.
   Span'lere bak: zamanın çoğu nerede geçmiş? Controller mı, SQL mi?
3. Prometheus'ta (http://localhost:4183) şu sorguyu çalıştır, ne döndüğünü kendi cümlenle açıkla:
   ```
   rate(http_server_requests_seconds_count{uri="/api/products"}[1m])
   ```
4. `./lab topsql` çalıştır. En çok toplam süre harcayan sorgu hangisi?

## 5. Kanıt

`results/` altında bugünün JSON dosyası olmalı. Ayrıca kağıda şu tabloyu doldur:

| | Tahminim | Ölçülen |
|---|---|---|
| p95 (fiyat listesi) | | |
| ortalama | | |
| p99 | | |
| en yavaş trace'te zamanın çoğu | | |

## 6. Kendi cümlenle (bunu yazmadan görev bitmez)

```bash
./lab done 01 "..."
```

Tek cümle: ortalama ile p99 arasındaki farkı bir bayiye nasıl anlatırsın?

## Kontrol sorusu

Dashboard'da p50 = 20 ms, p99 = 2.000 ms görüyorsun. Bir arkadaşın "ortalama 45 ms, sorun yok"
diyor. Ona hangi tek sayıyı gösterirsin ve neden?

## Java köşesi

`ProductController.java` dosyasını aç. Şu üç şeyi bul ve kendine açıkla:
- `@RestController` ne yapıyor?
- `@RequestParam String category` URL'deki hangi parçayı alıyor?
- `.stream().map(ProductView::from).toList()` satırı ne üretiyor?

Bilmediğin terimi java-dev-journal'a not et.
