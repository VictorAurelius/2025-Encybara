# Encybara - Quick Reference

## 🚀 Quick Start (3 bước)

```bash
# 1. Build tất cả services
./build-all.sh

# 2. Start tất cả services
./start-all.sh

# 3. Kiểm tra
curl http://localhost:8080/actuator/health
```

## 📦 Services

| Service | Port | URL |
|---------|------|-----|
| Backend API | 8080 | http://localhost:8080 |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| Content Scoring | 5001 | http://localhost:5001 |
| Pronunciation | 5000 | http://localhost:5000 |

## 🔧 Lệnh thường dùng

```bash
# Build
./build-all.sh                    # Build tất cả
./build-all.sh --clean            # Clean rebuild
./build-all.sh --no-cache         # No cache rebuild

# Start/Stop
./start-all.sh                    # Start tất cả
./start-all.sh --foreground       # Start với logs
./stop-all.sh                     # Stop tất cả
./stop-all.sh --volumes           # Stop + xóa data

# Logs
docker-compose -f docker-compose.all.yml logs -f
docker-compose -f docker-compose.all.yml logs -f backend

# Health check
curl http://localhost:5001/health
curl http://localhost:5000/health
curl http://localhost:8080/actuator/health
```

## 📝 Cấu hình Service URLs

**Mặc định (Localhost):**
- Content Scoring: `http://localhost:5001`
- Pronunciation: `http://localhost:5000`

**Docker Network (Tự động):**
- Content Scoring: `http://content-scoring-service:5001`
- Pronunciation: `http://pronunciation-assessment-service:5000`

**Public URLs (Chỉ khi cần):**
```bash
export CONTENT_SCORING_SERVICE_URL=https://your-domain.com
export PRONUNCIATION_SERVICE_URL=https://your-domain.com
```

Chi tiết: Xem `SERVICE_URL_CONFIGURATION.md`

## 🎯 Build riêng từng service

### Content Scoring
```bash
cd content-scoring-service
./build.sh
docker-compose up -d
```

### Pronunciation Assessment
```bash
cd pronunciation-assessment-service
./build.sh
docker-compose up -d
```

### Backend
```bash
cd backend-app
./build.sh
docker-compose -f ../build-docker/docker-compose.yml up -d
```

## 🧪 Testing

```bash
# Content Scoring
cd content-scoring-service && ./test-ngrok-public.sh

# Pronunciation
cd pronunciation-assessment-service && ./test-optimized.sh

# Backend Integration
cd backend-app && ./test-content-scoring.sh
```

## 📚 Documentation

- **BUILD_AND_RUN.md** - Hướng dẫn build và chạy chi tiết
- **SERVICE_URL_CONFIGURATION.md** - Cấu hình URL services
- **pronunciation-assessment-service/OPTIMIZATIONS.md** - Tối ưu hóa pronunciation (60-70% nhanh hơn!)
- **pronunciation-assessment-service/QUICK_START.md** - Quick start pronunciation service

## 🔥 Performance Highlights

### Pronunciation Service (Optimized!)
- **Before:** >30s cho file audio 10s
- **After:** ~8-12s cho file audio 10s
- **Improvement:** 60-70% nhanh hơn!

### Các tối ưu hóa:
- MFA beam width reduction (13→10)
- Single speaker mode
- Timeout reduction (300s→60s)
- Better code organization

## ⚠️ Important Notes

1. **Build-all.sh** build **TẤT CẢ** services **NGOẠI TRỪ ngrok-service**
2. URLs được config trong `application.properties` với **localhost mặc định**
3. Chỉ dùng **public URLs/env vars** khi thực sự cần thiết
4. Docker network tự động override URLs khi dùng `docker-compose.all.yml`

### 🪟 Windows Users

**IMPORTANT:** Pronunciation service **MUST use Docker** on Windows!

```powershell
cd pronunciation-assessment-service
docker-compose up -d
```

See `pronunciation-assessment-service/ERROR_WINDOWS.md` for details.

## 🆘 Troubleshooting

### Services không start được
```bash
# Check Docker
docker info

# Check ports
lsof -i :5000 :5001 :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# Clean restart
./stop-all.sh --volumes
./build-all.sh --clean
./start-all.sh
```

### Service connection issues
```bash
# Check health
curl http://localhost:5001/health
curl http://localhost:5000/health
curl http://localhost:8080/actuator/health

# Check logs
docker-compose -f docker-compose.all.yml logs backend
```

---

**Version:** 1.0.0
**Updated:** 2025-10-06

Đọc `BUILD_AND_RUN.md` để biết thêm chi tiết!
