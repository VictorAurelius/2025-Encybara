# REQ-7: Automated Testing for 4 Core APIs

**Date:** 2025-11-07
**Status:** In Progress
**Priority:** High
**Related Documents:**
- documents/input/API Document V2.pdf (Original - has errors)
- documents/output/API_Document_V3.md (Corrected version)
- documents/input/Testcase API V2.pdf
- backend-service/test-pronunciation-testcase.sh (Reference pattern)
- backend-service/test-grade-answer.sh (Reference pattern)

---

## 1. OVERVIEW

### 1.1 Context
The project requires comprehensive automated testing for 4 core APIs that manage the learning content system:
1. **CreateQuestion API** - Creates new questions for lessons
2. **AddQuestionsToLesson API** - Assigns questions to lessons
3. **Notification API** - Manages user notifications
4. **Review API** - Handles course reviews from users

These APIs are critical for content management and user engagement. Currently, there is documentation in "Testcase API V2.pdf" showing test cases, but no automated test scripts exist.

### 1.2 Problem Statement
During the investigation phase (section 3), a critical documentation error was discovered:
- **API Document V2.pdf** lists CreateQuestion endpoint as `/api/CreateQuestion`
- **Actual code** shows the endpoint is `/api/v1/questions`

This discrepancy would cause all tests to fail if following the V2 documentation. Additionally, the existing testcase documentation lacks detail on:
- Validation logic from service layer
- Complete error scenarios
- Response structure verification
- Realistic test data scenarios

### 1.3 Objectives
1. Create corrected API documentation (API Document V3) based on actual code
2. Develop 4 automated test scripts following the pattern of test-pronunciation-testcase.sh
3. Implement comprehensive test coverage including:
   - Request validation tests
   - Business logic tests
   - Error code verification
   - Response format validation
4. Ensure tests can run independently and produce clear reports
5. Document any additional issues found during testing

---

## 2. INPUT CONTEXT

### 2.1 API Document V2.pdf Analysis

**Issues Found:**
- ✗ CreateQuestion endpoint incorrect: `/api/CreateQuestion` → should be `/api/v1/questions`
- ✓ AddQuestionsToLesson endpoint correct
- ✓ Notification endpoint correct
- ✓ Review endpoint correct
- ✗ Missing detailed validation rules from service layer
- ✗ Missing complete ENUM value lists

**Corrected in:** `documents/output/API_Document_V3.md`

### 2.2 Testcase API V2.pdf Summary

| API | Total Test Cases | Passed | Failed | Coverage |
|-----|-----------------|--------|--------|----------|
| CreateQuestion | 53 | 37 | 16 | Extensive validation for all fields |
| AddQuestionsToLesson | 29 | 24 | 5 | Method, body, path param, logic tests |
| Notification | 30 | 19 | 11 | Message, userId, img, entity validation |
| Review | 30 | 19 | 11 | All fields + enrollment validation |

**Test Categories (based on test-pronunciation-testcase.sh pattern):**
1. **Validate Tests** - Input field validation (required, format, type)
2. **Logic Tests** - Business logic and workflow
3. **Error Code Tests** - HTTP status codes (400, 401, 403, 404, 409, 500)
4. **Format Response Tests** - Response structure validation

### 2.3 Existing Test Script Patterns

#### Pattern 1: test-pronunciation-testcase.sh (Simple, Direct API Calls)
- 688 lines, 30 test cases
- Direct POST calls to single endpoint
- No complex setup (enrollment, etc.)
- Good for: Independent API testing
- Used for: Pronunciation assessment API

#### Pattern 2: test-grade-answer.sh (Complex, Multi-Step)
- 773 lines
- Setup phase: Auth → Find course → Discover questions → Create enrollment
- Dynamic question discovery from database
- Tests multiple question types (TEXT, CHOICE)
- Used for: Answer grading API with enrollment context

**Decision:** Use Pattern 1 for all 4 APIs because:
- Each API is independent and doesn't require complex enrollment setup
- CreateQuestion, Notification: Standalone operations
- AddQuestionsToLesson: Only requires existing lesson/question IDs
- Review: Requires enrollment but can be set up in individual tests

### 2.4 Code Analysis Summary

**Files Analyzed:**
- QuestionController.java (line 27-34): POST /api/v1/questions
- LessonController.java (line 65-73): POST /api/v1/lessons/{lessonId}/questions
- NotificationController.java (line 24-43): POST /api/v1/notifications
- ReviewController.java (line 21-29): POST /api/v1/reviews

