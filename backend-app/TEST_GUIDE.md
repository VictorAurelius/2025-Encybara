# Backend-App Testing Guide

## Tóm Tắt Các Cải Tiến

### 🔧 Các Vấn Đề Đã Được Khắc Phục

#### 1. **Docker Compose Configuration Issues**
- **Vấn đề**: Hardcoded Content Scoring Service URL trong docker-compose.yml
- **Giải pháp**: Sử dụng environment variables để dynamic configuration
- **File đã sửa**: `build-docker/docker-compose.yml`

```yaml
# TRƯỚC (hardcoded):
- CONTENT_SCORING_SERVICE_URL=https://dural-rozanne-inquisitorial.ngrok-free.dev

# SAU (dynamic):
- CONTENT_SCORING_SERVICE_URL=${CONTENT_SCORING_SERVICE_URL:-http://localhost:5001}
```

#### 2. **Build Script Enhancement**
- **Vấn đề**: Script không hỗ trợ proper environment variable configuration cho cả 2 services
- **Giải pháp**: Enhanced build.sh với interactive và auto mode
- **Tính năng mới**:
  - Interactive mode: Prompt user cho service URLs
  - Auto mode: Sử dụng environment variables hoặc defaults
  - Proper validation và error handling

#### 3. **Missing Test Coverage**
- **Vấn đề**: Không có comprehensive test scripts cho 2 workflows
- **Giải pháp**: Tạo dedicated test scripts cho từng service

## 📋 Test Scripts Overview

### 1. Content Scoring Test Script: `test-content-scoring.sh`

**Chức năng**:
- Test complete content scoring workflow qua backend-app
- Validate API endpoints và error handling
- Performance testing với concurrent requests
- End-to-end integration testing

**Test Cases**:
1. Backend health check
2. Content scoring service health check
3. Service info retrieval
4. Valid content scoring request
5. Empty answer validation (should fail)
6. Missing question validation (should fail)
7. Long answer processing
8. Vietnamese language support
9. Suggestions endpoint (should be disabled)
10. Invalid HTTP method validation
11. Concurrent requests performance
12. End-to-end integration test

### 2. Pronunciation Assessment Test Script: `test-pronunciation.sh`

**Chức năng**:
- Test complete pronunciation assessment workflow qua backend-app
- File upload validation
- Audio file format support
- Error handling cho invalid inputs

**Test Cases**:
1. Backend health check
2. WAV file pronunciation assessment
3. MP3 file pronunciation assessment
4. Missing file validation (should fail)
5. Invalid HTTP method validation
6. Wrong content type validation (should fail)
7. File upload performance testing
8. End-to-end integration test

## 🚀 Cách Sử Dụng Test Scripts

### Prerequisites
```bash
# Đảm bảo backend-app đang chạy
cd build-docker
docker-compose ps backend  # Should show "healthy"

# Đảm bảo có curl command
curl --version
```

### Chạy Content Scoring Tests

#### Windows (Git Bash):
```bash
cd backend-app
bash test-content-scoring.sh
```

#### Linux/macOS:
```bash
cd backend-app
chmod +x test-content-scoring.sh
./test-content-scoring.sh
```

### Chạy Pronunciation Assessment Tests

#### Windows (Git Bash):
```bash
cd backend-app
bash test-pronunciation.sh
```

#### Linux/macOS:
```bash
cd backend-app
chmod +x test-pronunciation.sh
./test-pronunciation.sh
```

## 🔧 Build Script Configuration

### Interactive Mode (Default)
```bash
cd backend-app
./build.sh
```

Script sẽ prompt user để nhập:
1. **Pronunciation Assessment Service URL**
   - Ví dụ: `https://abc123.ngrok-free.app`
   - Để trống để sử dụng: `http://localhost:5000`

2. **Content Scoring Service URL**
   - Ví dụ: `https://def456.ngrok-free.app`
   - Để trống để sử dụng: `http://localhost:5001`

### Auto Mode (CI/CD hoặc Scripting)
```bash
# Sử dụng environment variables
export PRONUNCIATION_SERVICE_URL="https://your-pronunciation-url.ngrok-free.app"
export CONTENT_SCORING_SERVICE_URL="https://your-content-scoring-url.ngrok-free.app"

cd backend-app
./build.sh --auto
```

### Environment Variables Configuration
```bash
# Option 1: Set trước khi build
export PRONUNCIATION_SERVICE_URL="https://abc123.ngrok-free.app"
export CONTENT_SCORING_SERVICE_URL="https://def456.ngrok-free.app"
./build.sh --auto

# Option 2: Inline với Docker Compose
PRONUNCIATION_SERVICE_URL="https://abc123.ngrok-free.app" \
CONTENT_SCORING_SERVICE_URL="https://def456.ngrok-free.app" \
docker-compose up -d
```

