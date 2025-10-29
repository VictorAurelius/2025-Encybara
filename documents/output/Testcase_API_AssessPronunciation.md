# TEST REPORT

**Format:** Consolidated Excel-friendly format
**Last Updated:** 2025-10-29
**Project:** Encybara - English Learning Platform

---

## Summary Table

| No. | Sheet name          | API name                              | Total Testcase | Result |        |         |         | Execution |        |     | Status Testing | Progress | Remarks |
|-----|---------------------|---------------------------------------|----------------|--------|--------|---------|---------|-----------|--------|-----|----------------|----------|---------|
|     |                     |                                       |                | Passed | Failed | Blocked | Not run | Automation| Manual | N/A |                |          |         |
| 1   | AssessPronunciation | POST /api/v1/pronunciation/assess     | 30             | 0      | 0      | 0       | 30      | 20        | 10     | 0   |                |          |         |

---

## Project Information

| Project Name    | Encybara - English Learning Platform     |
|-----------------|------------------------------------------|
| API Name        | POST /api/v1/pronunciation/assess        |
| Owner           |                                          |

---

## Test Statistics

| PASSED              | 0                                    |
|---------------------|--------------------------------------|
| FAILED              | 0                                    |
| PENDING             | 30                                   |
| NOT RUN             | 0                                    |
| AUTOMATION          | 20                                   |
| MANUAL              | 10                                   |
| **Number of Testcase** | **30**                            |

---

## Test Cases

