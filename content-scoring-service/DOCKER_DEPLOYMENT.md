# Content Scoring Service - Hướng dẫn Docker Deployment

## 📋 Tổng quan

Dự án Content Scoring Service cung cấp một microservice hoàn chỉnh để tính điểm tương đồng nội dung giữa câu hỏi và câu trả lời sử dụng các kỹ thuật NLP tiên tiến. Tài liệu này mô tả chi tiết quá trình triển khai bằng Docker.

## 🏗️ Kiến trúc hệ thống

### Cấu trúc Docker

```
content-scoring-service/
├── Dockerfile                 # Docker image với tracking download
├── docker-compose.yml         # Orchestration cho tất cả services
├── build.sh                   # Script build tự động
├── app/                       # Source code ứng dụng
├── monitoring/                # Config monitoring (Prometheus + Grafana)
├── nginx/                     # Config NGINX reverse proxy
└── logs/                      # Thư mục logs
```

### Services trong Docker Compose

1. **content-scoring-service** (Main service)
   - Port: 5001
   - FastAPI application với NLP processing
   - Health check tự động
   - Multi-stage build để tối ưu kích thước

2. **prometheus** (Monitoring - optional)
   - Port: 9090
   - Thu thập metrics từ main service

3. **grafana** (Visualization - optional)
   - Port: 3100
   - Dashboard hiển thị metrics
   - Credentials: admin/admin123

4. **redis** (Caching - optional)
   - Port: 6379
   - Cache kết quả để tăng performance

5. **nginx** (Reverse Proxy - optional)
   - Port: 80/443
   - Load balancing và SSL termination

## 🚀 Hướng dẫn triển khai

### Yêu cầu hệ thống

- **Docker**: Version 20.10+
- **Docker Compose**: Version 2.0+
- **RAM**: Tối thiểu 4GB (8GB khuyến nghị)
- **Disk**: Tối thiểu 10GB trống
- **OS**: Linux/macOS/Windows với WSL2

### Bước 1: Chuẩn bị môi trường

```bash
# Clone repository (nếu cần)
git clone <repository-url>
cd content-scoring-service

# Kiểm tra Docker
docker --version
docker-compose --version

# Kiểm tra Docker daemon đang chạy
docker info
```

### Bước 2: Build và chạy service cơ bản

```bash
# Cách 1: Sử dụng build script (Khuyến nghị)
chmod +x build.sh
./build.sh

# Cách 2: Sử dụng Docker Compose trực tiếp
docker-compose up -d

# Cách 3: Build manual
docker build -t content-scoring-service .
docker run -p 5001:5001 content-scoring-service
```

### Bước 3: Các options build nâng cao

```bash
# Build với monitoring stack
./build.sh --monitoring

# Build với caching
./build.sh --caching

# Build với reverse proxy
./build.sh --proxy

# Build tất cả services
./build.sh --all

# Clean build (xóa tất cả containers cũ)
./build.sh --clean --no-cache

# Build với specific profiles
./build.sh --monitoring --caching
```

## 📊 Download Tracking

### Tính năng tracking download

Dockerfile được thiết kế với hệ thống tracking download đặc biệt để theo dõi các packages lớn:

#### Packages được tracking:
- **torch**: PyTorch framework (thường >500MB)
- **sentence-transformers**: Models for semantic similarity
- **spacy**: NLP processing library
- **numpy, scipy**: Scientific computing

#### Output tracking mẫu:

```bash
[BUILD] Installing packages with download tracking...
[LARGE DOWNLOAD] Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)
[LARGE DOWNLOAD] File size: 888.0 MB - This may take a while...
[DOWNLOAD PROGRESS] 15% - 133.2 MB / 888.0 MB
[DOWNLOAD PROGRESS] 35% - 310.8 MB / 888.0 MB
[DOWNLOAD PROGRESS] 67% - 594.96 MB / 888.0 MB
[DOWNLOAD PROGRESS] 100% - 888.0 MB / 888.0 MB
[SUCCESS] Successfully installed torch-2.8.0

[MODEL DOWNLOAD] Downloading sentence-transformers/all-MiniLM-L6-v2
[LARGE DOWNLOAD] Downloading model files (156.2 MB)
[MODEL DOWNLOAD] spaCy model downloaded successfully
```

#### Lợi ích của tracking:
- **Visibility**: Biết được tiến độ download của packages lớn
- **Debug**: Dễ dàng phát hiện lỗi download
- **Monitoring**: Theo dõi bandwidth usage
- **User Experience**: Không bị "treo" khi không biết gì đang xảy ra

## 🔧 Cấu hình chi tiết

### Environment Variables

```yaml
# docker-compose.yml
environment:
  - PYTHONPATH=/app
  - LOG_LEVEL=INFO
  - PORT=5001
  - REDIS_URL=redis://redis:6379  # Nếu dùng caching
```

### Health Check Configuration

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:5001/health"]
  interval: 30s      # Kiểm tra mỗi 30s
  timeout: 10s       # Timeout sau 10s
  retries: 3         # Thử lại 3 lần
  start_period: 60s  # Chờ 60s sau khi start
```

### Resource Limits

```yaml
# Thêm vào docker-compose.yml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 2G
    reservations:
      cpus: '0.5'
      memory: 512M
```

## 🏃 Chạy và quản lý services

### Các lệnh cơ bản

```bash
# Khởi động tất cả services
docker-compose up -d

# Khởi động với specific profiles
docker-compose --profile monitoring up -d

# Xem logs
docker-compose logs -f content-scoring-service