**Service Layer Validation:**
- QuestionService.java (line 38-98): Detailed validation for WRITING/SPEAKING constraints
- LessonService.java (line 66-95): Duplicate question prevention, transaction handling
- NotificationService.java (line 22-32): Auto-set defaults (isRead, createdAt)
- ReviewService.java (line 42-77): Enrollment + completion level validation

---

## 3. INVESTIGATION RESULTS

### 3.1 API 1: CreateQuestion

**Endpoint:** `POST /api/v1/questions`
**Controller:** QuestionController.java:27-34
**Service:** QuestionService.java:38-98

**Key Validation Rules (from code):**
1. `quesContent` cannot be empty (line 71-73)
2. `point` must be positive (line 76-78)
3. WRITING questions MUST have WRITING skillType (line 47-52)
4. SPEAKING questions MUST have SPEAKING skillType (line 54-58)
5. WRITING/SPEAKING questions CANNOT have choices (line 80-85)
6. Auto-fill keyword for WRITING/SPEAKING if empty (line 60-67)

**Test Data Requirements:**
- Valid question types: CHOICE, MULTIPLE, TEXT, LISTENING, WRITING, SPEAKING
- Valid skill types: LISTENING, READING, WRITING, SPEAKING, ALLSKILLS
- Need question choices structure: `{choiceContent, choiceKey}`

**Critical Test Cases:**
1. Valid CHOICE question with choices
2. Valid TEXT question without choices
3. Valid WRITING question (must have WRITING skill)
4. Valid SPEAKING question (must have SPEAKING skill)
5. WRITING question with wrong skillType → 400 error
6. SPEAKING question with wrong skillType → 400 error
7. WRITING question with choices → 400 error
8. Empty quesContent → 400 error
9. Zero or negative point → 400 error
10. Invalid quesType ENUM → 400 error
11. Invalid skillType ENUM → 400 error
12. Missing required fields → 400 error
13. Invalid token → 401 error
14. Malformed JSON → 400 error

### 3.2 API 2: AddQuestionsToLesson

**Endpoint:** `POST /api/v1/lessons/{lessonId}/questions`
**Controller:** LessonController.java:65-73
**Service:** LessonService.java:66-95

**Key Validation Rules (from code):**
1. Lesson must exist (line 67-68) → 404 if not
2. All question IDs must exist (line 70-73) → 404 if missing
3. Cannot add duplicate questions (line 75-84) → 409 if duplicate
4. Transaction ensures all-or-nothing (line 65 @Transactional)

**Test Data Requirements:**
- Valid lessonId (must exist in database)
- Valid questionIds array
- Need to create test lesson and questions first

**Critical Test Cases:**
1. Valid request with 1 question
2. Valid request with multiple questions (batch add)
3. Invalid lessonId → 404 error
4. Non-existent questionId → 404 error
5. Duplicate question in same lesson → 409 error
6. Empty questionIds array → 400 error
7. Invalid questionIds format (not array) → 400 error
8. Invalid lessonId format (not number) → 400 error
9. Missing request body → 400 error
10. Invalid token → 401 error

### 3.3 API 3: Notification

**Endpoint:** `POST /api/v1/notifications`
**Controller:** NotificationController.java:24-43
**Service:** NotificationService.java:22-32

**Key Validation Rules (from code):**
1. Auto-set isRead to false (line 26)
2. Auto-set createdAt to now (line 27)
3. img must be valid ENUM: STUDY, FLASHCARD, SCHEDULE, ACCOUNT
4. userId must reference existing user (FK constraint)

**Test Data Requirements:**
- Valid userId (must exist in database)
- Valid img ENUM values
- Optional: entityId, entityType

**Critical Test Cases:**
1. Valid notification with all fields
2. Valid notification with minimal fields (no entityId/entityType)
3. Invalid img ENUM value → 400 error
4. Missing message → 400 error
5. Missing userId → 400 error
6. Invalid userId (non-existent) → 404 or FK error
7. Missing img field → 400 error
8. Invalid JSON format → 400 error
9. Invalid token → 401 error

### 3.4 API 4: Review

**Endpoint:** `POST /api/v1/reviews`
**Controller:** ReviewController.java:21-29
**Service:** ReviewService.java:42-77

