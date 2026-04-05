# Pehli E-Commerce Kafka Demo

[![License: AGPL-3.0](https://img.shields.io/badge/license-AGPL--3.0-f28c28)](LICENSE)
![Java](https://img.shields.io/badge/Java-21%2F25-2ea44f)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6db33f)
![Kafka](https://img.shields.io/badge/Kafka-Event%20Streaming-231f20)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479a1)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-4.x-ff6600)
![Prometheus](https://img.shields.io/badge/Prometheus-Metrics-e6522c)
![Grafana](https://img.shields.io/badge/Grafana-Dashboards-f46800)
![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Local%20Stack-2496ed)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088ff)

This is a Spring Boot based e-commerce demo application. The project runs with MySQL, Kafka, RabbitMQ, MailHog, Prometheus, and Grafana, and focuses on monitoring both application and broker metrics.

## Contents

- `kafka/`: Spring Boot application
- `compose.yaml`: Docker Compose file for local services
- `monitoring/`: Prometheus and Grafana provisioning/dashboard files
- `.github/workflows/`: CI and scheduled workflow definitions

## Tech Stack

- Java 21 target, Maven build
- Spring Boot 4
- Spring MVC + Thymeleaf
- Spring Data JPA + Flyway + MySQL
- Apache Kafka
- RabbitMQ
- Micrometer + Prometheus + Grafana
- Testcontainers + JUnit 5 + Mockito

## Features

- Product and category listing
- Cart and checkout flow
- Order creation and cancellation
- Admin-side category and product management
- Kafka topic and RabbitMQ mail queue monitoring screens
- Observability with Prometheus and Grafana

## Requirements

- JDK 25 or JDK 21
- Docker + Docker Compose

Notes:
- Maven compiles with target `release 21`.
- Running with JDK 25 is supported, but you may still see Lombok-related transition warnings.

## Quick Start

### 1. Start the infrastructure

From the repository root:

```bash
docker compose up -d
```

This starts:

- MySQL
- Kafka broker
- Kafka UI
- Prometheus
- Grafana
- MailHog
- RabbitMQ

### 2. Run the application

From the repository root:

```bash
./kafka/mvnw -f kafka/pom.xml spring-boot:run
```

Alternatively, from the module directory:

```bash
cd kafka
./mvnw spring-boot:run
```

## Running Tests

Run the full test suite:

```bash
./kafka/mvnw -B -f kafka/pom.xml test
```

Run a specific test class:

```bash
./kafka/mvnw -B -f kafka/pom.xml -Dtest=AdminCategoryControllerTest test
```

## Default Endpoints

- Application: `http://localhost:8080`
- Kafka UI: `http://localhost:8090`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- MailHog: `http://localhost:8025`
- RabbitMQ Management: `http://localhost:15672`

## Default Credentials

These values come from `.env` and `kafka/.env`:

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

Prometheus scrape targets:

- `spring-app`: `wsl-host:8080/actuator/prometheus`
- `kafka`: `broker:7071/metrics`

Grafana dashboard files:

- `monitoring/grafana/dashboards/kafka-app-vs-broker.json`
- `monitoring/grafana/dashboards/kafka-broker-observability.json`

Grafana reads dashboard data through the Prometheus datasource. Datasource provisioning file:

- `monitoring/grafana/provisioning/datasources/grafana-prometheus-datasource.yml`

## Important Files

- Application settings: [`kafka/src/main/resources/application.properties`](/mnt/d/Code/Kafka/pehli_e_commerce/kafka/src/main/resources/application.properties)
- Root compose: [`compose.yaml`](/mnt/d/Code/Kafka/pehli_e_commerce/compose.yaml)
- Module compose: [`kafka/compose.yaml`](/mnt/d/Code/Kafka/pehli_e_commerce/kafka/compose.yaml)
- Prometheus config: [`monitoring/prometheus.yml`](/mnt/d/Code/Kafka/pehli_e_commerce/monitoring/prometheus.yml)
- CI workflow: [`.github/workflows/ci.yml`](/mnt/d/Code/Kafka/pehli_e_commerce/.github/workflows/ci.yml)
- Monthly update workflow: [`.github/workflows/update.yml`](/mnt/d/Code/Kafka/pehli_e_commerce/.github/workflows/update.yml)

## CI

The project currently has two workflows:

- `CI`: push, pull request, manual trigger, and daily at `13:00 UTC`
- `Update`: manual trigger and monthly on day 1 at `03:00 UTC`

Both workflows run `./kafka/mvnw -B -f kafka/pom.xml test` with JDK 25.
