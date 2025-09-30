# 🔍 Debug Guide - Content Scoring Service

## ✅ Build thành công rồi!

Build script đã hoàn thành thành công. Tuy nhiên có health check timeout, cần kiểm tra logs.

## 🚨 Debug Steps

### 1. Kiểm tra container status
```bash
docker compose ps
```

### 2. Xem logs chi tiết
```bash
# Xem logs của service
docker compose logs -f content-scoring-service

# Hoặc xem logs ngắn gọn
docker compose logs content-scoring-service --tail=50
```

### 3. Kiểm tra container có chạy không
```bash
# Xem tất cả containers
docker ps -a

# Kiểm tra specific container
docker inspect content-scoring-service
```

### 4. Test endpoints manual
```bash
# Test health check
curl http://localhost:5001/health

# Nếu không response, check port
netstat -tulpn | grep :5001
```

### 5. Nếu service không start

#### Option A: Restart container
```bash
docker compose restart content-scoring-service
```

#### Option B: Debug inside container
```bash
# Enter container
docker compose exec content-scoring-service bash

# Hoặc nếu container không chạy
docker run -it --rm content-scoring-service bash

# Test Python và imports
python -c "import app.main; print('App imported successfully')"
```

#### Option C: Check dependencies
```bash
# Run container với command override
docker compose run --rm content-scoring-service python -c "
import sys
print('Python version:', sys.version)
try:
    import fastapi
    print('FastAPI imported successfully')
    import uvicorn
    print('Uvicorn imported successfully')
    import sentence_transformers
    print('Sentence transformers imported successfully')
    import spacy
    print('spaCy imported successfully')
    print('All dependencies OK!')
except Exception as e:
    print('Import error:', e)
"
```

## 🔧 Common Solutions

### 1. Service takes time to start
ML models need time to load. Wait 2-3 minutes then check:
```bash
curl http://localhost:5001/health
```

### 2. Memory issues
Increase Docker memory to 4GB+ in Docker Desktop settings.

### 3. Port conflicts
Change port in docker-compose.yml:
```yaml
ports:
  - "5002:5001"  # Use different external port
```

### 4. Dependencies issues
Rebuild with verbose output:
```bash
docker build --no-cache --progress=plain -t content-scoring-service .
```

## 🧪 Test API Once Working

```bash
# Health check
curl http://localhost:5001/health

# API test
curl -X POST http://localhost:5001/api/content-scoring \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is machine learning?",
    "answer": "Machine learning is a subset of AI that uses algorithms to learn patterns from data."
  }'

# Check docs
open http://localhost:5001/docs
```

## 📋 Status Checklist

Run these commands và báo kết quả:

```bash
# 1. Container status
docker compose ps

# 2. Service logs
docker compose logs content-scoring-service --tail=20

# 3. Health check
curl -v http://localhost:5001/health

# 4. Port check
netstat -tulpn | grep :5001
```

---

**Hãy chạy `docker compose logs content-scoring-service` để xem logs và báo lại kết quả!**