# TEST REPORT - Grade Answer API (Placement Course)

**Format:** Consolidated Excel-friendly format
**Last Updated:** 2025-11-04
**Project:** Encybara - English Learning Platform
**Target Course:** English Placement Test

---

## Summary Table

| No. | Sheet name          | API name                              | Total Testcase | Result |        |         |         | Execution |        |     | Status Testing | Progress | Remarks |
|-----|---------------------|---------------------------------------|----------------|--------|--------|---------|---------|-----------|--------|-----|----------------|----------|---------|
|     |                     |                                       |                | Passed | Failed | Blocked | Not run | Automation| Manual | N/A |                |          |         |
| 1   | GradeAnswer-Placement | PUT /api/v1/answers/grade/{answerId} | 30             | 0      | 0      | 0       | 30      | 30        | 0      | 0   |                |          |         |

---

## Project Information

| Project Name    | Encybara - English Learning Platform     |
|-----------------|------------------------------------------|
| API Name        | PUT /api/v1/answers/grade/{answerId}     |
| Test Course     | English Placement Test                   |
| Owner           |                                          |

---

## Test Statistics

| PASSED              | 0                                    |
|---------------------|--------------------------------------|
| FAILED              | 0                                    |
| PENDING             | 30                                   |
| NOT RUN             | 0                                    |
| AUTOMATION          | 30                                   |
| MANUAL              | 0                                    |
| **Number of Testcase** | **30**                            |

---

## Test Data - Placement Questions

### TEXT Questions (5 questions)

| Question # | Question Content | Correct Answer | Points |
|------------|------------------|----------------|--------|
| Q1 | Rewrite sentence using "Tired" | "Tired as he was, he agreed to help me with my homework" | 10 |
| Q2 | Relative clauses | "The man whose daughter is fond of dancing works for my father's company" | 10 |
| Q3 | Apologize structure | "Martin apologized to Angela for having damaged her car" | 10 |
| Q4 | Passive voice transformation | "Maradona is thought to be the best football player in the 20th century" | 10 |
| Q5 | Conditional sentence | "If it hadn't been for your help, I couldn't overcome the problem" | 10 |

### CHOICE Questions (9 questions)

| Question # | Question Content | Correct Answer | Incorrect Options | Points |
|------------|------------------|----------------|-------------------|--------|
| Q1 | OPPOSITE meaning of "critical" | "supportive" | intolerant, concerned, respectful | 10 |
| Q2 | OPPOSITE meaning of "post" | "displace" | locate, dispatch, consign | 10 |
| Q3 | CLOSEST meaning of "efficient" | "successful" | famous, observant, disorganised | 10 |
| Q4 | CLOSEST meaning of "endeavoring" | "trying" | sustaining, requesting, offering | 10 |
| Q5 | Grammar: correct form | "narrow-minded" | narrow-mind, narrowed-minded, narrow-minding | 10 |
| Q6 | Dialogue completion | "OK, let me just check the diary." | Not at the moment. He can't be disturbed. / He's having lunch with a colleague. / Yes, of course. I'll get you through. | 10 |
| Q7 | Verb tense | "went / have not been" | went / wasn't / went / was not / have gone / haven't been | 10 |
| Q8 | Phrasal verb | "look up" | give up, get up, live up | 10 |
| Q9 | Comparative structure | "The harder/ the better" | The more / the much / The more / the good / The harder / the good | 10 |

---

## Test Cases