# Xem status
docker-compose ps

# Restart service
docker-compose restart content-scoring-service

# Dừng tất cả services
docker-compose down

# Dừng và xóa volumes
docker-compose down --volumes
```

### Scaling services

```bash
# Scale main service
docker-compose up -d --scale content-scoring-service=3

# Load balancing với NGINX
docker-compose --profile proxy up -d --scale content-scoring-service=3
```

## 🧪 Testing và validation

### Kiểm tra service health

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

# Metrics endpoint
curl http://localhost:5001/metrics
```

### Performance testing

```bash
# Install Apache Bench
sudo apt-get install apache2-utils

# Load testing
ab -n 1000 -c 10 -H "Content-Type: application/json" \
   -p test_data.json http://localhost:5001/api/content-scoring
```

### Monitoring checks

```bash
# Prometheus targets
curl http://localhost:9090/api/v1/targets

# Grafana health
curl http://localhost:3100/api/health

# Redis connection
redis-cli -h localhost -p 6379 ping
```

## 📈 Monitoring và logging

### Prometheus metrics

Service cung cấp các metrics sau:

- `content_scoring_requests_total`: Tổng số requests
- `content_scoring_request_duration_seconds`: Latency của requests
- `content_scoring_errors_total`: Số lượng errors theo type

### Grafana dashboards

Dashboard tự động bao gồm:
- Request rate và latency
- Error rate theo time
- Resource usage (CPU, Memory)
- Service health status

### Log management

```bash
# Xem logs realtime
docker-compose logs -f

# Logs của service cụ thể
docker-compose logs -f content-scoring-service

# Logs với timestamp
docker-compose logs -f -t content-scoring-service

# Logs trong Docker container
docker exec -it content-scoring-service tail -f /app/logs/app.log
```

## 🛡️ Security và best practices

### Security features

1. **Non-root user**: Container chạy với user không có quyền root
2. **Multi-stage build**: Chỉ copy cần thiết vào production image
3. **Health checks**: Tự động restart khi service unhealthy
4. **Resource limits**: Giới hạn CPU và memory usage

### Production recommendations

```yaml
# production docker-compose.override.yml
version: '3.8'
services:
  content-scoring-service:
    deploy:
      replicas: 3
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    environment:
      - LOG_LEVEL=WARNING
    networks:
      - internal
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.api.tls=true"
```

### Backup strategies

```bash
# Backup volumes
docker run --rm -v content-scoring-service_grafana-storage:/data \
  -v $(pwd):/backup alpine tar czf /backup/grafana-backup.tar.gz -C /data .

# Backup Redis data
docker exec content-scoring-redis redis-cli BGSAVE
```

## 🚨 Troubleshooting

### Các lỗi thường gặp

#### 1. Service không start được

```bash
# Kiểm tra logs
docker-compose logs content-scoring-service

# Kiểm tra ports đã bị sử dụng
netstat -tulpn | grep :5001

# Restart service
docker-compose restart content-scoring-service
```

#### 2. Download tracking không hoạt động

```bash
# Kiểm tra script permissions
docker exec content-scoring-service ls -la /usr/local/bin/pip-with-tracking

# Rebuild với verbose
docker build --no-cache -t content-scoring-service .
```

#### 3. Memory issues

```bash
# Kiểm tra memory usage
docker stats

# Tăng memory limit
# Sửa docker-compose.yml:
deploy:
  resources:
    limits:
      memory: 4G
```

#### 4. Health check fails

```bash
# Kiểm tra service có running không
curl http://localhost:5001/health

# Kiểm tra logs
docker-compose logs content-scoring-service

# Tăng timeout health check
# Sửa healthcheck trong docker-compose.yml
```

### Debug mode

```bash
# Chạy container trong interactive mode
docker run -it --rm content-scoring-service bash

# Debug với Python
docker exec -it content-scoring-service python -c "
import app.main
print('App imported successfully')
"
```

## 📞 Support và bảo trì

### Log rotation

```bash
# Setup logrotate cho Docker
sudo nano /etc/logrotate.d/docker

# Content:
/var/lib/docker/containers/*/*.log {
  rotate 7
  daily
  compress
  size=1M
  missingok
  delaycompress
  copytruncate
}
```

### Auto-update strategy

```bash
# Script auto-update
#!/bin/bash
cd /path/to/content-scoring-service
git pull
./build.sh --clean
docker-compose up -d
```

### Monitoring alerts

```yaml
# prometheus/alerts.yml
groups:
- name: content-scoring
  rules:
  - alert: ServiceDown
    expr: up{job="content-scoring"} == 0
    for: 1m
    annotations:
      summary: Content Scoring Service is down
  
  - alert: HighErrorRate
    expr: rate(content_scoring_errors_total[5m]) > 0.1
    for: 2m
    annotations:
      summary: High error rate detected
```

---

## 📝 Kết luận

Hệ thống Docker deployment cho Content Scoring Service cung cấp:

✅ **Download tracking** cho packages lớn với progress monitoring  
✅ **One-command build** với script tự động  
✅ **Monitoring stack** hoàn chỉnh với Prometheus + Grafana  
✅ **Scalability** với Docker Compose và load balancing  
✅ **Security** với non-root user và resource limits  
✅ **Production-ready** với health checks và restart policies  

Để bắt đầu sử dụng, chỉ cần chạy:
```bash
chmod +x build.sh
./build.sh --all
```

Service sẽ sẵn sàng tại: http://localhost:5001

**Liên hệ hỗ trợ:** Tạo issue trong repository hoặc liên hệ team development.