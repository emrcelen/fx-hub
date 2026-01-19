# Rate Hub 

## 📌 Genel Bakış
**Rate Hub**, gerçek zamanlı FX (döviz) kurlarını WebSocket üzerinden client’lara dağıtmak için tasarlanmış,
**multi-instance (scale edilebilir)** bir Spring Boot uygulamasıdır.

Bu proje, load balancer arkasında çalışan birden fazla instance’ın,
WebSocket mesajlarını **tüm client’lara tutarlı şekilde iletmesini** sağlar.

---

## 🧩 Problem: Multi-Instance WebSocket

WebSocket bağlantıları **stateful** olduğu için:

- Client sadece bağlandığı instance ile konuşur
- Load balancer arkasında farklı instance’lara bağlı client’lar
  birbirinin mesajını göremez
- Bu da “bazı client’lara mesaj gidiyor, bazılarına gitmiyor” problemine yol açar

---

## ✅ Çözüm Mimarisi

```
                ┌────────────┐
                │   Client   │
                └─────┬──────┘
                      │ WS / HTTP
                      ▼
                ┌────────────┐
                │   NGINX    │
                │ LoadBalancer│
                └─────┬──────┘
          ┌────────────┼────────────┐
          ▼            ▼            ▼
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │ rate-hub  │ │ rate-hub  │ │ rate-hub  │
    │ instance1 │ │ instance2 │ │ instance3 │
    └─────┬────┘ └─────┬────┘ └─────┬────┘
          │ Hazelcast Topic (rate-updates)
          └────────────┬────────────┘
                       ▼
                ┌────────────┐
                │ Hazelcast  │
                │   Cluster  │
                └────────────┘
```

---

### 1️⃣ Nginx Load Balancer
Nginx, gelen HTTP ve WebSocket isteklerini Rate Hub instance’larına dağıtır.

```
Client → Nginx → RateHub-1
                RateHub-2
                RateHub-3
```

- WebSocket upgrade desteklenir
- Round-robin load balancing yapılır
- Client hangi instance’a düşerse düşsün bağlantı sağlanır

---

### 2️⃣ Hazelcast Cluster (Embedded)
Her Rate Hub instance’ı aynı Hazelcast cluster’ına katılır.

- Cluster name: `rate-hub`
- Discovery: TCP/IP (Docker network üzerinden)
- Her instance aynı cluster’ın üyesidir

```text
RateHub-1  ┐
RateHub-2  ├── Hazelcast Cluster
RateHub-3  ┘
```

---

### 3️⃣ Hazelcast Topic ile Broadcast

Bir instance rate güncellemesi aldığında:

1. Hazelcast Topic’e publish eder
2. Topic mesajı **tüm instance’lara** gider
3. Her instance kendi WebSocket session’larına mesaj gönderir

```text
Producer
   |
   v
RateHub-1 → Topic → RateHub-2 → Client
                    RateHub-3 → Client
```

📌 **Böylece:**
> Hangi instance’a bağlı olursa olsun, tüm client’lar aynı mesajı alır

---

## 🔁 Neden Topic?
| Yapı | Amaç |
|------|------|
| Map | State paylaşımı |
| Queue | Tek consumer |
| **Topic** | 🔥 Broadcast (fan-out) |

---

## 🐳 Docker & Network

### ⚠️ shared-net ZORUNLUDUR
Hazelcast instance’ları birbirini bulabilsin diye network **manuel** oluşturulmalıdır:

```bash
docker network create shared-net
```

Aksi halde:
- Her instance ayrı cluster olur
- WebSocket broadcast çalışmaz
- Topic mesajları dağılmaz

---

## 🚀 Çalıştırma

### Multi-instance başlatma:
```bash
docker compose -p rate-hub up --build --scale rate-hub=2
```

Bu komut ile:
- 2 adet Rate Hub instance ayağa kalkar
- Nginx load balancer aktif olur
- Hazelcast cluster otomatik oluşur

---

## 📡 WebSocket Akışı

1. Client → Nginx → Rate Hub
2. Client subscribe mesajı gönderir
3. Instance subscribe listesini tutar
4. Producer yeni rate gönderir
5. Rate Hub topic’e publish eder
6. Tüm instance’lar alır
7. Her instance kendi client’larına push eder

---