| ID     | Item            | Testcase Name                                  | Precondition                                    | Test Case                                           | params              | body                        | Status code | Body                                                      | Expected Database Result | Result  | Execution  | Note                        |
|--------|-----------------|------------------------------------------------|-------------------------------------------------|-----------------------------------------------------|---------------------|-----------------------------|--------------|---------------------------------------------------------|--------------------------|---------|------------|------------------------------|
| **Validate**                                                                                                                                                                                                                                                                                                                                                        |
| ID-001 | Validate        | Gọi API với method PUT (valid)                 | 1. User đã login<br>2. Có enrollment<br>3. Có answer ID hợp lệ | 1. Gọi API với method PUT<br>2. Provide valid answer ID | answerId={validId}  |                             | 200 OK       | {\"statusCode\":200,\"error\":null,\"message\":\"...\",\"data\":{\"id\":...,\"pointAchieved\":...}} | pointAchieved được cập nhật | PENDING | AUTOMATION | Happy path |\n| ID-002 | Validate        | Gọi API với method GET (invalid)               | 1. User đã login<br>2. Có answer ID               | 1. Gọi API với method GET thay vì PUT                     | answerId={validId}  |                             | 405 Method Not Allowed | {\"statusCode\":405,\"error\":\"Method Not Allowed\",...} |                          | PENDING | AUTOMATION | HTTP method validation |\n| ID-003 | Validate        | Gọi API với method POST (invalid)              | 1. User đã login<br>2. Có answer ID               | 1. Gọi API với method POST thay vì PUT                    | answerId={validId}  |                             | 405 Method Not Allowed | {\"statusCode\":405,\"error\":\"Method Not Allowed\",...} |                          | PENDING | AUTOMATION | HTTP method validation |\n| ID-004 | Validate        | Gọi API với method DELETE (invalid)            | 1. User đã login<br>2. Có answer ID               | 1. Gọi API với method DELETE thay vì PUT                  | answerId={validId}  |                             | 405 Method Not Allowed | {\"statusCode\":405,\"error\":\"Method Not Allowed\",...} |                          | PENDING | AUTOMATION | HTTP method validation |\n| ID-005 | Validate        | Request without authentication token           | 1. User NOT authenticated<br>2. Có answer ID      | 1. Gọi API không có Authorization header<br>2. Provide valid answer ID | answerId={validId}  |                             | 401 Unauthorized | {\"statusCode\":401,\"error\":\"Unauthorized\",...} |                          | PENDING | AUTOMATION | Spring Security auth check |\n| ID-006 | Validate        | Request with invalid token                     | 1. User có invalid JWT token                      | 1. Gọi API với invalid/malformed token<br>2. Provide valid answer ID | answerId={validId}  |                             | 401 Unauthorized | {\"statusCode\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid JWT token\",...} |                          | PENDING | AUTOMATION | JWT validation |\n| ID-007 | Validate        | Request with expired token                     | 1. User có expired JWT token                      | 1. Gọi API với expired token<br>2. Provide valid answer ID | answerId={validId}  |                             | 401 Unauthorized | {\"statusCode\":401,\"error\":\"Unauthorized\",\"message\":\"JWT token has expired\",...} |                          | PENDING | AUTOMATION | Token expiration check |\n| ID-008 | Validate        | Invalid answer ID (non-existent)               | 1. User đã login                                  | 1. Gọi API với answer ID không tồn tại (e.g., 999999)    | answerId=999999     |                             | 404 Not Found | {\"statusCode\":404,\"error\":\"Answer not found\",\"message\":\"Answer with id 999999 does not exist\",...} |                          | PENDING | AUTOMATION | Resource not found validation |\n| ID-009 | Validate        | Invalid answer ID (non-numeric)                | 1. User đã login                                  | 1. Gọi API với answer ID không phải số (e.g., \"abc\")   | answerId=abc        |                             | 400 Bad Request | {\"statusCode\":400,\"error\":\"Bad Request\",\"message\":\"Invalid answer ID format\",...} |                          | PENDING | AUTOMATION | Path variable validation |\n| ID-010 | Validate        | Grade answer không thuộc về user               | 1. User A đã login<br>2. Answer thuộc về User B   | 1. User A gọi API grade answer của User B                 | answerId={userB_answer} |                         | 403 Forbidden | {\"statusCode\":403,\"error\":\"Forbidden\",\"message\":\"You are not authorized to grade this answer\",...} |                          | PENDING | AUTOMATION | Authorization check |\n| **Logic - TEXT**                                                                                                                                                                                                                                                                                                                                                    |\n| ID-011 | Logic-TEXT      | Grade TEXT Q1 - Correct answer                 | 1. User enrolled in placement<br>2. Answer created với correct answer | 1. Grade answer với correct answer<br>2. Answer: \"Tired as he was, he agreed to help me with my homework\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | Exact match |\n| ID-012 | Logic-TEXT      | Grade TEXT Q1 - Incorrect answer               | 1. User enrolled in placement<br>2. Answer created với incorrect answer | 1. Grade answer với incorrect answer<br>2. Answer: \"He was very tired but helped me\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | PENDING | AUTOMATION | No match |\n| ID-013 | Logic-TEXT      | Grade TEXT Q1 - Case insensitive               | 1. User enrolled in placement<br>2. Answer created với UPPERCASE | 1. Grade answer với uppercase correct answer<br>2. Answer: \"TIRED AS HE WAS, HE AGREED TO HELP ME WITH MY HOMEWORK\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | Normalization: lowercase |\n| ID-014 | Logic-TEXT      | Grade TEXT Q1 - Whitespace handling            | 1. User enrolled in placement<br>2. Answer created với extra spaces | 1. Grade answer với extra whitespace<br>2. Answer: \"  Tired as  he  was, he agreed to help me with my homework  \" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | Normalization: trim + compress spaces |\n| ID-015 | Logic-TEXT      | Grade TEXT Q2 - Correct (Relative clauses)     | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q2<br>2. Answer: \"The man whose daughter is fond of dancing works for my father's company\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | TEXT Q2 correct |\n| ID-016 | Logic-TEXT      | Grade TEXT Q3 - Correct (Apologize)            | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q3<br>2. Answer: \"Martin apologized to Angela for having damaged her car\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | TEXT Q3 correct |\n| ID-017 | Logic-TEXT      | Grade TEXT Q4 - Correct (Passive voice)        | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q4<br>2. Answer: \"Maradona is thought to be the best football player in the 20th century\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | TEXT Q4 correct |\n| ID-018 | Logic-TEXT      | Grade TEXT Q5 - Correct (Conditional)          | 1. User enrolled in placement<br>2. Answer created | 1. Grade answer Q5<br>2. Answer: \"If it hadn't been for your help, I couldn't overcome the problem\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | TEXT Q5 correct |\n| ID-019 | Logic-TEXT      | Grade TEXT Q5 - Punctuation handling           | 1. User enrolled in placement<br>2. Answer created với trailing punctuation | 1. Grade answer với trailing punctuation<br>2. Answer: \"If it hadn't been for your help, I couldn't overcome the problem...\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | Normalization: remove trailing punctuation |\n| ID-020 | Logic-TEXT      | Grade TEXT Q2 - Incorrect (Partial match)      | 1. User enrolled in placement<br>2. Answer created với partial answer | 1. Grade answer với incomplete sentence<br>2. Answer: \"The man whose daughter is fond of dancing\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | PENDING | AUTOMATION | Binary grading: no partial credit |\n| **Logic - CHOICE**                                                                                                                                                                                                                                                                                                                                                  |\n| ID-021 | Logic-CHOICE    | Grade CHOICE Q1 - Correct (supportive)         | 1. User enrolled in placement<br>2. Answer created với correct choice | 1. Grade CHOICE answer Q1<br>2. Answer: \"supportive\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q1 correct |\n| ID-022 | Logic-CHOICE    | Grade CHOICE Q1 - Incorrect (intolerant)       | 1. User enrolled in placement<br>2. Answer created với incorrect choice | 1. Grade CHOICE answer Q1<br>2. Answer: \"intolerant\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | PENDING | AUTOMATION | CHOICE Q1 incorrect |\n| ID-023 | Logic-CHOICE    | Grade CHOICE Q1 - Case insensitive             | 1. User enrolled in placement<br>2. Answer created với uppercase | 1. Grade CHOICE answer với uppercase<br>2. Answer: \"SUPPORTIVE\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | Normalization: case insensitive |\n| ID-024 | Logic-CHOICE    | Grade CHOICE Q2 - Correct (displace)           | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q2<br>2. Answer: \"displace\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q2 correct |\n| ID-025 | Logic-CHOICE    | Grade CHOICE Q3 - Correct (successful)         | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q3<br>2. Answer: \"successful\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q3 correct |\n| ID-026 | Logic-CHOICE    | Grade CHOICE Q5 - Correct (narrow-minded)      | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q5<br>2. Answer: \"narrow-minded\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q5 correct |\n| ID-027 | Logic-CHOICE    | Grade CHOICE Q5 - Incorrect (narrow-mind)      | 1. User enrolled in placement<br>2. Answer created với incorrect form | 1. Grade CHOICE answer Q5<br>2. Answer: \"narrow-mind\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":0,...}}  | pointAchieved = 0        | PENDING | AUTOMATION | CHOICE Q5 incorrect (grammar) |\n| ID-028 | Logic-CHOICE    | Grade CHOICE Q7 - Correct (went/have not been) | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q7<br>2. Answer: \"went / have not been\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q7 correct (verb tense) |\n| ID-029 | Logic-CHOICE    | Grade CHOICE Q8 - Correct (look up)            | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q8<br>2. Answer: \"look up\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q8 correct (phrasal verb) |\n| ID-030 | Logic-CHOICE    | Grade CHOICE Q9 - Correct (The harder/better)  | 1. User enrolled in placement<br>2. Answer created | 1. Grade CHOICE answer Q9<br>2. Answer: \"The harder/ the better\" | answerId={id}       |                             | 200 OK       | {\"statusCode\":200,\"data\":{\"pointAchieved\":10,...}} | pointAchieved = 10       | PENDING | AUTOMATION | CHOICE Q9 correct (comparative) |

