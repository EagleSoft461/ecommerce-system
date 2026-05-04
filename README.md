# 🛒 E-Commerce System

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6-black?style=flat-square&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=flat-square&logo=swagger)
![CI](https://github.com/EagleSoft461/ecommerce-system/actions/workflows/ci.yml/badge.svg)

A production-level e-commerce backend built with Spring Boot, featuring microservice architecture, event-driven design, and distributed caching.

---

## 🏗 Architecture

```
Client
  ↓
API Gateway (8090)          ← Single entry point, JWT validation
  ├── /api/v1/auth/**    → auth-service    (8081)
  ├── /api/v1/products/** → product-service (8082)
  └── /api/v1/orders/**  → order-service   (8083)

Each service has its own database:
  auth-service    → auth_db    (PostgreSQL)
  product-service → product_db (PostgreSQL + Redis)
  order-service   → order_db   (PostgreSQL)

Async communication:
  order-service → Kafka → notification-service
```

---

## ✨ Features

- **JWT Authentication** — Stateless auth with access & refresh tokens
- **Role-Based Access Control** — USER / ADMIN roles via Spring Security
- **Product Management** — CRUD, pagination, sorting, keyword search
- **Order Management** — Stock validation, status tracking, price snapshot
- **Event-Driven Architecture** — Apache Kafka for async notifications
- **Redis Caching** — Product caching with automatic invalidation
- **API Gateway** — Single entry point with JWT validation and routing
- **Microservice Architecture** — Each service independently deployable
- **Global Exception Handling** — Consistent error responses
- **API Documentation** — Swagger / OpenAPI 3.0

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Caching | Redis 7 |
| Message Broker | Apache Kafka |
| API Gateway | Spring Cloud Gateway |
| Build | Maven |
| Containerization | Docker Compose |
| Documentation | Swagger / OpenAPI 3.0 |

---

## 📁 Project Structure

```
ecommerce-system/
├── src/                          # Monolith (reference implementation)
│   └── main/java/com/ecommerce/
│       ├── auth/
│       ├── product/
│       ├── order/
│       ├── event/
│       └── config/
│
└── microservices/                # Microservice architecture
    ├── api-gateway/              # Port 8090
    ├── auth-service/             # Port 8081
    ├── product-service/          # Port 8082
    ├── order-service/            # Port 8083
    └── docker-compose.yml
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose

### Run Microservices

```bash
# Start infrastructure (PostgreSQL x3, Redis, Kafka, Zookeeper)
docker-compose -f microservices/docker-compose.yml up -d

# Start each service in separate terminals
mvn spring-boot:run -f microservices/auth-service/pom.xml
mvn spring-boot:run -f microservices/product-service/pom.xml
mvn spring-boot:run -f microservices/order-service/pom.xml
mvn spring-boot:run -f microservices/api-gateway/pom.xml
```

### Run Monolith (reference)

```bash
docker-compose up -d
mvn spring-boot:run
```

---

## 📡 API Endpoints

All requests go through the API Gateway at `http://localhost:8090`

### Authentication
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |

### Products
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/v1/products` | Public |
| GET | `/api/v1/products/{id}` | Public |
| GET | `/api/v1/products/search?keyword=` | Public |
| POST | `/api/v1/products` | ADMIN |
| PUT | `/api/v1/products/{id}` | ADMIN |
| DELETE | `/api/v1/products/{id}` | ADMIN |

### Categories
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/v1/categories` | Public |
| POST | `/api/v1/categories` | ADMIN |
| PUT | `/api/v1/categories/{id}` | ADMIN |
| DELETE | `/api/v1/categories/{id}` | ADMIN |

### Orders
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/v1/orders` | Authenticated |
| GET | `/api/v1/orders/my-orders` | Authenticated |
| GET | `/api/v1/orders/{id}` | Authenticated |
| GET | `/api/v1/orders` | ADMIN |
| PATCH | `/api/v1/orders/{id}/status` | ADMIN |
| PATCH | `/api/v1/orders/{id}/cancel` | Authenticated |

---

## 🗺 Roadmap

- [x] Auth module (JWT, BCrypt, RBAC)
- [x] Product module (CRUD, pagination, search)
- [x] Order module (stock control, status tracking, price snapshot)
- [x] Event-driven architecture (Apache Kafka)
- [x] Redis caching
- [x] Microservice migration (API Gateway + 3 services)
- [x] Docker containerization
- [x] CI/CD pipeline (GitHub Actions)
- [ ] Payment integration
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline

---

## 📄 License

MIT
