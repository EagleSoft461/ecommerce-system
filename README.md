# 🛒 E-Commerce System

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6-black?style=flat-square&logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Minikube-326CE5?style=flat-square&logo=kubernetes)
![Helm](https://img.shields.io/badge/Helm-4.x-0F1689?style=flat-square&logo=helm)
![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C?style=flat-square&logo=prometheus)
![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?style=flat-square&logo=grafana)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=flat-square&logo=swagger)
![CI](https://github.com/EagleSoft461/ecommerce-system/actions/workflows/ci.yml/badge.svg)

A production-level e-commerce backend built with Spring Boot microservices, deployed on Kubernetes with full observability stack.

---

## 🏗 Architecture

```
                        ┌─────────────────────────────────────────┐
                        │           Kubernetes Cluster             │
                        │                                          │
  Client ──────────────►│  API Gateway (NodePort 30090)           │
                        │       ↓ JWT Validation                   │
                        │  ┌────┴──────────────────────────────┐  │
                        │  │         ecommerce namespace        │  │
                        │  │                                    │  │
                        │  │  /auth/**   → auth-service:8081   │  │
                        │  │  /products/** → product-service:8082│ │
                        │  │  /orders/** → order-service:8083  │  │
                        │  └────────────────────────────────────┘  │
                        │                                          │
                        │  ┌─────────────────────────────────────┐ │
                        │  │         Infrastructure               │ │
                        │  │  auth-db │ product-db │ order-db    │ │
                        │  │  Redis   │ Kafka      │ Zookeeper   │ │
                        │  └─────────────────────────────────────┘ │
                        │                                          │
                        │  ┌─────────────────────────────────────┐ │
                        │  │      monitoring namespace            │ │
                        │  │  Prometheus │ Grafana │ Alertmanager │ │
                        │  └─────────────────────────────────────┘ │
                        └─────────────────────────────────────────┘

Async communication:
  order-service ──Kafka──► product-service (stock update)
```

---

## ✨ Features

- **JWT Authentication** — Stateless auth with access & refresh tokens
- **Role-Based Access Control** — USER / ADMIN roles via Spring Security
- **Product Management** — CRUD, pagination, sorting, keyword search
- **Order Management** — Stock validation, status tracking, price snapshot
- **Event-Driven Architecture** — Apache Kafka for async order events
- **Redis Caching** — Product caching with automatic invalidation
- **API Gateway** — Single entry point with JWT validation and routing
- **Kubernetes Deployment** — All services running on K8s with health checks
- **Observability** — Prometheus metrics + Grafana dashboards
- **Global Exception Handling** — Consistent error responses
- **API Documentation** — Swagger / OpenAPI 3.0

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT (JJWT) |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Caching | Redis 7 |
| Message Broker | Apache Kafka |
| API Gateway | Spring Cloud Gateway |
| Build | Maven |
| Containerization | Docker + Docker Compose |
| Orchestration | Kubernetes (Minikube) |
| Package Manager | Helm 4.x |
| Monitoring | Prometheus + Grafana (kube-prometheus-stack) |
| Documentation | Swagger / OpenAPI 3.0 |
| CI/CD | GitHub Actions |

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
├── microservices/                # Microservice architecture
│   ├── api-gateway/              # Spring Cloud Gateway — Port 8090
│   ├── auth-service/             # JWT Auth — Port 8081
│   ├── product-service/          # Products + Redis — Port 8082
│   ├── order-service/            # Orders + Kafka — Port 8083
│   └── docker-compose.yml        # Local development
│
├── k8s/                          # Kubernetes manifests
│   ├── namespace.yml
│   ├── secret.yml
│   ├── configmap.yml
│   ├── infrastructure/           # PostgreSQL x3, Redis, Kafka, Zookeeper
│   └── services/                 # Microservice deployments
│
└── .github/workflows/
    └── ci.yml                    # GitHub Actions CI pipeline
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker Desktop
- kubectl
- Minikube
- Helm

### Option 1: Docker Compose (Local Development)

```bash
# Start infrastructure + all services
docker-compose -f microservices/docker-compose.yml up -d
```

API Gateway available at `http://localhost:8090`

### Option 2: Kubernetes (Minikube)

```bash
# Start Minikube
minikube start --driver=docker --cpus=4 --memory=6144

# Point Docker to Minikube
& minikube -p minikube docker-env --shell powershell | Invoke-Expression  # Windows
eval $(minikube docker-env)                                                # Linux/Mac

# Build images inside Minikube
docker build -t auth-service:latest microservices/auth-service
docker build -t product-service:latest microservices/product-service
docker build -t order-service:latest microservices/order-service
docker build -t api-gateway:latest microservices/api-gateway

# Deploy
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secret.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/infrastructure/
kubectl apply -f k8s/services/

# Get API Gateway URL
minikube service api-gateway -n ecommerce --url
```

### Option 3: With Monitoring Stack

```bash
# Install Prometheus + Grafana via Helm
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring

helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --set grafana.adminPassword=admin123 \
  --set grafana.service.type=NodePort \
  --set grafana.service.nodePort=30030

# Access Grafana
minikube service prometheus-grafana -n monitoring --url
# Login: admin / admin123
```

---

## 📡 API Endpoints

All requests go through the API Gateway.

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

## 📊 Monitoring

Grafana dashboards available after deploying the monitoring stack:

- **Kubernetes / Compute Resources / Namespace (Pods)** — CPU & memory per pod
- **Kubernetes / Compute Resources / Cluster** — Overall cluster health
- **Kubernetes / Networking** — Network traffic between services

---

## 🗺 Roadmap

- [x] Auth module (JWT, BCrypt, RBAC)
- [x] Product module (CRUD, pagination, search)
- [x] Order module (stock control, status tracking)
- [x] Event-driven architecture (Apache Kafka)
- [x] Redis caching
- [x] Microservice migration (API Gateway + 3 services)
- [x] Docker containerization
- [x] CI/CD pipeline (GitHub Actions)
- [x] Kubernetes deployment (Minikube)
- [x] Monitoring (Prometheus + Grafana via Helm)
- [ ] Distributed Tracing (Zipkin)
- [ ] Helm Charts for application
- [ ] Payment integration

---

## 📄 License

MIT
