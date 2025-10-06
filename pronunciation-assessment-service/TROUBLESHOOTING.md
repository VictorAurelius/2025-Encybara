# Troubleshooting Guide - Pronunciation Assessment Service

## Common Errors and Solutions

### 1. "The system cannot find the file specified" (Windows)

**Full Error:**
```
ERROR:gop_scorer:MFA alignment error: [WinError 2] The system cannot find the file specified
ERROR:gop_scorer:Forced alignment failed
```

**Cause:**
- Montreal Forced Aligner (MFA) is not installed or not in PATH
- Running on Windows without Docker

**Solution:**

#### ✅ Option 1: Use Docker (RECOMMENDED)

```powershell
cd pronunciation-assessment-service
docker-compose up -d
```

**Why this works:**
- Docker image includes MFA pre-installed
- All dependencies are bundled
- Works consistently across all platforms

#### ✅ Option 2: Use WSL2 (Windows Subsystem for Linux)

See `WINDOWS_SETUP.md` for detailed instructions.

#### ❌ Option 3: Native Windows Installation (NOT RECOMMENDED)

MFA is not officially supported on Windows. Use Docker instead.

---

### 2. "MFA command not found" (Linux/Mac)

**Error:**
```
FileNotFoundError: [Errno 2] No such file or directory: 'mfa'
```

**Cause:**
- MFA is not installed
- MFA is not in PATH
- Conda environment not activated

**Solution:**

#### Install MFA via Conda:

```bash
# Install Miniconda (if not already installed)
wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh
bash Miniconda3-latest-Linux-x86_64.sh

# Create environment
conda create -n aligner -c conda-forge montreal-forced-aligner python=3.10
conda activate aligner

# Download models
mfa model download acoustic english_us_arpa
mfa model download dictionary english_us_arpa

# Verify installation
mfa version

# Install Python dependencies
pip install -r requirements.txt

# Run service
python run.py
```

---

### 3. Service Won't Start

**Error:**
```
CRITICAL ERROR: Failed to initialize Pronunciation Assessment Pipeline
```

**Cause:**
- MFA is not properly installed
- Models not downloaded
- Environment issues

**Solution:**

#### Check MFA Installation:

```bash
# Test MFA
mfa version

# Should output something like:
# Montreal Forced Aligner 2.x.x
```

#### Check Models:

```bash
# List installed models
mfa model list

# Download if missing
mfa model download acoustic english_us_arpa
mfa model download dictionary english_us_arpa
```

#### Use Docker:

```bash
# Start with Docker (recommended)
docker-compose up -d

# Check logs
docker-compose logs -f
```

---

### 4. Timeout Error

**Error:**
```
subprocess.TimeoutExpired: MFA alignment timed out after 60 seconds
```

**Cause:**
- Audio file too long
- MFA processing too slow
- Insufficient resources

**Solution:**

#### Increase Timeout:

Edit `app/services/mfa_aligner.py`:

```python
# Change timeout from 60 to higher value
timeout=120  # 2 minutes
```

#### Optimize Audio:

- Use shorter audio clips (<30 seconds)
- Convert to 16kHz mono WAV
- Reduce background noise

#### Use Better Hardware:

- More CPU cores
- More RAM (>4GB)
- SSD storage

---

### 5. Port Already in Use

**Error:**
```
OSError: [Errno 48] Address already in use
```

**Cause:**
- Port 5000 is already in use by another service

**Solution:**

#### Find and Kill Process:

**Linux/Mac:**
```bash
# Find process
lsof -i :5000

# Kill process
kill -9 <PID>
```

**Windows:**
```powershell
# Find process
netstat -ano | findstr :5000

# Kill process
taskkill /PID <PID> /F
```

#### Change Port:

Edit `docker-compose.yml`:

```yaml
ports:
  - "5001:5000"  # Use 5001 instead
```

Or set environment variable:

```bash
export PORT=5001
python run.py
```

---

### 6. Memory Issues

**Error:**
```
MemoryError: Unable to allocate memory
```

**Cause:**
- MFA models require significant memory
- System running out of RAM

**Solution:**

#### Increase Docker Memory:

Docker Desktop → Settings → Resources → Memory → 4GB+

#### Close Other Applications:

Free up RAM by closing unnecessary programs

#### Use Smaller Batch Size:

Process audio files one at a time

---

