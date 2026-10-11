# Görev 02 · Sabah 09:00: kırılma noktası

**Süre:** 60-90 dk · **Kavram:** Little's Law, connection pool, saturation
**Bağlantı:** system-design müfredatı 0.2 ve 1.4(c). Kağıtta yaptığın hesabı bugün gerçek sistemde göreceksin.

## Senaryo

Kampanya günü. Bayiler sabah fiyat listesini açtı, şimdi sipariş veriyorlar. Siparişlerin %80'i
20 kampanya ürününden birini içeriyor. Patron soruyor: **"Saniyede kaç sipariş kaldırırız?"**

Görev 01'de fiyat listesinin saniyede 1.000+ isteği rahat kaldırdığını gördün. Sipariş yolu
farklı: DB'de stok düşürüyor, ödeme sağlayıcısını çağırıyor, satır yazıyor.

## 1. Önce hesapla (kağıda, testten ÖNCE)

Önce düşük yükte bir sipariş ne kadar sürüyor, onu ölç:

```bash
./lab load orders 20
```

Çıktıdaki `med` değerini `W` olarak al. `application.yml` dosyasında
`maximum-pool-size` değerini bul.

Şimdi 4 adımı yaz:

1. **Ne bozuk?** Henüz bilmiyorsun. "Kaç sipariş/sn'de bozulur?" sorusunu yaz.
2. **Sayılar:** `W` = ? s, pool = ? connection.
3. **İsteğin yolu:** `OrderService.place()` metodunu oku. İstek connection'ı ne zaman alıyor,
   ne zaman bırakıyor? (İpucu: `@Transactional`.) Yolu sırayla yaz.
4. **Hesap:** Her istek bir connection'ı `W` saniye tutuyorsa, 10 connection saniyede en fazla
   kaç isteği bitirebilir? `λ_max = pool / W`

Sonucu yaz: "Sistem saniyede ___ sipariş üstünde bozulur."

## 2. Ölç

Tahminin altında ve üstünde birer test:

```bash
./lab load orders 40
./lab load orders 80
```

Grafana'da **DB connection pool** paneline bak. `pending` çizgisi ne zaman sıfırdan ayrılıyor?

## 3. Kavram: neden birden çöküyor?

Pool bir marketin 10 kasası gibi. Kasada geçen süre `W`. Kasalar doluyken gelen müşteri
**kuyruğa** girer.

- Gelen < kapasite: kuyruk yok, herkes `W` kadar bekler.
- Gelen > kapasite: her saniye kuyruk büyür. Bekleme süresi `W` olmaktan çıkar,
  **kuyruktaki herkesin süresi** olur. Yavaşlama doğrusal değil, uçurum gibidir.

Bu yüzden 20/sn'de 160 ms olan şey, 80/sn'de 5 saniye olur. Sadece 4 kat yük, 30 kat yavaşlık.

`L = λ × W` burada canlı: `L` = aynı anda içeride olan istek sayısı. `L` pool'u (10) geçtiği an
fazlası `pending` olarak bekler. Dashboard'daki `pending` çizgisi tam olarak budur.

## 4. Deney: pool'u büyütmek çözer mi?

`application.yml` içinde `maximum-pool-size: 10` değerini `30` yap, uygulamayı yeniden başlat,
aynı testi çalıştır:

```bash
./lab load orders 80
./lab compare orders
```

Sonuçtan önce tahmin et: p95 3 kat düzelir mi? Sonra ölç. Beklediğin olmadıysa nedenini ara:
- Grafana'da **Outbound HTTP (payment provider)** paneli ne gösteriyor?
- `./lab sql "SELECT count(*) FROM pg_stat_activity WHERE wait_event_type = 'Lock'"` testin
  ortasında çalıştır. Sıfırdan büyük mü? (Kampanya ürünlerinin satırları.)

Deney bitince pool'u **10'a geri al**. Asıl düzeltme Görev 05 ve 06'da.

## 5. Kanıt

| | 20/sn | 40/sn | 80/sn | 80/sn (pool 30) |
|---|---|---|---|---|
| p95 | | | | |
| max pool pending | | | | |
| dropped iterations | | | | |

Hesapladığın `λ_max` ile gerçek kırılma noktası arasındaki fark yüzde kaç?

## 6. Kendi cümlenle

```bash
./lab done 02 "..."
```

Tek cümle: patrona "saniyede kaç sipariş" sorusunun cevabını hangi iki sayıyla verirsin?

## Kontrol sorusu

`W` 0,17 s'den 0,05 s'ye düşerse, pool hiç değişmeden kapasite kaç sipariş/sn olur?
Bunu sağlamak için `place()` metodunda hangi satırı transaction'ın dışına çıkarmayı düşünürdün?
(Sadece yaz, değiştirme. Görev 05'in konusu.)

## Java köşesi

`OrderService.place()` içinde `@Transactional` var. Şunları kendine açıkla:
- Spring bu anotasyonu görünce metodun başında ve sonunda ne yapıyor?
- Metot exception fırlatırsa stok düşürme ne olur?
- `payments.charge(...)` satırı transaction'ın içinde mi dışında mı?
