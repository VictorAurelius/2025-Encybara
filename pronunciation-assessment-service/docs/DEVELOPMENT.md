# Development Setup Guide

## Prerequisites

### Required Software
- **Docker**: Version 20.0+ with Docker Desktop
- **Git**: For version control
- **cURL**: For testing API endpoints
- **Text Editor/IDE**: VS Code, PyCharm, or similar

### Optional Tools
- **jq**: For JSON formatting in terminal
- **sox**: For audio file generation during testing
- **Python 3.10+**: For local development without Docker

### System Requirements
- **RAM**: Minimum 4GB, recommended 8GB+
- **Storage**: At least 5GB free space for Docker images
- **OS**: Windows 10/11, macOS 10.15+, or Linux

## Quick Start

### 1. Clone and Setup
```bash
git clone <your-repository>
cd pronunciation-assessment-service
```

### 2. Build Docker Image
```bash
# On Unix/Linux/macOS
./scripts/build.sh

# On Windows
bash scripts/build.sh
```

### 3. Run Service
```bash
# On Unix/Linux/macOS
./scripts/run.sh

# On Windows
bash scripts/run.sh
```

### 4. Test Service
```bash
# On Unix/Linux/macOS
./scripts/test.sh

# On Windows
bash scripts/test.sh
```

## Local Development

### Without Docker (Advanced)

1. **Install Conda/Mamba**
   ```bash
   # Install Mambaforge
   wget https://github.com/conda-forge/miniforge/releases/latest/download/Mambaforge-Linux-x86_64.sh
   bash Mambaforge-Linux-x86_64.sh
   ```

2. **Create Environment**
   ```bash
   mamba create -n aligner -c conda-forge montreal-forced-aligner python=3.10
   conda activate aligner
   ```

3. **Install Dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Download MFA Models**
   ```bash
   mfa model download acoustic english_us_arpa
   mfa model download dictionary english_us_arpa
   ```

5. **Run Application**
   ```bash
   python app.py
   ```

### With Docker (Recommended)

Docker provides a consistent environment and handles all dependencies automatically.

## Project Structure

```
pronunciation-assessment-service/
├── app.py                          # Main Flask application
├── gop_scorer.py                   # GOP algorithm implementation
├── utils.py                        # Utility functions
├── requirements.txt                # Python dependencies
├── Dockerfile                      # Docker configuration
├── README.md                       # Project overview
├── scripts/                        # Build and deployment scripts
│   ├── build.sh                    # Docker build script
│   ├── run.sh                      # Container run script
│   └── test.sh                     # Testing script
├── docs/                           # Documentation
│   ├── API.md                      # API documentation
│   ├── DEVELOPMENT.md              # This file
│   └── DEPLOYMENT.md               # Deployment guide
├── tests/                          # Test files (optional)
└── mfa_data/                       # MFA alignment data (auto-created)
```

## Development Workflow

### 1. Code Changes
```bash
# Make your code changes
vim app.py

# Rebuild Docker image
./scripts/build.sh

# Run updated service
./scripts/run.sh

# Test changes
./scripts/test.sh
```

### 2. Testing Changes

**Quick Health Check:**
```bash
curl http://localhost:5000/health
```

**Test with Sample Audio:**
```bash
# Create test audio (requires sox)
sox -n -r 16000 -c 1 test.wav synth 2.0 sine 440

# Test pronunciation assessment
curl -X POST \
  -F "audio=@test.wav" \
  -F "transcript=hello world" \
  http://localhost:5000/api/pronunciation-assessment
```

**Check Logs:**
```bash
docker logs -f pronunciation-assessment-container
```

### 3. Debugging

**Access Container:**
```bash
docker exec -it pronunciation-assessment-container /bin/bash
```

**Check MFA Installation:**
```bash
docker exec -it pronunciation-assessment-container bash -c "source activate aligner && mfa version"
```

**Monitor Memory Usage:**
```bash
curl http://localhost:5000/health | jq '.memory_usage_mb'
```

## Environment Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SECRET_KEY` | Auto-generated | Flask secret key |
| `FLASK_ENV` | `production` | Flask environment |
| `MAX_CONTENT_LENGTH` | `6MB` | Maximum file upload size |
| `MFA_ACOUSTIC_MODEL` | `english_us_arpa` | MFA acoustic model |
| `MFA_DICTIONARY` | `english_us_arpa` | MFA dictionary |

### Docker Environment
```bash
docker run -d \
  --name pronunciation-assessment-container \
  --publish 5000:5000 \
  --memory="3g" \
  --env SECRET_KEY="your-secret-key" \
  --env FLASK_ENV="development" \
  pronunciation-assessment:latest
```

