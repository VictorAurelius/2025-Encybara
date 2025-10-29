# API DOCUMENT

**Format:** Consolidated Excel-friendly format
**Last Updated:** 2025-10-29
**Project:** Encybara - English Learning Platform

---

## Summary Table

| No. | Sheet name              | API                                   | Status | Remarks |
|-----|-------------------------|---------------------------------------|--------|---------|
| 1   | AssessPronunciation     | POST /api/v1/pronunciation/assess     | Done   |         |

---

## API Detail: AssessPronunciation

| API name        | AssessPronunciation                    |
|-----------------|----------------------------------------|
| Endpoint        | /api/v1/pronunciation/assess           |

---

### Request

| Method          | POST                                   |
|-----------------|----------------------------------------|
| Format data     | multipart/form-data                    |

---

### Header

| No  | Header        | Data Type | Mandatory | Note                                    |
|-----|---------------|-----------|-----------|-----------------------------------------|
| 1   | Authorization | String    | ◯         | Bearer token (JWT) - Required for authenticated users |
| 2   | Content-Type  | String    | ◯         | multipart/form-data                     |

---

### Params

| No  | Params | Data Type       | Mandatory | Note                                                |
|-----|--------|-----------------|-----------|-----------------------------------------------------|
| 1   | file   | MultipartFile   | ◯         | Audio file (WAV, MP3, FLAC, M4A), max 6MB          |
| 2   | text   | String          | ◯         | Reference text for pronunciation assessment         |

---

### Body

| No  | Body Data | Data Type | Mandatory | Note                                    |
|-----|-----------|-----------|-----------|------------------------------------------|
|     |           |           |           | N/A (uses multipart/form-data params)    |

---

### Request Sample

```
file: audio_sample.wav (binary audio file)
text: "Hello, how are you today?"
```

**cURL Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/pronunciation/assess" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/audio.wav" \
  -F "text=Hello, how are you today?"
```

---

### Response

| No  | Key                  | Data Type       | Mandatory | Note                                           |
|-----|----------------------|-----------------|-----------|------------------------------------------------|
| 1   | statusCode           | Integer         | ◯         | HTTP status code (200 for success)             |
| 2   | error                | String          | ◯         | Error message (null on success)                |
| 3   | message              | String          | ◯         | Success/error message                          |
| 4   | data                 | Object          | ◯         | Assessment result object                       |
| 4.1 | data.overall_score   | Double          | ◯         | Overall pronunciation score (0-100)            |
| 4.2 | data.fluency_score   | Double          | ◯         | Fluency score (0-100)                          |
| 4.3 | data.phoneme_scores  | Array[Object]   | ◯         | Array of phoneme-level assessment scores       |
| 4.4 | data.total_phonemes  | Integer         | ◯         | Total number of phonemes analyzed              |
| 4.5 | data.average_duration| Double          | ◯         | Average duration per phoneme (seconds)         |

---

### Phoneme Score Object Structure

| No  | Key            | Data Type | Mandatory | Note                                           |
|-----|----------------|-----------|-----------|------------------------------------------------|
| 1   | phoneme        | String    | ◯         | IPA phoneme symbol (e.g., "h", "ɛ", "l")      |
| 2   | gop_score      | Double    | ◯         | GOP (Goodness of Pronunciation) score (0-100)  |
| 3   | quality        | String    | ◯         | Quality level: excellent/good/fair/poor        |
| 4   | start_time     | Double    | ◯         | Start time of phoneme in seconds               |
| 5   | end_time       | Double    | ◯         | End time of phoneme in seconds                 |
| 6   | character      | String    | ◯         | Corresponding character in transcript text     |
| 7   | word_index     | Integer   | ◯         | Index of word in sentence (0-based)            |
| 8   | phoneme_index  | Integer   | ◯         | Index of phoneme within word (0-based)         |

---

### Response Example

**Success Response (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
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
        "end_time": 0.090,
        "character": "h",
        "word_index": 0,
        "phoneme_index": 0
      },
      {
        "phoneme": "ɛ",
        "gop_score": 82.5,
        "quality": "good",
        "start_time": 0.090,
        "end_time": 0.180,
        "character": "e",
        "word_index": 0,
        "phoneme_index": 1
      },
      {
        "phoneme": "l",
        "gop_score": 79.3,
        "quality": "good",
        "start_time": 0.180,
        "end_time": 0.250,
        "character": "l",
        "word_index": 0,
        "phoneme_index": 2
      }
    ],
    "total_phonemes": 12,
    "average_duration": 0.130
  }
}
```

