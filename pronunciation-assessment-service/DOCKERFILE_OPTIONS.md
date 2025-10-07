# Dockerfile Options for WhisperX Service

## 🚀 Quick Start (Recommended)

```bash
# Windows/Linux - Simple build (CPU-only, no CUDA)
bash build-windows.sh

# Or using main build script
bash build.sh
```

## 📋 Available Dockerfiles

### 1. `Dockerfile.cpu-only` ⭐ **RECOMMENDED**
- **Purpose**: CPU-only, fastest build, no CUDA downloads
- **Build Time**: 3-5 minutes
- **Size**: Smallest (~1GB)
- **Downloads**: ~200MB (no CUDA packages)
- **Use Case**: Production, development, any machine

```bash
# Used by default in build.sh
bash build.sh
```

### 2. `Dockerfile.optimized`
- **Purpose**: Optimized build with controlled dependencies
- **Build Time**: 5-8 minutes  
- **Size**: Medium
- **Use Case**: When you need specific package versions

```bash
bash build.sh --optimized
```

### 3. `Dockerfile.gpu`
- **Purpose**: GPU acceleration with CUDA support
- **Build Time**: 10-15 minutes
- **Size**: Large (~3GB+)
- **Downloads**: 2GB+ CUDA packages
- **Use Case**: GPU-enabled machines only

```bash
bash build.sh --gpu
```

### 4. `Dockerfile` (Original)
- **Purpose**: Multi-stage build with runtime model download
- **Build Time**: 5-10 minutes
- **Size**: Medium
- **Use Case**: Balanced approach

```bash
# Manual build
docker build -t pronunciation-assessment-service .
```

### 5. `Dockerfile.simple`
- **Purpose**: Minimal dependencies for unreliable networks
- **Build Time**: 2-3 minutes
- **Size**: Smallest
- **Use Case**: Development, testing, poor network

```bash
bash build.sh --simple
```

## 📊 Comparison

| Dockerfile | Build Time | Size | CUDA Downloads | GPU Support | Recommended For |
|------------|------------|------|----------------|-------------|-----------------|
| `cpu-only` ⭐ | 3-5 min | Small | None | No | **Most users** |
| `optimized` | 5-8 min | Medium | None | No | Version control |
| `gpu` | 10-15 min | Large | 2GB+ | Yes | GPU machines |
| Original | 5-10 min | Medium | Minimal | No | Balanced |
| `simple` | 2-3 min | Smallest | None | No | Development |

## 🔧 Build Commands Summary

```bash
# Default (CPU-only, recommended)
bash build.sh

# Fast minimal build
bash build.sh --simple

# Optimized build
bash build.sh --optimized  

# GPU support (large download)
bash build.sh --gpu

# With monitoring
bash build.sh --monitoring

# Clean build
bash build.sh --clean --no-cache

# Windows simple build
bash build-windows.sh
```

## 🚨 Common Issues & Solutions

### Issue: CUDA Downloads (888MB torch, 706MB cudnn)
**Solution**: Use `Dockerfile.cpu-only` (default in build.sh)

### Issue: Build timeout
**Solution**: 
```bash
# Increase timeout or use simple build
bash build.sh --simple
```

### Issue: Windows line ending errors
**Solution**:
```bash
# Use Windows-specific script
bash build-windows.sh
```

### Issue: Network timeouts
**Solution**:
```bash
# Use simple Dockerfile
bash build.sh --simple
```

## 🎯 Recommendations by Use Case

### Production Deployment
```bash
bash build.sh  # Uses cpu-only by default
```

### Development/Testing  
```bash
bash build.sh --simple
```

### GPU-enabled Server
```bash
bash build.sh --gpu
```

### Poor Network Connection
```bash
bash build-windows.sh  # Or build.sh --simple
```

### CI/CD Pipeline
```bash
bash build.sh --clean --no-cache
```

## 🔍 Verification

After build, verify the service:

```bash
# Check health
curl http://localhost:5000/health

# Run tests
python3 test_whisperx.py

# Check logs
docker logs pronunciation-assessment-service-whisperx
```

## 📈 Expected Performance

- **Processing Time**: 2-8 seconds per assessment
- **Memory Usage**: 1-4GB
- **Startup Time**: 60-180 seconds (model download on first use)
- **Concurrent Users**: 2-4 requests

---

**Recommended**: Use `bash build.sh` for most cases - it automatically selects the CPU-only Dockerfile which avoids large CUDA downloads and builds fastest.