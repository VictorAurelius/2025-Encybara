# Hướng Dẫn Cấu Hình Ngrok URL

## Tổng Quan

Hệ thống backend-app có thể kết nối với 2 services thông qua Ngrok tunneling:
- **Content Scoring Service** (content-scoring-service)
- **Pronunciation Assessment Service** (pronunciation-assessment-service)

## Giải Pháp Đã Triển Khai

### 1. Default Ngrok URL
- **URL mặc định**: `https://dural-rozanne-inquisitorial.ngrok-free.dev`
- Được cấu hình cứng trong `application.properties`
- Tự động áp dụng khi không có environment variables

### 2. Multi-level Configuration
Backend hỗ trợ 3 cấp độ cấu hình:

```
Environment Variables → application.properties defaults → fallback
```

## Scripts Quản Lý

### Build Script (`./build.sh`)
```bash
# Auto mode với default URL
./build.sh

# Với custom URL
NGROK_URL="your-url" ./build.sh

# Interactive mode
./build.sh --interactive
```

### Fix Environment Variables (`./fix-env-vars.sh`)
```bash
# Sửa environment variables mà không cần rebuild
./fix-env-vars.sh

# Options:
# 1) Use default Ngrok URL
# 2) Use custom Ngrok URL  
# 3) Use individual service URLs
# 4) Check current configuration
```

### Rebuild Backend (`./rebuild-backend.sh`)
```bash
# Rebuild container với application.properties mới
./rebuild-backend.sh
```

### Test Connection (`./test-ngrok-connection.sh`)
```bash
# Test kết nối và hiển thị configuration
./test-ngrok-connection.sh
```

## Cấu Hình Files

### application.properties
```properties
# Content Scoring Service Configuration
content-scoring.service.url=${CONTENT_SCORING_SERVICE_URL:https://dural-rozanne-inquisitorial.ngrok-free.dev}
content-scoring.service.timeout.connect=10
content-scoring.service.timeout.read=10

# Pronunciation Assessment Service Configuration
pronunciation-assessment.service.url=${PRONUNCIATION_SERVICE_URL:https://dural-rozanne-inquisitorial.ngrok-free.dev}
pronunciation-assessment.service.timeout.connect=10
pronunciation-assessment.service.timeout.read=10
```

### docker-compose.yml
```yaml
environment:
  # Content Scoring Service URL (Environment variable, no default)
  - CONTENT_SCORING_SERVICE_URL=${CONTENT_SCORING_SERVICE_URL:-}
  # Pronunciation Assessment Service URL (Environment variable, no default)  
  - PRONUNCIATION_SERVICE_URL=${PRONUNCIATION_SERVICE_URL:-}
```

## Workflow Sử Dụng

### Scenario 1: Quick Setup (Default URL)
```bash
# Chỉ cần chạy build
cd backend-app
./build.sh
```

### Scenario 2: Custom URL Setup
```bash
# Set environment variable và build
export NGROK_URL="https://your-custom-url.ngrok.io"
./build.sh
```

### Scenario 3: Fix URL Sau Khi Build
```bash
# Container đã chạy nhưng cần đổi URL
./fix-env-vars.sh
# Chọn option 1 cho default URL hoặc option 2 cho custom URL
```

### Scenario 4: Hard Reset (Rebuild)
```bash
# Khi cần update application.properties
./rebuild-backend.sh
```

## Troubleshooting

### Vấn đề: Environment Variables Không Được Set
**Triệu chứng**: Test script hiển thị "(Not set)"

**Giải pháp**:
1. Chạy `./fix-env-vars.sh` và chọn option 1
2. Hoặc rebuild: `./rebuild-backend.sh`

### Vấn đề: Service Connection Failed
**Triệu chứng**: HTTP 500 khi test API

**Kiểm tra**:
1. `./test-ngrok-connection.sh` - xem configuration
2. Verify Ngrok tunnel: `curl http://localhost:4040/api/tunnels`
3. Test direct service: `curl https://your-ngrok-url/health`

### Vấn đề: Container Restart Mất Configuration
**Nguyên nhân**: Environment variables không được persist

**Giải pháp**: Default URL trong `application.properties` sẽ tự động áp dụng

## API Endpoints Test

### Health Check
```bash
curl http://localhost:8080/api/v1/content-scoring/health
curl http://localhost:8080/api/v1/pronunciation/info
```

### Service Info
```bash
curl http://localhost:8080/api/v1/content-scoring/info
curl http://localhost:8080/api/v1/pronunciation/info
```

### Full Workflow Test
```bash
./test-content-scoring.sh
./test-pronunciation.sh
```

## Best Practices

### 1. Development
- Sử dụng default URL để setup nhanh
- Test với `./test-ngrok-connection.sh` trước khi deploy

### 2. Production  
- Set environment variables proper
- Monitor với health check endpoints
- Backup configuration script

### 3. Debugging
- Check container logs: `docker-compose logs -f backend`
- Verify environment trong container: `docker exec <container> printenv`
- Test direct service connection trước khi test qua backend

## Environment Variables Reference

| Variable | Purpose | Default |
|----------|---------|---------|
| `CONTENT_SCORING_SERVICE_URL` | Content scoring service endpoint | Default Ngrok URL |
| `PRONUNCIATION_SERVICE_URL` | Pronunciation service endpoint | Default Ngrok URL |
| `NGROK_URL` | Single URL cho cả 2 services | N/A |

## Scripts Reference

| Script | Purpose | Usage |
|--------|---------|-------|
| `build.sh` | Build và start containers | `./build.sh [--interactive]` |
| `fix-env-vars.sh` | Fix environment variables | `./fix-env-vars.sh` |
| `rebuild-backend.sh` | Rebuild backend container | `./rebuild-backend.sh` |
| `test-ngrok-connection.sh` | Test connection và configuration | `./test-ngrok-connection.sh` |
| `test-content-scoring.sh` | Test content scoring workflow | `./test-content-scoring.sh` |
| `test-pronunciation.sh` | Test pronunciation workflow | `./test-pronunciation.sh` |