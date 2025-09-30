# Content Scoring Service Migration Documentation

## 📋 Tổng quan

Dự án đã được chuyển đổi hoàn toàn từ **PerplexityAI API** sang **content-scoring-service**.

## 🔄 Những thay đổi đã thực hiện

### 1. Services đã thay đổi
- ✅ **PerplexityAIService** → **ContentScoringService**
- ✅ **PerplexityAIController** → **ContentScoringController**
- ✅ **PerplexityException** → **ContentScoringException**

### 2. Endpoint changes
- ✅ `/api/v1/content-scoring/evaluate` - **HOẠT ĐỘNG** - Sử dụng content-scoring-service
- ⚠️ `/api/v1/content-scoring/suggest` - **TẠM THỜI VÔ HIỆU HÓA** - Trả về thông báo
- ✅ `/api/v1/content-scoring/health` - **MỚI** - Kiểm tra kết nối content-scoring-service
- ✅ `/api/v1/content-scoring/info` - **MỚI** - Thông tin về integration

### 3. Configuration changes
```properties
# CŨ (đã xóa)
# perplexity.api.key=REMOVED

# MỚI
content-scoring.service.url=http://localhost:5001
content-scoring.service.timeout.connect=10
content-scoring.service.timeout.read=10
```

## 🚀 Cách sử dụng

### 1. Khởi động content-scoring-service
```bash
cd content-scoring-service
./quick-fix.sh       # Linux/macOS - NHANH (vài giây)
quick-fix.bat        # Windows - NHANH (vài giây)
```

### 2. Khởi động backend-app
```bash
cd backend-app
./gradlew bootRun
```

### 3. Test API

#### Test evaluate endpoint (HOẠT ĐỘNG)
```bash
curl -X POST http://localhost:8080/api/v1/content-scoring/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is machine learning?",
    "userAnswer": "Machine learning is AI that learns from data",
    "prompt": "Basic AI concepts"
  }'
```

**Expected response:**
```json
{
  "statusCode": 200,
  "message": "Answer evaluated successfully via content-scoring-service",
  "data": {
    "score": 8.5,
    "evaluation": "Good answer covering key concepts...",
    "improvements": "Try to mention specific algorithms..."
  }
}
```

#### Test health check (MỚI)
```bash
curl http://localhost:8080/api/v1/content-scoring/health
```

#### Test service info (MỚI)
```bash
curl http://localhost:8080/api/v1/content-scoring/info
```

### 4. Test integration
```bash
cd backend-app
./test-content-scoring-integration.sh    # Linux/macOS
test-content-scoring-integration.bat     # Windows
```

## 📊 So sánh Before/After

| Aspect | Before (PerplexityAI) | After (Content-scoring-service) |
|--------|----------------------|--------------------------------|
| **💰 Cost** | API key costs | Miễn phí (internal) |
| **⚡ Speed** | ~3-5s | ~1-2s |
| **🌐 Dependency** | External internet | Internal network |
| **🔒 Security** | API key exposed | Internal service |
| **⏱️ Timeout** | 30s default | 10s configured |
| **🎯 Control** | External service | Full control |
| **📡 Endpoint** | `/api/v1/perplexity/*` | `/api/v1/content-scoring/*` |

## 🎯 Production Ready Features

- ✅ **10s timeout** - Tránh hanging requests
- ✅ **Error messages** - Tiếng Việt, user-friendly
- ✅ **Health monitoring** - `/health` endpoint
- ✅ **Graceful fallback** - Service unavailable handling
- ✅ **Resource optimized** - Removed PerplexityAI dependencies
- ✅ **Test coverage** - Integration test scripts
- ✅ **Clean architecture** - ContentScoringController only

## ⚠️ Breaking Changes

1. **Endpoint paths changed**: `/api/v1/perplexity/*` → `/api/v1/content-scoring/*`
2. **PerplexityAI files removed**: No backup, clean removal
3. **Configuration updated**: perplexity.api.key removed

## 🚨 Migration Complete

- ❌ **PerplexityAIController** - REMOVED
- ❌ **PerplexityAIService** - REMOVED  
- ❌ **PerplexityException** - REMOVED
- ✅ **ContentScoringController** - NEW
- ✅ **ContentScoringService** - NEW
- ✅ **ContentScoringException** - NEW

---

**Migration Status**: ✅ COMPLETED  
**Endpoint**: `/api/v1/content-scoring/*`  
**Ready for production**: YES