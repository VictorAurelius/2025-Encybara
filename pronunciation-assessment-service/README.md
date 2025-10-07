# Pronunciation Assessment Microservice (WhisperX)

A high-performance RESTful microservice for pronunciation assessment using **WhisperX** and **GOP (Goodness of Pronunciation)** algorithm. This service provides fast phoneme-level pronunciation scoring with comprehensive audio processing capabilities.

## 🚀 Features

- **RESTful API** with comprehensive pronunciation assessment endpoint
- **WhisperX Integration** for ultra-fast forced alignment (2-8s processing time)
- **GOP Algorithm** with confidence metrics for sophisticated pronunciation scoring
- **Docker Containerization** with optimized memory usage and GPU/CPU acceleration
- **Security Features** with file validation, size limits, and input sanitization
- **Multi-format Support** for WAV, MP3, FLAC, and M4A audio files
- **Professional Logging** and error handling
- **Memory Optimization** with automatic cleanup and garbage collection
- **GPU/CPU Acceleration** with automatic device detection
- **Comprehensive Documentation** and testing guides

## 📋 Requirements

- **Docker** 20.0+ with Docker Desktop
- **System Memory** 2GB+ (4GB+ recommended, less than MFA)
- **Storage** 3GB+ for Docker images and WhisperX models
- **Network** Internet access for model downloads
- **GPU** (Optional) CUDA-compatible GPU for acceleration

## ⚡ Quick Start

### 1. Clone and Build
```bash
git clone <repository-url>
cd pronunciation-assessment-service

## ⚠️ IMPORTANT SAFETY NOTICE

**Safe Docker Cleanup:** 
- Script `./build.sh --clean` đã được cập nhật để CHỈ xóa containers và images của project này
- KHÔNG ảnh hưởng đến các Docker projects khác (content-scoring-service, etc.)
- Không chạy `docker system prune` để bảo vệ dữ liệu của các containers khác


# Build Docker image with WhisperX (first build takes ~5-10 minutes)
./build.sh

# Start the service
./scripts/run.sh
```

### 2. Test the Service
```bash
# Run comprehensive WhisperX tests
python3 test_whisperx.py

# Or test manually
curl http://localhost:5000/health
```

### 3. Make Your First Assessment
```bash
# Create a test audio file (requires sox)
sox -n -r 16000 -c 1 test.wav synth 2.0 sine 440

# Get pronunciation assessment
curl -X POST \
  -F "audio=@test.wav" \
  -F "transcript=hello world" \
  http://localhost:5000/api/pronunciation-assessment
```

## 📡 API Endpoints

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/health` | GET | Service health and memory usage |
| `/api/info` | GET | Service capabilities and configuration |
| `/api/pronunciation-assessment` | POST | Main pronunciation assessment |
| `/` | GET | Basic service information |

## 📊 Sample Response

```json
{
  "success": true,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "overall_score": 84.2,
    "fluency_score": 92.0,
    "phoneme_scores": [
      {
        "phoneme": "h",
        "gop_score": 88.9,
        "quality": "excellent",
        "start_time": 0.010,
        "end_time": 0.090
      },
      {
        "phoneme": "eh",
        "gop_score": 82.5,
        "quality": "good",
        "start_time": 0.090,
        "end_time": 0.180
      }
    ],
    "total_phonemes": 12,
    "average_duration": 0.130
  }
}
```

## 🏗️ Architecture Overview

### Tech Stack
- **Backend**: Python 3.10, Flask
- **Audio Processing**: Librosa, SoundFile
- **Alignment**: WhisperX (OpenAI Whisper + Forced Alignment)
- **Containerization**: Docker with Python environment
- **Machine Learning**: PyTorch, Transformers

### Processing Pipeline
1. **Audio Validation** → File format, size, and quality checks
2. **Audio Preprocessing** → Conversion to 16kHz mono WAV
3. **WhisperX Alignment** → Ultra-fast phoneme-level alignment (2-8s)
4. **Feature Extraction** → Spectral features optimized for speed
5. **GOP Scoring** → Phoneme-level pronunciation assessment with confidence
6. **Results Aggregation** → Overall and fluency scores

## 📁 Project Structure

```
pronunciation-assessment-service/
├── 📄 README.md                    # This file
├── 📄 TESTING_GUIDE.md             # Comprehensive testing guide
├── 🐍 app.py                       # Flask API application
├── 🐍 gop_scorer.py                # GOP algorithm implementation
├── 🐍 utils.py                     # Utility functions
├── 📄 requirements.txt             # Python dependencies
├── 🐳 Dockerfile                   # Docker configuration
├── 📁 scripts/                     # Build and deployment scripts
│   ├── 🔨 build.sh                 # Docker build script
│   ├── 🚀 run.sh                   # Container run script
│   └── 🧪 test.sh                  # Testing script
├── 📁 docs/                        # Documentation
│   ├── 📖 API.md                   # API documentation
│   ├── 🛠️ DEVELOPMENT.md           # Development setup
│   └── 🌐 DEPLOYMENT.md            # Deployment guide
├── 📁 tests/                       # Test files
│   └── 🐍 sample_test.py           # Python test suite
└── 📁 mfa_data/                    # MFA models (auto-created)
```

## 🔧 Configuration

### Environment Variables
```bash
SECRET_KEY="your-secret-key"
FLASK_ENV="production"
MAX_CONTENT_LENGTH="6291456"  # 6MB
WHISPERX_DEVICE="auto"  # cpu, cuda, or auto
WHISPERX_COMPUTE_TYPE="float16"  # float16 for speed, float32 for accuracy
```

### Docker Resource Limits
- **Memory**: 4GB limit, 1GB reserved (more efficient than MFA)
- **CPU**: 0.5-2 cores (auto-scaling)
- **Storage**: 3GB for WhisperX models and cache
- **GPU**: Optional CUDA support for acceleration

## 🛡️ Security Features

- **File Size Validation**: 6MB maximum
- **MIME Type Checking**: Audio files only
- **Input Sanitization**: Transcript cleaning
- **Resource Limits**: Memory and CPU constraints
- **Error Handling**: Secure error messages

## 📈 Performance Characteristics

| Metric | WhisperX Value | Previous MFA Value |
|--------|---------------|--------------------|
| Memory Usage | ≤ 4GB | ≤ 3GB |
| Processing Time | **2-8s** per assessment | 15-60s per assessment |
| Startup Time | **60s** | 180s |
| Supported File Size | Up to 6MB | Up to 6MB |
| Audio Duration | Up to 5 minutes recommended | Up to 5 minutes |
| Concurrent Requests | 2-4 (improved efficiency) | 1-3 (memory limited) |
| GPU Acceleration | ✅ Supported | ❌ Not supported |

## 🧪 Testing

### Quick Tests
```bash
# Test all WhisperX functionality
python3 test_whisperx.py