**Key Validation Rules (from code):**
1. User cannot review same course twice (line 44-47) → 409 if duplicate
2. User must be enrolled in course (line 50-52) → 404 if not enrolled
3. Enrollment completion must be ≥30% (line 55-58) → 400 if less
4. User must exist (line 61-62) → 404 if not
5. Course must exist (line 63-64) → 404 if not
6. Auto-set numLike to 0 (line 68)
7. Auto-create notification (line 74)
8. status must be valid ENUM: CONTRIBUTING, CONTENT, MISTAKE

**Test Data Requirements:**
- Valid userId with enrollment
- Valid courseId
- Enrollment with ≥30% completion
- Valid status ENUM values

**Critical Test Cases:**
1. Valid review (user enrolled, >30% completion)
2. Duplicate review attempt → 409 error
3. User not enrolled → 404 error
4. Enrollment <30% completion → 400 error
5. Non-existent userId → 404 error
6. Non-existent courseId → 404 error
7. Invalid status ENUM → 400 error
8. Missing required fields → 400 error
9. Invalid numStar (out of range) → validate if range check exists
10. Invalid token → 401 error

---

## 4. TASK PLAN

### 4.1 Test Script Architecture

Each script will follow this structure (based on test-pronunciation-testcase.sh):

```bash
#!/bin/bash

# Configuration
BACKEND_URL="http://localhost:8080"
USERNAME="admin@gmail.com"
PASSWORD="123456"
ACCESS_TOKEN=""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Test tracking
TEST_RESULTS=()
PASSED_COUNT=0
FAILED_COUNT=0
PENDING_COUNT=0
TOTAL_TESTS=0

# Functions
get_auth_token()
run_test()
print_section_header()
generate_report()

# Test Categories
# 1. VALIDATE TESTS (field validation)
# 2. LOGIC TESTS (business rules)
# 3. ERROR CODE TESTS (HTTP status)
# 4. FORMAT RESPONSE TESTS (structure)

# Main execution
main()
```

### 4.2 Script 1: test-create-question.sh

**Test Breakdown:**

**A. VALIDATE Tests (10 tests)**
1. Empty quesContent → 400
2. Null quesContent → 400
3. Zero point → 400
4. Negative point → 400
5. Empty keyword → Auto-filled, 200 OK
6. Null quesType → 400
7. Invalid quesType ENUM → 400
8. Null skillType → 400
9. Invalid skillType ENUM → 400
10. Missing required field (point) → 400

**B. LOGIC Tests (8 tests)**
1. Valid CHOICE question with 4 choices → 200 OK
2. Valid MULTIPLE question with choices → 200 OK
3. Valid TEXT question without choices → 200 OK
4. Valid LISTENING question → 200 OK
5. Valid WRITING question (WRITING skill, no choices) → 200 OK
6. Valid SPEAKING question (SPEAKING skill, no choices) → 200 OK
7. WRITING question with READING skill → 400
8. SPEAKING question with LISTENING skill → 400

**C. ERROR CODE Tests (8 tests)**
9. WRITING question with choices → 400
10. SPEAKING question with choices → 400
11. No auth token → 401
12. Invalid auth token → 401
13. Non-admin user (if permission check) → 403
14. Malformed JSON → 400
15. Wrong HTTP method (GET instead of POST) → 405
16. Database connection error simulation → 500

**D. FORMAT RESPONSE Tests (5 tests)**
1. Response has statusCode field → 200
2. Response has message field → 200
3. Response has data.id field → 200
4. Response data contains all request fields → 200
5. Response data.questionChoices matches request → 200

**Total: ~31 tests**

### 4.3 Script 2: test-add-questions-to-lesson.sh

**Setup Phase:**
- Create test lesson (store lessonId)
- Create 3 test questions (store questionIds)

**Test Breakdown:**

**A. VALIDATE Tests (8 tests)**
1. Invalid lessonId (non-numeric) → 400
2. Non-existent lessonId → 404
3. Empty questionIds array → 400
4. Null questionIds → 400
5. Invalid questionIds format (not array) → 400
6. questionIds with non-numeric values → 400
7. Missing request body → 400
8. Malformed JSON → 400

**B. LOGIC Tests (6 tests)**
1. Add 1 question to lesson → 200 OK
2. Add multiple questions (batch) → 200 OK
3. Add same question twice → 409 (duplicate)
4. Add non-existent questionId → 404
5. Partial failure (1 valid, 1 invalid) → 404 (transaction rollback)
6. Add question to non-existent lesson → 404

