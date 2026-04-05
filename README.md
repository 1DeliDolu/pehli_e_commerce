# Pehli E-Commerce Kafka Demo

Spring Boot tabanli bir e-commerce demo uygulamasi. Proje; MySQL, Kafka, RabbitMQ, MailHog, Prometheus ve Grafana ile birlikte calisiyor ve hem uygulama hem broker metriklerini izlemeyi hedefliyor.

## Icerik

- `kafka/`: Spring Boot uygulamasi
- `compose.yaml`: lokal servisleri kaldiran Docker Compose dosyasi
- `monitoring/`: Prometheus ve Grafana provisioning/dashboards
- `.github/workflows/`: CI ve zamanlanmis workflow tanimlari

## Kullanilan Teknolojiler

- Java 21 target, Maven build
- Spring Boot 4
- Spring MVC + Thymeleaf
- Spring Data JPA + Flyway + MySQL
- Apache Kafka
- RabbitMQ
- Micrometer + Prometheus + Grafana
- Testcontainers + JUnit 5 + Mockito

## Ozellikler

- Urun ve kategori listeleme
- Sepet ve checkout akisi
- Siparis olusturma ve iptal etme
- Admin tarafinda kategori ve urun yonetimi
- Kafka topic ve RabbitMQ mail queue monitoring ekranlari
- Prometheus ve Grafana ile observability

## Gereksinimler

- JDK 25 veya JDK 21
- Docker + Docker Compose

Not:
- Maven derleme hedefi `release 21`.
- JDK 25 ile calistirmak destekleniyor; Lombok kaynakli gecis uyarilari gorulebilir.

## Hizli Baslangic

### 1. Altyapiyi kaldir

Repo kokunden:

```bash
docker compose up -d
```

Bu servisler acilir:

- MySQL
- Kafka broker
- Kafka UI
- Prometheus
- Grafana
- MailHog
- RabbitMQ

### 2. Uygulamayi calistir

Repo kokunden:

```bash
./kafka/mvnw -f kafka/pom.xml spring-boot:run
```

Alternatif olarak modul klasorunden:

```bash
cd kafka
./mvnw spring-boot:run
```

## Test Calistirma

Tum testler:

```bash
./kafka/mvnw -B -f kafka/pom.xml test
```

Belirli bir test sinifi:

```bash
./kafka/mvnw -B -f kafka/pom.xml -Dtest=AdminCategoryControllerTest test
```

## Varsayilan Erisim Noktalari

- Uygulama: `http://localhost:8080`
- Kafka UI: `http://localhost:8090`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- MailHog: `http://localhost:8025`
- RabbitMQ Management: `http://localhost:15672`

## Varsayilan Kimlik Bilgileri

Bu degerler `.env` ve `kafka/.env` icinden gelir:

- MySQL
  - database: `kafka`
  - user: `musta`
  - password: `D0cker57`
- Grafana
  - user: `admin`
  - password: `D0cker!`
- Prometheus basic auth
  - user: `admin`
  - password: `D0cker!`
- RabbitMQ
  - user: `guest`
  - password: `guest`

## Monitoring

Prometheus scrape targetlari:

- `spring-app`: `wsl-host:8080/actuator/prometheus`
- `kafka`: `broker:7071/metrics`

Grafana dashboard dosyalari:

- `monitoring/grafana/dashboards/kafka-app-vs-broker.json`
- `monitoring/grafana/dashboards/kafka-broker-observability.json`

Grafana, dashboard verilerini dogrudan Prometheus datasource uzerinden alir. Datasource provisioning dosyasi:

- `monitoring/grafana/provisioning/datasources/grafana-prometheus-datasource.yml`

## Onemli Dosyalar

- Uygulama ayarlari: [`kafka/src/main/resources/application.properties`](/mnt/d/Code/Kafka/pehli_e_commerce/kafka/src/main/resources/application.properties)
- Root compose: [`compose.yaml`](/mnt/d/Code/Kafka/pehli_e_commerce/compose.yaml)
- Module compose: [`kafka/compose.yaml`](/mnt/d/Code/Kafka/pehli_e_commerce/kafka/compose.yaml)
- Prometheus config: [`monitoring/prometheus.yml`](/mnt/d/Code/Kafka/pehli_e_commerce/monitoring/prometheus.yml)
- CI workflow: [`.github/workflows/ci.yml`](/mnt/d/Code/Kafka/pehli_e_commerce/.github/workflows/ci.yml)
- Aylik update workflow: [`.github/workflows/update.yml`](/mnt/d/Code/Kafka/pehli_e_commerce/.github/workflows/update.yml)

## CI

Projede iki workflow var:

- `CI`: push, pull request, manuel calistirma ve her gun `13:00 UTC`
- `Update`: manuel calistirma ve her ayin 1'i `03:00 UTC`

Her iki workflow da JDK 25 ile `./kafka/mvnw -B -f kafka/pom.xml test` komutunu calistirir.