| ID     | Item            | Testcase Name                                  | Precondition                                    | Test Case                                           | params              | body                        | Status code | Body                                                      | Expected Database Result | Result  | Execution  | Note                        |
|--------|-----------------|------------------------------------------------|-------------------------------------------------|-----------------------------------------------------|---------------------|-----------------------------|--------------|---------------------------------------------------------|--------------------------|---------|------------|------------------------------|
| **Validate**                                                                                                                                                                                                                                                                                                                                                        |
| ID-001 | Validate        | Gọi API với method POST                        | 1. Đăng nhập thành công                         | 1. Gọi API với method POST                          |                     |                             | 200 OK       |                                                         |                          | PENDING | AUTOMATION |                              |
| ID-002 | Validate        | Gọi API với method GET                         | 1. Đăng nhập thành công                         | 1. Gọi API với method GET                           |                     |                             | 405 Method Not Allowed |                                                  |                          | PENDING | AUTOMATION |                              |
| ID-003 | Validate        | Gọi API với method PUT                         | 1. Đăng nhập thành công                         | 1. Gọi API với method PUT                           |                     |                             | 405 Method Not Allowed |                                                  |                          | PENDING | AUTOMATION |                              |
| ID-004 | Validate        | Gọi API với method DELETE                      | 1. Đăng nhập thành công                         | 1. Gọi API với method DELETE                        |                     |                             | 405 Method Not Allowed |                                                  |                          | PENDING | AUTOMATION |                              |
| ID-005 | Validate        | Valid request với đầy đủ params                | 1. User authenticated<br>2. Service running<br>3. Valid audio file available | 1. Send POST to endpoint<br>2. Include valid JWT<br>3. Include valid audio and text | file=audio_sample.wav<br>text=Hello, how are you today? | N/A (multipart) | 200 | {"statusCode":200,"error":null,"message":"Pronunciation assessment completed successfully","data":{"overall_score":84.2,"fluency_score":92.0,"phoneme_scores":[...],"total_phonemes":12,"average_duration":0.130}} | N/A | PENDING | AUTOMATION | Happy path test |
| ID-006 | Validate        | Missing audio file                             | 1. User authenticated                           | 1. Send POST without audio file<br>2. Include text parameter | text=Hello world | N/A | 400 | {"statusCode":400,"error":"Missing 'audio' file in request","message":"Audio file không được để trống","data":null} | N/A | PENDING | AUTOMATION | Required file validation |
| ID-007 | Validate        | Empty audio file                               | 1. User authenticated                           | 1. Send POST with empty audio file (0 bytes)<br>2. Include text | file=empty.wav (0 bytes)<br>text=Hello world | N/A | 400 | {"statusCode":400,"error":"Missing 'audio' file in request","message":"Audio file không được để trống","data":null} | N/A | PENDING | AUTOMATION | File not empty validation |
| ID-008 | Validate        | Missing text parameter                         | 1. User authenticated                           | 1. Send POST with audio file<br>2. Do NOT include text parameter | file=audio_sample.wav | N/A | 400 | {"statusCode":400,"error":"Missing 'text' field in request","message":"Text transcript là bắt buộc để đánh giá phát âm","data":null} | N/A | PENDING | AUTOMATION | Required text validation |
| ID-009 | Validate        | Empty text parameter (whitespace only)         | 1. User authenticated                           | 1. Send POST with audio file<br>2. Include empty text (whitespace) | file=audio_sample.wav<br>text="   " | N/A | 400 | {"statusCode":400,"error":"Missing 'text' field in request","message":"Text transcript là bắt buộc để đánh giá phát âm","data":null} | N/A | PENDING | AUTOMATION | Text trim() validation |
| ID-010 | Validate        | Invalid audio file format                      | 1. User authenticated<br>2. Service running     | 1. Send POST with unsupported file format<br>2. Include text | file=document.txt<br>text=Hello world | N/A | 400 or 503 | {"statusCode":400,"error":"Pronunciation assessment failed","message":"Dữ liệu gửi tới pronunciation-assessment-service không hợp lệ. Vui lòng kiểm tra lại file audio.","data":null} | N/A | PENDING | MANUAL | Error from external service |
| **Logic**                                                                                                                                                                                                                                                                                                                                                           |
| ID-011 | Logic           | Text mismatch với audio content                | 1. User authenticated<br>2. Service running<br>3. Audio says "Hello world" but text is different | 1. Send POST with audio file<br>2. Provide text that doesn't match audio content | file=audio_hello_world.wav<br>text=Good morning everyone | N/A | 200 | {"statusCode":200,"error":null,"data":{"overall_score":15.0,"fluency_score":20.0,"phoneme_scores":[...],"total_phonemes":10}} | N/A | PENDING | MANUAL | Low scores indicate mismatch |
| ID-012 | Logic           | Long text assessment (paragraph-length)        | 1. User authenticated<br>2. Service running<br>3. Audio contains full paragraph | 1. Send POST with long audio file (2 min)<br>2. Provide matching paragraph text (200+ words) | file=long_paragraph.wav<br>text=The quick brown fox... (200+ words) | N/A | 200 | {"statusCode":200,"error":null,"data":{"overall_score":78.5,"fluency_score":85.0,"phoneme_scores":[...],"total_phonemes":500+}} | N/A | PENDING | MANUAL | Tests scalability |
| ID-013 | Logic           | Multiple punctuation and special characters in text | 1. User authenticated<br>2. Service running | 1. Send POST with audio<br>2. Provide text with punctuation: commas, periods, questions, exclamations | file=audio_punctuation.wav<br>text=Hello! How are you? I'm fine, thank you. | N/A | 200 | {"statusCode":200,"error":null,"data":{"overall_score":82.0,"fluency_score":88.0,"phoneme_scores":[...]}} | N/A | PENDING | AUTOMATION | Tests text parsing with punctuation |
| ID-014 | Logic           | Special characters and numbers in text         | 1. User authenticated<br>2. Service running     | 1. Send POST with audio<br>2. Provide text with numbers and special chars | file=audio_special.wav<br>text=Call me at 123-456-7890! | N/A | 200 | {"statusCode":200,"error":null,"data":{"overall_score":70.0,"phoneme_scores":[...]}} | N/A | PENDING | AUTOMATION | Tests special character handling |
| ID-015 | Logic           | Very short audio (< 1 second)                  | 1. User authenticated<br>2. Service running     | 1. Send POST with very short audio file<br>2. Provide matching short text | file=audio_short.wav (0.5s)<br>text=Hi | N/A | 200 | {"statusCode":200,"error":null,"data":{"overall_score":75.0,"total_phonemes":2}} | N/A | PENDING | MANUAL | Tests minimum audio length |
| **Error code**                                                                                                                                                                                                                                                                                                                                                      |
| ID-016 | Error code      | Request without authentication token           | 1. User NOT authenticated                       | 1. Send POST without Authorization header<br>2. Include valid audio and text | file=audio_sample.wav<br>text=Hello world | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"Full authentication is required to access this resource","data":null} | N/A | PENDING | AUTOMATION | Spring Security authentication |
| ID-017 | Error code      | Request with expired token                     | 1. User has expired JWT token                   | 1. Send POST with expired JWT<br>2. Include valid audio and text | file=audio_sample.wav<br>text=Hello world | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"JWT token has expired","data":null} | N/A | PENDING | AUTOMATION | JWT expiration handling |
| ID-018 | Error code      | Request with invalid token                     | 1. User has invalid/malformed JWT token         | 1. Send POST with invalid JWT<br>2. Include valid audio and text | file=audio_sample.wav<br>text=Hello world | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"Invalid JWT token","data":null} | N/A | PENDING | AUTOMATION | JWT validation |
| ID-019 | Error code      | Pronunciation service not configured           | 1. User authenticated<br>2. PRONUNCIATION_SERVICE_URL not set or empty | 1. Stop or remove service URL config<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 503 | {"statusCode":503,"error":"Pronunciation assessment failed","message":"Pronunciation assessment service chưa được cấu hình. Vui lòng kiểm tra biến môi trường PRONUNCIATION_SERVICE_URL.","data":null} | N/A | PENDING | MANUAL | Configuration validation |
| ID-020 | Error code      | Pronunciation service timeout                  | 1. User authenticated<br>2. Service slow/unresponsive (>30s) | 1. Configure service to delay response >30s<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 503 | {"statusCode":503,"error":"Pronunciation assessment failed","message":"Không thể kết nối tới pronunciation-assessment-service. Vui lòng kiểm tra service có đang chạy không (timeout 30s).","data":null} | N/A | PENDING | MANUAL | ResourceAccessException handling |
| ID-021 | Error code      | Pronunciation service returns 404              | 1. User authenticated<br>2. Service running but endpoint wrong | 1. Configure wrong endpoint URL<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 404 | {"statusCode":404,"error":"Pronunciation assessment failed","message":"Pronunciation-assessment-service endpoint không tìm thấy. Vui lòng kiểm tra cấu hình service.","data":null} | N/A | PENDING | MANUAL | HttpClientErrorException 404 |
| ID-022 | Error code      | Pronunciation service returns 500              | 1. User authenticated<br>2. Service returns 500 internal error | 1. Configure service to return 500 error<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 500 | {"statusCode":500,"error":"Internal server error","message":"Lỗi không mong muốn khi đánh giá phát âm: ...","data":null} | N/A | PENDING | MANUAL | HttpServerErrorException handling |
| ID-023 | Error code      | Pronunciation service returns invalid response | 1. User authenticated<br>2. Service returns non-JSON or malformed response | 1. Configure service to return invalid response<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 503 | {"statusCode":503,"error":"Pronunciation assessment failed","message":"Pronunciation-assessment-service không phản hồi đúng cách. Vui lòng thử lại sau.","data":null} | N/A | PENDING | MANUAL | Response parsing error |
| ID-024 | Error code      | Large file exceeds size limit                  | 1. User authenticated                           | 1. Send POST with very large audio file (>6MB)<br>2. Include text | file=large_audio.wav (10MB)<br>text=Test | N/A | 400 or 413 | {"statusCode":400,"error":"File size exceeds maximum limit","message":"...","data":null} | N/A | PENDING | MANUAL | External service file size limit |
| ID-025 | Error code      | Concurrent requests handling                   | 1. Multiple users authenticated<br>2. Service running | 1. Send 10 concurrent POST requests from different users<br>2. Verify all complete successfully | Different files/texts for each user | N/A | 200 (all) | Each request gets own correct response, no data mixing | N/A | PENDING | MANUAL | Thread safety and isolation |
| **Format response**                                                                                                                                                                                                                                                                                                                                                 |
| ID-026 | Format response | Verify response structure for success case     | 1. User authenticated<br>2. Service running     | 1. Send valid POST request<br>2. Verify response contains all required fields<br>3. Verify field types match specification | file=audio_sample.wav<br>text=Hello world | N/A | 200 | Verify: statusCode (Integer), error (null), message (String), data (Object with overall_score, fluency_score, phoneme_scores, total_phonemes, average_duration) | N/A | PENDING | AUTOMATION | RestResponse structure validation |
| ID-027 | Format response | Verify response structure for error case       | 1. User authenticated                           | 1. Send POST missing required field<br>2. Verify error response structure | text=Hello world (no file) | N/A | 400 | Verify: statusCode (Integer matches HTTP), error (String not null), message (String), data (null) | N/A | PENDING | AUTOMATION | Error response consistency |
| ID-028 | Format response | Verify overall_score field exists and valid    | 1. User authenticated<br>2. Service running     | 1. Send valid POST request<br>2. Verify data.overall_score exists<br>3. Verify value is Double between 0-100 | file=audio_sample.wav<br>text=Hello | N/A | 200 | Verify: data.overall_score is Double, 0 <= value <= 100 | N/A | PENDING | AUTOMATION | Field validation |
| ID-029 | Format response | Verify fluency_score field exists and valid    | 1. User authenticated<br>2. Service running     | 1. Send valid POST request<br>2. Verify data.fluency_score exists<br>3. Verify value is Double between 0-100 | file=audio_sample.wav<br>text=World | N/A | 200 | Verify: data.fluency_score is Double, 0 <= value <= 100 | N/A | PENDING | AUTOMATION | Field validation |
| ID-030 | Format response | Verify phoneme_scores array structure          | 1. User authenticated<br>2. Service running     | 1. Send valid POST request<br>2. Verify data.phoneme_scores is Array<br>3. Verify each element has required fields | file=audio_sample.wav<br>text=Test | N/A | 200 | Verify: phoneme_scores is Array, each element has: phoneme, gop_score, quality, start_time, end_time, character, word_index, phoneme_index | N/A | PENDING | AUTOMATION | Array structure validation |