**C. ERROR CODE Tests (6 tests)**
1. No auth token → 401
2. Invalid auth token → 401
3. Non-admin user (if permission check) → 403
4. Wrong HTTP method (GET) → 405
5. Invalid path parameter → 404
6. Server error simulation → 500

**D. FORMAT RESPONSE Tests (4 tests)**
1. Response has statusCode = 200
2. Response has message field
3. Response data is null (void return)
4. Lesson sumQues updated correctly (query lesson)

**Total: ~24 tests**

### 4.4 Script 3: test-notification.sh

**Setup Phase:**
- Get valid userId from database (can use admin user)

**Test Breakdown:**

**A. VALIDATE Tests (8 tests)**
1. Empty message → 400
2. Null message → 400
3. Null userId → 400
4. Invalid userId (non-numeric) → 400
5. Non-existent userId → 404 or FK error
6. Null img → 400
7. Invalid img ENUM → 400
8. Missing required field → 400

**B. LOGIC Tests (6 tests)**
1. Valid notification with all fields → 200 OK
2. Valid notification without entityId/entityType → 200 OK
3. Notification with entityId but no entityType → 200 OK
4. Notification with entityType but no entityId → 200 OK
5. Each img ENUM value (STUDY, FLASHCARD, SCHEDULE, ACCOUNT) → 200 OK each

**C. ERROR CODE Tests (6 tests)**
1. No auth token → 401
2. Invalid auth token → 401
3. Non-admin user (if permission check) → 403
4. Malformed JSON → 400
5. Wrong HTTP method (GET) → 405
6. Server error simulation → 500

**D. FORMAT RESPONSE Tests (6 tests)**
1. Response has statusCode = 200
2. Response has data.id (auto-generated)
3. Response data.isRead = false
4. Response data.createdAt is valid timestamp
5. Response data matches request fields
6. Response data.img matches request

**Total: ~26 tests**

### 4.5 Script 4: test-review.sh

**Setup Phase:**
- Find or create test user
- Find or create test course
- Create enrollment with ≥30% completion
- Create second enrollment with <30% completion (for negative test)

**Test Breakdown:**

**A. VALIDATE Tests (8 tests)**
1. Null userId → 400
2. Null courseId → 400
3. Null reContent → 400
4. Null reSubject → 400
5. Null numStar → 400
6. Null status → 400
7. Invalid status ENUM → 400
8. Invalid numStar (negative) → validate if exists

**B. LOGIC Tests (8 tests)**
1. Valid review (enrolled, >30% completion) → 200 OK
2. Each status ENUM (CONTRIBUTING, CONTENT, MISTAKE) → 200 OK each
3. User not enrolled in course → 404
4. Enrollment <30% completion → 400
5. Duplicate review attempt → 409
6. Review with different numStar values (1-5) → 200 OK
7. Non-existent userId → 404
8. Non-existent courseId → 404

**C. ERROR CODE Tests (6 tests)**
1. No auth token → 401
2. Invalid auth token → 401
3. Non-enrolled user → 404
4. Malformed JSON → 400
5. Wrong HTTP method (GET) → 405
6. Server error simulation → 500

**D. FORMAT RESPONSE Tests (6 tests)**
1. Response has statusCode = 200
2. Response has data.id (auto-generated)
3. Response data.numLike = 0
4. Response data matches request fields
5. Notification created (check notification table)
6. All response fields present

**Total: ~28 tests**

### 4.6 Implementation Order

**Phase 1: Foundation**
1. Create test-create-question.sh (most independent, no dependencies)
2. Test and validate script pattern

**Phase 2: Dependent APIs**
3. Create test-add-questions-to-lesson.sh (depends on questions existing)
4. Create test-notification.sh (simple, like create-question)

**Phase 3: Complex Logic**
5. Create test-review.sh (most complex: needs user, course, enrollment)

**Phase 4: Documentation**
6. Create TEST_README.md with:
   - How to run each script
   - Prerequisites (database state, etc.)
   - Expected results
   - Troubleshooting guide

---

## 5. TECHNICAL DETAILS

### 5.1 Common Functions (All Scripts)

