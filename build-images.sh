#!/bin/sh
set -e

# Auth Service
cp /home/docker/auth-service.jar /tmp/auth.jar
cat > /tmp/Dockerfile-auth << 'EOF'
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY auth.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
cd /tmp && docker build -f Dockerfile-auth -t auth-service:latest .
echo "AUTH_DONE"

# Product Service
cp /home/docker/product-service.jar /tmp/product.jar
cat > /tmp/Dockerfile-product << 'EOF'
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY product.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
cd /tmp && docker build -f Dockerfile-product -t product-service:latest .
echo "PRODUCT_DONE"

# Order Service
cp /home/docker/order-service.jar /tmp/order.jar
cat > /tmp/Dockerfile-order << 'EOF'
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY order.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
cd /tmp && docker build -f Dockerfile-order -t order-service:latest .
echo "ORDER_DONE"

# API Gateway
cp /home/docker/api-gateway.jar /tmp/gateway.jar
cat > /tmp/Dockerfile-gateway << 'EOF'
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY gateway.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF
cd /tmp && docker build -f Dockerfile-gateway -t api-gateway:latest .
echo "GATEWAY_DONE"

docker images | grep -E "auth-service|product-service|order-service|api-gateway"