# Test service components
./scripts/test.sh

# Docker compose test
docker-compose up -d && python3 test_whisperx.py
```

### Manual Testing Examples
```bash
# Health check
curl http://localhost:5000/health | jq

# Service information
curl http://localhost:5000/api/info | jq

# Assessment with your audio file
curl -X POST \
  -F "audio=@your_audio.wav" \
  -F "transcript=your expected text" \
  http://localhost:5000/api/pronunciation-assessment | jq
```

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [📖 API Documentation](docs/API.md) | Complete API reference with examples |
| [🛠️ Development Guide](docs/DEVELOPMENT.md) | Setup, debugging, and contribution guide |
| [🌐 Deployment Guide](docs/DEPLOYMENT.md) | Production deployment options |
| [🧪 Testing Guide](TESTING_GUIDE.md) | Comprehensive testing procedures |

## 🐛 Troubleshooting

### Common Issues

**Service Won't Start**
```bash
# Check Docker status
docker ps -a | grep pronunciation

# View startup logs
docker logs pronunciation-assessment-container

# Check port conflicts
netstat -tulpn | grep :5000
```

**Memory Issues**
```bash
# Check current usage
curl http://localhost:5000/health | jq '.memory_usage_mb'

# Restart to clear memory
docker restart pronunciation-assessment-container
```

**Assessment Failures**
```bash
# Verify audio format
file your_audio.wav
ffprobe your_audio.wav

# Test with simple audio
sox -n -r 16000 -c 1 simple.wav synth 2.0 sine 440
```

### Getting Help
1. Check the [Testing Guide](TESTING_GUIDE.md) for diagnostic procedures
2. Review service logs: `docker logs pronunciation-assessment-container`
3. Test with provided sample files in `tests/` directory
4. Verify system requirements and Docker installation

## 🚀 Deployment Options

### Local Development
```bash
./build.sh && ./scripts/run.sh
```

### Docker Compose
```yaml
version: '3.8'
services:
  pronunciation-assessment:
    build: .
    ports: ["5000:5000"]
    memory: 3g
    restart: unless-stopped
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pronunciation-assessment
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: app
        image: pronunciation-assessment:latest
        resources:
          limits: {memory: "3Gi", cpu: "2"}
```

### Cloud Platforms
- **AWS ECS/Fargate**: See [deployment guide](docs/DEPLOYMENT.md#aws-ecs-deployment)
- **Google Cloud Run**: `gcloud run deploy --memory 3Gi`
- **Azure Container Instances**: `az container create --memory 3`

## 🔄 Updates and Maintenance

### Regular Maintenance
```bash
# Update service
git pull origin main
./build.sh
./scripts/run.sh

# Clean up Docker resources (WhisperX specific)
docker system prune -f
docker volume prune -f

# Monitor logs
docker logs -f pronunciation-assessment-service-whisperx
```

### Performance Monitoring
```bash
# Memory usage (should show improved efficiency)
curl http://localhost:5000/health | jq '.memory_usage_mb'

