# ============================================================
# E-Commerce Microservices - Kubernetes Deploy Script
# Kullanim: .\k8s\deploy.ps1
# ============================================================

$ErrorActionPreference = "Stop"
$MAVEN = "$env:USERPROFILE\.m2\maven-3.9.6\bin\mvn.cmd"
$SERVICES = @("auth-service", "product-service", "order-service", "api-gateway")

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " E-Commerce K8s Deploy Basliyor..." -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# 1. Minikube Docker env'ini aktif et
Write-Host "`n[1/5] Minikube Docker environment aktif ediliyor..." -ForegroundColor Yellow
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

# 2. Maven build (her servis)
Write-Host "`n[2/5] Maven build basliyor..." -ForegroundColor Yellow
foreach ($svc in $SERVICES) {
    Write-Host "  Building $svc..." -ForegroundColor Gray
    & $MAVEN -f "microservices/$svc/pom.xml" package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  HATA: $svc build basarisiz!" -ForegroundColor Red
        exit 1
    }
    Write-Host "  $svc build OK" -ForegroundColor Green
}

# 3. Docker image build (minikube icinde)
Write-Host "`n[3/5] Docker image'lar build ediliyor (minikube icinde)..." -ForegroundColor Yellow
foreach ($svc in $SERVICES) {
    Write-Host "  Building image: $svc`:latest" -ForegroundColor Gray
    docker build -t "$svc`:latest" "microservices/$svc"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  HATA: $svc image build basarisiz!" -ForegroundColor Red
        exit 1
    }
    Write-Host "  $svc image OK" -ForegroundColor Green
}

# 4. K8s manifest'leri uygula
Write-Host "`n[4/5] Kubernetes manifest'leri uygulanıyor..." -ForegroundColor Yellow

kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secret.yml
kubectl apply -f k8s/configmap.yml

Write-Host "  Infrastructure deploy ediliyor..." -ForegroundColor Gray
kubectl apply -f k8s/infrastructure/

Write-Host "  Infrastructure hazir olana kadar bekleniyor (30s)..." -ForegroundColor Gray
Start-Sleep -Seconds 30

Write-Host "  Microservices deploy ediliyor..." -ForegroundColor Gray
kubectl apply -f k8s/services/

# 5. Durum kontrolu
Write-Host "`n[5/5] Pod durumu kontrol ediliyor..." -ForegroundColor Yellow
Write-Host "  (Pod'lar baslamasi ~60 saniye surebilir)" -ForegroundColor Gray
kubectl get pods -n ecommerce

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host " Deploy tamamlandi!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pod durumu icin:" -ForegroundColor White
Write-Host "  kubectl get pods -n ecommerce -w" -ForegroundColor Yellow
Write-Host ""
Write-Host "API Gateway URL icin:" -ForegroundColor White
Write-Host "  minikube service api-gateway -n ecommerce --url" -ForegroundColor Yellow
Write-Host ""
Write-Host "Log gormek icin:" -ForegroundColor White
Write-Host "  kubectl logs -f deployment/auth-service -n ecommerce" -ForegroundColor Yellow