```bash
# Get authentication token
get_auth_token() {
    response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}" \
        "${BACKEND_URL}/api/v1/auth/login")

    ACCESS_TOKEN=$(echo "$response" | jq -r '.data.access_token')

    if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
        echo -e "${RED}Failed to get authentication token${NC}"
        exit 1
    fi
}

# Run a test case
run_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    local category="$6"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    response=$(timeout 10s curl -s -w "\n%{http_code}" -X ${method} \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        ${data} \
        "${BACKEND_URL}${endpoint}" 2>/dev/null)

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC}: $test_name"
        TEST_RESULTS+=("PASS|$category|$test_name")
        PASSED_COUNT=$((PASSED_COUNT + 1))
    else
        echo -e "${RED}✗ FAIL${NC}: $test_name (Expected: $expected_status, Got: $http_code)"
        TEST_RESULTS+=("FAIL|$category|$test_name|Expected: $expected_status, Got: $http_code")
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
}

# Generate final report
generate_report() {
    echo -e "\n${BLUE}================================${NC}"
    echo -e "${BLUE}TEST EXECUTION SUMMARY${NC}"
    echo -e "${BLUE}================================${NC}"
    echo -e "Total Tests: ${TOTAL_TESTS}"
    echo -e "${GREEN}Passed: ${PASSED_COUNT}${NC}"
    echo -e "${RED}Failed: ${FAILED_COUNT}${NC}"
    echo -e "${YELLOW}Pending: ${PENDING_COUNT}${NC}"

    if [ ${FAILED_COUNT} -eq 0 ]; then
        echo -e "\n${GREEN}ALL TESTS PASSED!${NC}"
    else
        echo -e "\n${RED}SOME TESTS FAILED${NC}"
        echo -e "\nFailed Tests:"
        for result in "${TEST_RESULTS[@]}"; do
            if [[ $result == FAIL* ]]; then
                IFS='|' read -ra parts <<< "$result"
                echo -e "${RED}- [${parts[1]}] ${parts[2]}: ${parts[3]}${NC}"
            fi
        done
    fi
}
```

### 5.2 Test Data Templates

**CreateQuestion - CHOICE:**
```json
{
  "quesContent": "What is the capital of France?",
  "keyword": "geography",
  "quesType": "CHOICE",
  "skillType": "READING",
  "point": 10,
  "questionChoices": [
    {"choiceContent": "Paris", "choiceKey": true},
    {"choiceContent": "London", "choiceKey": false},
    {"choiceContent": "Berlin", "choiceKey": false},
    {"choiceContent": "Madrid", "choiceKey": false}
  ]
}
```

**CreateQuestion - WRITING:**
```json
{
  "quesContent": "Write an essay about climate change (min 250 words)",
  "keyword": "writing task",
  "quesType": "WRITING",
  "skillType": "WRITING",
  "point": 25,
  "questionChoices": []
}
```

**AddQuestionsToLesson:**
```json
{
  "questionIds": [1, 2, 3]
}
```

**Notification:**
```json
{
  "message": "New lesson available in your course",
  "userId": 1,
  "entityId": 123,
  "entityType": "LESSON",
  "img": "STUDY"
}
```

**Review:**
```json
{
  "userId": 1,
  "courseId": 1,
  "reContent": "Great course! Very helpful for learning English.",
  "reSubject": "Excellent content and structure",
  "numStar": 5,
  "status": "CONTENT"
}
```

---

## 6. QUALITY CHECKLIST

### 6.1 Code Quality
- [ ] Scripts follow bash best practices
- [ ] Error handling for network failures
- [ ] Timeout handling for API calls (10s max)
- [ ] Clear, descriptive test names
- [ ] Consistent formatting and indentation
- [ ] Comments for complex logic

### 6.2 Test Coverage
- [ ] All required field validations tested
- [ ] All ENUM values tested
- [ ] All business logic rules tested
- [ ] All error codes (400, 401, 403, 404, 409, 500) tested
- [ ] Response structure validated
- [ ] Edge cases covered (empty, null, invalid format)

### 6.3 Documentation
- [ ] API Document V3 created with corrections
- [ ] Test script headers explain purpose
- [ ] README explains how to run tests
- [ ] Prerequisites clearly documented
- [ ] Expected database state documented
- [ ] Known issues/limitations documented

### 6.4 Reliability
- [ ] Tests can run independently
- [ ] Tests clean up test data (or use isolated test data)
- [ ] Tests don't interfere with each other
- [ ] Clear success/failure reporting
- [ ] Reproducible results

### 6.5 Usability
- [ ] Colored output for readability
- [ ] Progress indicators during execution
- [ ] Clear error messages
- [ ] Summary report at end
- [ ] Easy to identify which tests failed