**Error Response (400 Bad Request - Missing audio file):**
```json
{
  "statusCode": 400,
  "error": "Missing 'audio' file in request",
  "message": "Audio file không được để trống",
  "data": null
}
```

**Error Response (400 Bad Request - Missing text):**
```json
{
  "statusCode": 400,
  "error": "Missing 'text' field in request",
  "message": "Text transcript là bắt buộc để đánh giá phát âm",
  "data": null
}
```

**Error Response (503 Service Unavailable - Service not configured):**
```json
{
  "statusCode": 503,
  "error": "Pronunciation assessment failed",
  "message": "Pronunciation assessment service chưa được cấu hình. Vui lòng kiểm tra biến môi trường PRONUNCIATION_SERVICE_URL.",
  "data": null
}
```

---

### Error code

| No  | Error code | Note                                                                        |
|-----|------------|-----------------------------------------------------------------------------|
| 1   | 200        | Success                                                                     |
| 2   | 400        | Bad Request - Missing or empty audio file                                   |
| 3   | 400        | Bad Request - Missing or empty text transcript                              |
| 4   | 401        | Unauthorized - Invalid or missing JWT token                                 |
| 5   | 404        | Not Found - Pronunciation assessment service endpoint not found             |
| 6   | 503        | Service Unavailable - Pronunciation service not configured                  |
| 7   | 503        | Service Unavailable - Cannot connect to pronunciation service (timeout 30s) |
| 8   | 500        | Internal Server Error - Unexpected server error                             |

---

## Additional Information

### Validation Rules

**Audio File (file parameter):**
- Cannot be null or empty
- Must be valid audio file format
- Supported formats: WAV, MP3, FLAC, M4A
- Maximum size: 6MB (enforced by external service)
- Recommended duration: ≤ 5 minutes (optimal ≤ 2 minutes)

**Text Transcript (text parameter):**
- Cannot be null, empty, or whitespace only
- Validated with trim() method
- Maximum length: 1000 characters (external service limit)
- Should match audio content for accurate assessment

### Error Messages

| Scenario | Error Message |
|----------|---------------|
| Missing file | "Audio file không được để trống" |
| Missing text | "Text transcript là bắt buộc để đánh giá phát âm" |
| Service not configured | "Pronunciation assessment service chưa được cấu hình. Vui lòng kiểm tra biến môi trường PRONUNCIATION_SERVICE_URL." |
| Service timeout | "Không thể kết nối tới pronunciation-assessment-service. Vui lòng kiểm tra service có đang chạy không (timeout 30s)." |
| Service 404 | "Pronunciation-assessment-service endpoint không tìm thấy. Vui lòng kiểm tra cấu hình service." |
| Invalid data | "Dữ liệu gửi tới pronunciation-assessment-service không hợp lệ. Vui lòng kiểm tra lại file audio." |
| Invalid response | "Pronunciation-assessment-service không phản hồi đúng cách. Vui lòng thử lại sau." |
| Unexpected error | "Lỗi không mong muốn khi đánh giá phát âm: [error details]" |

### Authentication

**Required:** Yes
- Endpoint path `/api/v1/pronunciation/**` is NOT in security whitelist
- Valid JWT token must be provided in Authorization header
- Token format: `Bearer <token>`
- Token obtained from: `/api/v1/auth/login` endpoint

