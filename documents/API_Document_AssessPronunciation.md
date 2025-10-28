# API Document - Assess Pronunciation

## API Overview

| Field | Value |
|-------|-------|
| **API name** | Assess Pronunciation |
| **Endpoint** | `/api/v1/pronunciation/assess` |
| **Method** | POST |
| **Format data** | multipart/form-data |

---

## Request

### Headers

| No | Header | Data Type | Mandatory | Note |
|----|--------|-----------|-----------|------|
| 1 | Authorization | String | Yes | Bearer token (JWT) - Required for authenticated users |
| 2 | Content-Type | String | Yes | multipart/form-data |

### Params

| No | Params | Data Type | Mandatory | Note |
|----|--------|-----------|-----------|------|
| 1 | file | File (MultipartFile) | Yes | Audio file to assess pronunciation (WAV, MP3, etc.) |
| 2 | text | String | Yes | Reference text/transcript for pronunciation assessment |

### Body

This endpoint uses `multipart/form-data`, not JSON body. All parameters are sent as form data:

| No | Field | Data Type | Mandatory | Note |
|----|-------|-----------|-----------|------|
| 1 | file | File | Yes | Audio file containing the user's pronunciation |
| 2 | text | String | Yes | The text that was supposed to be pronounced |

### Request Sample

**cURL Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/pronunciation/assess" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/audio.wav" \
  -F "text=Hello, how are you today?"
```

**Form Data:**
- `file`: (binary audio file)
- `text`: "Hello, how are you today?"

---

## Response

### Success Response

| No | Key | Data Type | Mandatory | Note |
|----|-----|-----------|-----------|------|
| 1 | statusCode | Integer | Yes | HTTP status code (200 for success) |
| 2 | error | String/null | Yes | Error message (null on success) |
| 3 | message | String | Yes | Success message |
| 4 | data | Object | Yes | Assessment result from pronunciation-assessment-service |

### Response Example

**Success Response (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "accuracyScore": 85.5,
    "pronunciationScore": 82.3,
    "fluencyScore": 88.7,
    "completenessScore": 90.0,
    "prosodyScore": 84.2,
    "words": [
      {
        "word": "Hello",
        "accuracyScore": 95.0,
        "errorType": "None"
      },
      {
        "word": "how",
        "accuracyScore": 78.5,
        "errorType": "Mispronunciation"
      },
      {
        "word": "are",
        "accuracyScore": 92.0,
        "errorType": "None"
      },
      {
        "word": "you",
        "accuracyScore": 88.0,
        "errorType": "None"
      },
      {
        "word": "today",
        "accuracyScore": 72.0,
        "errorType": "Mispronunciation"
      }
    ],
    "feedback": "Good pronunciation overall. Pay attention to 'how' and 'today'."
  }
}
```

---

## Error Codes

| No | Error Code | Note |
|----|------------|------|
| 1 | 400 | Bad Request - Missing or empty audio file |
| 2 | 400 | Bad Request - Missing or empty text transcript |
| 3 | 401 | Unauthorized - Invalid or missing JWT token |
| 4 | 404 | Not Found - Pronunciation assessment service endpoint not found |
| 5 | 503 | Service Unavailable - Pronunciation service not configured |
| 6 | 503 | Service Unavailable - Cannot connect to pronunciation service |
| 7 | 500 | Internal Server Error - Unexpected server error |

### Error Response Examples

**Missing audio file (400):**
```json
{
  "statusCode": 400,
  "error": "Missing 'audio' file in request",
  "message": "Audio file không được để trống",
  "data": null
}
```

**Missing text field (400):**
```json
{
  "statusCode": 400,
  "error": "Missing 'text' field in request",
  "message": "Text transcript là bắt buộc để đánh giá phát âm",
  "data": null
}
```

**Service not configured (503):**
```json
{
  "statusCode": 503,
  "error": "Pronunciation assessment failed",
  "message": "Pronunciation assessment service chưa được cấu hình. Vui lòng kiểm tra biến môi trường PRONUNCIATION_SERVICE_URL.",
  "data": null
}
```

**Service timeout (503):**
```json
{
  "statusCode": 503,
  "error": "Pronunciation assessment failed",
  "message": "Không thể kết nối tới pronunciation-assessment-service. Vui lòng kiểm tra service có đang chạy không (timeout 30s).",
  "data": null
}
```

**Internal server error (500):**
```json
{
  "statusCode": 500,
  "error": "Internal server error",
  "message": "Lỗi không mong muốn khi đánh giá phát âm: [error details]",
  "data": null
}
```

---

## Business Logic

1. **File Validation**: The endpoint first validates that the audio file is not null or empty
2. **Text Validation**: The endpoint validates that the text transcript is provided and not empty
3. **External Service Call**: The endpoint forwards the request to the pronunciation-assessment-service
   - Service URL is configured via `pronunciation-assessment.service.url` property
   - Timeout is configurable (default 30s for both connect and read)
4. **Response Mapping**: The service returns pronunciation assessment results including scores and word-level feedback
5. **Error Handling**: Multiple layers of error handling:
   - Service not configured
   - Connection timeout
   - HTTP client errors (4xx)
   - Server errors (5xx)

---

## Configuration

Required environment variables/application properties:

```properties
pronunciation-assessment.service.url=http://pronunciation-service:8000
pronunciation-assessment.service.timeout.connect=30
pronunciation-assessment.service.timeout.read=30
```

---

## Authentication

This endpoint **requires authentication**. The `/api/v1/pronunciation/**` path is NOT in the security whitelist, so users must provide a valid JWT token in the Authorization header.

**Security Configuration:**
- Method: Bearer Token (JWT)
- Header: `Authorization: Bearer <token>`
- Token obtained from: `/api/v1/auth/login` endpoint

---

## Dependencies

**External Services:**
- `pronunciation-assessment-service` - External microservice for pronunciation analysis
  - Endpoint: `POST /api/pronunciation-assessment`
  - Fields: `audio` (multipart file), `transcript` (string)

**Internal Services:**
- `PronunciationAssessmentService` - Service layer handling external API calls
- `RestTemplate` - HTTP client with configurable timeouts

---

## Notes

1. The audio file format should be compatible with the pronunciation-assessment-service (typically WAV, MP3)
2. The text field must match what the user is supposed to pronounce
3. The external service must be running and accessible
4. Response data structure depends on the pronunciation-assessment-service implementation
5. Large audio files may take longer to process; adjust timeout accordingly
6. File size limits may apply based on Spring Boot configuration
