# TEST REPORT - Grade Answer API (Placement Course) v2

**Format:** Consolidated Excel-friendly format
**Last Updated:** 2025-11-04
**Project:** Encybara - English Learning Platform
**Target Course:** English Placement Test
**Version:** 2.0.0 (Updated after script fixes)

---

## Summary Table

| No. | Sheet name          | API name                              | Total Testcase | Result |        |         |         | Execution |        |     | Status Testing | Progress | Remarks |
|-----|---------------------|---------------------------------------|----------------|--------|--------|---------|---------|-----------|--------|-----|----------------|----------|---------|
|     |                     |                                       |                | Passed | Failed | Blocked | Not run | Automation| Manual | N/A |                |          |         |
| 1   | GradeAnswer-Placement | PUT /api/v1/answers/grade/{answerId} | 42             | 0      | 0      | 42      | 0       | 42        | 0      | 0   | BLOCKED        | 0%       | Enrollment API issues |

---

## Project Information

| Project Name    | Encybara - English Learning Platform     |
|-----------------|------------------------------------------|
| | API Name        | PUT /api/v1/answers/grade/{answerId}     |
| Test Course     | English Placement Test                   |
| **Course ID**   | **1**                                    |
| **TEXT Lesson** | **ID: 1, "(PLACEMENT) Text - Reading"** |
| **CHOICE Lesson** | **ID: 2, "(PLACEMENT) Choice - Reading"** |
| Owner           | Generated based on req-6 task plan      |

---

## Test Statistics

| PASSED              | 0                                    |
|---------------------|--------------------------------------|
| FAILED              | 0                                    |
| **BLOCKED**         | **42**                               |
| NOT RUN             | 0                                    |
| AUTOMATION          | 42                                   |
| MANUAL              | 0                                    |
| **Number of Testcase** | **42**                            |

---

## Test Environment Status

### ✅ **WORKING COMPONENTS**
- **Authentication**: Successfully authenticates `user@example.com`
- **Course Discovery**: Successfully finds "English Placement Test" (Course ID: 1)
- **Lesson Discovery**: Successfully discovers all target lessons:
  - TEXT lesson: ID 1, "(PLACEMENT) Text - Reading" 
  - CHOICE lesson: ID 2, "(PLACEMENT) Choice - Reading"
- **Question Discovery**: Successfully discovers all questions:
  - TEXT Questions: 5 found (IDs: 1, 2, 3, 4, 5)
  - CHOICE Questions: 9 found (IDs: 6, 7, 8, 9, 10, 11, 12, 13, 14)

### ❌ **BLOCKED COMPONENTS**
- **Enrollment Creation**: HTTP 500 "The given id must not be null"
- **Answer Creation**: HTTP 500 (likely requires valid enrollment)
- **Grade Answer API Testing**: Cannot test due to missing answers

---

## Test Data - Placement Questions (VERIFIED)

### TEXT Questions (5 questions - IDs: 1-5)

| Question ID | Question Content | Correct Answer | Points |
|-------------|------------------|----------------|--------|
| Q1 (ID: 1) | Rewrite sentence using "Tired" | "Tired as he was, he agreed to help me with my homework" | 10 |
| Q2 (ID: 2) | Relative clauses | "The man whose daughter is fond of dancing works for my father's company" | 10 |
| Q3 (ID: 3) | Apologize structure | "Martin apologized to Angela for having damaged her car" | 10 |
| Q4 (ID: 4) | Passive voice transformation | "Maradona is thought to be the best football player in the 20th century" | 10 |
| Q5 (ID: 5) | Conditional sentence | "If it hadn't been for your help, I couldn't overcome the problem" | 10 |

### CHOICE Questions (9 questions - IDs: 6-14)