## 📊 Expected Test Results

### ✅ Successful Test Output Example:
```bash
============================================
  CONTENT SCORING WORKFLOW TEST
============================================
✓ Backend Health Check - HTTP 200
✓ Content Scoring Service Health Check - HTTP 200
✓ Content Scoring Service Info - HTTP 200
✓ Content Scoring - Valid Request - HTTP 200
✓ Content Scoring - Empty Answer (should fail) - HTTP 400
✓ INTEGRATION TEST PASSED
  - Backend is healthy
  - Content scoring service is accessible
  - Scoring functionality works
```

### ❌ Failed Test Scenarios:
```bash
✗ Content Scoring Service Health Check - HTTP 503
  - Service not available hoặc URL không correct
✗ Content Scoring - Valid Request - HTTP 500
  - Internal service error hoặc timeout
```

## 🛠️ Troubleshooting

### 1. Backend Not Responding
```bash
# Check backend status
docker-compose ps backend

# Check backend logs
docker-compose logs -f backend

# Check backend health
curl http://localhost:8080/actuator/health
```

### 2. Content Scoring Service Not Available
```bash
# Check service configuration
curl http://localhost:8080/api/v1/content-scoring/health

# Check environment variables
docker-compose exec backend env | grep CONTENT_SCORING

# Test direct service call
curl https://your-content-scoring-url.ngrok-free.app/health
```

### 3. Pronunciation Assessment Service Not Available
```bash
# Check service configuration in application.properties
docker-compose exec backend env | grep PRONUNCIATION

# Test direct service call
curl https://your-pronunciation-url.ngrok-free.app/health
```

### 4. Ngrok URL Issues
```bash
# Get current Ngrok URL
cd content-scoring-service
./get-url-simple.sh

cd pronunciation-assessment-service
# Check if service is running
docker-compose ps
```

## 📝 API Endpoints Summary

### Content Scoring Endpoints (qua Backend-App):
```bash
# Health check
GET http://localhost:8080/api/v1/content-scoring/health

# Service info
GET http://localhost:8080/api/v1/content-scoring/info

# Evaluate answer
POST http://localhost:8080/api/v1/content-scoring/evaluate
Content-Type: application/json
{
  "question": "What is AI?",
  "userAnswer": "AI is artificial intelligence",
  "prompt": "Evaluate this answer"
}

# Suggestions (temporarily disabled)
POST http://localhost:8080/api/v1/content-scoring/suggest
```

### Pronunciation Assessment Endpoints (qua Backend-App):
```bash
# Assess pronunciation
POST http://localhost:8080/api/v1/pronunciation/assess
Content-Type: multipart/form-data
file: [audio file]
```

## 🔄 Workflow Integration

### 1. Development Workflow:
```bash
# 1. Start microservices
cd content-scoring-service
./build.sh
cd ../pronunciation-assessment-service  
./run.sh

# 2. Get public URLs (if using Ngrok)
cd content-scoring-service
./get-url-simple.sh

# 3. Build backend with URLs
cd ../backend-app
./build.sh  # Enter URLs when prompted

# 4. Run tests
./test-content-scoring.sh
./test-pronunciation.sh
```

### 2. Production Workflow:
```bash
# 1. Set environment variables
export CONTENT_SCORING_SERVICE_URL="https://prod-content-scoring.example.com"
export PRONUNCIATION_SERVICE_URL="https://prod-pronunciation.example.com"

# 2. Build và deploy
cd backend-app
./build.sh --auto

# 3. Validate với tests
./test-content-scoring.sh
./test-pronunciation.sh
```

## 📈 Performance Expectations

### Content Scoring:
- **Response Time**: < 2 seconds cho typical request
- **Concurrent Requests**: Support 5+ concurrent users
- **Score Range**: 0-10 (converted từ 0-100)

### Pronunciation Assessment:
- **File Upload**: Support WAV, MP3 formats
- **Processing Time**: Depends on audio length
- **File Size Limit**: Định nghĩa trong Spring Boot config

## 🔐 Security Considerations

### 1. Service URLs:
- Không commit Ngrok URLs vào code
- Sử dụng environment variables
- Validate service certificates trong production

### 2. File Uploads:
- Validate file types và sizes
- Scan uploaded files for malware
- Implement rate limiting

### 3. API Security:
- Implement authentication cho external access
- Add request logging và monitoring
- Set up proper CORS policies

## 📞 Support và Contact

Nếu gặp issues:

1. **Check logs**: `docker-compose logs -f backend`
2. **Validate configuration**: Environment variables và service URLs
3. **Run diagnostics**: Test scripts sẽ show detailed error messages
4. **Check service health**: Direct calls to microservices

**Lưu ý**: Test scripts được thiết kế để provide comprehensive diagnostics và troubleshooting information.