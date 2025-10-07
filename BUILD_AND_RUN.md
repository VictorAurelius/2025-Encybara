# Build and Run Guide - Encybara Services

## Tổng quan

Encybara bao gồm 3 microservices chính:

1. **Backend Application** (Spring Boot) - Port 8080
2. **Content Scoring Service** (Python/Flask) - Port 5001
3. **Pronunciation Assessment Service** (Python/Flask) - Port 5000

## Quick Start

### 1. Build All Services (Recommended)

```bash
# Build tất cả services (trừ ngrok)
./build-all.sh

# Build với clean rebuild
./build-all.sh --clean

# Build không dùng cache
./build-all.sh --no-cache
```

### 2. Start All Services

```bash
# Start tất cả trong background
./start-all.sh

# Start và xem logs trực tiếp
./start-all.sh --foreground

# Start với rebuild
./start-all.sh --rebuild
```

### 3. Stop All Services

```bash
# Stop tất cả services
./stop-all.sh

# Stop và xóa volumes (database sẽ bị xóa!)
./stop-all.sh --volumes
```

## Service URLs

Khi tất cả services đang chạy:

| Service | URL | Description |
|---------|-----|-------------|
| Backend API | http://localhost:8080 | Main API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API Documentation |
| Content Scoring | http://localhost:5001 | Content scoring service |
| Content Scoring Health | http://localhost:5001/health | Health check |
| Pronunciation | http://localhost:5000 | Pronunciation assessment |
| Pronunciation Health | http://localhost:5000/health | Health check |

## Build Individual Services

### Content Scoring Service

```bash
cd content-scoring-service

# Build
./build.sh

# Build với clean
./build.sh --clean

# Start service
docker-compose up -d

# View logs
docker-compose logs -f
```

### Pronunciation Assessment Service

```bash
cd pronunciation-assessment-service

# Build
./build.sh

# Build với clean
./build.sh --clean

# Start service
docker-compose up -d

# View logs
docker-compose logs -f
```

### Backend Application

```bash
cd backend-app

# Build
./build.sh

# Start service (requires MySQL)
docker-compose -f ../build-docker/docker-compose.yml up -d

# View logs
docker-compose -f ../build-docker/docker-compose.yml logs -f backend
```

## Service Configuration

### URL Configuration

Services được cấu hình trong `backend-app/src/main/resources/application.properties`:

```properties
# Content Scoring Service
content-scoring.service.url=${CONTENT_SCORING_SERVICE_URL:http://localhost:5001}

# Pronunciation Assessment Service
pronunciation-assessment.service.url=${PRONUNCIATION_SERVICE_URL:http://localhost:5000}
```

**Mặc định:** Sử dụng localhost URLs

**Docker Network:** URLs được override tự động trong `docker-compose.all.yml`:
- `http://content-scoring-service:5001`
- `http://pronunciation-assessment-service:5000`

**Public URLs:** Set environment variables nếu cần:
```bash
export CONTENT_SCORING_SERVICE_URL=https://your-domain.com
export PRONUNCIATION_SERVICE_URL=https://your-domain.com
```

Chi tiết: Xem `SERVICE_URL_CONFIGURATION.md`

## Health Checks

### Check All Services

```bash
# Content Scoring
curl http://localhost:5001/health

# Pronunciation Assessment
curl http://localhost:5000/health

# Backend
curl http://localhost:8080/actuator/health
```

### Expected Responses

**Content Scoring:**
```json
{
  "status": "healthy",
  "version": "1.0.0"
}
```

**Pronunciation Assessment:**
```json
{
  "status": "healthy",
  "memory_usage_mb": 150.5,
  "service": "pronunciation-assessment",
  "version": "1.0.0-optimized"
}
```

**Backend:**
```json
{
  "status": "UP"
}
```

## Testing

### Content Scoring Service

```bash
cd content-scoring-service
./test-ngrok-public.sh
```

### Pronunciation Assessment Service

```bash
cd pronunciation-assessment-service
./test-optimized.sh
```

### Backend Integration

```bash
cd backend-app

# Test content scoring integration
./test-content-scoring.sh

# Test pronunciation integration
./test-pronunciation.sh
```

## View Logs

### All Services

```bash
docker-compose -f docker-compose.all.yml logs -f
```

### Specific Service

```bash
# Content Scoring
docker-compose -f docker-compose.all.yml logs -f content-scoring-service

# Pronunciation
docker-compose -f docker-compose.all.yml logs -f pronunciation-assessment-service

# Backend
docker-compose -f docker-compose.all.yml logs -f backend

# MySQL
docker-compose -f docker-compose.all.yml logs -f db-mysql
```

### Individual Service Logs

