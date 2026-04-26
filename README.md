# E-Commerce System - Production-Level Backend

Modern, scalable e-commerce backend built with Spring Boot, designed for microservice architecture.

## 🚀 Tech Stack

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Lombok**

## 📁 Project Structure

```
src/main/java/com/ecommerce/
├── EcommerceApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── security/
│       ├── JwtAuthenticationFilter.java
│       └── UserDetailsServiceImpl.java
├── common/
│   ├── exception/
│   ├── response/
│   └── util/
└── auth/
    ├── controller/
    ├── service/
    ├── repository/
    ├── model/
    └── dto/
```

## 🔧 Setup

### Prerequisites
- Java 17+
- PostgreSQL
- Maven

### Database Setup

```sql
CREATE DATABASE ecommerce_db;
```

### Environment Variables

```bash
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
```

### Run Application

```bash
mvn clean install
mvn spring-boot:run
```

## 🔐 API Endpoints

### Authentication

#### Register
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "Bearer",
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "USER"
    }
  },
  "timestamp": "2024-04-26T10:30:00"
}
```

## 🎯 Features

✅ JWT Authentication  
✅ Password Encryption (BCrypt)  
✅ Global Exception Handling  
✅ DTO Pattern  
✅ Clean Architecture  
✅ Role-Based Access Control  
✅ Validation  

## 🔜 Next Steps

- [ ] Product Module
- [ ] Order Module
- [ ] Payment Integration
- [ ] Inventory Management
- [ ] Event-Driven Architecture (Kafka)
- [ ] Redis Caching
- [ ] Docker Support
- [ ] Microservice Migration

## 📝 License

MIT
