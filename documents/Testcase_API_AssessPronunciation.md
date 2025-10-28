# Test Cases - Assess Pronunciation API

## Project Information

| Field | Value |
|-------|-------|
| **Project Name** | Encybara - English Learning Platform |
| **API Name** | POST /api/v1/pronunciation/assess |
| **Owner** | Development Team |
| **Number of Testcases** | 20 |

## Test Summary

| Status | Count |
|--------|-------|
| PASSED | 0 |
| FAILED | 0 |
| PENDING | 20 |
| NOT RUN | 0 |

| Execution | Count |
|-----------|-------|
| AUTOMATION | 15 |
| MANUAL | 5 |

---

## Section 1: Validate (Input Validation Tests)

### TC001 - Valid pronunciation assessment with all required fields

| Field | Value |
|-------|-------|
| **ID** | TC001 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation with valid audio file and text |
| **Precondition** | - User is authenticated with valid JWT token<br>- Pronunciation assessment service is running and accessible<br>- Valid audio file is available |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token in Authorization header<br>3. Include valid audio file (WAV format)<br>4. Include reference text matching audio content |

**Test Data - Request:**

**Params:**
```
file: audio_sample_hello.wav (binary file, ~100KB)
text: "Hello, how are you today?"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 200

**Body:**
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
    "words": [...]
  }
}
```

**Validate:**
- Response status code is 200
- Response contains statusCode, error, message, and data fields
- data contains pronunciation scores
- error field is null
- message indicates success

**Expected Database Result:** N/A (no database changes)

**Execution:** AUTOMATION

**Note:** This is the happy path test case

**Status:** PENDING

---

### TC002 - Missing audio file

| Field | Value |
|-------|-------|
| **ID** | TC002 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation without audio file |
| **Precondition** | - User is authenticated with valid JWT token |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token<br>3. Do NOT include audio file<br>4. Include text parameter |

**Test Data - Request:**

**Params:**
```
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 400

**Body:**
```json
{
  "statusCode": 400,
  "error": "Missing 'audio' file in request",
  "message": "Audio file không được để trống",
  "data": null
}
```

**Validate:**
- Response status code is 400
- error field contains appropriate error message
- data field is null

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Validates required file parameter

**Status:** PENDING

---

### TC003 - Empty audio file

| Field | Value |
|-------|-------|
| **ID** | TC003 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation with empty audio file |
| **Precondition** | - User is authenticated with valid JWT token |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token<br>3. Include empty audio file (0 bytes)<br>4. Include text parameter |

**Test Data - Request:**

**Params:**
```
file: empty.wav (0 bytes)
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 400

**Body:**
```json
{
  "statusCode": 400,
  "error": "Missing 'audio' file in request",
  "message": "Audio file không được để trống",
  "data": null
}
```

**Validate:**
- Response status code is 400
- Appropriate error message returned

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Validates file not empty

**Status:** PENDING

---

### TC004 - Missing text parameter

| Field | Value |
|-------|-------|
| **ID** | TC004 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation without text transcript |
| **Precondition** | - User is authenticated with valid JWT token |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token<br>3. Include valid audio file<br>4. Do NOT include text parameter |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav (binary file)
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 400

**Body:**
```json
{
  "statusCode": 400,
  "error": "Missing 'text' field in request",
  "message": "Text transcript là bắt buộc để đánh giá phát âm",
  "data": null
}
```

**Validate:**
- Response status code is 400
- error message indicates missing text field
- data is null

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Text transcript is mandatory

**Status:** PENDING

---

### TC005 - Empty text parameter

| Field | Value |
|-------|-------|
| **ID** | TC005 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation with empty text |
| **Precondition** | - User is authenticated with valid JWT token |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token<br>3. Include valid audio file<br>4. Include empty text parameter (empty string or whitespace) |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav (binary file)
text: "   " (whitespace only)
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 400

**Body:**
```json
{
  "statusCode": 400,
  "error": "Missing 'text' field in request",
  "message": "Text transcript là bắt buộc để đánh giá phát âm",
  "data": null
}
```

**Validate:**
- Response status code is 400
- Empty/whitespace text is rejected

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Tests trim() validation

**Status:** PENDING

---

### TC006 - Invalid audio file format

| Field | Value |
|-------|-------|
| **ID** | TC006 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation with unsupported audio format |
| **Precondition** | - User is authenticated with valid JWT token<br>- Pronunciation service is running |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token<br>3. Include audio file with unsupported format (e.g., .txt file)<br>4. Include text parameter |

