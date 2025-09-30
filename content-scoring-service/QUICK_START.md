# 🚀 Quick Start Guide - Content Scoring Service

## 🔧 FINAL FIX: Permission errors đã được sửa!

Tôi đã fix lỗi permission `/home/appuser` và HuggingFace cache. Có 2 cách:

### ⚡ Cách 1: Quick Fix (NHANH - không cần rebuild 3000s)
```bash
cd content-scoring-service

# Sử dụng quick fix scripts
./quick-fix.sh      # Linux/macOS
quick-fix.bat       # Windows
```

### 🐌 Cách 2: Rebuild hoàn toàn (CHẬM - 3000s)
```bash
cd content-scoring-service

# Stop containers hiện tại
docker compose down

# Clean rebuild với fix mới
docker build --no-cache -t content-scoring-service .

# Hoặc dùng build script
./build.sh --clean --no-cache
```

## 🚀 Quick Fix Scripts - Giải pháp nhanh

Quick fix sẽ:
- ✅ Sử dụng image đã build (không rebuild)
- ✅ Fix permissions bằng environment variables
- ✅ Chạy service với `/tmp/.cache` thay vì `/home/appuser/.cache`
- ✅ Tự động kiểm tra health và test API
- ✅ Tiết kiệm thời gian (vài giây thay vì 3000s)

```bash
# Linux/macOS
./quick-fix.sh

# Windows
quick-fix.bat
```

## ✅ Dockerfile đã được fixed:
- ✅ Direct pip install thay vì script wrapper
- ✅ Verification step để đảm bảo uvicorn installed
- ✅ Download tracking vẫn hoạt động
- ✅ Error handling tốt hơn

## 🚀 Sau khi rebuild với fix:

Giờ bạn có thể chạy service với các cách sau:

## 🏃 Cách chạy service

### 1. Chạy service cơ bản (chỉ main service)
```bash
cd content-scoring-service
docker compose up -d content-scoring-service
```

### 2. Chạy với monitoring (Prometheus + Grafana)
```bash
cd content-scoring-service
docker compose --profile monitoring up -d
```

### 3. Chạy với caching (Redis)
```bash
cd content-scoring-service
docker compose --profile caching up -d
```

### 4. Chạy tất cả services
```bash
cd content-scoring-service
docker compose --profile monitoring --profile caching --profile proxy up -d
```

### 5. Sử dụng build script đã fix
```bash
cd content-scoring-service
./build.sh              # Chỉ main service
./build.sh --monitoring  # Với monitoring
./build.sh --all         # Tất cả services
```

## 🔍 Kiểm tra service

### Kiểm tra các containers đang chạy
```bash
docker compose ps
```

### Kiểm tra health của service
```bash
curl http://localhost:5001/health
```

### Test API
```bash
curl -X POST http://localhost:5001/api/content-scoring \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is machine learning?",
    "answer": "Machine learning is a subset of AI that uses algorithms to learn from data."
  }'
```

## 🌐 Các endpoints có sẵn

- **Main Service**: http://localhost:5001
- **API Documentation**: http://localhost:5001/docs
- **Health Check**: http://localhost:5001/health
- **Metrics**: http://localhost:5001/metrics

Nếu chạy với monitoring:
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3100 (admin/admin123)

## 📋 Các lệnh hữu ích

```bash
# Xem logs
docker compose logs -f content-scoring-service

# Dừng services
docker compose down

# Dừng và xóa volumes
docker compose down --volumes

# Restart service
docker compose restart content-scoring-service

# Scale service (chạy nhiều instances)
docker compose up -d --scale content-scoring-service=3
```

## 🎯 Next Steps

1. **Test API**: Thử nghiệm API với các câu hỏi và câu trả lời khác nhau
2. **Monitor**: Kiểm tra metrics trên Prometheus/Grafana
3. **Production**: Deploy lên production environment
4. **Customize**: Tùy chỉnh configs theo nhu cầu

## 🚨 Nếu có vấn đề

### Lỗi permissions (Khuyến nghị: dùng Quick Fix)
```bash
# NHANH: Quick Fix (vài giây)
./quick-fix.sh      # Linux/macOS
quick-fix.bat       # Windows

# CHẬM: Rebuild (3000s)
docker compose down
docker build --no-cache -t content-scoring-service .
docker compose up -d
```

### Các lỗi khác
```bash
# Kiểm tra logs nếu service không start
docker compose logs content-scoring-service

# Với quick fix
docker compose -f docker-compose-quick-fix.yml logs content-scoring-service

# Kiểm tra port có bị sử dụng không
netstat -tulpn | grep :5001     # Linux
netstat -ano | findstr :5001    # Windows

# Stop tất cả containers
docker compose down
docker compose -f docker-compose-quick-fix.yml down
```

### So sánh Quick Fix vs Rebuild

| Tiêu chí | Quick Fix | Rebuild |
|----------|-----------|---------|
| ⏱️ **Thời gian** | Vài giây | ~3000s (50 phút) |
| 💾 **Tài nguyên** | Ít | Nhiều (download ML packages) |
| 🎯 **Mục đích** | Fix permissions nhanh | Build hoàn toàn mới |
| 📁 **Cache location** | `/tmp/.cache` | `/home/appuser/.cache` |
| 🔐 **User** | root (tạm thời) | appuser |
| ✅ **Khuyến nghị** | ⭐ Dùng đầu tiên | Chỉ khi Quick Fix không work |

---

**🎉 Chúc mừng! Service đã sẵn sàng sử dụng!**