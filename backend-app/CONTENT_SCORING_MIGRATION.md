# Content Scoring Service Migration Documentation

## 📋 Tổng quan

Dự án đã được chuyển đổi hoàn toàn từ **PerplexityAI API** sang **content-scoring-service** với mapping đúng format response mới.

## 🔄 Những thay đổi đã thực hiện

### 1. Models đã đổi tên hoàn toàn
- ✅ **PerplexityRequest** → **ScoringRequest**
- ✅ **PerplexityResponse** → **ScoringResponse** (bỏ evaluation, chỉ giữ score + improvements)
- ✅ **PerplexitySuggestionRequest** → **SuggestionRequest**
- ✅ **PerplexitySuggestionResponse** → **SuggestionResponse**
- ✅ **PerplexityException** → **ContentScoringException**

### 2. Response format mới
**Cũ (PerplexityAI):**
```json
{
  "statusCode": 200,
  "message": "Answer evaluated successfully",
  "data": {
    "score": 8.5,
    "evaluation": "Good answer...",
    "improvements": "Try to mention..."
  }
}
```

**Mới (Content-scoring-service):**
```json
{
  "statusCode": 200,
  "message": "Answer evaluated successfully via content-scoring-service",
  "data": {
    "score": 8.5,
    "improvements": "Gợi ý: Regarding machine learning...\n\nĐiểm cần cải thiện:\n• Provide more detailed explanation\n• Include discussion of: artificial intelligence\n\nKhái niệm còn thiếu: artificial intelligence"
  }
}
```

### 3. Score mapping
- **Content-scoring-service trả về**: Thang 100 (0-100)
- **API /evaluate trả về**: Thang 10 (0-10)
- **Conversion**: `scoreIn10 = scoreIn100 / 10.0`

### 4. Advanced_answer mapping
Content-scoring-service trả về:
```json
{
  "success": true,
  "score": 85.5,
  "similarity": 0.855,
  "key_points": [...],
  "advanced_answer": {
    "suggestion": "Regarding machine learning, a comprehensive answer would address...",
    "improvement_points": [
      "Provide more detailed explanation",
      "Include discussion of: artificial intelligence"
    ],
    "missing_concepts": ["artificial intelligence"]
  }
}
```

Được mapping thành `improvements` field:
```
Gợi ý: [suggestion content]

Điểm cần cải thiện:
• [improvement_point 1]
• [improvement_point 2]

Khái niệm còn thiếu: [missing_concepts joined by comma]
```

## 🚀 Cách sử dụng

### 1. Khởi động content-scoring-service
```bash
cd content-scoring-service
./quick-fix.sh       # Linux/macOS
quick-fix.bat        # Windows
```

### 2. Khởi động backend-app
```bash
cd backend-app
./gradlew bootRun
```

### 3. Test API với format mới

#### Test evaluate endpoint
```bash
curl -X POST http://localhost:8080/api/v1/content-scoring/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is machine learning?",
    "userAnswer": "Machine learning is AI that learns from data",
    "prompt": "Basic AI concepts"
  }'
```

**Expected response (NEW FORMAT):**
```json
{
  "statusCode": 200,
  "message": "Answer evaluated successfully via content-scoring-service",
  "data": {
    "score": 8.5,
    "improvements": "Gợi ý: Regarding machine learning, a comprehensive answer would address the following aspects...\n\nĐiểm cần cải thiện:\n• Provide more detailed explanation\n• Include discussion of: artificial intelligence\n• Add specific examples to illustrate your points\n\nKhái niệm còn thiếu: artificial intelligence"
  }
}
```

### 4. Test integration
```bash
cd backend-app
./test-content-scoring-integration.sh    # Linux/macOS
test-content-scoring-integration.bat     # Windows
```

## 📊 Mapping Details

### Score Conversion
| Content-scoring-service | API Response | Note |
|------------------------|--------------|------|
| 0-100 | 0-10 | Division by 10 |
| 85.5 | 8.55 | Precise conversion |
| 100 | 10.0 | Perfect score |

### Response Field Mapping
| Content-scoring-service | API Response | Source |
|------------------------|--------------|--------|
| `score` (0-100) | `score` (0-10) | Direct conversion |
| `advanced_answer.suggestion` | `improvements` (part 1) | Mapped as "Gợi ý: ..." |
| `advanced_answer.improvement_points[]` | `improvements` (part 2) | Mapped as "Điểm cần cải thiện:" |
| `advanced_answer.missing_concepts[]` | `improvements` (part 3) | Mapped as "Khái niệm còn thiếu:" |
| ~~`evaluation`~~ | ❌ **REMOVED** | No longer exists |

## 🎯 Breaking Changes

1. **Response structure**: Removed `evaluation` field completely
2. **Model names**: All "Perplexity" → "Scoring/Suggestion"
3. **Score scale**: 100 → 10 conversion
4. **Improvements format**: Rich text from advanced_answer
5. **Package structure**: `perplexity` → `scoring`

## 🔧 Technical Implementation

### New Package Structure
```
domain/
├── request/scoring/
│   ├── ScoringRequest.java
│   └── SuggestionRequest.java
└── response/scoring/
    ├── ScoringResponse.java
    └── SuggestionResponse.java
```

### Service Logic
```java
// Score conversion
double scoreIn100 = responseBody.get("score");
double scoreIn10 = scoreIn100 / 10.0;

// Improvements mapping
Map<String, Object> advancedAnswer = responseBody.get("advanced_answer");
String improvements = extractImprovements(advancedAnswer);
```

## 🚨 Migration Status

- ❌ **PerplexityAI dependencies** - COMPLETELY REMOVED
- ✅ **Content-scoring-service integration** - FULLY IMPLEMENTED
- ✅ **Advanced_answer mapping** - DONE
- ✅ **Score conversion 100→10** - DONE
- ✅ **Response format updated** - DONE
- ✅ **Test scripts updated** - DONE

---

**Migration Status**: ✅ COMPLETED  
**Response format**: `{score: number, improvements: string}`  
**Score scale**: 0-10 (converted from 0-100)  
**Advanced features**: ✅ Mapped from advanced_answer