**Test Data - Request:**

**Params:**
```
file: document.txt (text file disguised as audio)
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 400 or 503 (depends on pronunciation service validation)

**Body:**
```json
{
  "statusCode": 400,
  "error": "Pronunciation assessment failed",
  "message": "Dữ liệu gửi tới pronunciation-assessment-service không hợp lệ. Vui lòng kiểm tra lại file audio.",
  "data": null
}
```

**Validate:**
- Response indicates invalid file format
- Appropriate error message returned

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Error may come from external service

**Status:** PENDING

---

### TC007 - Very large audio file

| Field | Value |
|-------|-------|
| **ID** | TC007 |
| **Item** | Validate |
| **Testcase Name** | Assess pronunciation with large audio file exceeding size limit |
| **Precondition** | - User is authenticated with valid JWT token |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include valid JWT token<br>3. Include very large audio file (e.g., 100MB)<br>4. Include text parameter |

**Test Data - Request:**

**Params:**
```
file: large_audio.wav (100MB)
text: "This is a long pronunciation test"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 413 (Payload Too Large) or 400

**Body:**
```json
{
  "statusCode": 413,
  "error": "Payload too large",
  "message": "File size exceeds maximum allowed limit",
  "data": null
}
```

**Validate:**
- Request is rejected due to file size
- Appropriate error status code

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Depends on Spring Boot max file size configuration

**Status:** PENDING

---

## Section 2: Logic (Business Logic Tests)

### TC008 - Text mismatch with audio content

| Field | Value |
|-------|-------|
| **ID** | TC008 |
| **Item** | Logic |
| **Testcase Name** | Assess pronunciation when text doesn't match audio |
| **Precondition** | - User is authenticated<br>- Pronunciation service is running<br>- Audio says "Hello world" but text is different |
| **Test Case** | 1. Send POST request with audio file<br>2. Provide text that doesn't match audio content<br>3. Verify assessment still completes but with low scores |

**Test Data - Request:**

**Params:**
```
file: audio_hello_world.wav (says "Hello world")
text: "Good morning everyone"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 200

**Body:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "accuracyScore": 15.0,
    "pronunciationScore": 20.0,
    "completenessScore": 0.0,
    "words": [...]
  }
}
```

**Validate:**
- Assessment completes successfully
- Low accuracy/completeness scores indicate mismatch
- No errors thrown

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests business logic handling of mismatched content

**Status:** PENDING

---

### TC009 - Long text assessment

| Field | Value |
|-------|-------|
| **ID** | TC009 |
| **Item** | Logic |
| **Testcase Name** | Assess pronunciation with paragraph-length text |
| **Precondition** | - User is authenticated<br>- Pronunciation service is running<br>- Audio file contains full paragraph |
| **Test Case** | 1. Send POST request with long audio file<br>2. Provide matching paragraph text (200+ words)<br>3. Verify assessment processes successfully |

**Test Data - Request:**

**Params:**
```
file: long_paragraph.wav (2-3 minutes)
text: "The quick brown fox jumps over the lazy dog. This pangram sentence... (200+ words)"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 200

**Body:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "accuracyScore": 78.5,
    "words": [...], // many words
    "feedback": "..."
  }
}
```

**Validate:**
- Long text is processed successfully
- All words are assessed
- Response time is reasonable (< 60s)

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests scalability with longer content

**Status:** PENDING

---

### TC010 - Multiple punctuation and special characters in text

| Field | Value |
|-------|-------|
| **ID** | TC010 |
| **Item** | Logic |
| **Testcase Name** | Assess pronunciation with text containing punctuation |
| **Precondition** | - User is authenticated<br>- Pronunciation service is running |
| **Test Case** | 1. Send POST request with audio<br>2. Provide text with punctuation: commas, periods, questions, exclamations<br>3. Verify punctuation is handled correctly |

**Test Data - Request:**

**Params:**
```
file: audio_punctuation.wav
text: "Hello! How are you? I'm fine, thank you. What's your name?"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 200

**Body:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "accuracyScore": 82.0,
    "words": [...],
    "prosodyScore": 85.0
  }
}
```

**Validate:**
- Punctuation is handled appropriately
- Words are assessed correctly
- Prosody score reflects intonation

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Tests text parsing with punctuation

**Status:** PENDING

---

## Section 3: Error Code (Error Handling Tests)

### TC011 - Request without authentication token

| Field | Value |
|-------|-------|
| **ID** | TC011 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation without JWT token |
| **Precondition** | - User is NOT authenticated |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Do NOT include Authorization header<br>3. Include valid audio file and text |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 401

