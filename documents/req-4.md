# Task req-4: API Document & Testcase - GradeAnswer API

**Created:** 2025-10-29
**Status:** Plan Ready
**Target API:** `gradeAnswer` in `AnswerController`

---

## 📋 Task Overview

Create comprehensive API documentation and test cases for the **Grade Answer API** following the consolidated SAMPLE format established in req-3.

**Deliverables:**
1. `documents/output/API_Document_GradeAnswer.md` - Consolidated API documentation
2. `documents/output/Testcase_API_GradeAnswer.md` - Consolidated test cases (30 test cases)

---

## 🎯 Investigation Results

### API Information (from AnswerController.java)

**Endpoint Details:**
- **HTTP Method:** PUT
- **Path:** `/api/v1/answers/grade/{answerId}`
- **Path Parameter:** `answerId` (Long, required)
- **Request Body:** None
- **Authentication:** Required (JWT Bearer token)

**Controller Method (Line 68-77):**
```java
@PutMapping("/grade/{answerId}")
public ResponseEntity<RestResponse<ResAnswerDTO>> gradeAnswer(
        @PathVariable("answerId") Long answerId) {
    ResAnswerDTO gradedAnswer = answerService.gradeAnswer(answerId);
    RestResponse<ResAnswerDTO> response = new RestResponse<>();
    response.setStatusCode(200);
    response.setMessage("Answer graded successfully");
    response.setData(gradedAnswer);
    return ResponseEntity.ok(response);
}
```

---

### Business Logic (from AnswerService.java)

**Grading Algorithm (Lines 105-162):**

1. **Retrieve Answer and Question Data**
   - Find answer by answerId
   - Get associated question and question choices
   - Extract user's answer content

2. **Grade Based on Question Type:**

   **MULTIPLE (Multiple Choice):**
   - Compare user's selected choices with all correct choices
   - **Full points:** If all correct choices selected AND no incorrect choices
   - **Partial points:** Calculate based on ratio of correct selections
   - Formula: `partialPoint = (correctCount / totalCorrect) * questionPoint`

   **CHOICE (Single Choice):**
   - Compare user's choice with correct choice
   - **Full points:** If correct
   - **Zero points:** If incorrect

   **TEXT (Text Input):**
   - Compare user's text with correct answer (normalized)
   - Normalization: trim, lowercase, remove extra spaces, remove trailing punctuation
   - **Full points:** If matches correct answer
   - **Zero points:** If doesn't match

3. **Update and Return**
   - Update `point_achieved` field in database
   - Return ResAnswerDTO with updated data

---

### Response Structure (from ResAnswerDTO.java)

