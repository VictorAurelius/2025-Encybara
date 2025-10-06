# Pronunciation Assessment Service - Optimizations

## Phiên bản: 1.0.0-optimized

### 🚀 Cải tiến hiệu năng

#### 1. Tối ưu hóa MFA (Montreal Forced Aligner)

**Vấn đề cũ:** MFA mất hơn 30 giây để xử lý file audio 10 giây

**Giải pháp:**

- ✅ **Single Speaker Mode** (`--single_speaker`): Bật chế độ 1 người nói duy nhất, giảm độ phức tạp xử lý
- ✅ **Beam Width Reduction** (`--beam 10`): Giảm beam width từ 13 xuống 10 để tìm kiếm nhanh hơn
- ✅ **Retry Beam Reduction** (`--retry_beam 40`): Giảm retry beam từ 100 xuống 40
- ✅ **No Debug Mode** (`--no_debug`): Tắt debug output để tăng tốc
- ✅ **Timeout Reduction**: Giảm timeout từ 300 giây xuống 60 giây
- ✅ **Multiprocessing** (`--use_mp`): Sử dụng multiprocessing khi có thể
- ✅ **Single Job** (`--num_jobs 1`): Sử dụng 1 job cho file nhỏ để tránh overhead

**Kết quả dự kiến:** Giảm thời gian xử lý từ >30s xuống còn ~8-12 giây cho file audio 10 giây

#### 2. Cấu trúc code mới

**Cấu trúc cũ:**
```
pronunciation-assessment-service/
├── app.py
├── gop_scorer.py
└── utils.py
```

**Cấu trúc mới (được tổ chức tốt hơn):**
```
pronunciation-assessment-service/
├── run.py                          # Entry point
├── app/
│   ├── __init__.py
│   ├── main.py                     # Flask application
│   ├── core/                       # Core utilities
│   │   ├── __init__.py
│   │   ├── validators.py           # Audio & text validation
│   │   ├── file_manager.py         # File management
│   │   ├── memory_manager.py       # Memory optimization
│   │   └── response_formatter.py   # API response formatting
│   └── services/                   # Business logic
│       ├── __init__.py
│       ├── mfa_aligner.py          # MFA integration (optimized)
│       ├── gop_scorer.py           # GOP algorithm
│       └── assessment_pipeline.py  # Complete pipeline
```

**Lợi ích:**
- ✅ Dễ bảo trì và mở rộng
- ✅ Tách biệt concerns rõ ràng
- ✅ Dễ test từng module
- ✅ Code cleaner và professional hơn

### 📊 So sánh hiệu năng

| Metric | Trước | Sau | Cải thiện |
|--------|-------|-----|-----------|
| Thời gian xử lý (10s audio) | >30s | ~8-12s | ~60-70% |
| MFA timeout | 300s | 60s | -80% |
| Beam width | 13 | 10 | -23% |
| Retry beam | 100 | 40 | -60% |
| Code organization | 3 files | 9 files (modular) | Better maintainability |

### 🔧 Cách sử dụng

#### Development (local)
```bash
# Chạy service với code mới
python run.py
```

#### Production (Docker)
```bash
# Build image mới
docker build -t pronunciation-assessment-service:optimized .

# Chạy container
docker run -p 5000:5000 pronunciation-assessment-service:optimized
```

### 📝 API Endpoints

Không có thay đổi về API endpoints, vẫn tương thích 100% với version cũ:

- `GET /health` - Health check
- `GET /api/info` - Service information
- `POST /api/pronunciation-assessment` - Pronunciation assessment

### ⚡ Tham số tối ưu hóa MFA

Có thể điều chỉnh thêm trong file `app/services/mfa_aligner.py`:

```python
mfa_command = [
    'mfa', 'align',
    corpus_dir,
    self.dictionary,
    self.acoustic_model,
    output_dir,
    '--clean',
    '--single_speaker',    # Single speaker mode
    '--no_debug',          # No debug output
    '--beam', '10',        # Beam width (default: 13)
    '--retry_beam', '40',  # Retry beam (default: 100)
    '--num_jobs', '1',     # Number of jobs
    '--use_mp',            # Use multiprocessing
]
```

**Lưu ý:**
- Giảm `--beam` quá thấp (<8) có thể ảnh hưởng đến độ chính xác
- Tăng `--num_jobs` chỉ có ích khi xử lý nhiều file cùng lúc
- `--single_speaker` chỉ phù hợp khi chắc chắn chỉ có 1 người nói

### 🧪 Testing

```bash
# Test với file audio mẫu
curl -X POST http://localhost:5000/api/pronunciation-assessment \
  -F "audio=@test.wav" \
  -F "transcript=hello world"
```

### 📚 Dependencies

Không thay đổi dependencies, vẫn sử dụng:
- Montreal Forced Aligner (MFA)
- librosa
- praatio
- Flask
- numpy

### 🎯 Roadmap tiếp theo

- [ ] Caching kết quả alignment cho cùng transcript
- [ ] Batch processing cho nhiều files
- [ ] GPU acceleration cho MFA
- [ ] Pre-load models vào memory khi khởi động
- [ ] WebSocket support cho real-time assessment

### 📞 Support

Nếu có vấn đề về hiệu năng, kiểm tra:
1. Memory usage: `GET /health`
2. MFA logs trong container
3. Audio file quality (16kHz mono WAV recommended)
4. Transcript accuracy

---

**Version:** 1.0.0-optimized
**Updated:** 2025-10-06
**Author:** Encybara Team