**Body:**
```json
{
  "statusCode": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "data": null
}
```

**Validate:**
- Response status code is 401
- Request is rejected before reaching controller
- Appropriate authentication error message

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Tests Spring Security authentication

**Status:** PENDING

---

### TC012 - Request with invalid/expired token

| Field | Value |
|-------|-------|
| **ID** | TC012 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation with expired JWT token |
| **Precondition** | - User has expired JWT token |
| **Test Case** | 1. Send POST request to `/api/v1/pronunciation/assess`<br>2. Include expired JWT token<br>3. Include valid audio file and text |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.EXPIRED_TOKEN
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 401

**Body:**
```json
{
  "statusCode": 401,
  "error": "Unauthorized",
  "message": "JWT token has expired",
  "data": null
}
```

**Validate:**
- Response status code is 401
- Token validation fails
- Access denied

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Tests JWT expiration handling

**Status:** PENDING

---

### TC013 - Pronunciation service not configured

| Field | Value |
|-------|-------|
| **ID** | TC013 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation when service URL is not configured |
| **Precondition** | - User is authenticated<br>- Environment variable `PRONUNCIATION_SERVICE_URL` is empty or not set |
| **Test Case** | 1. Stop or remove pronunciation service URL configuration<br>2. Send POST request with valid data<br>3. Verify appropriate error message |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 503

**Body:**
```json
{
  "statusCode": 503,
  "error": "Pronunciation assessment failed",
  "message": "Pronunciation assessment service chưa được cấu hình. Vui lòng kiểm tra biến môi trường PRONUNCIATION_SERVICE_URL.",
  "data": null
}
```

**Validate:**
- Response status code is 503
- Error message indicates configuration issue
- data is null

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests configuration validation

**Status:** PENDING

---

### TC014 - Pronunciation service timeout

| Field | Value |
|-------|-------|
| **ID** | TC014 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation when external service times out |
| **Precondition** | - User is authenticated<br>- Pronunciation service is slow/unresponsive (takes > 30s) |
| **Test Case** | 1. Configure pronunciation service to delay response > 30s<br>2. Send POST request with valid data<br>3. Verify timeout error is returned |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 503

**Body:**
```json
{
  "statusCode": 503,
  "error": "Pronunciation assessment failed",
  "message": "Không thể kết nối tới pronunciation-assessment-service. Vui lòng kiểm tra service có đang chạy không (timeout 30s).",
  "data": null
}
```

**Validate:**
- Response status code is 503
- Error message indicates timeout
- Request doesn't hang indefinitely

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests ResourceAccessException handling

**Status:** PENDING

---

### TC015 - Pronunciation service returns 404

| Field | Value |
|-------|-------|
| **ID** | TC015 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation when service endpoint not found |
| **Precondition** | - User is authenticated<br>- Pronunciation service is running but endpoint is wrong |
| **Test Case** | 1. Configure wrong endpoint URL for pronunciation service<br>2. Send POST request with valid data<br>3. Verify 404 error is handled |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 404

**Body:**
```json
{
  "statusCode": 404,
  "error": "Pronunciation assessment failed",
  "message": "Pronunciation-assessment-service endpoint không tìm thấy. Vui lòng kiểm tra cấu hình service.",
  "data": null
}
```

**Validate:**
- Response status code is 404
- Appropriate error message
- HttpClientErrorException handled correctly

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests 404 error from external service

**Status:** PENDING

---

### TC016 - Pronunciation service returns 500

| Field | Value |
|-------|-------|
| **ID** | TC016 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation when external service has internal error |
| **Precondition** | - User is authenticated<br>- Pronunciation service returns 500 error |
| **Test Case** | 1. Configure pronunciation service to return 500 error<br>2. Send POST request with valid data<br>3. Verify error is propagated correctly |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 500

**Body:**
```json
{
  "statusCode": 500,
  "error": "Internal server error",
  "message": "Lỗi không mong muốn khi đánh giá phát âm: [error details]",
  "data": null
}
```

**Validate:**
- Response status code is 500
- Error is caught and handled
- Generic error message returned

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests unexpected exception handling

**Status:** PENDING

---

### TC017 - Pronunciation service returns invalid response

| Field | Value |
|-------|-------|
| **ID** | TC017 |
| **Item** | Error code |
| **Testcase Name** | Assess pronunciation when service returns malformed response |
| **Precondition** | - User is authenticated<br>- Pronunciation service returns non-JSON or invalid structure |
| **Test Case** | 1. Configure pronunciation service to return invalid response<br>2. Send POST request with valid data<br>3. Verify parsing error is handled |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 503