**RestResponse Wrapper:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Answer graded successfully",
  "data": { ... }
}
```

**ResAnswerDTO Fields (7 fields):**
| No | Field | Type | Mandatory | Description |
|----|-------|------|-----------|-------------|
| 1 | id | Long | Yes | Answer ID |
| 2 | questionId | Long | Yes | Associated question ID |
| 3 | answerContent | String | Yes | User's answer content |
| 4 | pointAchieved | Integer | Yes | Points awarded after grading |
| 5 | sessionId | Long | Yes | Session identifier for this attempt |
| 6 | improvement | String | No | Improvement notes/feedback |
| 7 | enrollmentId | Long | No | Associated enrollment ID |

---

### Error Scenarios

**From Controller & Service Analysis:**

| Status Code | Scenario | Error Message |
|-------------|----------|---------------|
| 200 | Success | Answer graded successfully |
| 400 | Invalid answerId format | Invalid path variable format |
| 401 | No authentication token | Unauthorized |
| 401 | Invalid/expired token | Invalid or expired token |
| 404 | Answer not found | Answer not found |
| 500 | Database error | Internal server error |
| 500 | Unexpected error during grading | Internal server error |

---

## 📝 Output Requirements

### 1. API Document Structure

**Format:** Consolidated (SAMPLE-compliant)
- **Page 1:** Summary table (1 API)
- **Page 2+:** Full API specification

**Required Sections:**
1. Summary Table
2. API Overview
3. Request Details
   - Method: PUT
   - Endpoint: /api/v1/answers/grade/{answerId}
4. Header Table
   - Authorization: Bearer {token}
   - Content-Type: application/json
5. Path Parameters Table
   - answerId: Long, required
6. Request Body
   - N/A (no body required)
7. Response Table
   - Main fields: statusCode, error, message, data
   - Data fields: 7 ResAnswerDTO fields
8. Response Examples
   - Success (CHOICE question)
   - Success (MULTIPLE question - full points)
   - Success (MULTIPLE question - partial points)
   - Success (TEXT question)
   - Error (404 - Answer not found)
9. Grading Logic Explanation
   - MULTIPLE: Partial points calculation
   - CHOICE: Binary grading
   - TEXT: Normalized comparison
10. Error Code Table (7 codes)
11. Additional Information
    - Supported question types
    - Normalization rules
    - Validation requirements

---

### 2. Test Case Structure

**Format:** Consolidated (SAMPLE-compliant)
- **Page 1:** TEST REPORT summary
- **Page 2:** Project information + statistics
- **Page 3+:** All test cases in single table

**Test Coverage:** 30 test cases

**Category Distribution:**

| Category | Test Case IDs | Count | Description |
|----------|---------------|-------|-------------|
| **Validate** | ID-001 ~ ID-010 | 10 | Input validation (method, path param, auth) |
| **Logic** | ID-011 ~ ID-018 | 8 | Grading logic (CHOICE, MULTIPLE, TEXT) |
| **Error code** | ID-019 ~ ID-026 | 8 | Error handling scenarios |
| **Format response** | ID-027 ~ ID-030 | 4 | Response structure validation |
| **TOTAL** | | **30** | |

**Execution Type:**
- AUTOMATION: 20 test cases
- MANUAL: 10 test cases

---

## 🔍 Detailed Test Case Plan

### Validate (10 TCs)

| ID | Test Case Name | Purpose |
|----|----------------|---------|
| ID-001 | Valid PUT method | Verify PUT is accepted |
| ID-002 | Invalid GET method | Verify GET is rejected |
| ID-003 | Invalid POST method | Verify POST is rejected |
| ID-004 | Invalid DELETE method | Verify DELETE is rejected |
| ID-005 | Valid answerId format | Test with valid Long ID |
| ID-006 | Invalid answerId format (text) | Test with non-numeric ID |
| ID-007 | Invalid answerId format (negative) | Test with negative ID |
| ID-008 | Missing authentication token | Verify 401 response |
| ID-009 | Invalid authentication token | Verify 401 response |
| ID-010 | Expired authentication token | Verify 401 response |

### Logic (8 TCs)

| ID | Test Case Name | Purpose |
|----|----------------|---------|
| ID-011 | Grade CHOICE - correct answer | Verify full points awarded |
| ID-012 | Grade CHOICE - incorrect answer | Verify zero points awarded |
| ID-013 | Grade MULTIPLE - all correct | Verify full points awarded |
| ID-014 | Grade MULTIPLE - partial correct | Verify partial points calculation |
| ID-015 | Grade MULTIPLE - all incorrect | Verify zero points awarded |
| ID-016 | Grade TEXT - correct (case insensitive) | Verify normalization works |
| ID-017 | Grade TEXT - correct (whitespace) | Verify trimming works |
| ID-018 | Grade TEXT - incorrect | Verify zero points awarded |

### Error code (8 TCs)

| ID | Test Case Name | Purpose |
|----|----------------|---------|
| ID-019 | Answer not found | Verify 404 error |
| ID-020 | Non-existent answerId | Verify 404 error |
| ID-021 | Database connection error | Verify 500 error |
| ID-022 | Invalid path variable | Verify 400 error |
| ID-023 | Question not loaded properly | Verify error handling |
| ID-024 | Choices not loaded properly | Verify error handling |
| ID-025 | Update fails after grading | Verify transaction rollback |
| ID-026 | Concurrent grading requests | Verify consistency |

### Format response (4 TCs)

| ID | Test Case Name | Purpose |
|----|----------------|---------|
| ID-027 | Success response structure | Verify all 7 ResAnswerDTO fields |
| ID-028 | Error response structure | Verify error fields |
| ID-029 | pointAchieved update validation | Verify field is updated |
| ID-030 | Null handling for optional fields | Verify improvement, enrollmentId |

---

## 📊 Success Criteria

### API Document:
- ✅ Follows SAMPLE consolidated format
- ✅ Page 1 summary table with 1 API
- ✅ Page 2+ complete API specification
- ✅ All 7 ResAnswerDTO fields documented
- ✅ Grading logic explained (3 question types)
- ✅ All 7 error codes documented
- ✅ Excel-ready tables
- ✅ Response examples for all question types

### Testcase:
- ✅ Follows SAMPLE format (TEST REPORT + single table)
- ✅ 30 test cases total
- ✅ Grouped correctly (Validate 10, Logic 8, Error 8, Format 4)
- ✅ All columns filled (13 columns)
- ✅ Response examples accurate (based on ResAnswerDTO)
- ✅ Execution types assigned (20 AUTO, 10 MANUAL)
- ✅ Excel-ready format
- ✅ Sequential rows like SAMPLE

### Investigation Accuracy:
- ✅ All ResAnswerDTO fields documented (7 fields)
- ✅ Grading algorithms explained (MULTIPLE, CHOICE, TEXT)
- ✅ Error handling scenarios accurate
- ✅ Normalization rules documented
- ✅ Partial points calculation explained
- ✅ No information loss from investigation

---

## 🔧 Technical Details

### Answer Normalization Algorithm
```java
private String normalizeAnswer(String answer) {
    return answer.trim()
        .toLowerCase()
        .replaceAll("\\s+", " ")
        .replaceAll("[.!?]+$", "");
}
```

**Rules:**
1. Trim leading/trailing whitespace
2. Convert to lowercase
3. Replace multiple spaces with single space
4. Remove trailing punctuation (. ! ?)

### Partial Points Calculation (MULTIPLE)
```java
double partialPoint = (double) correctCount / correctChoices.size() * question.getPoint();
answer.setPoint_achieved((int) Math.round(partialPoint));
```

**Example:**
- Question has 4 correct choices, worth 10 points
- User selects 3 correct + 0 incorrect
- Partial = (3 / 4) * 10 = 7.5 → **8 points** (rounded)

---

## 📁 Expected Output Files

```
documents/
├── input/
│   ├── API Document - SAMPLE.pdf
│   └── Testcase API - SAMPLE.pdf
├── output/
│   ├── API_Document_AssessPronunciation.md      (from req-3) ✅
│   ├── Testcase_API_AssessPronunciation.md      (from req-3) ✅
│   ├── API_Document_GradeAnswer.md              (req-4) 🆕
│   └── Testcase_API_GradeAnswer.md              (req-4) 🆕
├── req-1.md
├── req-2.md
├── req-3.md
└── req-4.md
```

---

## 🎯 Implementation Notes

1. **Path Parameter Documentation:**
   - Clearly show {answerId} in endpoint
   - Specify type: Long
   - Provide valid/invalid examples

2. **Grading Logic Examples:**
   - Include realistic question scenarios
   - Show partial points calculation
   - Demonstrate normalization

3. **Response Examples:**
   - Show before/after point_achieved values
   - Include all 7 fields
   - Cover all question types (CHOICE, MULTIPLE, TEXT)

4. **Test Data:**
   - Use realistic answer IDs (1, 2, 100, etc.)
   - Provide example answerContent values
   - Show partial grading scenarios

5. **Error Messages:**
   - Match actual exception messages from service
   - Use ResourceNotFoundException format
   - Include validation error details

---

## 🚀 Execution Steps

1. **Read Investigation Results** ✅ (Completed)
   - AnswerController.java
   - AnswerService.java
   - ResAnswerDTO.java
   - Answer.java
   - Question_Choice.java
   - QuestionTypeEnum.java

2. **Create API Document** (Next)
   - Generate consolidated format
   - Document all 7 ResAnswerDTO fields
   - Explain grading algorithms
   - Include response examples for all question types
   - Add error code table

3. **Create Test Cases** (Next)
   - Generate 30 test cases in single table
   - Group by category
   - Assign execution types
   - Fill all columns with realistic data

4. **Quality Verification**
   - Verify 30 test case count
   - Verify format compliance with SAMPLE
   - Verify all ResAnswerDTO fields documented
   - Verify grading logic accuracy

5. **Create Completion Report**
   - Document verification results
   - Include file sizes
   - Confirm format compliance

---

## ⏱️ Estimated Time

- **Investigation:** 15 minutes ✅ (Completed)
- **API Document creation:** 30 minutes
- **Testcase creation:** 45 minutes
- **Quality verification:** 15 minutes
- **Total:** ~1 hour 45 minutes

---

## 📚 Reference Files

**Input References:**
- ✅ `backend-service/src/main/java/utc/englishlearning/Encybara/controller/AnswerController.java`
- ✅ `backend-service/src/main/java/utc/englishlearning/Encybara/service/AnswerService.java`
- ✅ `backend-service/src/main/java/utc/englishlearning/Encybara/domain/response/answer/ResAnswerDTO.java`
- ✅ `backend-service/src/main/java/utc/englishlearning/Encybara/domain/Answer.java`
- ✅ `documents/input/API Document - SAMPLE.pdf`
- ✅ `documents/input/Testcase API - SAMPLE.pdf`

**Format References:**
- ✅ `documents/output/API_Document_AssessPronunciation.md` (req-3 - for format)
- ✅ `documents/output/Testcase_API_AssessPronunciation.md` (req-3 - for format)

---

## ✅ Ready to Execute

This plan is complete and ready for execution. All investigation has been completed, and the response structure is fully documented.

**Execute command:** `do req-4`

---

**END OF PLAN**
