# TEST REPORT

**Format:** Consolidated Excel-friendly format
**Last Updated:** 2025-10-29
**Project:** Encybara - English Learning Platform

---

## Summary Table

| No. | Sheet name          | API name                              | Total Testcase | Result |        |         |         | Execution |        |     | Status Testing | Progress | Remarks |
|-----|---------------------|---------------------------------------|----------------|--------|--------|---------|---------|-----------|--------|-----|----------------|----------|---------|
|     |                     |                                       |                | Passed | Failed | Blocked | Not run | Automation| Manual | N/A |                |          |         |
| 1   | GradeAnswer         | PUT /api/v1/answers/grade/{answerId}  | 30             | 0      | 0      | 0       | 30      | 23        | 7      | 0   |                |          |         |

---

## Project Information

| Project Name    | Encybara - English Learning Platform     |
|-----------------|------------------------------------------|
| API Name        | PUT /api/v1/answers/grade/{answerId}     |
| Owner           |                                          |

---

## Test Statistics

| PASSED              | 0                                    |
|---------------------|--------------------------------------|
| FAILED              | 0                                    |
| PENDING             | 30                                   |
| NOT RUN             | 0                                    |
| AUTOMATION          | 23                                   |
| MANUAL              | 7                                    |
| **Number of Testcase** | **30**                            |

---

## Test Cases

