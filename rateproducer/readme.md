# Rate Producer

Bu proje, **FX rate verilerini** sisteme üretmek ve **Rate Hub** üzerinden WebSocket ile canlı olarak publish edilmesini sağlamak için geliştirilmiştir.

---

## 📦 Bileşenler

- **Rate Producer**
- **RabbitMQ**

---

## 🧱 Mimari Akış

```
Client Script / Postman
        |
        v
Rate Producer (REST)
        |
        v
RabbitMQ (Direct Exchange)
        |
        v
Rate Hub (Multi Instance)
        |
        v
Hazelcast Cluster
        |
        v
WebSocket Clients
```

---

## ⚙️ Ön Gereksinimler

### 1️⃣ Docker Network (ZORUNLU)

Bu proje **external docker network** kullanır.  
İlk çalıştırmadan önce **manuel olarak oluşturulmalıdır**:

```bash
docker network create shared-net
```

> ⚠️ Bu adım yapılmazsa container’lar birbirini **göremez** ve Hazelcast cluster oluşmaz.

---

## 🚀 Projeyi Çalıştırma

Proje root dizininde:

```bash
docker compose -p rate-platform up --build
```

Multi-instance Rate Hub çalıştırmak için:

```bash
docker compose -p rate-producer up --build --scale rate-producer=2
```

> Instance sayısı **dinamik olarak artırılabilir**.  
> Hazelcast discovery otomatik çalışır.

---

## 🧪 Test & Veri Üretme

Bu projede test için iki yöntem vardır:

### ✅ 1. PowerShell Script
`/scripts/send-rates.bat`

- 10 adet FX pair için random bid/ask üretir
- İlk hatada script durur
- Başarılı olursa uyarı mesajı gösterir

### ✅ 2. Postman Collection

`/postman/rate-producer.postman_collection.json`

---

## 🧠 Önemli Notlar
### 🔹 RabbitMQ

- Producer yalnızca **publish** eder
- Consumer Rate Hub’dır

---

## 🛠 Faydalı Komutlar

Tüm container’ları durdur:

```bash
docker compose down
```
Network sil:
```bash
docker network rm shared-net
```
Log izle:

```bash
docker compose logs -f rate-hub
```

---
