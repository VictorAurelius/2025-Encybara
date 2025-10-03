# 🚇 Tunnel Setup Guide - Content Scoring Service

## ❌ Tại sao `./build.sh --tunnel` không hoạt động?

### 🔍 Root Cause Analysis:

1. **Docker Compose Profiles Not Supported**
   ```bash
   [WARNING] Profiles not supported in this docker-compose version
   [BUILD] Starting basic service only...
   ```
   - Docker Compose version cũ không support `--profile` flag
   - Ngrok container có `profiles: [tunnel]` nên không được start

2. **Missing Authtoken**
   ```bash
   [WARNING] ⚠️  Lưu ý: Cần cấu hình authtoken trong ngrok/ngrok.yml trước khi sử dụng tunnel!
   ```
   - Ngrok cần valid authtoken để hoạt động
   - File `ngrok/ngrok.yml` có placeholder `your_ngrok_auth_token_here`

3. **Build Script Logic Issue**
   - Script cố gắng start với profiles nhưng fallback về basic service only
   - Không có mechanism để start ngrok container riêng biệt

## ✅ Solution: Manual Tunnel Setup

### 🚀 Quick Fix Workflow:

#### **Step 1: Build Service**
```bash
# Option A: Full build (slow)
./build.sh

# Option B: Quick fix (fast)
./quick-fix.sh
```

#### **Step 2: Start Tunnel**
```bash
# Simple wrapper command
./tunnel.sh start YOUR_NGROK_AUTHTOKEN

# Or detailed command
./start-tunnel.sh YOUR_NGROK_AUTHTOKEN
```

#### **Step 3: Get Public URL**
```bash
# Quick URL
./tunnel.sh url

# Full status
./tunnel.sh status
```

#### **Step 4: Test Tunnel**
```bash
# Test all endpoints
./tunnel.sh test

# Manual test
curl $(./tunnel.sh url)/health
```

## 📋 Complete Example:

```bash
# 1. Build service
ADMIN@VANKIET MINGW64 /f/code/a-hoctap/project-1/2025-Encybara/content-scoring-service
$ ./quick-fix.sh
[SUCCESS] Service is healthy and ready!

# 2. Start tunnel with authtoken
$ ./tunnel.sh start 33RzjpvLWHRqcfUdoHqdM42am8J_413xRQLhDiAY5LZ51TyDB
[SUCCESS] Tunnel setup complete!

# 3. Get public URL
$ ./tunnel.sh url
https://def456.ngrok-free.app

# 4. Test tunnel
$ ./tunnel.sh test
[SUCCESS] ✓ All endpoints working!

# 5. Use in backend-app
$ cd ../backend-app
$ ./build.sh
# Enter URL when prompted: https://def456.ngrok-free.app
```

## 🛠️ Available Tunnel Commands:

### **tunnel.sh - Main Wrapper**
```bash
./tunnel.sh start [AUTHTOKEN]   # Start tunnel
./tunnel.sh stop                # Stop tunnel  
./tunnel.sh status              # Check status + get URL
./tunnel.sh url                 # Get URL only
./tunnel.sh test                # Test all endpoints
```

### **start-tunnel.sh - Detailed Control**
```bash
./start-tunnel.sh AUTHTOKEN     # Start with authtoken
./start-tunnel.sh --stop        # Stop tunnel
./start-tunnel.sh --status      # Check status
./start-tunnel.sh --help        # Show help
```

### **URL Extraction Scripts**
```bash
./get-tunnel-url.sh             # Standard method
./get-ngrok-url-direct.sh       # Direct container method (Windows compatible)
```

### **Testing Scripts**
```bash
./test-ngrok-public.sh          # Comprehensive endpoint testing
./test-ngrok-public.sh URL      # Test specific URL
```

## 🔧 Troubleshooting:

### **Issue: "Ngrok không chạy hoặc không accessible tại http://localhost:4040"**
**Solution:**
```bash
# Method 1: Use direct extraction
./get-ngrok-url-direct.sh

# Method 2: Check container logs
docker logs content-scoring-ngrok

# Method 3: Restart tunnel
./tunnel.sh stop && ./tunnel.sh start YOUR_AUTHTOKEN
```

### **Issue: "Authentication failed: Usage of ngrok requires verified account"**
**Solution:**
```bash
# Get authtoken from: https://dashboard.ngrok.com/get-started/your-authtoken
./tunnel.sh start YOUR_REAL_AUTHTOKEN
```

### **Issue: "Content-scoring-service is not running"**
**Solution:**
```bash
# Start service first
./quick-fix.sh

# Then start tunnel
./tunnel.sh start YOUR_AUTHTOKEN
```

## 📝 Why This Approach Works:

### **✅ Docker Compose Independent**
- Không depend vào profiles support
- Manual container management
- Network connectivity với existing service

### **✅ Windows Git Bash Compatible**
- Proper path conversion cho volume mounts
- Multiple URL extraction methods
- Cross-platform shell scripting

### **✅ Authtoken Management**
- Easy authtoken update
- Config file persistence
- Validation và error handling

## 🎯 Migration Path:

### **From Docker Compose Profiles:**
```bash
# OLD (doesn't work)
./build.sh --tunnel

# NEW (works)
./build.sh && ./tunnel.sh start AUTHTOKEN
```

### **From Manual Ngrok:**
```bash
# OLD (manual)
ngrok http 5001

# NEW (automated)
./tunnel.sh start AUTHTOKEN
```

---

## 🏁 Summary:

**Problem:** `./build.sh --tunnel` fails due to Docker Compose profiles not supported

**Solution:** Use separate tunnel management với `./tunnel.sh` commands

**Result:** Reliable, cross-platform tunnel setup cho content-scoring-service

**Next Steps:** Sử dụng `./tunnel.sh start YOUR_AUTHTOKEN` để start tunnel sau khi build service