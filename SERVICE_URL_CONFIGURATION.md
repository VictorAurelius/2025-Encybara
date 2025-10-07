# Service URL Configuration Guide

## Tổng quan

Encybara sử dụng 3 microservices chính:
1. **Backend Application** (Spring Boot) - Port 8080
2. **Content Scoring Service** (Python/Flask) - Port 5001
3. **Pronunciation Assessment Service** (Python/Flask) - Port 5000

## Cấu hình URL trong application.properties

### Mặc định (Local Development)

```properties
# Content Scoring Service Configuration
content-scoring.service.url=${CONTENT_SCORING_SERVICE_URL:http://localhost:5001}
content-scoring.service.timeout.connect=10
content-scoring.service.timeout.read=10

# Pronunciation Assessment Service Configuration
pronunciation-assessment.service.url=${PRONUNCIATION_SERVICE_URL:http://localhost:5000}
pronunciation-assessment.service.timeout.connect=30
pronunciation-assessment.service.timeout.read=30
```

### Giải thích

- **Format:** `${ENV_VAR:default_value}`
- **`CONTENT_SCORING_SERVICE_URL`**: Biến môi trường (optional)
  - Mặc định: `http://localhost:5001`
- **`PRONUNCIATION_SERVICE_URL`**: Biến môi trường (optional)
  - Mặc định: `http://localhost:5000`

## Các Scenario Sử dụng

### 1. Local Development (Mặc định)

**Không cần config gì**, chỉ cần chạy các services:

```bash
# Terminal 1: Content Scoring Service
cd content-scoring-service
docker-compose up

# Terminal 2: Pronunciation Assessment Service
cd pronunciation-assessment-service
docker-compose up

# Terminal 3: Backend
cd backend-app
./gradlew bootRun
```

**URLs sử dụng:**
- Content Scoring: `http://localhost:5001`
- Pronunciation: `http://localhost:5000`

### 2. Docker Compose All (Recommended)

Chạy tất cả services trong cùng 1 Docker network:

```bash
docker-compose -f docker-compose.all.yml up -d
```

**Environment variables được set tự động:**
```yaml
environment:
  - CONTENT_SCORING_SERVICE_URL=http://content-scoring-service:5001
  - PRONUNCIATION_SERVICE_URL=http://pronunciation-assessment-service:5000
```

**URLs sử dụng:**
- Content Scoring: `http://content-scoring-service:5001` (trong Docker network)
- Pronunciation: `http://pronunciation-assessment-service:5000` (trong Docker network)

### 3. Production với Public URLs

**Chỉ khi cần expose ra internet (ví dụ: Ngrok, domain thực):**

```bash
# Set environment variables
export CONTENT_SCORING_SERVICE_URL=https://content-scoring.example.com
export PRONUNCIATION_SERVICE_URL=https://pronunciation.example.com

# Hoặc trong docker-compose:
environment:
  - CONTENT_SCORING_SERVICE_URL=https://content-scoring.example.com
  - PRONUNCIATION_SERVICE_URL=https://pronunciation.example.com
```

### 4. Hybrid Setup (Backend local, Services trong Docker)

```bash
# Start services
docker-compose -f content-scoring-service/docker-compose.yml up -d
docker-compose -f pronunciation-assessment-service/docker-compose.yml up -d

# Backend sử dụng localhost URLs (default)
cd backend-app
./gradlew bootRun
```

**URLs sử dụng:** `http://localhost:5001` và `http://localhost:5000`

## Build All Services

Script `build-all.sh` sẽ build tất cả services **trừ ngrok-service**:

```bash
# Build tất cả
./build-all.sh

# Build với clean
./build-all.sh --clean

# Build với no-cache
./build-all.sh --no-cache

# Build cả 3 services
# 1. Content Scoring Service
# 2. Pronunciation Assessment Service
# 3. Backend Application
```

## Kiểm tra cấu hình hiện tại

### 1. Kiểm tra từ Backend logs