---

## Notes

1. All test cases are currently in PENDING status - ready for automated execution
2. All 30 test cases are marked as AUTOMATION - can be run via test-grade-answer.sh script
3. Test data uses actual questions from English Placement Test course
4. TEXT questions test normalization: case insensitivity, whitespace handling, punctuation removal
5. CHOICE questions test case insensitivity and exact matching after normalization
6. Binary grading: correct answers get 10 points, incorrect answers get 0 points
7. No partial credit for TEXT questions - must match exactly after normalization
8. Authentication and authorization tests (ID-005 to ID-010) validate security controls
9. HTTP method tests (ID-002 to ID-004) validate REST API constraints
10. Test script creates multiple answer instances per question to test various scenarios

---

## Grading Algorithm

### TEXT Question Grading
```
1. Normalize user answer:
   - Trim whitespace
   - Convert to lowercase
   - Replace multiple spaces with single space
   - Remove trailing punctuation (. ! ?)

2. Normalize correct answer (same steps)

3. Compare:
   - If normalized answers match exactly → 10 points
   - Otherwise → 0 points
```

### CHOICE Question Grading
```
1. Normalize user answer:
   - Trim whitespace
   - Convert to lowercase
   - Remove trailing punctuation

2. Normalize correct answer (same steps)

3. Compare:
   - If normalized answers match exactly → 10 points
   - Otherwise → 0 points
```

---

## Test Execution Summary

| Category         | Test IDs        | Total | Automation | Manual | Description |
|------------------|-----------------|-------|------------|--------|-------------|
| **Validate**     | ID-001 ~ ID-010 | 10    | 10         | 0      | HTTP methods, auth, path validation |
| **Logic - TEXT** | ID-011 ~ ID-020 | 10    | 10         | 0      | TEXT question grading with normalization |
| **Logic - CHOICE**| ID-021 ~ ID-030 | 10    | 10         | 0      | CHOICE question grading with case handling |
| **TOTAL**        |                 | **30**| **30**     | **0**  | |

---

## Related Documents

- **API Document:** `documents/output/API_Document_GradeAnswer.md`
- **Test Script:** `backend-service/test-grade-answer.sh`
- **README:** `backend-service/TEST_GRADE_ANSWER_README.md`
- **Plan Task:** `documents/req-6.md`

---

**Last Updated:** 2025-11-04
**Version:** 1.0.0
**Author:** Generated based on req-6 task plan
