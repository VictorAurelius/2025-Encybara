# Content Scoring Service - Docker Setup

## 🚀 Quick Start

### Chạy service cơ bản (Linux/macOS)
```bash
chmod +x build.sh
./build.sh
```

### Chạy service cơ bản (Windows)
```cmd
build.bat
```

### Chạy với tất cả services
```bash
# Linux/macOS
./build.sh --all

# Windows
build.bat --all
```

## 📁 Files được tạo

### 1. [`Dockerfile`](./Dockerfile)
- Multi-stage build để tối ưu kích thước image
- **Download tracking đặc biệt** cho packages lớn (>10MB)
- Tracking progress cho PyTorch, sentence-transformers, spaCy
- Non-root user cho security
- Health check tự động

### 2. [`docker-compose.yml`](./docker-compose.yml)
- Main service: content-scoring-service (port 5001)
- Optional services với profiles:
  - Monitoring: Prometheus (9090) + Grafana (3100)
  - Caching: Redis (6379)
  - Proxy: NGINX (80)

### 3. Build Scripts
- [`build.sh`](./build.sh) - Script cho Linux/macOS
- [`build.bat`](./build.bat) - Script cho Windows
- One-command build với options đầy đủ

### 4. [`DOCKER_DEPLOYMENT.md`](./DOCKER_DEPLOYMENT.md)
- Tài liệu triển khai chi tiết bằng tiếng Việt
- Hướng dẫn troubleshooting
- Best practices cho production

## 🔍 Download Tracking Features

### Packages được tracking:
- **torch**: PyTorch framework (~500-800MB)
- **sentence-transformers**: NLP models (~50-200MB)  
- **spacy**: NLP library + models (~50-100MB)
- **numpy, scipy**: Scientific computing

### Output mẫu:
```bash
[LARGE DOWNLOAD] Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)
[LARGE DOWNLOAD] File size: 888.0 MB - This may take a while...
[DOWNLOAD PROGRESS] 25% - 222.0 MB / 888.0 MB
[DOWNLOAD PROGRESS] 50% - 444.0 MB / 888.0 MB
[DOWNLOAD PROGRESS] 75% - 666.0 MB / 888.0 MB
[DOWNLOAD PROGRESS] 100% - 888.0 MB / 888.0 MB
[SUCCESS] Successfully installed torch-2.8.0
```

## 🎯 Endpoints

Sau khi build thành công:

- **Main Service**: http://localhost:5001
- **API Docs**: http://localhost:5001/docs
- **Health Check**: http://localhost:5001/health
- **Metrics**: http://localhost:5001/metrics

Optional (với --monitoring):
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3100 (admin/admin123)

## 📋 Commands

### Build options
```bash
./build.sh --help                 # Xem tất cả options
./build.sh                        # Build cơ bản
./build.sh --clean --no-cache     # Clean build
./build.sh --monitoring           # Với monitoring
./build.sh --all                  # Tất cả services
```

### Management
```bash
# Xem logs
docker-compose logs -f content-scoring-service

# Xem status
docker-compose ps

# Dừng services  
docker-compose down

# Scale service
docker-compose up -d --scale content-scoring-service=3
```

### Testing
```bash
# Health check
curl http://localhost:5001/health

# API test
curl -X POST http://localhost:5001/api/content-scoring \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is machine learning?",
    "answer": "ML is a subset of AI that uses algorithms to learn from data."
  }'
```

## 🛠️ Troubleshooting

### Service không start
```bash
# Kiểm tra logs
docker-compose logs content-scoring-service

# Restart
docker-compose restart content-scoring-service
```

### Port bị sử dụng
```bash
# Linux/macOS
netstat -tulpn | grep :5001

# Windows
netstat -ano | findstr :5001
```

### Download tracking không hoạt động
```bash
# Rebuild với verbose
docker build --no-cache -t content-scoring-service .
```

## 📞 Support

- Đọc [`DOCKER_DEPLOYMENT.md`](./DOCKER_DEPLOYMENT.md) để biết chi tiết
- Tạo issue trong repository
- Liên hệ development team

---

**Built with ❤️ - Docker deployment với download tracking cho Content Scoring Service**