```bash
# Content Scoring
docker-compose -f content-scoring-service/docker-compose.yml logs -f

# Pronunciation
docker-compose -f pronunciation-assessment-service/docker-compose.yml logs -f

# Backend
docker-compose -f build-docker/docker-compose.yml logs -f backend
```

## Troubleshooting

### Services Won't Start

1. **Check Docker is running:**
   ```bash
   docker info
   ```

2. **Check ports are available:**
   ```bash
   # Linux/Mac
   lsof -i :5000
   lsof -i :5001
   lsof -i :8080

   # Windows
   netstat -ano | findstr :5000
   netstat -ano | findstr :5001
   netstat -ano | findstr :8080
   ```

3. **Check previous containers:**
   ```bash
   docker ps -a
   docker-compose -f docker-compose.all.yml down
   ```

### Service Connection Issues

1. **Check service health:**
   ```bash
   curl http://localhost:5001/health
   curl http://localhost:5000/health
   curl http://localhost:8080/actuator/health
   ```

2. **Check Docker network:**
   ```bash
   docker network inspect 2025-encybara_encybara-network
   ```

3. **Check logs for errors:**
   ```bash
   docker-compose -f docker-compose.all.yml logs backend | grep -i error
   ```

### Database Connection Issues

1. **Check MySQL is running:**
   ```bash
   docker-compose -f docker-compose.all.yml ps db-mysql
   ```

2. **Check MySQL health:**
   ```bash
   docker exec encybara-mysql mysqladmin -u root -p123456 ping
   ```

3. **Reset database (WARNING: Data loss!):**
   ```bash
   ./stop-all.sh --volumes
   ./start-all.sh
   ```

### Build Issues

1. **Clean rebuild:**
   ```bash
   ./build-all.sh --clean --no-cache
   ```

2. **Remove old images:**
   ```bash
   docker images | grep encybara
   docker rmi <image-id>
   ```

3. **Prune Docker:**
   ```bash
   docker system prune -a
   ```

## Development Workflow

### Local Development (without Docker)

1. **Start Python services locally:**
   ```bash
   # Content Scoring
   cd content-scoring-service
   python -m venv venv
   source venv/bin/activate  # Windows: venv\Scripts\activate
   pip install -r requirements.txt
   python app/main.py

   # Pronunciation Assessment
   cd pronunciation-assessment-service
   # Setup requires MFA installation - see README
   python run.py
   ```

2. **Start Backend locally:**
   ```bash
   cd backend-app
   ./gradlew bootRun
   ```

### Docker Development

```bash
# Build and start
./build-all.sh
./start-all.sh

# Make changes
# ... edit code ...

# Rebuild specific service
cd <service-directory>
docker-compose build
docker-compose up -d

# Or rebuild all
./start-all.sh --rebuild
```

## Production Deployment

### 1. Build production images

```bash
./build-all.sh --clean --no-cache
```

### 2. Tag images

```bash
docker tag content-scoring-service:latest your-registry/content-scoring:v1.0.0
docker tag pronunciation-assessment-service:optimized your-registry/pronunciation:v1.0.0
docker tag encybara-backend:latest your-registry/backend:v1.0.0
```

### 3. Push to registry

```bash
docker push your-registry/content-scoring:v1.0.0
docker push your-registry/pronunciation:v1.0.0
docker push your-registry/backend:v1.0.0
```

### 4. Deploy

Update `docker-compose.all.yml` with registry URLs and deploy.

## Performance Notes

### Content Scoring Service
- Memory: ~200MB
- Startup: ~30-60 seconds
- Response time: ~500ms-2s

### Pronunciation Assessment Service (Optimized)
- Memory: ~2-4GB (MFA models)
- Startup: ~2-3 minutes (model loading)
- Response time: ~8-12s for 10s audio (improved from >30s)

### Backend Application
- Memory: ~512MB-1GB
- Startup: ~30-60 seconds
- Database required: MySQL 5.7

## Useful Commands

```bash
# Build all services
./build-all.sh

# Start all services
./start-all.sh

# Stop all services
./stop-all.sh

# View all logs
docker-compose -f docker-compose.all.yml logs -f

# Restart specific service
docker-compose -f docker-compose.all.yml restart backend

# Check service status
docker-compose -f docker-compose.all.yml ps

# Execute command in container
docker exec -it encybara-backend bash

# Check resource usage
docker stats
```

## Documentation

- `SERVICE_URL_CONFIGURATION.md` - URL configuration guide
- `content-scoring-service/README.md` - Content scoring docs
- `pronunciation-assessment-service/OPTIMIZATIONS.md` - Pronunciation optimizations
- `pronunciation-assessment-service/QUICK_START.md` - Quick start guide
- `backend-app/API_DOCUMENTATION.md` - Backend API docs

---

**Version:** 1.0.0
**Last Updated:** 2025-10-06