## Code Style and Standards

### Python Code Style
- **Formatter**: Black (line length: 88)
- **Linter**: Flake8 with relaxed rules
- **Type Hints**: Encouraged but not required
- **Docstrings**: Google style

### Example Configuration (.flake8)
```ini
[flake8]
max-line-length = 88
extend-ignore = E203, W503
exclude = .git,__pycache__,docs/source/conf.py,old,build,dist
```

### Pre-commit Hooks (Optional)
```yaml
# .pre-commit-config.yaml
repos:
  - repo: https://github.com/psf/black
    rev: 22.3.0
    hooks:
      - id: black
  - repo: https://github.com/pycqa/flake8
    rev: 4.0.1
    hooks:
      - id: flake8
```

## Performance Optimization

### Memory Management
- Monitor memory usage with `/health` endpoint
- GOP scorer uses sequential processing to limit RAM
- Temporary files are automatically cleaned up
- Garbage collection is forced after assessments

### Processing Speed
- Audio preprocessing is optimized for 16kHz mono
- MFA alignment uses efficient model caching
- Phoneme scoring is vectorized where possible

### Scaling Considerations
- Service is designed for single-request processing
- For high load, deploy multiple containers with load balancer
- Consider Redis for caching frequent assessments
- Implement request queuing for burst traffic

## Testing

### Unit Tests (Future Enhancement)
```bash
# Install test dependencies
pip install pytest pytest-cov

# Run tests
pytest tests/ -v --cov=.

# Generate coverage report
pytest tests/ --cov=. --cov-report=html
```

### Integration Tests
The `test.sh` script provides basic integration testing:

```bash
# Run all tests
./scripts/test.sh comprehensive

# Test specific endpoint
./scripts/test.sh health
./scripts/test.sh assessment

# Manual testing examples
./scripts/test.sh manual
```

### Load Testing (Optional)
```bash
# Install Apache Bench
sudo apt-get install apache2-utils

# Basic load test
ab -n 10 -c 2 http://localhost:5000/health

# Load test with file upload (requires test file)
# Note: ab doesn't handle multipart uploads well
# Consider using curl in a loop or specialized tools
```

## Troubleshooting

### Common Development Issues

**1. Docker Build Fails**
```bash
# Clear Docker cache
docker system prune -a

# Build with no cache
docker build --no-cache -t pronunciation-assessment .

# Check disk space
df -h
```

**2. Container Won't Start**
```bash
# Check container logs
docker logs pronunciation-assessment-container

# Check port conflicts
netstat -tulpn | grep :5000

# Verify image integrity
docker images pronunciation-assessment
```

**3. MFA Alignment Fails**
```bash
# Access container
docker exec -it pronunciation-assessment-container bash

# Test MFA manually
source activate aligner
echo "hello world" > test.txt
sox -n -r 16000 -c 1 test.wav synth 1.0 sine 440
mfa align test.wav test.txt english_us_arpa english_us_arpa output/
```

**4. Memory Issues**
```bash
# Check system memory
free -h

# Monitor container memory
docker stats pronunciation-assessment-container

# Reduce memory limit
docker run --memory="2g" ...
```

**5. Audio Processing Issues**
```bash
# Install audio debugging tools in container
docker exec -it pronunciation-assessment-container bash
apt-get update && apt-get install -y ffmpeg sox

# Test audio file
ffprobe your_audio.wav
sox your_audio.wav -n stat
```

### Debug Mode

Enable debug logging by modifying `app.py`:

```python
# Set debug logging
logging.basicConfig(level=logging.DEBUG)

# Run Flask in debug mode (development only)
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
```

### Performance Profiling

Add profiling to identify bottlenecks:

```python
import cProfile
import pstats

def profile_assessment():
    profiler = cProfile.Profile()
    profiler.enable()
    
    # Your code here
    result = pipeline.assess_pronunciation(audio_path, transcript)
    
    profiler.disable()
    stats = pstats.Stats(profiler)
    stats.sort_stats('cumulative').print_stats(10)
    
    return result
```

## Contributing Guidelines

### Code Submission
1. Fork the repository
2. Create feature branch: `git checkout -b feature/new-feature`
3. Make changes and test thoroughly
4. Update documentation if needed
5. Submit pull request with detailed description

### Testing Requirements
- All new features must include tests
- Maintain code coverage above 80%
- Test with various audio formats and edge cases
- Verify memory usage stays within limits

### Documentation Requirements
- Update API documentation for new endpoints
- Add inline code comments for complex logic
- Update README for new features
- Include usage examples

---

*For deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md). For API details, see [API.md](API.md).*