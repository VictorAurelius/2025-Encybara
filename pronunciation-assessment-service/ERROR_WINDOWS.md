# Windows Error - Quick Fix

## ❌ Lỗi bạn đang gặp

```
ERROR:gop_scorer:MFA alignment error: [WinError 2] The system cannot find the file specified
ERROR:gop_scorer:Forced alignment failed
```

## 🔍 Nguyên nhân

Bạn đang chạy service trực tiếp trên **Windows** mà **không dùng Docker**.

Montreal Forced Aligner (MFA) **KHÔNG hỗ trợ Windows** natively.

## ✅ Giải pháp (3 bước)

### Bước 1: Cài Docker Desktop

1. Download: https://www.docker.com/products/docker-desktop
2. Cài đặt và restart máy
3. Enable WSL2 nếu được hỏi

### Bước 2: Start Service bằng Docker

Mở PowerShell hoặc CMD:

```powershell
# Di chuyển vào thư mục service
cd pronunciation-assessment-service

# Build và start
docker-compose up -d
```

### Bước 3: Kiểm tra

```powershell
# Check health
curl http://localhost:5000/health

# Test service
curl -X POST http://localhost:5000/api/pronunciation-assessment `
  -F "audio=@test.wav" `
  -F "transcript=hello world"
```

## ✅ Kết quả mong đợi

```json
{
  "status": "healthy",
  "memory_usage_mb": 150.5,
  "service": "pronunciation-assessment",
  "version": "1.0.0-optimized"
}
```

## 📝 Tại sao phải dùng Docker?

| Cách chạy | Windows | Linux/Mac | Khó khăn | Khuyến nghị |
|-----------|---------|-----------|----------|-------------|
| **Docker** | ✅ Hoạt động | ✅ Hoạt động | ⭐ Dễ | ✅ **RECOMMENDED** |
| **Native** | ❌ Không hoạt động | ✅ Hoạt động | ⭐⭐⭐⭐⭐ Rất khó | ❌ KHÔNG |
| **WSL2** | ✅ Hoạt động | N/A | ⭐⭐⭐ Khá khó | ⚠️ Alternative |

## 🚫 Đừng thử

- ❌ Cài MFA trực tiếp trên Windows (sẽ thất bại)
- ❌ Chạy `python run.py` trên Windows (sẽ lỗi)
- ❌ Cài qua Conda trên Windows (không ổn định)

## ✅ Chỉ làm

- ✅ Dùng Docker Desktop
- ✅ Chạy `docker-compose up -d`
- ✅ Đọc `WINDOWS_SETUP.md` nếu cần chi tiết

## 🆘 Nếu Docker không chạy

### Lỗi: "Docker is not running"

**Giải pháp:**
1. Mở Docker Desktop
2. Đợi nó khởi động xong (icon whale màu xanh)
3. Thử lại

### Lỗi: "Port 5000 already in use"

**Giải pháp:**

```powershell
# Tìm process đang dùng port 5000
netstat -ano | findstr :5000

# Kill process (thay <PID> bằng số từ lệnh trên)
taskkill /PID <PID> /F

# Hoặc đổi port trong docker-compose.yml:
# ports:
#   - "5001:5000"
```

### Lỗi: Docker Desktop không cài được

**Giải pháp:**
1. Update Windows lên version mới nhất
2. Enable Virtualization trong BIOS
3. Enable WSL2:
   ```powershell
   wsl --install
   ```
4. Restart máy và thử lại

## 📚 Tài liệu chi tiết

- `WINDOWS_SETUP.md` - Hướng dẫn Windows chi tiết
- `TROUBLESHOOTING.md` - Các lỗi khác
- `QUICK_START.md` - Quick start guide
- `README.md` - Documentation đầy đủ

## 💡 Quick Reference

```powershell
# Build and start (lần đầu)
cd pronunciation-assessment-service
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop
docker-compose down

# Restart
docker-compose restart

# Rebuild
docker-compose build --no-cache
docker-compose up -d
```

## ✅ Tóm tắt

1. **Cài Docker Desktop** (nếu chưa có)
2. **Chạy `docker-compose up -d`** (trong thư mục service)
3. **Test: `curl http://localhost:5000/health`**

**Đơn giản vậy thôi!** 🎉

---

**Updated:** 2025-10-06
**Platform:** Windows
**Solution:** Docker Only