**Body:**
```json
{
  "statusCode": 503,
  "error": "Pronunciation assessment failed",
  "message": "Pronunciation-assessment-service không phản hồi đúng cách. Vui lòng thử lại sau.",
  "data": null
}
```

**Validate:**
- Response parsing error is caught
- Appropriate error message
- Application doesn't crash

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests response validation

**Status:** PENDING

---

## Section 4: Format Response (Response Format Tests)

### TC018 - Verify response structure for success case

| Field | Value |
|-------|-------|
| **ID** | TC018 |
| **Item** | Format response |
| **Testcase Name** | Verify success response structure matches specification |
| **Precondition** | - User is authenticated<br>- Pronunciation service is running |
| **Test Case** | 1. Send valid POST request<br>2. Verify response contains all required fields<br>3. Verify field types match specification |

**Test Data - Request:**

**Params:**
```
file: audio_sample.wav
text: "Hello world"
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 200

**Body:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    ...
  }
}
```

**Validate:**
- Response has statusCode (integer)
- Response has error (null for success)
- Response has message (string)
- Response has data (object, not null)
- JSON structure is valid

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Tests RestResponse<Map<String, Object>> structure

**Status:** PENDING

---

### TC019 - Verify response structure for error case

| Field | Value |
|-------|-------|
| **ID** | TC019 |
| **Item** | Format response |
| **Testcase Name** | Verify error response structure matches specification |
| **Precondition** | - User is authenticated |
| **Test Case** | 1. Send POST request missing required field<br>2. Verify error response structure<br>3. Verify all error fields are present |

**Test Data - Request:**

**Params:**
```
text: "Hello world"
(no file)
```

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 400

**Body:**
```json
{
  "statusCode": 400,
  "error": "Missing 'audio' file in request",
  "message": "Audio file không được để trống",
  "data": null
}
```

**Validate:**
- Response has statusCode (integer, matches HTTP status)
- Response has error (string, not null for errors)
- Response has message (string)
- Response has data (null for errors)
- JSON structure is valid

**Expected Database Result:** N/A

**Execution:** AUTOMATION

**Note:** Tests error response structure consistency

**Status:** PENDING

---

## Section 5: Other (Additional Tests)

### TC020 - Concurrent requests handling

| Field | Value |
|-------|-------|
| **ID** | TC020 |
| **Item** | Other |
| **Testcase Name** | Multiple concurrent pronunciation assessment requests |
| **Precondition** | - Multiple users are authenticated<br>- Pronunciation service is running |
| **Test Case** | 1. Send 10 concurrent POST requests from different users<br>2. Verify all requests complete successfully<br>3. Verify no data mixing between requests |

**Test Data - Request:**

**Params:** (for each concurrent request)
```
file: audio_sample_N.wav (different audio for each)
text: "Different text for each request N"
```

**Headers:** (different tokens for each)
```
Authorization: Bearer [unique_token_for_user_N]
Content-Type: multipart/form-data
```

**Expected Response:**

**Status Code:** 200 (for all requests)

**Body:** (each request should get its own response)
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "accuracyScore": [...different for each]
  }
}
```

**Validate:**
- All 10 requests complete successfully
- No data mixing between responses
- Response times are reasonable
- No deadlocks or race conditions

**Expected Database Result:** N/A

**Execution:** MANUAL

**Note:** Tests thread safety and concurrent request handling

**Status:** PENDING

---

## Test Execution Summary

| Category | Test IDs | Total |
|----------|----------|-------|
| **Validate** | TC001-TC007 | 7 |
| **Logic** | TC008-TC010 | 3 |
| **Error Code** | TC011-TC017 | 7 |
| **Format Response** | TC018-TC019 | 2 |
| **Other** | TC020 | 1 |
| **TOTAL** | | **20** |

---

## Notes

1. All test cases are currently in PENDING status - ready for execution
2. Tests marked as AUTOMATION can be automated using testing frameworks (JUnit, REST Assured, etc.)
3. Tests marked as MANUAL require manual setup or specific environmental conditions
4. The pronunciation-assessment-service must be properly configured and running for most tests
5. Test data audio files should be prepared before test execution
6. JWT tokens should be valid and not expired during test execution
7. Consider adding performance tests for large-scale usage scenarios
8. Some error scenarios may require mocking the external pronunciation service