| ID     | Item            | Testcase Name                                  | Precondition                                    | Test Case                                           | params              | body                        | Status code | Body                                                      | Expected Database Result | Result  | Execution  | Note                        |
|--------|-----------------|------------------------------------------------|-------------------------------------------------|-----------------------------------------------------|---------------------|-----------------------------|--------------|---------------------------------------------------------|--------------------------|---------|------------|------------------------------|
| **Validate**                                                                                                                                                                                                                                                                                                                                                        |
| ID-001 | Validate        | Gọi API với method PUT                         | 1. Đăng nhập thành công<br>2. Answer ID 123 tồn tại | 1. Gọi API với method PUT                           | answerId=123        |                             | 200 OK       | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{...}} | Answer.point_achieved updated | PENDING | AUTOMATION | Happy path |
| ID-002 | Validate        | Gọi API với method GET                         | 1. Đăng nhập thành công                         | 1. Gọi API với method GET                           | answerId=123        |                             | 405 Method Not Allowed | {"statusCode":405,"error":"Method Not Allowed","message":"...","data":null} |  | PENDING | AUTOMATION | Wrong HTTP method |
| ID-003 | Validate        | Gọi API với method POST                        | 1. Đăng nhập thành công                         | 1. Gọi API với method POST                          | answerId=123        |                             | 405 Method Not Allowed | {"statusCode":405,"error":"Method Not Allowed","message":"...","data":null} |  | PENDING | AUTOMATION | Wrong HTTP method |
| ID-004 | Validate        | Gọi API với method DELETE                      | 1. Đăng nhập thành công                         | 1. Gọi API với method DELETE                        | answerId=123        |                             | 405 Method Not Allowed | {"statusCode":405,"error":"Method Not Allowed","message":"...","data":null} |  | PENDING | AUTOMATION | Wrong HTTP method |
| ID-005 | Validate        | Valid answerId format (positive number)        | 1. User authenticated<br>2. Answer ID 1 exists in DB<br>3. Question and choices loaded | 1. Send PUT request to /api/v1/answers/grade/1<br>2. Include valid JWT | answerId=1 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":1,"questionId":45,"answerContent":"Paris","pointAchieved":10,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 10 | PENDING | AUTOMATION | Valid Long format |
| ID-006 | Validate        | Invalid answerId format (text string)          | 1. User authenticated                           | 1. Send PUT to /api/v1/answers/grade/abc<br>2. Include valid JWT | answerId=abc | N/A | 400 | {"statusCode":400,"error":"Bad Request","message":"Invalid path variable format","data":null} | No DB changes | PENDING | AUTOMATION | Type validation |
| ID-007 | Validate        | Invalid answerId format (negative number)      | 1. User authenticated                           | 1. Send PUT to /api/v1/answers/grade/-1<br>2. Include valid JWT | answerId=-1 | N/A | 404 | {"statusCode":404,"error":"Resource Not Found","message":"Answer not found","data":null} | No DB changes | PENDING | AUTOMATION | Negative ID handling |
| ID-008 | Validate        | Request without authentication token           | 1. User NOT authenticated                       | 1. Send PUT without Authorization header           | answerId=123 | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"Full authentication is required to access this resource","data":null} | No DB changes | PENDING | AUTOMATION | Spring Security auth |
| ID-009 | Validate        | Request with invalid JWT token                 | 1. User has invalid/malformed JWT               | 1. Send PUT with invalid JWT token<br>2. Use valid answerId | answerId=123 | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"Invalid JWT token","data":null} | No DB changes | PENDING | AUTOMATION | JWT validation |
| ID-010 | Validate        | Request with expired JWT token                 | 1. User has expired JWT token                   | 1. Send PUT with expired JWT<br>2. Use valid answerId | answerId=123 | N/A | 401 | {"statusCode":401,"error":"Unauthorized","message":"JWT token has expired","data":null} | No DB changes | PENDING | AUTOMATION | JWT expiration |
| **Logic**                                                                                                                                                                                                                                                                                                                                                           |
| ID-011 | Logic           | Grade CHOICE question - correct answer         | 1. User authenticated<br>2. Answer ID 200 exists<br>3. Question type = CHOICE<br>4. answerContent = "Paris"<br>5. Correct choice = "Paris"<br>6. Question.point = 10 | 1. Send PUT to /api/v1/answers/grade/200<br>2. System compares normalized answers<br>3. Match found | answerId=200 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":200,"questionId":45,"answerContent":"Paris","pointAchieved":10,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 10 (full points) | PENDING | AUTOMATION | CHOICE grading - correct |
| ID-012 | Logic           | Grade CHOICE question - incorrect answer       | 1. User authenticated<br>2. Answer ID 201 exists<br>3. Question type = CHOICE<br>4. answerContent = "London"<br>5. Correct choice = "Paris"<br>6. Question.point = 10 | 1. Send PUT to /api/v1/answers/grade/201<br>2. System compares normalized answers<br>3. No match found | answerId=201 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":201,"questionId":45,"answerContent":"London","pointAchieved":0,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 0 (zero points) | PENDING | AUTOMATION | CHOICE grading - incorrect |
| ID-013 | Logic           | Grade MULTIPLE question - all correct          | 1. User authenticated<br>2. Answer ID 202 exists<br>3. Question type = MULTIPLE<br>4. answerContent = "A, B, C"<br>5. Correct choices = A, B, C<br>6. Question.point = 12 | 1. Send PUT to /api/v1/answers/grade/202<br>2. System parses comma-separated answers<br>3. All match correct choices | answerId=202 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":202,"questionId":50,"answerContent":"A, B, C","pointAchieved":12,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 12 (full points) | PENDING | AUTOMATION | MULTIPLE full credit |
| ID-014 | Logic           | Grade MULTIPLE question - partial correct      | 1. User authenticated<br>2. Answer ID 203 exists<br>3. Question type = MULTIPLE<br>4. answerContent = "A, B"<br>5. Correct choices = A, B, C, D<br>6. Question.point = 10 | 1. Send PUT to /api/v1/answers/grade/203<br>2. System calculates partial: (2/4)*10 = 5<br>3. Round result | answerId=203 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":203,"questionId":51,"answerContent":"A, B","pointAchieved":5,"sessionId":1,"improvement":"Partial credit: 2 of 4 correct","enrollmentId":78}} | Answer.point_achieved = 5 (partial points) | PENDING | AUTOMATION | MULTIPLE partial credit |
| ID-015 | Logic           | Grade MULTIPLE question - all incorrect        | 1. User authenticated<br>2. Answer ID 204 exists<br>3. Question type = MULTIPLE<br>4. answerContent = "X, Y, Z"<br>5. Correct choices = A, B, C<br>6. Question.point = 10 | 1. Send PUT to /api/v1/answers/grade/204<br>2. System checks each answer<br>3. None match correct choices | answerId=204 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":204,"questionId":52,"answerContent":"X, Y, Z","pointAchieved":0,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 0 (zero points) | PENDING | AUTOMATION | MULTIPLE all wrong |
| ID-016 | Logic           | Grade TEXT question - correct (case insensitive) | 1. User authenticated<br>2. Answer ID 205 exists<br>3. Question type = TEXT<br>4. answerContent = "THE CAPITAL OF FRANCE IS PARIS."<br>5. Correct = "the capital of france is paris"<br>6. Question.point = 5 | 1. Send PUT to /api/v1/answers/grade/205<br>2. System normalizes both strings (lowercase, trim, remove punctuation)<br>3. Match found | answerId=205 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":205,"questionId":48,"answerContent":"THE CAPITAL OF FRANCE IS PARIS.","pointAchieved":5,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 5 (full points) | PENDING | AUTOMATION | TEXT normalization - case |
| ID-017 | Logic           | Grade TEXT question - correct (whitespace)     | 1. User authenticated<br>2. Answer ID 206 exists<br>3. Question type = TEXT<br>4. answerContent = "  Paris  "<br>5. Correct = "Paris"<br>6. Question.point = 5 | 1. Send PUT to /api/v1/answers/grade/206<br>2. System trims whitespace from both<br>3. Normalizes to lowercase<br>4. Match found | answerId=206 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":206,"questionId":49,"answerContent":"  Paris  ","pointAchieved":5,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 5 (full points) | PENDING | AUTOMATION | TEXT normalization - whitespace |
| ID-018 | Logic           | Grade TEXT question - incorrect                | 1. User authenticated<br>2. Answer ID 207 exists<br>3. Question type = TEXT<br>4. answerContent = "London"<br>5. Correct = "Paris"<br>6. Question.point = 5 | 1. Send PUT to /api/v1/answers/grade/207<br>2. System normalizes both<br>3. No match found | answerId=207 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"id":207,"questionId":48,"answerContent":"London","pointAchieved":0,"sessionId":1,"improvement":null,"enrollmentId":78}} | Answer.point_achieved = 0 (zero points) | PENDING | AUTOMATION | TEXT grading - incorrect |
| **Error code**                                                                                                                                                                                                                                                                                                                                                      |
| ID-019 | Error code      | Answer not found - non-existent ID             | 1. User authenticated<br>2. Answer ID 99999 does NOT exist in DB | 1. Send PUT to /api/v1/answers/grade/99999<br>2. Service searches DB<br>3. Answer not found | answerId=99999 | N/A | 404 | {"statusCode":404,"error":"Resource Not Found","message":"Answer not found","data":null} | No DB changes | PENDING | AUTOMATION | ResourceNotFoundException |
| ID-020 | Error code      | Answer exists but question not loaded          | 1. User authenticated<br>2. Answer ID 300 exists but question FK broken or deleted | 1. Send PUT to /api/v1/answers/grade/300<br>2. Service tries to load question<br>3. Question null or missing | answerId=300 | N/A | 500 | {"statusCode":500,"error":"Internal server error","message":"Unexpected error during grading process","data":null} | No DB changes | PENDING | MANUAL | Data integrity issue |
| ID-021 | Error code      | Question choices not available                 | 1. User authenticated<br>2. Answer ID 301 exists<br>3. Question exists but has no choices | 1. Send PUT to /api/v1/answers/grade/301<br>2. Service tries to load question choices<br>3. Empty list returned | answerId=301 | N/A | 500 or 200 | {"statusCode":200,"message":"Answer graded successfully","data":{"pointAchieved":0,...}} | Answer.point_achieved = 0 (no correct choices to match) | PENDING | MANUAL | Edge case - no choices |
| ID-022 | Error code      | Database connection error during read          | 1. User authenticated<br>2. Database connection lost/unavailable | 1. Stop database or simulate connection failure<br>2. Send PUT to any valid answerId<br>3. Repository query fails | answerId=123 | N/A | 500 | {"statusCode":500,"error":"Internal server error","message":"Database connection error","data":null} | No DB changes | PENDING | MANUAL | Infrastructure failure |
| ID-023 | Error code      | Database connection error during update        | 1. User authenticated<br>2. Answer ID 123 exists<br>3. Grading successful<br>4. Database fails during save | 1. Send PUT to /api/v1/answers/grade/123<br>2. Grading completes<br>3. answerRepository.save() fails<br>4. Transaction rollback | answerId=123 | N/A | 500 | {"statusCode":500,"error":"Internal server error","message":"Unexpected error during grading process","data":null} | No DB changes (transaction rollback) | PENDING | MANUAL | Transaction safety |
| ID-024 | Error code      | Null answer content                            | 1. User authenticated<br>2. Answer ID 302 exists but ansContent is NULL | 1. Send PUT to /api/v1/answers/grade/302<br>2. Service calls trim() on null<br>3. NullPointerException | answerId=302 | N/A | 500 | {"statusCode":500,"error":"Internal server error","message":"Unexpected error during grading process","data":null} | No DB changes | PENDING | MANUAL | Data validation issue |
| ID-025 | Error code      | Concurrent grading of same answer              | 1. User authenticated<br>2. Answer ID 123 exists<br>3. Two requests sent simultaneously | 1. Send two PUT requests concurrently to /api/v1/answers/grade/123<br>2. Both try to update same answer<br>3. Verify transactional consistency | answerId=123 (both) | N/A | 200 (both) | Both requests complete, point_achieved updated correctly, no data corruption | Answer.point_achieved consistent (last update wins or optimistic locking) | PENDING | MANUAL | Concurrency safety |
| ID-026 | Error code      | Invalid question type (not gradable)           | 1. User authenticated<br>2. Answer ID 303 exists<br>3. Question type = LISTENING or WRITING or SPEAKING | 1. Send PUT to /api/v1/answers/grade/303<br>2. Service checks question type<br>3. Type not in (CHOICE, MULTIPLE, TEXT) | answerId=303 | N/A | 200 | {"statusCode":200,"error":null,"message":"Answer graded successfully","data":{"pointAchieved":0,...}} | Answer.point_achieved = 0 (no grading logic for this type) | PENDING | MANUAL | Non-gradable question type |
| **Format response**                                                                                                                                                                                                                                                                                                                                                 |
| ID-027 | Format response | Verify success response structure              | 1. User authenticated<br>2. Answer ID 1 exists<br>3. Valid grading scenario | 1. Send PUT to /api/v1/answers/grade/1<br>2. Verify response has correct structure<br>3. Check all ResAnswerDTO fields present | answerId=1 | N/A | 200 | Verify response contains: statusCode (Integer=200), error (null), message (String), data (Object with 7 fields: id, questionId, answerContent, pointAchieved, sessionId, improvement, enrollmentId) | Answer updated | PENDING | AUTOMATION | RestResponse structure |
| ID-028 | Format response | Verify error response structure                | 1. User authenticated<br>2. Answer ID 99999 does NOT exist | 1. Send PUT to /api/v1/answers/grade/99999<br>2. Trigger 404 error<br>3. Verify error response structure | answerId=99999 | N/A | 404 | Verify response contains: statusCode (Integer=404), error (String not null), message (String="Answer not found"), data (null) | No DB changes | PENDING | AUTOMATION | Error response consistency |
| ID-029 | Format response | Verify pointAchieved field updated             | 1. User authenticated<br>2. Answer ID 100 exists with pointAchieved=0<br>3. Correct answer scenario | 1. Check initial pointAchieved value (0)<br>2. Send PUT to /api/v1/answers/grade/100<br>3. Verify response data.pointAchieved changed<br>4. Verify DB updated | answerId=100 | N/A | 200 | Verify: data.pointAchieved is Integer > 0 (e.g., 10), matches question.point | Answer.point_achieved = 10 (updated from 0) | PENDING | AUTOMATION | Field update validation |
| ID-030 | Format response | Verify optional fields handle null correctly   | 1. User authenticated<br>2. Answer ID 101 exists<br>3. improvement = null, enrollmentId = null | 1. Send PUT to /api/v1/answers/grade/101<br>2. Verify response includes null fields<br>3. Check JSON serialization | answerId=101 | N/A | 200 | Verify: data.improvement = null, data.enrollmentId = null (not omitted from response) | Answer graded, optional fields remain null | PENDING | AUTOMATION | Null field serialization |

