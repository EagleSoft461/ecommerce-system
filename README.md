# 🛒 E-Commerce System

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square&logo=jsonwebtokens)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=flat-square&logo=swagger)

A production-level e-commerce backend built with Spring Boot, designed with clean layered architecture and ready for microservice migration.

## ✨ Features

- **JWT Authentication** — Stateless auth with access & refresh tokens
- **Role-Based Access Control** — USER / ADMIN roles via Spring Security
- **Product Management** — CRUD, pagination, sorting, keyword search
- **Category Management** — Hierarchical product categorization
- **Soft Delete** — Data integrity preserved, no hard deletes
- **Global Exception Handling** — Consistent error responses
- **API Documentation** — Swagger / OpenAPI 3.0

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Containerization | Docker Compose |
| Documentation | Swagger / OpenAPI 3.0 |
| Message Broker | Apache Kafka |

## 📁 Architecture

```
src/main/java/com/ecommerce/
├── auth/               # Authentication & authorization
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
├── product/            # Product & category management
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
├── common/             # Shared utilities
│   ├── exception/
│   ├── response/
│   └── util/
└── config/             # Security & Swagger config
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose

### Run with Docker

```bash
# Start PostgreSQL
docker-compose up -d

# Run the application
./mvnw spring-boot:run
```

### API Documentation

Once running, visit: `http://localhost:8082/swagger-ui/index.html`

### Orders
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/v1/orders` | Authenticated |
| GET | `/api/v1/orders/my-orders` | Authenticated |
| GET | `/api/v1/orders/{id}` | Authenticated |
| GET | `/api/v1/orders` | ADMIN |
| PATCH | `/api/v1/orders/{id}/status` | ADMIN |
| PATCH | `/api/v1/orders/{id}/cancel` | Authenticated |

## 📡 API Endpoints

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

## 🗺 Roadmap

- [x] Auth module (JWT, BCrypt, RBAC)
- [x] Product module (CRUD, pagination, search)
- [x] Order module (stock control, status tracking, price snapshot)
- [x] Event-driven architecture (Apache Kafka)
- [ ] Redis caching
- [ ] Inventory management
- [ ] Payment integration
- [ ] Apache Kafka (event-driven architecture)
- [ ] Redis caching
- [ ] Microservice migration

## 📄 License

MIT