### External Service Dependencies

**pronunciation-assessment-service:**
- **Type:** Python Flask microservice
- **External Endpoint:** `POST /api/pronunciation-assessment`
- **Service URL:** Configured via `pronunciation-assessment.service.url` property
- **Parameters:**
  - `audio`: MultipartFile (forwarded from request)
  - `transcript`: String (mapped from `text` parameter)
- **Timeout Settings:**
  - Connect timeout: 30s (configurable via `pronunciation-assessment.service.timeout.connect`)
  - Read timeout: 30s (configurable via `pronunciation-assessment.service.timeout.read`)
- **Response Format:** JSON with fields: overall_score, fluency_score, phoneme_scores, total_phonemes, average_duration

**Internal Services:**
- `PronunciationAssessmentService` - Service layer handling external API calls
- `RestTemplate` - HTTP client with configurable timeouts
- `SecurityConfiguration` - JWT authentication enforcement

### Configuration Properties

```properties
# Required configuration in application.properties
pronunciation-assessment.service.url=http://pronunciation-service:5000
pronunciation-assessment.service.timeout.connect=30
pronunciation-assessment.service.timeout.read=30
```

### Business Logic

1. **Request Validation:**
   - Validate audio file is not null or empty
   - Validate text transcript is provided and not empty (after trim)

2. **External Service Call:**
   - Map request parameters: `text` → `transcript` (external service field name)
   - Forward audio file as `audio` multipart parameter
   - Call external service: `POST {serviceUrl}/api/pronunciation-assessment`

3. **Response Handling:**
   - Wrap external service response in RestResponse wrapper
   - Return entire external service data object as-is (no transformation)
   - Fields: statusCode, error, message, data

4. **Error Handling:**
   - Service not configured (empty URL) → 503
   - Connection timeout (ResourceAccessException) → 503
   - HTTP 404 from service → 404
   - HTTP 4xx from service → 400
   - HTTP 5xx or unexpected errors → 500

### Quality Scores Interpretation

**Overall Score (0-100):**
- 90-100: Excellent pronunciation
- 80-89: Good pronunciation
- 70-79: Fair pronunciation
- 60-69: Needs improvement
- 0-59: Poor pronunciation

**Fluency Score (0-100):**
- Measures speaking flow and rhythm
- Higher score indicates more natural speaking pace

**GOP Score per Phoneme (0-100):**
- Goodness of Pronunciation score
- Higher score indicates better pronunciation of specific phoneme

**Quality Levels:**
- **excellent**: GOP score 85-100
- **good**: GOP score 70-84
- **fair**: GOP score 55-69
- **poor**: GOP score 0-54

### Performance Metrics

| Metric | Value | Note |
|--------|-------|------|
| Processing Time | 1-3 seconds | For 30s audio with SimpleAligner |
| Memory Usage | ≤ 4GB | External service memory limit |
| Max File Size | 6MB | Enforced by external service |
| Max Audio Duration | 5 minutes | Recommended ≤ 2 minutes for optimal performance |
| Concurrent Requests | 2-4 | Depends on external service resources |
| Service Startup Time | ~15 seconds | External service warm-up time |

### Notes

1. **Parameter Mapping:** Backend `text` parameter is mapped to external service `transcript` field
2. **Response Pass-through:** Backend returns external service response data as-is without transformation
3. **File Format Support:** Actual format validation performed by external service
4. **Phoneme Array:** Length varies based on transcript text (typically 3-5 phonemes per word)
5. **Time Alignment:** start_time and end_time provide temporal alignment of phonemes in audio
6. **IPA Symbols:** phoneme field uses International Phonetic Alphabet notation
7. **Character Mapping:** character field maps phoneme back to original transcript text
8. **Indexing:** Both word_index and phoneme_index are zero-based
