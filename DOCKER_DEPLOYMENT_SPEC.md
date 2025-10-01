# Docker Deployment Specification

## Tổng quan

Repository này chứa 2 services chính với Docker deployment hoàn chỉnh:

1. **content-scoring-service** - AI content scoring với sentence-transformers
2. **pronunciation-assessment-service** - Pronunciation assessment với audio processing

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network: 172.20.0.0/16           │
├─────────────────────────────────────────────────────────────┤
│  content-scoring-service (Port 8080)                       │
│  ├── FastAPI backend                                       │
│  ├── Sentence-transformers models                          │
│  ├── Redis cache (optional)                                │
│  └── Monitoring stack (optional)                           │
├─────────────────────────────────────────────────────────────┤
│  pronunciation-assessment-service (Port 5000)              │
│  ├── Flask backend                                         │
│  ├── Audio processing dependencies                         │
│  ├── Ngrok tunnel support                                  │
│  └── Monitoring stack (optional)                           │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Content Scoring Service

```bash
cd content-scoring-service
./build.sh                    # Basic build
./build.sh --all              # Build với tất cả services
./build.sh --monitoring       # Build với Prometheus + Grafana
```

### Pronunciation Assessment Service

```bash
cd pronunciation-assessment-service
./build.sh                    # Basic build
./build.sh --simple           # Simple build cho network không ổn định
./build.sh --tunnel           # Build với Ngrok tunnel
./build.sh --all              # Build tất cả services
```

## 📦 Build Features

### 1. Download Tracking cho Large Packages

Cả 2 services đều implement download tracking cho packages > 10MB:

```bash
[LARGE DOWNLOAD] Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)
[DOWNLOAD PROGRESS] 200 MB / 888.0 MB (22.5%) - torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl
[DOWNLOAD PROGRESS] 400 MB / 888.0 MB (45.0%) - torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl
```

### 2. One-Command Build Scripts

Mỗi service có `build.sh` script hỗ trợ:
- ✅ **Clean build** (`--clean`)
- ✅ **No cache build** (`--no-cache`)
- ✅ **Monitoring stack** (`--monitoring`)
- ✅ **Caching layer** (`--cache`)
- ✅ **Reverse proxy** (`--proxy`)
- ✅ **All services** (`--all`)

### 3. Network Isolation

```yaml
# content-scoring-service: 172.20.0.0/16
# pronunciation-assessment-service: 172.21.0.0/16
```

Tách biệt network để tránh port conflicts khi chạy cả 2 services.

## 🔧 Service-Specific Features

### Content Scoring Service

**Ports:**
- 8080: Main service
- 6379: Redis cache
- 9090: Prometheus
- 3000: Grafana
- 8081: Nginx proxy

**Key Features:**
- FastAPI với auto-generated OpenAPI docs
- Sentence-transformers model caching
- Multi-stage Docker build để optimize size
- Health checks cho tất cả containers

### Pronunciation Assessment Service

**Ports:**
- 5000: Main service
- 6380: Redis cache
- 9091: Prometheus
- 3101: Grafana
- 81: Nginx proxy
- 4041: Ngrok tunnel

**Key Features:**
- Flask backend với audio processing
- Ngrok tunnel support cho EC2-to-local connectivity
- Simple Dockerfile option cho network không ổn định
- Cross-platform local development scripts

## 📋 Service Comparison

| Feature | Content Scoring | Pronunciation Assessment |
|---------|----------------|-------------------------|
| **Framework** | FastAPI | Flask |
| **Main Port** | 8080 | 5000 |
| **Docker Network** | 172.20.0.0/16 | 172.21.0.0/16 |
| **AI Models** | sentence-transformers | Audio processing models |
| **Special Features** | Auto OpenAPI docs | Ngrok tunnel, Simple build |
| **Use Case** | Text content scoring | Audio pronunciation assessment |

## 🛠️ Build Options Matrix

| Option | Content Scoring | Pronunciation Assessment | Description |
|--------|----------------|-------------------------|-------------|
| `--clean` | ✅ | ✅ | Xóa containers và images cũ |
| `--no-cache` | ✅ | ✅ | Build không sử dụng Docker cache |
| `--monitoring` | ✅ | ✅ | Prometheus + Grafana monitoring |
| `--cache` | ✅ | ✅ | Redis caching layer |
| `--proxy` | ✅ | ✅ | Nginx reverse proxy |
| `--tunnel` | ❌ | ✅ | Ngrok tunnel support |
| `--simple` | ❌ | ✅ | Simple Dockerfile cho network kém |
| `--all` | ✅ | ✅ | Build tất cả services |

## 🚨 Network Issues Solutions

### 1. Docker Build Timeout

```bash
# Option 1: Use simple build (pronunciation-assessment only)
./build.sh --simple

# Option 2: Use clean build
./build.sh --clean --no-cache

# Option 3: Manual retry
docker build --no-cache -t service-name .
```

### 2. Package Download Issues

```bash
# Dockerfile implement retry logic:
RUN for i in 1 2 3; do \
    pip install --no-cache-dir -r requirements.txt && break || \
    (echo "Attempt $i failed, retrying..." && sleep 10); \
    done
```

### 3. Interactive Prompts

```bash
# Both Dockerfiles set:
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Ho_Chi_Minh
```

## 📊 Monitoring Stack

Cả 2 services đều hỗ trợ monitoring với:

### Prometheus Metrics
- Container resource usage
- Application metrics
- Custom business metrics

### Grafana Dashboards
- Service health overview
- Performance metrics
- Error rate tracking

### Access URLs
- **Content Scoring**: Grafana http://localhost:3000, Prometheus http://localhost:9090
- **Pronunciation Assessment**: Grafana http://localhost:3101, Prometheus http://localhost:9091

## 🔐 Security Considerations

### Network Isolation
- Services chạy trên separate Docker networks
- Internal communication qua Docker DNS
- External access chỉ qua exposed ports

### Container Security
- Non-root user execution
- Minimal base images
- No unnecessary packages

## 📝 Usage Examples

### Development Workflow

```bash
# 1. Start content scoring service
cd content-scoring-service
./build.sh --monitoring

# 2. Start pronunciation assessment service (separate terminal)
cd pronunciation-assessment-service
./build.sh --tunnel

# 3. Test integration
curl http://localhost:8080/health
curl http://localhost:5000/health
```

### Production Deployment

```bash
# Content scoring với full stack
cd content-scoring-service
./build.sh --all

# Pronunciation assessment với proxy
cd pronunciation-assessment-service
./build.sh --proxy --monitoring
```

## 🎯 Conclusion

Deployment specification này cung cấp:

✅ **Robust Docker builds** với retry logic và download tracking  
✅ **Network isolation** để tránh conflicts  
✅ **Comprehensive monitoring** cho production readiness  
✅ **Flexible build options** cho different environments  
✅ **Cross-platform compatibility** Windows/Linux/macOS  
✅ **One-command deployment** cho ease of use  

Cả 2 services đều production-ready với full monitoring, caching, và proxy support.