---

## Notes

1. All test cases are currently in PENDING status - ready for execution
2. Tests marked as AUTOMATION (20 TCs) can be automated using testing frameworks (JUnit, REST Assured, Mockito)
3. Tests marked as MANUAL (10 TCs) require manual setup, database manipulation, or infrastructure simulation
4. The grading algorithm supports three question types: CHOICE, MULTIPLE, TEXT
5. LISTENING, WRITING, SPEAKING question types are not auto-gradable (ID-026)
6. Partial credit is only available for MULTIPLE question type
7. Answer normalization rules: trim, lowercase, replace multiple spaces, remove trailing punctuation
8. JWT authentication is required for all requests
9. Database transactions ensure data consistency during concurrent operations
10. Test data should include answers for all three gradable question types

---

## Test Execution Summary

| Category         | Test IDs        | Total | Automation | Manual |
|------------------|-----------------|-------|------------|--------|
| **Validate**     | ID-001 ~ ID-010 | 10    | 10         | 0      |
| **Logic**        | ID-011 ~ ID-018 | 8     | 8          | 0      |
| **Error code**   | ID-019 ~ ID-026 | 8     | 1          | 7      |
| **Format response** | ID-027 ~ ID-030 | 4  | 4          | 0      |
| **TOTAL**        |                 | **30**| **23**     | **7**  |