---

## Notes

1. All test cases are currently in PENDING status - ready for execution
2. Tests marked as AUTOMATION (20 TCs) can be automated using testing frameworks (JUnit, REST Assured, etc.)
3. Tests marked as MANUAL (10 TCs) require manual setup, specific environmental conditions, or service mocking
4. The pronunciation-assessment-service must be properly configured and running for most tests
5. Test data audio files should be prepared before test execution with matching transcript text
6. JWT tokens should be valid and not expired during test execution
7. Some error scenarios (ID-019 to ID-024) may require mocking the external pronunciation service
8. Response body validation should check actual field names from external service: overall_score, fluency_score, phoneme_scores, total_phonemes, average_duration
9. Phoneme_scores array validation should verify 8 fields per object: phoneme, gop_score, quality, start_time, end_time, character, word_index, phoneme_index
10. Quality field values are: "excellent", "good", "fair", "poor"

---

## Test Execution Summary

| Category         | Test IDs        | Total | Automation | Manual |
|------------------|-----------------|-------|------------|--------|
| **Validate**     | ID-001 ~ ID-010 | 10    | 9          | 1      |
| **Logic**        | ID-011 ~ ID-015 | 5     | 2          | 3      |
| **Error Code**   | ID-016 ~ ID-025 | 10    | 3          | 7      |
| **Format Response** | ID-026 ~ ID-030 | 5  | 5          | 0      |
| **TOTAL**        |                 | **30**| **20**     | **10** |