```bash
# Khi backend khởi động, check logs:
grep -i "content-scoring\|pronunciation" logs/application.log

# Hoặc trong Docker:
docker logs encybara-backend | grep -i "service.url"
```

### 2. Kiểm tra service health

```bash
# Content Scoring
curl http://localhost:5001/health

# Pronunciation Assessment
curl http://localhost:5000/health

# Backend
curl http://localhost:8080/actuator/health
```

### 3. Test integration

```bash
# Test content scoring integration
cd backend-app
./test-content-scoring.sh

# Test pronunciation integration
cd backend-app
./test-pronunciation.sh
```

## Troubleshooting

### Service không kết nối được

**Lỗi:** `Connection refused` hoặc `Service unavailable`

**Giải pháp:**

1. **Check service đã chạy chưa:**
   ```bash
   curl http://localhost:5001/health  # Content Scoring
   curl http://localhost:5000/health  # Pronunciation
   ```

2. **Trong Docker network, dùng container name:**
   ```bash
   # Sai (trong Docker network):
   http://localhost:5001

   # Đúng (trong Docker network):
   http://content-scoring-service:5001
   ```

3. **Check environment variables:**
   ```bash
   # In Docker container
   docker exec encybara-backend env | grep SERVICE_URL
   ```

### URL bị override không mong muốn

**Nếu bạn muốn dùng localhost nhưng env var đang set:**

```bash
# Unset env var
unset CONTENT_SCORING_SERVICE_URL
unset PRONUNCIATION_SERVICE_URL

# Hoặc set về localhost
export CONTENT_SCORING_SERVICE_URL=http://localhost:5001
export PRONUNCIATION_SERVICE_URL=http://localhost:5000
```

### Public URL (Ngrok) scenario

**Chỉ dùng khi cần expose ra internet:**

```bash
# Start Ngrok tunnel cho content-scoring
cd content-scoring-service
docker-compose --profile tunnel up -d

# Get public URL
curl http://localhost:4040/api/tunnels | jq '.tunnels[0].public_url'

# Set env var cho backend
export CONTENT_SCORING_SERVICE_URL=https://abc123.ngrok.io

# Restart backend
./gradlew bootRun
```

## Best Practices

### ✅ Nên làm

1. **Local Development:** Dùng localhost URLs (mặc định)
2. **Docker Compose All:** Dùng container names
3. **Production:** Set env vars rõ ràng
4. **Testing:** Luôn check health endpoints trước khi test integration

### ❌ Không nên làm

1. **Hardcode URLs** trong code
2. **Mix localhost và container names** trong cùng 1 setup
3. **Expose public URLs** khi không cần thiết
4. **Quên set timeout** cho các service calls

## Summary Table

| Scenario | Content Scoring URL | Pronunciation URL | How to Set |
|----------|-------------------|-------------------|------------|
| **Local Dev (Default)** | `http://localhost:5001` | `http://localhost:5000` | No config needed |
| **Docker Compose All** | `http://content-scoring-service:5001` | `http://pronunciation-assessment-service:5000` | Set in docker-compose.all.yml |
| **Production/Public** | `https://your-domain.com` | `https://your-domain.com` | Set env vars |
| **Ngrok** | `https://xxx.ngrok.io` | `https://yyy.ngrok.io` | Set env vars |

## Quick Reference

```bash
# Build all services (except ngrok)
./build-all.sh

# Start all services
docker-compose -f docker-compose.all.yml up -d

# Start individual services
docker-compose -f content-scoring-service/docker-compose.yml up -d
docker-compose -f pronunciation-assessment-service/docker-compose.yml up -d
docker-compose -f build-docker/docker-compose.yml up -d

# Check health
curl http://localhost:5001/health
curl http://localhost:5000/health
curl http://localhost:8080/actuator/health

# View logs
docker-compose -f docker-compose.all.yml logs -f

# Stop all
docker-compose -f docker-compose.all.yml down
```

---

**Last Updated:** 2025-10-06
**Version:** 1.0.0