### 7. Audio Quality Issues

**Error:**
```
ERROR:__main__:Assessment pipeline failed
```

**Cause:**
- Poor audio quality
- Transcript doesn't match audio
- Unsupported audio format

**Solution:**

#### Check Audio Format:

- Use WAV, MP3, or FLAC
- Max file size: 6MB
- Recommended: 16kHz mono WAV

#### Verify Transcript:

- Transcript must match spoken words
- Remove special characters
- Use lowercase letters

#### Improve Audio Quality:

- Reduce background noise
- Use clear recording
- Avoid music/overlapping speech

---

### 8. Docker Build Issues

**Error:**
```
ERROR: failed to solve: process "/bin/sh -c conda create..." did not complete
```

**Cause:**
- Network issues during build
- Docker resources too low
- Corrupted Docker cache

**Solution:**

#### Clean Rebuild:

```bash
# Remove old images
docker-compose down --rmi all

# Rebuild from scratch
docker-compose build --no-cache

# Start
docker-compose up -d
```

#### Increase Docker Resources:

Docker Desktop → Settings → Resources:
- Memory: 4GB+
- CPUs: 2+
- Disk: 20GB+

#### Check Network:

- Ensure stable internet connection
- Try again if download fails

---

### 9. Model Download Issues

**Error:**
```
ERROR: Could not download model
```

**Cause:**
- Network connectivity issues
- MFA server down
- Incorrect model name

**Solution:**

#### Manual Download:

```bash
# Activate environment
conda activate aligner

# Download models manually
mfa model download acoustic english_us_arpa
mfa model download dictionary english_us_arpa

# Verify
mfa model list
```

#### Use Docker:

Models are pre-downloaded in Docker image

---

### 10. Permission Issues (Linux)

**Error:**
```
PermissionError: [Errno 13] Permission denied
```

**Cause:**
- Insufficient permissions for temp directory
- Docker volume permissions

**Solution:**

#### Fix Permissions:

```bash
# Create temp directory with proper permissions
sudo mkdir -p /app/temp
sudo chmod 777 /app/temp

# Or run with sudo (not recommended)
sudo python run.py
```

#### Use Docker:

Docker handles permissions automatically

---

## Platform-Specific Solutions

### Windows

✅ **ALWAYS use Docker**
- Native Windows installation NOT supported
- WSL2 is an alternative
- See `WINDOWS_SETUP.md`

### Linux

✅ **Docker (recommended)** or **Conda**
- Docker: `docker-compose up -d`
- Conda: See installation guide above

### macOS

✅ **Docker (recommended)** or **Conda**
- M1/M2: Docker works well
- Intel: Both Docker and Conda work

---

## Quick Diagnostics

### Check Service Health

```bash
# 1. Test health endpoint
curl http://localhost:5000/health

# Expected response:
# {"status":"healthy","memory_usage_mb":150.5,"service":"pronunciation-assessment"}
```

### Check MFA Installation

```bash
# 2. Verify MFA
mfa version

# Expected output:
# Montreal Forced Aligner 2.x.x
```

### Check Logs

```bash
# 3. Docker logs
docker-compose logs -f

# 4. Look for errors
docker-compose logs | grep ERROR
```

### Test Simple Request

```bash
# 5. Test with curl
curl -X POST http://localhost:5000/api/pronunciation-assessment \
  -F "audio=@test.wav" \
  -F "transcript=hello world"
```

---

## Getting Help

### Check Documentation

1. `README.md` - General documentation
2. `WINDOWS_SETUP.md` - Windows-specific guide
3. `OPTIMIZATIONS.md` - Performance details
4. `QUICK_START.md` - Quick start guide

### Debug Steps

1. Check service is running: `curl http://localhost:5000/health`
2. Check Docker logs: `docker-compose logs -f`
3. Verify MFA: `mfa version` (if not using Docker)
4. Test with simple audio file
5. Check audio format and quality

### Common Solutions Summary

| Problem | Solution |
|---------|----------|
| Windows errors | Use Docker |
| MFA not found | Install via Conda or use Docker |
| Timeout | Increase timeout or use shorter audio |
| Port in use | Change port or kill process |
| Memory issues | Increase Docker memory |
| Audio issues | Check format and quality |

---

**Updated:** 2025-10-06
**For more help:** See README.md and WINDOWS_SETUP.md
