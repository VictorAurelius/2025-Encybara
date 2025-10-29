# TEST REPORT

**Format:** Excel-friendly table format (v2)
**Last Updated:** 2025-10-29

---

## Summary Table

| No. | Sheet name          | API name                              | Total Testcase | Result |        |         |         | Execution |        |     | Status Testing | Progress | Remarks |
|-----|---------------------|---------------------------------------|----------------|--------|--------|---------|---------|-----------|--------|-----|----------------|----------|---------|
|     |                     |                                       |                | Passed | Failed | Blocked | Not run | Automation| Manual | N/A |                |          |         |
| 1   | AssessPronunciation | POST /api/v1/pronunciation/assess     | 20             | 0      | 0      | 0       | 20      | 15        | 5      | 0   |                |          |         |

---

## Project Information

| Project Name    | Encybara - English Learning Platform     |
|-----------------|------------------------------------------|
| API Name        | POST /api/v1/pronunciation/assess        |
| Owner           |                                          |

---

## Test Statistics

| PASSED          | 0                                        |
|-----------------|------------------------------------------|
| FAILED          | 0                                        |
| PENDING         | 20                                       |
| NOT RUN         | 0                                        |
| AUTOMATION      | 15                                       |
| MANUAL          | 5                                        |
| **Number of Testcase** | **20**                            |

---

## Test Cases