---

## Grading Algorithm Reference

### CHOICE (Single Choice)
- **Input:** User selects one answer (e.g., "Paris")
- **Comparison:** Normalized string matching (case-insensitive, trimmed)
- **Output:** Full points if correct, zero if incorrect

### MULTIPLE (Multiple Choice)
- **Input:** User selects multiple answers (e.g., "A, B, C")
- **Parsing:** Split by comma, trim each element
- **Full Credit:** All correct choices selected AND no incorrect choices
- **Partial Credit:** `(correctCount / totalCorrectChoices) * questionPoints`, rounded
- **Output:** Integer points (0 to question.point)

### TEXT (Text Input)
- **Input:** User types free-form text
- **Normalization:**
  1. Trim whitespace: `"  Paris  "` → `"Paris"`
  2. Lowercase: `"PARIS"` → `"paris"`
  3. Multiple spaces: `"hello    world"` → `"hello world"`
  4. Trailing punctuation: `"Paris."` → `"Paris"`
- **Comparison:** Exact match after normalization
- **Output:** Full points if match, zero if no match

---

## Database Schema Reference

**Answer Table:**
- `id` (Long) - Primary key
- `question_id` (FK to Question)
- `user_id` (FK to User)
- `enrollment_id` (FK to Enrollment, nullable)
- `ansContent` (TEXT) - User's answer
- `point_achieved` (Integer) - **Updated by grading**
- `sessionId` (Long) - Attempt number
- `improvement` (TEXT, nullable) - Feedback

**Question Table:**
- `id` (Long) - Primary key
- `quesType` (Enum) - CHOICE, MULTIPLE, TEXT, LISTENING, WRITING, SPEAKING
- `point` (Integer) - Maximum points for question

**Question_Choice Table:**
- `id` (Long) - Primary key
- `question_id` (FK to Question)
- `choiceContent` (TEXT) - Choice text
- `choiceKey` (Boolean) - True if correct answer

---

**END OF TEST REPORT**