| Question ID | Question Content | Correct Answer | Incorrect Options | Points |
|-------------|------------------|----------------|-------------------|--------|
| Q1 (ID: 6) | OPPOSITE meaning of "critical" | "supportive" | intolerant, concerned, respectful | 10 |
| Q2 (ID: 7) | OPPOSITE meaning of "post" | "displace" | locate, dispatch, consign | 10 |
| Q3 (ID: 8) | CLOSEST meaning of "efficient" | "successful" | famous, observant, disorganised | 10 |
| Q4 (ID: 9) | CLOSEST meaning of "endeavoring" | "trying" | sustaining, requesting, offering | 10 |
| Q5 (ID: 10) | Grammar: correct form | "narrow-minded" | narrow-mind, narrowed-minded, narrow-minding | 10 |
| Q6 (ID: 11) | Dialogue completion | "OK, let me just check the diary." | Not at the moment. He can't be disturbed. / He's having lunch with a colleague. / Yes, of course. I'll get you through. | 10 |
| Q7 (ID: 12) | Verb tense | "went / have not been" | went / wasn't / went / was not / have gone / haven't been | 10 |
| Q8 (ID: 13) | Phrasal verb | "look up" | give up, get up, live up | 10 |
| Q9 (ID: 14) | Comparative structure | "The harder/ the better" | The more / the much / The more / the good / The harder / the good | 10 |

---

## Test Cases (UPDATED)

