# API DOCUMENT

**Format:** Excel-friendly table format (v2)
**Last Updated:** 2025-10-29

---

## Summary Table

| No. | Sheet name              | API                                   | Status | Remarks |
|-----|-------------------------|---------------------------------------|--------|---------|
| 1   | AssessPronunciation     | POST /api/v1/pronunciation/assess     | Done   |         |

---

## API Detail: AssessPronunciation

### API Overview

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
| 1   | file   | MultipartFile   | ◯         | Audio file to assess pronunciation (WAV, MP3, etc.) |
| 2   | text   | String          | ◯         | Reference text/transcript for pronunciation assessment |

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

| No  | Key        | Data Type | Mandatory | Note                                           |
|-----|------------|-----------|-----------|------------------------------------------------|
| 1   | statusCode | Integer   | ◯         | HTTP status code (200 for success)             |
| 2   | error      | String    | ◯         | Error message (null on success)                |
| 3   | message    | String    | ◯         | Success/error message                          |
| 4   | data       | Object    | ◯         | Assessment result from pronunciation-assessment-service |

---

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
      }
    ],
    "feedback": "Good pronunciation overall. Pay attention to 'how' and 'today'."
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "statusCode": 400,
  "error": "Missing 'audio' file in request",
  "message": "Audio file không được để trống",
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
| 7   | 503        | Service Unavailable - Cannot connect to pronunciation service (timeout)     |
| 8   | 500        | Internal Server Error - Unexpected server error                             |

---

## Additional Notes

### Validation Rules:
1. **file** - Cannot be null or empty, must be valid audio file
2. **text** - Cannot be null, empty, or whitespace only (validated with trim())

### Error Messages:
- Missing file: "Audio file không được để trống"
- Missing text: "Text transcript là bắt buộc để đánh giá phát âm"
- Service error: Custom message from PronunciationAssessmentException
- Unexpected error: "Lỗi không mong muốn khi đánh giá phát âm: [error details]"

### Authentication:
- Endpoint requires valid JWT token in Authorization header
- Path /api/v1/pronunciation/** is NOT in security whitelist
- Token obtained from /api/v1/auth/login endpoint

### Dependencies:
- **External Service:** pronunciation-assessment-service
- **Service Configuration:** pronunciation-assessment.service.url property
- **Timeout Settings:** Configurable connect and read timeouts (default 30s)