---

## 7. DELIVERABLES

### 7.1 Documentation
1. ✅ **API_Document_V3.md** - Corrected API documentation
2. ✅ **req-7.md** - This requirements document
3. ⏳ **TEST_README.md** - Test execution guide

### 7.2 Test Scripts
1. ⏳ **test-create-question.sh** - ~31 tests for CreateQuestion API
2. ⏳ **test-add-questions-to-lesson.sh** - ~24 tests for AddQuestionsToLesson API
3. ⏳ **test-notification.sh** - ~26 tests for Notification API
4. ⏳ **test-review.sh** - ~28 tests for Review API

### 7.3 Supporting Files
1. ⏳ **test-data-setup.sql** - SQL script to set up test data (if needed)
2. ⏳ **test-data-cleanup.sql** - SQL script to clean up test data

---

## 8. RISKS AND MITIGATIONS

### 8.1 Identified Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Database state affects tests | High | Use isolated test data or clean up after tests |
| API changes break tests | High | Version tests with API versions |
| Network timeouts | Medium | Implement 10s timeout, retry logic |
| Auth token expiration | Medium | Refresh token if needed |
| Test data conflicts | Medium | Use unique identifiers, check before create |
| Permission issues | Low | Document required user roles |

### 8.2 Known Limitations

1. **Review API Testing**: Requires pre-existing enrollment with ≥30% completion
   - Mitigation: Setup phase creates enrollment or uses existing test user

2. **AddQuestionsToLesson**: Requires existing questions
   - Mitigation: Setup phase creates test questions or references known IDs

3. **Database Dependencies**: Tests assume certain data exists (users, courses)
   - Mitigation: Document prerequisites, provide setup scripts

4. **Concurrent Execution**: Tests may not be safe to run in parallel
   - Mitigation: Run sequentially, document this limitation

---

## 9. SUCCESS CRITERIA

### 9.1 Test Scripts
- ✅ Each script runs without errors
- ✅ All tests execute and report results
- ✅ Passed/Failed count is accurate
- ✅ Report clearly shows which tests failed
- ✅ Scripts complete in reasonable time (<2 minutes each)

### 9.2 Test Coverage
- ✅ >90% of test cases from Testcase API V2.pdf are covered
- ✅ All validation rules from code are tested
- ✅ All error codes are verified
- ✅ Response formats are validated

### 9.3 Documentation
- ✅ API Document V3 accurately reflects actual code
- ✅ README provides clear instructions
- ✅ Prerequisites are documented
- ✅ Test results are reproducible

---

## 10. NEXT STEPS

1. **Implement test-create-question.sh** (Priority 1)
   - Most independent, sets pattern for others
   - ~31 tests, estimated 2-3 hours

2. **Implement test-notification.sh** (Priority 2)
   - Simple, similar to create-question
   - ~26 tests, estimated 2 hours

3. **Implement test-add-questions-to-lesson.sh** (Priority 3)
   - Needs setup phase for test data
   - ~24 tests, estimated 2-3 hours

4. **Implement test-review.sh** (Priority 4)
   - Most complex, needs enrollment setup
   - ~28 tests, estimated 3-4 hours

5. **Create TEST_README.md** (Priority 5)
   - Document how to run all tests
   - Estimated 1 hour

6. **Testing and Refinement** (Priority 6)
   - Run all scripts, fix issues
   - Validate coverage
   - Estimated 2-3 hours

**Total Estimated Time:** 12-16 hours

---

## 11. REFERENCES

### Code Files
- `backend-service/src/main/java/utc/englishlearning/Encybara/controller/QuestionController.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/controller/LessonController.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/controller/NotificationController.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/controller/ReviewController.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/service/QuestionService.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/service/LessonService.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/service/NotificationService.java`
- `backend-service/src/main/java/utc/englishlearning/Encybara/service/ReviewService.java`

### Documentation
- `documents/input/API Document V2.pdf` - Original (has errors)
- `documents/output/API_Document_V3.md` - Corrected version
- `documents/input/Testcase API V2.pdf` - Test case specifications

### Reference Scripts
- `backend-service/test-pronunciation-testcase.sh` - Pattern for simple API tests
- `backend-service/test-grade-answer.sh` - Pattern for complex multi-step tests

---

**Document Status:** Complete - Ready for Implementation
**Next Action:** Begin implementation of test-create-question.sh