# Container stats for WhisperX
docker stats pronunciation-assessment-service-whisperx

# Response times (should be 2-8s with WhisperX)
time curl -X POST -F "audio=@test.wav" -F "transcript=test" http://localhost:5000/api/pronunciation-assessment
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Make changes and test thoroughly
4. Update documentation as needed
5. Submit a pull request with detailed description

### Development Setup
```bash
# Clone and setup
git clone <repository-url>
cd pronunciation-assessment-service

# Build WhisperX development environment
./build.sh

# Run comprehensive tests
python3 test_whisperx.py
./scripts/test.sh
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **WhisperX** team for the fast alignment toolkit
- **OpenAI** for the Whisper model foundation
- **PyTorch** community for ML framework
- **Flask** framework for the web API foundation
- **Librosa** team for audio processing capabilities

---

## 📞 Support

For support and questions:
- 📚 Check the comprehensive documentation in `docs/`
- 🧪 Run the test suite with `./scripts/test.sh`
- 📝 Review logs with `docker logs pronunciation-assessment-container`
- 🔍 Search existing issues or create a new one

**Ready to assess pronunciation with WhisperX? Start with `./build.sh && ./scripts/run.sh` and begin testing!** 🎤✨

## 🆕 WhisperX Migration Benefits

### ⚡ Performance Improvements
- **10x Faster Processing**: 30s → 2-8s per assessment
- **3x Faster Startup**: 180s → 60s container startup
- **Better Memory Efficiency**: Optimized resource usage
- **GPU Acceleration**: Native CUDA support

### 🛠️ Technical Advantages
- **Simpler Setup**: No complex conda environment
- **Python Native**: Better API integration
- **Modern ML Stack**: PyTorch + Transformers
- **Active Development**: Regular updates and improvements

### 📊 Migration Summary
| Aspect | MFA (Previous) | WhisperX (Current) |
|--------|-----------------|-------------------|
| Processing Time | 30 seconds | 2-8 seconds |
| Setup Complexity | High | Medium |
| Dependencies | Conda + MFA | pip + PyTorch |
| GPU Support | Limited | Native |
| Memory Usage | Higher | Optimized |
| API Integration | Complex | Simple |

## 🚀 Simple Build Option

Cho những trường hợp network không ổn định hoặc Docker build thường xuyên bị timeout:

### Dockerfile.simple
- Không cài đặt audio processing dependencies phức tạp
- Download packages nhỏ hơn và nhanh hơn
- Thích hợp cho development/testing với text-based endpoints
- Không hỗ trợ pronunciation assessment với audio files

### Sử dụng Simple Build

```bash
# Build với simple Dockerfile
./build.sh --simple

# Kết hợp với các options khác
./build.sh --simple --monitoring
./build.sh --simple --clean --no-cache
```

### Khi nào sử dụng Simple Build

✅ **Sử dụng khi:**
- Network không ổn định, thường xuyên timeout
- Chỉ cần test basic endpoints
- Development với text-based features
- Container size nhỏ gọn

❌ **Không sử dụng khi:**
- Cần xử lý audio files
- Production environment
- Cần full pronunciation assessment features


## 🔧 Troubleshooting Docker Build Issues

### 1. Conda Package Resolution Errors

**Lỗi:** `pympi-ling does not exist` hoặc `soundfile does not exist`

**Giải pháp:**
```bash
# Package names đã được sửa trong Dockerfile:
# - soundfile -> pysoundfile (có sẵn trong conda-forge)
# - pympi-ling -> install qua pip thay vì conda

# Build với retry logic:
./build.sh --clean --no-cache
```

### 2. Docker Build Timeout

**Lỗi:** Build bị timeout sau 51 giây

**Giải pháp:**
```bash
# Build script đã có extended timeout (30 phút):
export DOCKER_BUILDKIT=1
export BUILDKIT_PROGRESS=plain
./build.sh --all

# Hoặc manual build với timeout:
timeout 1800 docker build --no-cache -t pronunciation-assessment-service .
```

### 3. Network Connection Issues

**Lỗi:** Connection timeout khi download packages

**Giải pháp:**
```bash
# Sử dụng simple Dockerfile (không có audio processing):
./build.sh --simple

# Hoặc retry build:
for i in {1..3}; do
    ./build.sh --clean && break
    echo "Retry $i/3..."
    sleep 30
done
```

### 4. Memory Issues

**Lỗi:** Out of memory during build

**Giải pháp:**
```bash
# Tăng Docker memory limits trong Docker Desktop
# Hoặc build với resource constraints:
docker build --memory=4g --memory-swap=8g -t pronunciation-assessment-service .
```

### 5. Container Startup Issues

**Lỗi:** Container exits immediately

**Giải pháp:**
```bash
# Kiểm tra logs:
docker logs pronunciation-assessment-service

# Debug container:
docker run -it --rm pronunciation-assessment-service:latest /bin/bash

# Health check extended timeout (3 phút):
# start_period: 180s trong docker-compose.yml
```