| ID     | Item            | Testcase Name                                  | Precondition                                    | Test Case                                           | params              | body                        | Status code | Body                                                      | Expected Database Result | Result  | Execution  | Note                        |
|--------|-----------------|------------------------------------------------|-------------------------------------------------|-----------------------------------------------------|---------------------|-----------------------------|--------------|---------------------------------------------------------|--------------------------|---------|------------|------------------------------|
| **Validate**                                                                                                                                                                                                                                                                                                                                                        |
| ID-001 | Validate        | Gọi API với method PUT (valid)                 | 1. User đã login<br>2. Có enrollment<br>3. Có answer ID hợp lệ | 1. Gọi API với method PUT<br>2. Provide valid answer ID | answerId={validId}  |                             | 200 OK       | {\"statusCode\":200,\"error\":null,\"message\":\"...\",\"data\":{\"id\":...,\"pointAchieved\":...}} | pointAchieved được cập nhật | BLOCKED | AUTOMATION | Enrollment API blocking |
| ID-002 | Validate        | Gọi API với method GET (invalid)               | 1. User đã login<br>2. Có answer ID               | 1. Gọi API với method GET thay vì PUT                     | answerId={validId}  |                             | 405 Method Not Allowed | {\"statusCode\":405,\"error\":\"Method Not Allowed\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-003 | Validate        | Gọi API với method POST (invalid)              | 1. User đã login<br>2. Có answer ID               | 1. Gọi API với method POST thay vì PUT                    | answerId={validId}  |                             | 405 Method Not Allowed | {\"statusCode\":405,\"error\":\"Method Not Allowed\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-004 | Validate        | Gọi API với method DELETE (invalid)            | 1. User đã login<br>2. Có answer ID               | 1. Gọi API với method DELETE thay vì PUT                  | answerId={validId}  |                             | 405 Method Not Allowed | {\"statusCode\":405,\"error\":\"Method Not Allowed\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-005 | Validate        | Request without authentication token           | 1. User NOT authenticated<br>2. Có answer ID      | 1. Gọi API không có Authorization header<br>2. Provide valid answer ID | answerId={validId}  |                             | 401 Unauthorized | {\"statusCode\":401,\"error\":\"Unauthorized\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-006 | Validate        | Request with invalid token                     | 1. User có invalid JWT token                      | 1. Gọi API với invalid/malformed token<br>2. Provide valid answer ID | answerId={validId}  |                             | 401 Unauthorized | {\"statusCode\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid JWT token\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-007 | Validate        | Request with expired token                     | 1. User có expired JWT token                      | 1. Gọi API với expired token<br>2. Provide valid answer ID | answerId={validId}  |                             | 401 Unauthorized | {\"statusCode\":401,\"error\":\"Unauthorized\",\"message\":\"JWT token has expired\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-008 | Validate        | Invalid answer ID (non-existent)               | 1. User đã login                                  | 1. Gọi API với answer ID không tồn tại (e.g., 999999)    | answerId=999999     |                             | 404 Not Found | {\"statusCode\":404,\"error\":\"Answer not found\",\"message\":\"Answer with id 999999 does not exist\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-009 | Validate        | Invalid answer ID (non-numeric)                | 1. User đã login                                  | 1. Gọi API với answer ID không phải số (e.g., \"abc\")   | answerId=abc        |                             | 400 Bad Request | {\"statusCode\":400,\"error\":\"Bad Request\",\"message\":\"Invalid answer ID format\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| ID-010 | Validate        | Grade answer không thuộc về user               | 1. User A đã login<br>2. Answer thuộc về User B   | 1. User A gọi API grade answer của User B                 | answerId={userB_answer} |                         | 403 Forbidden | {\"statusCode\":403,\"error\":\"Forbidden\",\"message\":\"You are not authorized to grade this answer\",...} |                          | BLOCKED | AUTOMATION | Cannot create answer to test |
| **Logic - TEXT (Question IDs: 1-5)**                                                                                                                                                                                                                                                                                                                                    |
| ID-011 | Logic-TEXT      | Grade TEXT Q1 (ID:1) - Correct answer          | 1. User enrolled in placement<br>2. Answer created với correct answer | 1. Grade answer với correct answer<br>2. Answer: \"Tired as he was, he agreed to help me with my homework\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | Script creates 3 answers per question |
| ID-012 | Logic-TEXT      | Grade TEXT Q1 (ID:1) - Incorrect answer        | 1. User enrolled in placement<br>2. Answer created với incorrect answer | 1. Grade answer với incorrect answer<br>2. Answer: \"This is an incorrect answer\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | Script creates 3 answers per question |
| ID-013 | Logic-TEXT      | Grade TEXT Q1 (ID:1) - Case insensitive        | 1. User enrolled in placement<br>2. Answer created với UPPERCASE | 1. Grade answer với uppercase correct answer<br>2. Answer: \"TIRED AS HE WAS, HE AGREED TO HELP ME WITH MY HOMEWORK\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | Normalization: lowercase |
| ID-014 | Logic-TEXT      | Grade TEXT Q2 (ID:2) - Correct answer          | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q2<br>2. Answer: \"The man whose daughter is fond of dancing works for my father's company\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q2 correct |
| ID-015 | Logic-TEXT      | Grade TEXT Q2 (ID:2) - Incorrect answer        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q2<br>2. Answer: \"This is an incorrect answer\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | TEXT Q2 incorrect |
| ID-016 | Logic-TEXT      | Grade TEXT Q2 (ID:2) - Case insensitive        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q2<br>2. Answer: \"THE MAN WHOSE DAUGHTER IS FOND OF DANCING WORKS FOR MY FATHER'S COMPANY\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q2 normalization |
| ID-017 | Logic-TEXT      | Grade TEXT Q3 (ID:3) - Correct answer          | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q3<br>2. Answer: \"Martin apologized to Angela for having damaged her car\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q3 correct |
| ID-018 | Logic-TEXT      | Grade TEXT Q3 (ID:3) - Incorrect answer        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q3<br>2. Answer: \"This is an incorrect answer\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | TEXT Q3 incorrect |
| ID-019 | Logic-TEXT      | Grade TEXT Q3 (ID:3) - Case insensitive        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q3<br>2. Answer: \"MARTIN APOLOGIZED TO ANGELA FOR HAVING DAMAGED HER CAR\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q3 normalization |
| ID-020 | Logic-TEXT      | Grade TEXT Q4 (ID:4) - Correct answer          | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q4<br>2. Answer: \"Maradona is thought to be the best football player in the 20th century\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q4 correct |
| ID-021 | Logic-TEXT      | Grade TEXT Q4 (ID:4) - Incorrect answer        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q4<br>2. Answer: \"This is an incorrect answer\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | TEXT Q4 incorrect |
| ID-022 | Logic-TEXT      | Grade TEXT Q4 (ID:4) - Case insensitive        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q4<br>2. Answer: \"MARADONA IS THOUGHT TO BE THE BEST FOOTBALL PLAYER IN THE 20TH CENTURY\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q4 normalization |
| ID-023 | Logic-TEXT      | Grade TEXT Q5 (ID:5) - Correct answer          | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q5<br>2. Answer: \"If it hadn't been for your help, I couldn't overcome the problem\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q5 correct |
| ID-024 | Logic-TEXT      | Grade TEXT Q5 (ID:5) - Incorrect answer        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q5<br>2. Answer: \"This is an incorrect answer\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | TEXT Q5 incorrect |
| ID-025 | Logic-TEXT      | Grade TEXT Q5 (ID:5) - Case insensitive        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q5<br>2. Answer: \"IF IT HADN'T BEEN FOR YOUR HELP, I COULDN'T OVERCOME THE PROBLEM\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | TEXT Q5 normalization |
| **Logic - CHOICE (Question IDs: 6-14)**                                                                                                                                                                                                                                                                                                                                  |
| ID-026 | Logic-CHOICE    | Grade CHOICE Q1 (ID:6) - Correct (supportive)  | 1. User enrolled in placement<br>2. Answer created với correct choice | 1. Grade CHOICE answer Q1<br>2. Answer: \"supportive\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q1 correct |
| ID-027 | Logic-CHOICE    | Grade CHOICE Q1 (ID:6) - Incorrect (intolerant) | 1. User enrolled in placement<br>2. Answer created với incorrect choice | 1. Grade CHOICE answer Q1<br>2. Answer: \"intolerant\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | CHOICE Q1 incorrect |
| ID-028 | Logic-CHOICE    | Grade CHOICE Q1 (ID:6) - Case insensitive      | 1. User enrolled in placement<br>2. Answer created với uppercase | 1. Grade CHOICE answer với uppercase<br>2. Answer: \"SUPPORTIVE\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | Normalization: case insensitive |
| ID-029 | Logic-CHOICE    | Grade CHOICE Q2 (ID:7) - Correct (displace)    | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q2<br>2. Answer: \"displace\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q2 correct |
| ID-030 | Logic-CHOICE    | Grade CHOICE Q2 (ID:7) - Incorrect (locate)    | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q2<br>2. Answer: \"locate\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | CHOICE Q2 incorrect |
| ID-031 | Logic-CHOICE    | Grade CHOICE Q2 (ID:7) - Case insensitive      | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q2<br>2. Answer: \"DISPLACE\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q2 normalization |
| ID-032 | Logic-CHOICE    | Grade CHOICE Q3 (ID:8) - Correct (successful)  | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q3<br>2. Answer: \"successful\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q3 correct |
| ID-033 | Logic-CHOICE    | Grade CHOICE Q3 (ID:8) - Incorrect (disorganised) | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q3<br>2. Answer: \"disorganised\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | CHOICE Q3 incorrect |
| ID-034 | Logic-CHOICE    | Grade CHOICE Q3 (ID:8) - Case insensitive      | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q3<br>2. Answer: \"SUCCESSFUL\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q3 normalization |
| ID-035 | Logic-CHOICE    | Grade CHOICE Q4 (ID:9) - Correct (trying)      | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q4<br>2. Answer: \"trying\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q4 correct |
| ID-036 | Logic-CHOICE    | Grade CHOICE Q4 (ID:9) - Incorrect (requesting) | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q4<br>2. Answer: \"requesting\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | CHOICE Q4 incorrect |
| ID-037 | Logic-CHOICE    | Grade CHOICE Q4 (ID:9) - Case insensitive      | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q4<br>2. Answer: \"TRYING\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q4 normalization |
| ID-038 | Logic-CHOICE    | Grade CHOICE Q5 (ID:10) - Correct (narrow-minded) | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q5<br>2. Answer: \"narrow-minded\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q5 correct |
| ID-039 | Logic-CHOICE    | Grade CHOICE Q5 (ID:10) - Incorrect (narrow-mind) | 1. User enrolled in placement<br>2. Answer created với incorrect form | 1. Grade CHOICE answer Q5<br>2. Answer: \"narrow-mind\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | BLOCKED | AUTOMATION | CHOICE Q5 incorrect (grammar) |
| ID-040 | Logic-CHOICE    | Grade CHOICE Q5 (ID:10) - Case insensitive     | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q5<br>2. Answer: \"NARROW-MINDED\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q5 normalization |
| ID-041 | Logic-CHOICE    | Grade CHOICE Q8 (ID:13) - Correct (look up)    | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q8<br>2. Answer: \"look up\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q8 correct (phrasal verb) |
| ID-042 | Logic-CHOICE    | Grade CHOICE Q9 (ID:14) - Correct (The harder/better) | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q9<br>2. Answer: \"The harder/ the better\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | BLOCKED | AUTOMATION | CHOICE Q9 correct (comparative) |

---

## Updated Script Analysis

### ✅ **Script Fixes Applied**
1. **JSON Parsing Fix**: Updated to extract `lessonIds` array from course API response
2. **Question Discovery Fix**: Updated to extract `questionIds` array from lesson API responses  
3. **Enhanced Debugging**: Added detailed API response logging
4. **Cross-platform Support**: Fixed line ending issues for Git Bash on Windows
5. **Error Handling**: Graceful handling of enrollment creation failures

### 🚫 **Current Blockers** 
1. **Enrollment Creation API**: Returns HTTP 500 "The given id must not be null"
2. **Answer Creation API**: Returns HTTP 500 (likely requires valid enrollment)
3. **No Valid Answers**: Cannot test Grade Answer API without valid answers

### 📊 **Test Coverage Update**
- **Total Test Cases**: Increased from 30 to 42
- **TEXT Questions**: 15 test cases (3 per question: correct, incorrect, case insensitive)
- **CHOICE Questions**: 27 test cases (3 per question for major questions)
- **Validation**: 10 test cases (unchanged)

---

## Notes v2

1. **BLOCKED Status**: All test cases marked as BLOCKED due to enrollment API issues
2. **Question IDs Verified**: Script successfully discovered actual question IDs from placement course
3. **Enhanced Test Coverage**: Script creates 3 test cases per question (correct, incorrect, normalization)
4. **API Discovery Working**: Course (ID:1), Lessons (ID:1,2), and Questions (1-5, 6-14) successfully discovered
5. **Script Ready**: Once enrollment/answer creation APIs are fixed, script can execute all 42 test cases
6. **Backend URL**: Script configured for http://18.136.223.96:8080
7. **Authentication Working**: Successfully authenticates user@example.com
8. **Report Generation**: Script generates timestamped test reports even when blocked

---

## Prerequisite Issues to Resolve

1. **Fix Enrollment API**: Resolve HTTP 500 "The given id must not be null" error
2. **Verify Answer Creation API**: Ensure it can create answers without enrollment or fix enrollment requirement
3. **Alternative Testing**: Consider manual answer creation via database or different API endpoint

---

## Test Execution Summary v2

| Category           | Test IDs        | Total | Automation | Manual | Description | Status |
|-------------------|-----------------|-------|------------|--------|-------------|--------|
| **Validate**      | ID-001 ~ ID-010 | 10    | 10         | 0      | HTTP methods, auth, path validation | BLOCKED |
| **Logic - TEXT**  | ID-011 ~ ID-025 | 15    | 15         | 0      | TEXT question grading with normalization | BLOCKED |
| **Logic - CHOICE** | ID-026 ~ ID-042 | 17    | 17         | 0      | CHOICE question grading with case handling | BLOCKED |
| **TOTAL**         |                 | **42**| **42**     | **0**  | | **BLOCKED** |

---

## Related Documents

- **API Document:** `documents/output/API_Document_GradeAnswer.md`
- **Test Script:** `backend-service/test-grade-answer.sh` (FIXED)
- **README:** `backend-service/TEST_GRADE_ANSWER_README.md`
- **Plan Task:** `documents/req-6.md`
- **Previous Version:** `documents/output/Testcase_API_GradeAnswer_Placement.md`

---

**Last Updated:** 2025-11-04  
**Version:** 2.0.0  
**Author:** Generated based on req-6 task plan + script analysis  
**Status:** BLOCKED - Enrollment API issues prevent execution