| ID     | Item            | Testcase Name                                  | Precondition                                    | Test Case                                           | params              | body                        | Status code | Body                                                      | Expected Database Result | Result  | Execution  | Note                        |
|--------|-----------------|------------------------------------------------|-------------------------------------------------|-----------------------------------------------------|---------------------|-----------------------------|--------------|---------------------------------------------------------|--------------------------|---------|------------|------------------------------|
| **Validate**                                                                                                                                                                                                                                                                                                                                                        |
| TC-001 | Validate        | Valid pronunciation assessment with all required fields | 1. User authenticated<br>2. Pronunciation service running<br>3. Valid audio file available | 1. Send POST to /api/v1/pronunciation/assess<br>2. Include valid JWT<br>3. Include valid audio file<br>4. Include reference text | file=audio_sample.wav<br>text=Hello, how are you today? | N/A (multipart) | 200 | {"statusCode":200,"error":null,"message":"Pronunciation assessment completed successfully","data":{...}} | N/A | PENDING | AUTOMATION | Happy path test |
| TC-002 | Validate        | Missing audio file                             | 1. User authenticated                           | 1. Send POST without audio file<br>2. Include text parameter | text=Hello world | N/A | 400 | {"statusCode":400,"error":"Missing 'audio' file in request","message":"Audio file không được để trống","data":null} | N/A | PENDING | AUTOMATION | Required file validation |
| TC-003 | Validate        | Empty audio file                               | 1. User authenticated                           | 1. Send POST with empty audio file (0 bytes)<br>2. Include text | file=empty.wav (0 bytes)<br>text=Hello world | N/A | 400 | {"statusCode":400,"error":"Missing 'audio' file in request","message":"Audio file không được để trống","data":null} | N/A | PENDING | AUTOMATION | File not empty validation |
| TC-004 | Validate        | Missing text parameter                         | 1. User authenticated                           | 1. Send POST with audio file<br>2. Do NOT include text parameter | file=audio_sample.wav | N/A | 400 | {"statusCode":400,"error":"Missing 'text' field in request","message":"Text transcript là bắt buộc để đánh giá phát âm","data":null} | N/A | PENDING | AUTOMATION | Required text validation |
| TC-005 | Validate        | Empty text parameter                           | 1. User authenticated                           | 1. Send POST with audio file<br>2. Include empty text (whitespace only) | file=audio_sample.wav<br>text="   " | N/A | 400 | {"statusCode":400,"error":"Missing 'text' field in request","message":"Text transcript là bắt buộc để đánh giá phát âm","data":null} | N/A | PENDING | AUTOMATION | Text trim() validation |
| TC-006 | Validate        | Invalid audio file format                      | 1. User authenticated<br>2. Pronunciation service running | 1. Send POST with unsupported file format<br>2. Include text | file=document.txt<br>text=Hello world | N/A | 400 or 503 | {"statusCode":400,"error":"Pronunciation assessment failed","message":"Dữ liệu gửi tới pronunciation-assessment-service không hợp lệ..."} | N/A | PENDING | MANUAL | Error may come from external service |
| TC-007 | Validate        | Very large audio file exceeding size limit     | 1. User authenticated                           | 1. Send POST with very large file (100MB)<br>2. Include text | file=large_audio.wav (100MB)<br>text=Test | N/A | 413 or 400 | {"statusCode":413,"error":"Payload too large","message":"File size exceeds maximum allowed limit"} | N/A | PENDING | MANUAL | Depends on Spring Boot config |
| **Logic**                                                                                                                                                                                                                                                                                                                                                           |
| TC-008 | Logic           | Text mismatch with audio content               | 1. User authenticated<br>2. Service running<br>3. Audio says "Hello world" but text is different | 1. Send POST with audio file<br>2. Provide text that doesn't match audio | file=audio_hello_world.wav<br>text=Good morning everyone | N/A | 200 | {"statusCode":200,"error":null,"data":{"accuracyScore":15.0,"completenessScore":0.0}} | N/A | PENDING | MANUAL | Low scores indicate mismatch |
| TC-009 | Logic           | Long text assessment (paragraph-length)        | 1. User authenticated<br>2. Service running<br>3. Audio contains full paragraph | 1. Send POST with long audio file<br>2. Provide matching paragraph text (200+ words) | file=long_paragraph.wav<br>text=The quick brown fox... (200+ words) | N/A | 200 | {"statusCode":200,"error":null,"data":{"accuracyScore":78.5,"words":[...]}} | N/A | PENDING | MANUAL | Tests scalability |
| TC-010 | Logic           | Multiple punctuation and special characters in text | 1. User authenticated<br>2. Service running | 1. Send POST with audio<br>2. Provide text with punctuation: commas, periods, questions | file=audio_punctuation.wav<br>text=Hello! How are you? I'm fine, thank you. | N/A | 200 | {"statusCode":200,"error":null,"data":{"prosodyScore":85.0}} | N/A | PENDING | AUTOMATION | Tests text parsing |
| **Error code**                                                                                                                                                                                                                                                                                                                                                      |
| TC-011 | Error code      | Request without authentication token           | 1. User NOT authenticated                       | 1. Send POST without Authorization header<br>2. Include valid audio and text | file=audio_sample.wav<br>text=Hello world | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"Full authentication is required to access this resource"} | N/A | PENDING | AUTOMATION | Spring Security check |
| TC-012 | Error code      | Request with invalid/expired token             | 1. User has expired JWT token                   | 1. Send POST with expired JWT<br>2. Include valid audio and text | file=audio_sample.wav<br>text=Hello world | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"JWT token has expired"} | N/A | PENDING | AUTOMATION | JWT expiration handling |
| TC-013 | Error code      | Pronunciation service not configured           | 1. User authenticated<br>2. PRONUNCIATION_SERVICE_URL not set | 1. Stop or remove service URL config<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 503 | {"statusCode":503,"error":"Pronunciation assessment failed","message":"Pronunciation assessment service chưa được cấu hình..."} | N/A | PENDING | MANUAL | Configuration validation |
| TC-014 | Error code      | Pronunciation service timeout                  | 1. User authenticated<br>2. Service slow/unresponsive (>30s) | 1. Configure service to delay >30s<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 503 | {"statusCode":503,"error":"Pronunciation assessment failed","message":"Không thể kết nối tới pronunciation-assessment-service..."} | N/A | PENDING | MANUAL | Timeout error handling |
| TC-015 | Error code      | Pronunciation service returns 404              | 1. User authenticated<br>2. Service running but wrong endpoint | 1. Configure wrong endpoint URL<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 404 | {"statusCode":404,"error":"Pronunciation assessment failed","message":"Pronunciation-assessment-service endpoint không tìm thấy..."} | N/A | PENDING | MANUAL | 404 from external service |
| TC-016 | Error code      | Pronunciation service returns 500              | 1. User authenticated<br>2. Service returns 500 error | 1. Configure service to return 500<br>2. Send POST with valid data | file=audio_sample.wav<br>text=Hello world | N/A | 500 | {"statusCode":500,"error":"Internal server error","message":"Lỗi không mong muốn khi đánh giá phát âm..."} | N/A | PENDING | MANUAL | Unexpected exception handling |
| TC-017 | Error code      | Pronunciation service returns invalid response | 1. User authenticated<br>2. Service returns non-JSON/invalid structure | 1. Configure service to return invalid response<br>2. Send POST | file=audio_sample.wav<br>text=Hello world | N/A | 503 | {"statusCode":503,"error":"Pronunciation assessment failed","message":"Pronunciation-assessment-service không phản hồi đúng cách..."} | N/A | PENDING | MANUAL | Response validation |
| **Format response**                                                                                                                                                                                                                                                                                                                                                 |
| TC-018 | Format response | Verify response structure for success case     | 1. User authenticated<br>2. Service running     | 1. Send valid POST request<br>2. Verify response contains all required fields<br>3. Verify field types | file=audio_sample.wav<br>text=Hello world | N/A | 200 | Verify: statusCode (int), error (null), message (string), data (object not null) | N/A | PENDING | AUTOMATION | RestResponse structure |
| TC-019 | Format response | Verify response structure for error case       | 1. User authenticated                           | 1. Send POST missing required field<br>2. Verify error response structure | text=Hello world (no file) | N/A | 400 | Verify: statusCode (int matches HTTP), error (string not null), message (string), data (null) | N/A | PENDING | AUTOMATION | Error response consistency |
| **Other**                                                                                                                                                                                                                                                                                                                                                           |
| TC-020 | Other           | Concurrent requests handling                   | 1. Multiple users authenticated<br>2. Service running | 1. Send 10 concurrent POST from different users<br>2. Verify all complete successfully<br>3. Verify no data mixing | Different files/texts for each user | N/A | 200 (all) | Each request gets own correct response, no data mixing | N/A | PENDING | MANUAL | Thread safety |

---

## Notes

1. All test cases are currently in PENDING status - ready for execution
2. Tests marked as AUTOMATION (15 TCs) can be automated using testing frameworks
3. Tests marked as MANUAL (5 TCs) require manual setup or specific environmental conditions
4. The pronunciation-assessment-service must be properly configured and running for most tests
5. Test data audio files should be prepared before test execution
6. JWT tokens should be valid and not expired during test execution
7. Some error scenarios may require mocking the external pronunciation service
