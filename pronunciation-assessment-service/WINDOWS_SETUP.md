# Pronunciation Assessment Service - Windows Setup

## ⚠️ Important Notice

**Montreal Forced Aligner (MFA) is NOT officially supported on Windows natively.**

The pronunciation assessment service requires MFA, which has limited Windows support.

## Recommended Solutions for Windows

### ✅ Option 1: Docker (RECOMMENDED)

This is the **easiest and most reliable** way to run the service on Windows.

#### Prerequisites
- Docker Desktop for Windows
- WSL2 enabled

#### Steps

1. **Install Docker Desktop:**
   - Download from: https://www.docker.com/products/docker-desktop
   - Install and enable WSL2 integration
   - Restart computer if needed

2. **Navigate to service directory:**
   ```powershell
   cd pronunciation-assessment-service
   ```

3. **Build the Docker image:**
   ```powershell
   docker-compose build
   ```

4. **Start the service:**
   ```powershell
   docker-compose up
   ```

5. **Test the service:**
   ```powershell
   curl http://localhost:5000/health
   ```

**Advantages:**
- ✅ No complex setup
- ✅ Consistent with production
- ✅ All dependencies included
- ✅ Works out of the box

### ✅ Option 2: WSL2 (Windows Subsystem for Linux)

Run the service inside WSL2 with native Linux environment.

#### Prerequisites
- Windows 10 version 2004+ or Windows 11
- WSL2 enabled

#### Steps

1. **Install WSL2:**
   ```powershell
   # In PowerShell (Admin)
   wsl --install
   wsl --set-default-version 2
   ```

2. **Install Ubuntu from Microsoft Store**

3. **Open Ubuntu terminal and install dependencies:**
   ```bash
   # Update system
   sudo apt update && sudo apt upgrade -y

   # Install Miniconda
   wget https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh
   bash Miniconda3-latest-Linux-x86_64.sh

   # Restart terminal and create environment
   conda create -n aligner -c conda-forge montreal-forced-aligner python=3.10
   conda activate aligner

   # Download MFA models
   mfa model download acoustic english_us_arpa
   mfa model download dictionary english_us_arpa

   # Install Python dependencies
   cd /mnt/c/path/to/pronunciation-assessment-service
   pip install -r requirements.txt
   ```

4. **Run the service:**
   ```bash
   conda activate aligner
   python run.py
   ```

**Advantages:**
- ✅ Native Linux environment
- ✅ Better performance than VM
- ✅ Direct file access to Windows

### ❌ Option 3: Native Windows (NOT RECOMMENDED)

MFA has very limited Windows support and may not work correctly.

#### Why NOT recommended:
- ❌ MFA is not officially supported on Windows
- ❌ Complex conda environment setup
- ❌ Many compatibility issues
- ❌ Poor performance
- ❌ Hard to debug

#### If you still want to try:

1. **Install Miniconda for Windows:**
   - Download from: https://docs.conda.io/en/latest/miniconda.html

2. **Create environment (may fail):**
   ```powershell
   conda create -n aligner -c conda-forge montreal-forced-aligner python=3.10
   conda activate aligner
   ```

3. **This will likely fail with various errors on Windows**

## Error Messages Explained

### Error: "The system cannot find the file specified"

**Cause:** MFA executable not found in PATH

**Solution:** Use Docker or WSL2 instead

### Error: "MFA alignment error: [WinError 2]"

**Cause:** MFA is not installed or not accessible

**Solution:**
1. Use Docker (recommended)
2. Use WSL2
3. Check MFA installation: `mfa version`

## Comparison Table

| Method | Difficulty | Performance | Reliability | Recommended |
|--------|-----------|-------------|-------------|-------------|
| **Docker** | ⭐ Easy | ⭐⭐⭐ Good | ⭐⭐⭐⭐⭐ Excellent | ✅ YES |
| **WSL2** | ⭐⭐ Moderate | ⭐⭐⭐⭐ Very Good | ⭐⭐⭐⭐ Good | ✅ YES |
| **Native Windows** | ⭐⭐⭐⭐⭐ Very Hard | ⭐ Poor | ⭐ Poor | ❌ NO |

## Quick Start (Docker - Recommended)

```powershell
# 1. Clone repository
cd pronunciation-assessment-service

# 2. Build and start
docker-compose up -d

# 3. Check health
curl http://localhost:5000/health

# 4. Test
curl -X POST http://localhost:5000/api/pronunciation-assessment `
  -F "audio=@test.wav" `
  -F "transcript=hello world"
```

## Troubleshooting

### Docker Desktop not starting

**Solution:**
1. Enable WSL2 in Windows Features
2. Update Windows to latest version
3. Restart computer
4. Start Docker Desktop

### Port 5000 already in use

**Solution:**
```powershell
# Check what's using port 5000
netstat -ano | findstr :5000

# Kill the process or change port in docker-compose.yml
# Change: "5001:5000" instead of "5000:5000"
```

### WSL2 not available

**Solution:**
1. Update Windows 10 to version 2004 or higher
2. Enable Virtual Machine Platform:
   ```powershell
   dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
   ```
3. Install WSL2 kernel update
4. Restart computer

## Development on Windows

For development, we recommend:

1. **Use Docker for backend services** (pronunciation, content-scoring)
2. **Use Windows for Java backend** (Spring Boot works well on Windows)
3. **Use Git Bash or WSL2 terminal** for scripts

## Production Deployment

For production on Windows Server:

1. **Use Docker containers** (recommended)
2. **Use Windows containers** (if must use Windows)
3. **Consider Linux VPS** for better compatibility

## Support

If you encounter issues:

1. **Check Docker logs:**
   ```powershell
   docker-compose logs -f
   ```

2. **Check service health:**
   ```powershell
   curl http://localhost:5000/health
   ```

3. **Verify Docker is running:**
   ```powershell
   docker ps
   ```

4. **Read main documentation:**
   - `README.md` - General documentation
   - `OPTIMIZATIONS.md` - Performance improvements
   - `QUICK_START.md` - Quick start guide

## Summary

**For Windows users:**
- ✅ **Use Docker** - Easiest and most reliable
- ✅ **Use WSL2** - If you want native Linux experience
- ❌ **Avoid native Windows** - Not supported by MFA

**Quick command (Docker):**
```powershell
cd pronunciation-assessment-service
docker-compose up -d
curl http://localhost:5000/health
```

---

**Updated:** 2025-10-06
**For more info:** See README.md and QUICK_START.md
