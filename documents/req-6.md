# Plan Task req-6: Automated Test Script cho API GradeAnswer với Placement Course

**Created:** 2025-11-04
**Status:** Plan Ready
**Target API:** `gradeAnswer` in `AnswerController`
**Test Course:** English Placement Test

---

## 📋 Task Overview

Tạo script test tự động cho API Grade Answer, sử dụng dữ liệu thực từ Placement Course. Script sẽ test đầy đủ flow từ login → enroll → create answer → grade answer cho câu hỏi TEXT và CHOICE.

**Deliverables:**
1. `backend-service/test-grade-answer.sh` - Automated test script
2. `documents/output/Testcase_API_GradeAnswer_Placement.md` - Updated testcase cho placement course
3. `backend-service/TEST_GRADE_ANSWER_README.md` - Documentation

---

## 📥 Input Context

**Đã đọc và phân tích:**
1. ✅ `documents/output/API_Document_GradeAnswer.md` - API specification
2. ✅ `documents/output/Testcase_API_GradeAnswer.md` - 30 test cases (generic)
3. ✅ `backend-service/src/main/java/.../config/AdminDataInitializer.java` - Seeding logic
4. ✅ `backend-service/src/main/resources/data/placement/json/test1/paper1/*.json` - Placement course data

---

## 🎯 Objectives

1. **Tạo Automated Test Script** - Test đầy đủ flow từ login đến grade answer
2. **Test với Placement Course** - Sử dụng dữ liệu thực từ placement test
3. **Focus on TEXT & CHOICE** - Chỉ test 2 loại câu hỏi này (không test MULTIPLE)
4. **Update Testcase Document** - Tạo testcase mới cho placement course

---

## 🔍 Investigation Results

### 1. API GradeAnswer Details

**Endpoint:** `PUT /api/v1/answers/grade/{answerId}`

**Authentication:** JWT Bearer token required

**Grading Logic:**
- **CHOICE (Single Choice):**
  - Binary grading: full points hoặc zero points
  - Comparison: case-insensitive, normalized (trim, lowercase, remove trailing punctuation)

- **TEXT (Text Input):**
  - Binary grading: full points hoặc zero points
  - Normalization:
    - Trim whitespace
    - Convert to lowercase
    - Replace multiple spaces with single space
    - Remove trailing punctuation (. ! ?)

- **MULTIPLE (Multiple Choice):**
  - Partial credit allowed
  - **NOTE: User yêu cầu không test MULTIPLE trong script này**

**Response Structure (ResAnswerDTO):**
```json
{
  "id": Long,
  "questionId": Long,
  "answerContent": String,
  "pointAchieved": Integer,
  "sessionId": Long,
  "improvement": String (nullable),
  "enrollmentId": Long (nullable)
}
```

---

### 2. Placement Course Structure

**Course Name:** "English Placement Test"

**Lessons:** 6 lessons
1. (PLACEMENT) Text - Reading
2. (PLACEMENT) Choice - Reading
3. (PLACEMENT) Choice - Listening
4. (PLACEMENT) Text - Listening
5. (PLACEMENT) Writing
6. (PLACEMENT) Speaking

**Questions:**

| File | Question Type | Count | Skill Type | Points Each |
|------|---------------|-------|------------|-------------|
| question-1.json | CHOICE | 9 | READING | 10 |
| question-2.json | TEXT | 5 | READING | 10 |
| question-2.json | CHOICE | 5 | LISTENING | 10 |
| question-3.json | TBD | TBD | TBD | TBD |

**Total for Testing:**
- CHOICE questions: ~14 questions
- TEXT questions: ~5 questions

---

### 3. Default User Credentials

**Email:** `user@example.com`
**Password:** `Abc@123456`

**Created by:** AdminDataInitializer on application startup

**Learning Result:**
- Listening: 5.0
- Speaking: 5.0
- Reading: 5.0
- Writing: 5.0

---

### 4. Seeding Flow

**On Application Startup:**
1. AdminDataInitializer runs
2. Creates default user if not exists
3. Seeds course data:
   ```java
   dataManagementService.seedPlacementData();
   // Calls: courseDataSeeder.seedCourseData("placement", "test1", "paper1")
   ```
4. Placement course and questions are created in database

---

## 📋 Detailed Task Plan

### **Task 1: Investigate Placement Course Data Structure**
**Duration:** ~15 minutes

**Actions:**
- [x] Read `courses.json` - Course metadata
- [x] Read `question-1.json` - CHOICE questions (9 questions)
- [x] Read `question-2.json` - TEXT (5) + CHOICE (5) questions
- [ ] Read `question-3.json` - Additional questions
- [ ] Count total TEXT and CHOICE questions available for testing

**Expected Questions:**
- **CHOICE:**
  - Question 1: "critical" - correct: "supportive"
  - Question 2: "post" - correct: "displace"
  - Question 3: "efficient" - correct: "successful"
  - Question 4: "endeavoring" - correct: "trying"
  - Question 5: grammar - correct: "narrow-minded"
  - Question 6: dialogue - correct: "OK, let me just check the diary."
  - Question 7: grammar - correct: "went / have not been"
  - Question 8: phrasal verb - correct: "look up"
  - Question 9: comparative - correct: "The harder/ the better"

- **TEXT:**
  - Question 1: Rewrite with "Tired" - correct: "Tired as he was, he agreed to help me with my homework"
  - Question 2: Relative clauses - correct: "The man whose daughter is fond of dancing works for my father's company"
  - Question 3: Apologize - correct: "Martin apologized to Angela for having damaged her car"
  - Question 4: Passive - correct: "Maradona is thought to be the best football player in the 20th century"
  - Question 5: Conditional - correct: "If it hadn't been for your help, I couldn't overcome the problem"

**Output:** Complete understanding of available questions for testing

---

### **Task 2: Design Test Script Flow**
**Duration:** ~20 minutes

**Actions:**
- [ ] Design complete test flow:

```
1. SETUP PHASE
   ├── Check prerequisites (backend running, database ready)
   ├── Get authentication token for user@example.com
   └── Verify placement course exists

2. ENROLLMENT PHASE
   ├── Get placement course ID
   ├── Create enrollment for user
   ├── Verify enrollment created
   └── Save enrollment ID

3. ANSWER CREATION PHASE - TEXT Questions
   ├── Get TEXT questions from placement course
   ├── For each TEXT question:
   │   ├── Create correct answer (normalized text)
   │   ├── Create incorrect answer (different text)
   │   └── Save answer IDs
   └── Total: ~10 answers (5 correct + 5 incorrect)

4. ANSWER CREATION PHASE - CHOICE Questions
   ├── Get CHOICE questions from placement course
   ├── For each CHOICE question:
   │   ├── Create correct answer (select correct choice)
   │   ├── Create incorrect answer (select wrong choice)
   │   └── Save answer IDs
   └── Total: ~18 answers (9 correct + 9 incorrect)

5. GRADING PHASE
   ├── For each answer created:
   │   ├── Call PUT /api/v1/answers/grade/{answerId}
   │   ├── Verify response structure
   │   ├── Verify pointAchieved matches expectation
   │   └── Log result (PASSED/FAILED)
   └── Total: ~28 grading API calls

6. VALIDATION PHASE
   ├── Verify correct answers got full points
   ├── Verify incorrect answers got zero points
   ├── Verify response structure for all calls
   └── Generate test report

7. CLEANUP PHASE
   ├── Optional: Delete created answers
   ├── Optional: Delete enrollment
   └── Generate summary report
```

**Output:** Clear flow diagram and sequence

---

### **Task 3: Identify Required API Endpoints**
**Duration:** ~10 minutes

**Actions:**
- [ ] List all API endpoints needed for test script:

| # | Method | Endpoint | Purpose | Response |
|---|--------|----------|---------|----------|
| 1 | POST | /api/v1/auth/login | Get JWT token | access_token |
| 2 | GET | /api/v1/courses | Find placement course | Course list |
| 3 | GET | /api/v1/courses/{id} | Get course details | Course with lessons |
| 4 | POST | /api/v1/enrollments | Create enrollment | Enrollment ID |
| 5 | GET | /api/v1/lessons/{id} | Get lesson questions | Question list |
| 6 | POST | /api/v1/answers | Create answer | Answer ID |
| 7 | PUT | /api/v1/answers/grade/{id} | Grade answer | Graded answer |

**Output:** Complete API endpoint reference

---

### **Task 4: Create Script Structure**
**Duration:** ~20 minutes

**Actions:**
- [ ] Create `backend-service/test-grade-answer.sh` with structure:

```bash
#!/bin/bash

# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
API_BASE="/api/v1"
DEFAULT_EMAIL="user@example.com"
DEFAULT_PASSWORD="Abc@123456"
PLACEMENT_COURSE_NAME="English Placement Test"

# Global variables
ACCESS_TOKEN=""
PLACEMENT_COURSE_ID=""
ENROLLMENT_ID=""
TEXT_ANSWERS=()
CHOICE_ANSWERS=()
TEST_RESULTS=()
PASSED_COUNT=0
FAILED_COUNT=0

# Color codes
GREEN, RED, YELLOW, BLUE, NC

# Functions:
# 1. get_auth_token()
# 2. find_placement_course()
# 3. create_enrollment()
# 4. get_lesson_questions(lessonId)
# 5. create_text_answer(questionId, answerContent, enrollmentId, sessionId)
# 6. create_choice_answer(questionId, choiceContent, enrollmentId, sessionId)
# 7. grade_answer(answerId, expectedPoints)
# 8. test_text_questions()
# 9. test_choice_questions()
# 10. validate_results()
# 11. generate_report()
# 12. cleanup()
# 13. main()
```

**Output:** Script skeleton design

---

### **Task 5: Implement Authentication & Course Discovery**
**Duration:** ~15 minutes

**Actions:**
- [ ] Implement authentication function:

```bash
get_auth_token() {
    echo -e "${YELLOW}Authenticating user...${NC}"

    login_data="{
        \"username\": \"${DEFAULT_EMAIL}\",
        \"password\": \"${DEFAULT_PASSWORD}\"
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "${login_data}" \
        "${BACKEND_URL}${API_BASE}/auth/login")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" ]]; then
        ACCESS_TOKEN=$(echo "$response_body" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
        echo -e "${GREEN}✓ Authentication successful${NC}"
    else
        echo -e "${RED}✗ Authentication failed${NC}"
        exit 1
    fi
}
```

- [ ] Implement course discovery:

```bash
find_placement_course() {
    echo -e "${YELLOW}Finding placement course...${NC}"

    response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/courses")

    # Extract course ID for "English Placement Test"
    PLACEMENT_COURSE_ID=$(echo "$response" | grep -o '"name":"English Placement Test"' -A 20 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

    if [[ ! -z "$PLACEMENT_COURSE_ID" ]]; then
        echo -e "${GREEN}✓ Found placement course (ID: ${PLACEMENT_COURSE_ID})${NC}"
    else
        echo -e "${RED}✗ Placement course not found${NC}"
        exit 1
    fi
}
```

**Output:** Working authentication and course discovery functions

---

### **Task 6: Implement Enrollment Creation**
**Duration:** ~15 minutes

**Actions:**
- [ ] Implement enrollment function:

```bash
create_enrollment() {
    echo -e "${YELLOW}Creating enrollment for placement course...${NC}"

    enrollment_data="{
        \"courseId\": ${PLACEMENT_COURSE_ID}
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "${enrollment_data}" \
        "${BACKEND_URL}${API_BASE}/enrollments")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" || "$status_code" == "201" ]]; then
        ENROLLMENT_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        echo -e "${GREEN}✓ Enrollment created (ID: ${ENROLLMENT_ID})${NC}"
    else
        echo -e "${RED}✗ Enrollment creation failed${NC}"
        echo "Response: $response_body"
        exit 1
    fi
}
```

**Output:** Working enrollment creation

---

### **Task 7: Implement Answer Creation Functions**
**Duration:** ~25 minutes

**Actions:**
- [ ] Implement TEXT answer creation:

```bash
create_text_answer() {
    local question_id="$1"
    local answer_content="$2"
    local enrollment_id="$3"
    local session_id="${4:-1}"
    local expected_result="$5"  # "correct" or "incorrect"

    answer_data="{
        \"questionId\": ${question_id},
        \"answerContent\": \"${answer_content}\",
        \"enrollmentId\": ${enrollment_id},
        \"sessionId\": ${session_id}
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "${answer_data}" \
        "${BACKEND_URL}${API_BASE}/answers")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" || "$status_code" == "201" ]]; then
        answer_id=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        TEXT_ANSWERS+=("${answer_id}|${expected_result}")
        echo -e "${GREEN}✓ TEXT answer created (ID: ${answer_id}, Expected: ${expected_result})${NC}"
    else
        echo -e "${RED}✗ TEXT answer creation failed${NC}"
    fi
}
```

- [ ] Implement CHOICE answer creation:

```bash
create_choice_answer() {
    local question_id="$1"
    local choice_content="$2"
    local enrollment_id="$3"
    local session_id="${4:-1}"
    local expected_result="$5"  # "correct" or "incorrect"

    answer_data="{
        \"questionId\": ${question_id},
        \"answerContent\": \"${choice_content}\",
        \"enrollmentId\": ${enrollment_id},
        \"sessionId\": ${session_id}
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "${answer_data}" \
        "${BACKEND_URL}${API_BASE}/answers")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" || "$status_code" == "201" ]]; then
        answer_id=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        CHOICE_ANSWERS+=("${answer_id}|${expected_result}")
        echo -e "${GREEN}✓ CHOICE answer created (ID: ${answer_id}, Expected: ${expected_result})${NC}"
    else
        echo -e "${RED}✗ CHOICE answer creation failed${NC}"
    fi
}
```

**Output:** Working answer creation functions

---

### **Task 8: Implement Grading Function**
**Duration:** ~20 minutes

**Actions:**
- [ ] Implement grading function with validation:

```bash
grade_answer() {
    local answer_id="$1"
    local expected_result="$2"  # "correct" or "incorrect"
    local question_type="$3"    # "TEXT" or "CHOICE"
    local test_id="$4"          # Test case ID

    echo -e "${YELLOW}[${test_id}] Grading answer ${answer_id} (${question_type}, expected: ${expected_result})${NC}"

    response=$(curl -s -w "\n%{http_code}" -X PUT \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        "${BACKEND_URL}${API_BASE}/answers/grade/${answer_id}")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    # Validate status code
    if [[ "$status_code" != "200" ]]; then
        echo -e "${RED}  ✗ FAILED - HTTP ${status_code}${NC}"
        TEST_RESULTS+=("${test_id}|FAILED|Grade ${question_type} answer ${answer_id} - HTTP ${status_code}")
        FAILED_COUNT=$((FAILED_COUNT + 1))
        return
    fi

    # Extract pointAchieved from response
    point_achieved=$(echo "$response_body" | grep -o '"pointAchieved":[0-9]*' | cut -d':' -f2)

    # Validate pointAchieved based on expected result
    if [[ "$expected_result" == "correct" ]]; then
        # Expected: full points (10)
        if [[ "$point_achieved" == "10" ]]; then
            echo -e "${GREEN}  ✓ PASSED - Correct answer got full points (10)${NC}"
            TEST_RESULTS+=("${test_id}|PASSED|Grade ${question_type} answer ${answer_id} - Correct (10 points)")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - Expected 10 points, got ${point_achieved}${NC}"
            TEST_RESULTS+=("${test_id}|FAILED|Grade ${question_type} answer ${answer_id} - Expected 10, got ${point_achieved}")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        # Expected: zero points (0)
        if [[ "$point_achieved" == "0" ]]; then
            echo -e "${GREEN}  ✓ PASSED - Incorrect answer got zero points (0)${NC}"
            TEST_RESULTS+=("${test_id}|PASSED|Grade ${question_type} answer ${answer_id} - Incorrect (0 points)")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - Expected 0 points, got ${point_achieved}${NC}"
            TEST_RESULTS+=("${test_id}|FAILED|Grade ${question_type} answer ${answer_id} - Expected 0, got ${point_achieved}")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    fi

    # Validate response structure
    if ! echo "$response_body" | grep -q '"statusCode"' || \
       ! echo "$response_body" | grep -q '"message"' || \
       ! echo "$response_body" | grep -q '"data"'; then
        echo -e "${YELLOW}  ⚠ WARNING - Response structure incomplete${NC}"
    fi
}
```

**Output:** Complete grading function with validation

---

### **Task 9: Implement Test Case Functions**
**Duration:** ~30 minutes

**Actions:**
- [ ] Implement TEXT question testing:

```bash
test_text_questions() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  TEXT QUESTIONS TESTING${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # Note: Question IDs need to be discovered dynamically from database
    # For now, we'll use placeholders and document the approach

    # TEXT Question 1: Rewrite with "Tired"
    # Correct: "Tired as he was, he agreed to help me with my homework"
    echo -e "${YELLOW}Creating TEXT answers for testing...${NC}"

    # Correct answer (normalized should match)
    create_text_answer $TEXT_Q1_ID "Tired as he was, he agreed to help me with my homework" $ENROLLMENT_ID 1 "correct"

    # Incorrect answer (different text)
    create_text_answer $TEXT_Q1_ID "He was tired but helped me anyway" $ENROLLMENT_ID 1 "incorrect"

    # Correct answer with different case (should be normalized)
    create_text_answer $TEXT_Q1_ID "TIRED AS HE WAS, HE AGREED TO HELP ME WITH MY HOMEWORK" $ENROLLMENT_ID 1 "correct"

    # Correct answer with extra spaces (should be normalized)
    create_text_answer $TEXT_Q1_ID "  Tired as he was, he agreed to help me with my homework.  " $ENROLLMENT_ID 1 "correct"

    # TEXT Question 2-5: Similar pattern...

    echo ""
    echo -e "${YELLOW}Grading TEXT answers...${NC}"

    # Grade all TEXT answers
    local test_id=1
    for answer in "${TEXT_ANSWERS[@]}"; do
        IFS='|' read -r answer_id expected_result <<< "$answer"
        grade_answer "$answer_id" "$expected_result" "TEXT" "TEXT-$(printf '%03d' $test_id)"
        test_id=$((test_id + 1))
    done
}
```

- [ ] Implement CHOICE question testing:

```bash
test_choice_questions() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  CHOICE QUESTIONS TESTING${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # CHOICE Question 1: "critical" → correct: "supportive"
    echo -e "${YELLOW}Creating CHOICE answers for testing...${NC}"

    # Correct answer
    create_choice_answer $CHOICE_Q1_ID "supportive" $ENROLLMENT_ID 1 "correct"

    # Incorrect answer
    create_choice_answer $CHOICE_Q1_ID "intolerant" $ENROLLMENT_ID 1 "incorrect"

    # Correct answer with different case (should be normalized)
    create_choice_answer $CHOICE_Q1_ID "SUPPORTIVE" $ENROLLMENT_ID 1 "correct"

    # CHOICE Question 2-9: Similar pattern...

    echo ""
    echo -e "${YELLOW}Grading CHOICE answers...${NC}"

    # Grade all CHOICE answers
    local test_id=1
    for answer in "${CHOICE_ANSWERS[@]}"; do
        IFS='|' read -r answer_id expected_result <<< "$answer"
        grade_answer "$answer_id" "$expected_result" "CHOICE" "CHOICE-$(printf '%03d' $test_id)"
        test_id=$((test_id + 1))
    done
}
```

**Output:** Complete test case functions

---

### **Task 10: Implement Question Discovery**
**Duration:** ~25 minutes

**Actions:**
- [ ] Implement function to dynamically discover question IDs:

```bash
discover_placement_questions() {
    echo -e "${YELLOW}Discovering placement questions...${NC}"

    # Get course details with lessons
    response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/courses/${PLACEMENT_COURSE_ID}")

    # Extract lesson IDs
    # Lesson 1: "(PLACEMENT) Text - Reading" → TEXT questions
    # Lesson 2: "(PLACEMENT) Choice - Reading" → CHOICE questions

    TEXT_LESSON_ID=$(echo "$response" | grep -B5 '"name":"(PLACEMENT) Text - Reading"' | grep -o '"id":[0-9]*' | cut -d':' -f2)
    CHOICE_LESSON_ID=$(echo "$response" | grep -B5 '"name":"(PLACEMENT) Choice - Reading"' | grep -o '"id":[0-9]*' | cut -d':' -f2)

    echo -e "${GREEN}✓ TEXT Lesson ID: ${TEXT_LESSON_ID}${NC}"
    echo -e "${GREEN}✓ CHOICE Lesson ID: ${CHOICE_LESSON_ID}${NC}"

    # Get TEXT questions
    text_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/lessons/${TEXT_LESSON_ID}")

    # Extract TEXT question IDs (parse JSON)
    # Store in array: TEXT_QUESTION_IDS

    # Get CHOICE questions
    choice_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/lessons/${CHOICE_LESSON_ID}")

    # Extract CHOICE question IDs
    # Store in array: CHOICE_QUESTION_IDS

    echo -e "${GREEN}✓ Found ${#TEXT_QUESTION_IDS[@]} TEXT questions${NC}"
    echo -e "${GREEN}✓ Found ${#CHOICE_QUESTION_IDS[@]} CHOICE questions${NC}"
}
```

**Output:** Dynamic question discovery function

---

### **Task 11: Implement Report Generation**
**Duration:** ~15 minutes

**Actions:**
- [ ] Implement report generation:

```bash
generate_report() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  TEST EXECUTION SUMMARY${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    local total_tests=$((PASSED_COUNT + FAILED_COUNT))

    echo "Test Statistics:"
    echo "  Total Tests: ${total_tests}"
    echo -e "  ${GREEN}Passed: ${PASSED_COUNT}${NC}"
    echo -e "  ${RED}Failed: ${FAILED_COUNT}${NC}"
    echo ""

    if [[ $total_tests -gt 0 ]]; then
        success_rate=$(awk "BEGIN {printf \"%.1f\", ($PASSED_COUNT / $total_tests) * 100}")
        echo "Success Rate: ${success_rate}%"
    fi
    echo ""

    echo "Detailed Results:"
    echo "----------------------------------------"

    for result in "${TEST_RESULTS[@]}"; do
        IFS='|' read -r test_id status description <<< "$result"

        case "$status" in
            "PASSED")
                echo -e "${GREEN}✓${NC} ${test_id}: ${description}"
                ;;
            "FAILED")
                echo -e "${RED}✗${NC} ${test_id}: ${description}"
                ;;
        esac
    done

    echo "----------------------------------------"
    echo ""

    # Save report to file
    local report_file="test-grade-answer-report-$(date +%Y%m%d-%H%M%S).txt"
    {
        echo "========================================="
        echo "Grade Answer API Test Execution Report"
        echo "========================================="
        echo ""
        echo "Generated: $(date)"
        echo "Course: English Placement Test"
        echo "User: ${DEFAULT_EMAIL}"
        echo ""
        echo "Summary:"
        echo "  Total Tests: ${total_tests}"
        echo "  Passed: ${PASSED_COUNT}"
        echo "  Failed: ${FAILED_COUNT}"
        if [[ $total_tests -gt 0 ]]; then
            echo "  Success Rate: ${success_rate}%"
        fi
        echo ""
        echo "Detailed Results:"
        echo "-----------------------------------------"
        for result in "${TEST_RESULTS[@]}"; do
            IFS='|' read -r test_id status description <<< "$result"
            echo "[$status] $test_id: $description"
        done
        echo "-----------------------------------------"
    } > "$report_file"

    echo "Report saved to: ${report_file}"
}
```

**Output:** Complete report generation function

---

### **Task 12: Implement Main Execution Flow**
**Duration:** ~10 minutes

**Actions:**
- [ ] Implement main function:

```bash
main() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  GRADE ANSWER API TEST SUITE${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
    echo "Test Target: English Placement Test"
    echo "User: ${DEFAULT_EMAIL}"
    echo "Backend: ${BACKEND_URL}"
    echo ""

    # SETUP PHASE
    echo -e "${BLUE}>>> SETUP PHASE${NC}"
    get_auth_token
    find_placement_course
    discover_placement_questions
    echo ""

    # ENROLLMENT PHASE
    echo -e "${BLUE}>>> ENROLLMENT PHASE${NC}"
    create_enrollment
    echo ""

    # TEST PHASE
    echo -e "${BLUE}>>> TESTING PHASE${NC}"
    test_text_questions
    echo ""
    test_choice_questions
    echo ""

    # REPORT PHASE
    echo -e "${BLUE}>>> REPORT PHASE${NC}"
    generate_report
    echo ""

    # COMPLETION
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  TEST SUITE COMPLETED${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""

    if [[ $FAILED_COUNT -eq 0 ]]; then
        echo -e "${GREEN}All tests passed!${NC}"
    else
        echo -e "${RED}Some tests failed. Please review the report.${NC}"
    fi
}

# Run main
main
```

**Output:** Complete main execution flow

---

### **Task 13: Create Updated Testcase Document for Placement**
**Duration:** ~30 minutes

**Actions:**
- [ ] Create `documents/output/Testcase_API_GradeAnswer_Placement.md`
- [ ] Base on original testcase but with placement-specific data
- [ ] Update test cases to reference actual placement questions:

**Test Case Structure:**

| Category | Count | Test IDs | Description |
|----------|-------|----------|-------------|
| **Validate** | 10 | ID-001 ~ ID-010 | HTTP methods, authentication, path validation |
| **Logic - TEXT** | 10 | ID-011 ~ ID-020 | TEXT question grading with normalization |
| **Logic - CHOICE** | 10 | ID-021 ~ ID-030 | CHOICE question grading with case handling |
| **TOTAL** | **30** | | |

**Sample Test Cases:**

```markdown
## TEXT Question Test Cases

| ID | Test Name | Question | Correct Answer | Test Data | Expected Points |
|----|-----------|----------|----------------|-----------|-----------------|
| ID-011 | Grade TEXT Q1 - Correct | Rewrite with "Tired" | "Tired as he was, he agreed to help me with my homework" | Same as correct | 10 |
| ID-012 | Grade TEXT Q1 - Incorrect | Rewrite with "Tired" | (same) | "He was tired" | 0 |
| ID-013 | Grade TEXT Q1 - Case insensitive | (same) | (same) | "TIRED AS HE WAS..." | 10 |
| ID-014 | Grade TEXT Q1 - Whitespace | (same) | (same) | "  Tired as...  " | 10 |
| ID-015 | Grade TEXT Q1 - Punctuation | (same) | (same) | "Tired as he was..." | 10 |
...

## CHOICE Question Test Cases

| ID | Test Name | Question | Correct Answer | Test Data | Expected Points |
|----|-----------|----------|----------------|-----------|-----------------|
| ID-021 | Grade CHOICE Q1 - Correct | OPPOSITE of "critical" | "supportive" | "supportive" | 10 |
| ID-022 | Grade CHOICE Q1 - Incorrect | (same) | "supportive" | "intolerant" | 0 |
| ID-023 | Grade CHOICE Q1 - Case insensitive | (same) | "supportive" | "SUPPORTIVE" | 10 |
...
```

**Output:** Complete placement-specific testcase document

---

### **Task 14: Create Documentation (README)**
**Duration:** ~20 minutes

**Actions:**
- [ ] Create `backend-service/TEST_GRADE_ANSWER_README.md`
- [ ] Include:
  - Overview and purpose
  - Prerequisites
  - Usage instructions
  - Test flow diagram
  - Expected results
  - Troubleshooting
  - API endpoints reference

**Sections:**

```markdown
# Grade Answer API Test Suite

## Overview
Automated test script for Grade Answer API using Placement Course data.

## Prerequisites
1. Backend service running
2. Database seeded with placement course
3. Default user created (user@example.com)

## Usage
```bash
cd backend-service
chmod +x test-grade-answer.sh
./test-grade-answer.sh
```

## Test Flow
1. Authenticate user
2. Find placement course
3. Create enrollment
4. Discover questions (TEXT & CHOICE)
5. Create test answers (correct + incorrect)
6. Grade all answers
7. Validate results
8. Generate report

## Expected Results
- TEXT questions: 5 questions, ~10 test cases
- CHOICE questions: 9 questions, ~18 test cases
- Total: ~28 grading API calls
- Success rate: 95%+

## Troubleshooting
...
```

**Output:** Complete README documentation

---

## ✅ Quality Checklist

Before completion:
- [ ] Script can authenticate successfully
- [ ] Script can find placement course
- [ ] Script can create enrollment
- [ ] Script can discover question IDs dynamically
- [ ] Script can create TEXT answers
- [ ] Script can create CHOICE answers
- [ ] Script can grade answers
- [ ] Script validates grading results correctly
- [ ] Script generates detailed report
- [ ] Script handles errors gracefully
- [ ] Colored console output working
- [ ] Test data uses actual placement questions
- [ ] Normalization test cases included
- [ ] Documentation complete
- [ ] README with troubleshooting guide

---

## 📦 Final Deliverables

**Files to be created:**

1. **Test Script:**
   ```
   backend-service/test-grade-answer.sh
   ```
   - Complete bash script (~600-700 lines)
   - All test functions implemented
   - Dynamic question discovery
   - Report generation
   - Error handling

2. **Testcase Document:**
   ```
   documents/output/Testcase_API_GradeAnswer_Placement.md
   ```
   - 30 test cases for placement course
   - TEXT and CHOICE question specific
   - Actual question data referenced
   - Expected results documented

3. **Documentation:**
   ```
   backend-service/TEST_GRADE_ANSWER_README.md
   ```
   - Usage guide
   - Prerequisites
   - Troubleshooting
   - API reference

---

## 🎯 Success Criteria

### Test Script
- ✅ Authenticates with user@example.com
- ✅ Finds and enrolls in placement course
- ✅ Creates answers for TEXT questions (correct + incorrect)
- ✅ Creates answers for CHOICE questions (correct + incorrect)
- ✅ Grades all answers via API
- ✅ Validates pointAchieved values
- ✅ Tests normalization (case, whitespace, punctuation)
- ✅ Generates colored console output
- ✅ Creates detailed test report file
- ✅ Handles errors gracefully

### Testcase Document
- ✅ References actual placement questions
- ✅ Includes expected correct answers
- ✅ Documents normalization test cases
- ✅ 30 test cases total
- ✅ Clear preconditions and expected results

### Documentation
- ✅ Clear usage instructions
- ✅ Prerequisites documented
- ✅ Test flow diagram included
- ✅ Troubleshooting guide complete
- ✅ API endpoints documented

---

## 🔄 Test Coverage

### TEXT Questions (5 questions × 2-3 test cases each = ~10-15 tests)

| Question # | Content | Correct Answer | Test Cases |
|------------|---------|----------------|------------|
| 1 | Rewrite with "Tired" | "Tired as he was..." | Correct, Incorrect, Case-insensitive, Whitespace |
| 2 | Relative clauses | "The man whose daughter..." | Correct, Incorrect, Punctuation |
| 3 | Apologize sentence | "Martin apologized..." | Correct, Incorrect |
| 4 | Passive voice | "Maradona is thought..." | Correct, Incorrect |
| 5 | Conditional | "If it hadn't been..." | Correct, Incorrect |

### CHOICE Questions (9 questions × 2-3 test cases each = ~18-27 tests)

| Question # | Content | Correct Answer | Test Cases |
|------------|---------|----------------|------------|
| 1 | OPPOSITE "critical" | "supportive" | Correct, Incorrect, Case-insensitive |
| 2 | OPPOSITE "post" | "displace" | Correct, Incorrect |
| 3 | CLOSEST "efficient" | "successful" | Correct, Incorrect |
| 4 | CLOSEST "endeavoring" | "trying" | Correct, Incorrect |
| 5 | Grammar | "narrow-minded" | Correct, Incorrect |
| 6 | Dialogue | "OK, let me just check..." | Correct, Incorrect |
| 7 | Tense | "went / have not been" | Correct, Incorrect |
| 8 | Phrasal verb | "look up" | Correct, Incorrect |
| 9 | Comparative | "The harder/ the better" | Correct, Incorrect |

**Total Test Cases:** ~28-42 grading API calls

---

## ⏱️ Estimated Time Breakdown

| Task | Duration | Status |
|------|----------|--------|
| Task 1: Investigate placement data | 15 min | ⏳ Pending |
| Task 2: Design test flow | 20 min | ⏳ Pending |
| Task 3: Identify API endpoints | 10 min | ⏳ Pending |
| Task 4: Create script structure | 20 min | ⏳ Pending |
| Task 5: Implement auth & discovery | 15 min | ⏳ Pending |
| Task 6: Implement enrollment | 15 min | ⏳ Pending |
| Task 7: Implement answer creation | 25 min | ⏳ Pending |
| Task 8: Implement grading function | 20 min | ⏳ Pending |
| Task 9: Implement test cases | 30 min | ⏳ Pending |
| Task 10: Implement question discovery | 25 min | ⏳ Pending |
| Task 11: Implement report generation | 15 min | ⏳ Pending |
| Task 12: Implement main flow | 10 min | ⏳ Pending |
| Task 13: Create testcase document | 30 min | ⏳ Pending |
| Task 14: Create README | 20 min | ⏳ Pending |
| **Total** | **~4 hours 30 minutes** | |

---

## 📝 Implementation Notes

### Important Considerations

1. **Dynamic Question Discovery**
   - Question IDs change per deployment
   - Must discover IDs via API calls
   - Cannot hardcode question IDs

2. **Answer Creation**
   - Requires enrollment ID
   - Requires session ID (can default to 1)
   - answerContent must match question type format

3. **Grading Validation**
   - Correct answers should get full points (10)
   - Incorrect answers should get zero points (0)
   - Response structure must be validated

4. **Normalization Testing**
   - TEXT: Test case, whitespace, punctuation
   - CHOICE: Test case insensitivity
   - Both: Trim and normalize

5. **Error Handling**
   - Auth failures
   - Course not found
   - Enrollment failures
   - Answer creation failures
   - Grading API errors

6. **Test Data**
   - Use actual correct answers from JSON files
   - Create realistic incorrect answers
   - Test edge cases (empty, special chars)

7. **Reporting**
   - Colored console output
   - Detailed test report file
   - Statistics and success rate
   - Individual test results

---

## 🚀 Execution Order

1. ✅ **Task 1:** Investigate placement data (Completed during planning)
2. **Task 2:** Design test flow
3. **Task 3:** Identify API endpoints
4. **Task 4:** Create script structure
5. **Task 5:** Implement authentication & course discovery
6. **Task 6:** Implement enrollment creation
7. **Task 7:** Implement answer creation functions
8. **Task 8:** Implement grading function
9. **Task 9:** Implement test case functions
10. **Task 10:** Implement question discovery
11. **Task 11:** Implement report generation
12. **Task 12:** Implement main execution flow
13. **Task 13:** Create placement-specific testcase document
14. **Task 14:** Create README documentation

---

## 🔍 Key Differences from req-5

| Aspect | req-5 (Pronunciation) | req-6 (Grade Answer) |
|--------|----------------------|----------------------|
| **API Type** | POST with file upload | PUT with path parameter |
| **Test Data** | Single audio file + text | Multiple questions from DB |
| **Setup** | Simple (just auth) | Complex (auth + enroll + discover) |
| **Test Flow** | Direct API calls | Create answers → Grade answers |
| **Validation** | Response structure only | Response + pointAchieved values |
| **Test Count** | 30 test cases (API level) | ~28-42 grading calls (data level) |
| **Complexity** | Medium | High |

---

## ✅ Ready to Execute

**Status:** ✅ Plan Ready for Execution

This is a complete, detailed plan for:
1. Creating automated test script for Grade Answer API
2. Using real placement course data
3. Testing TEXT and CHOICE questions only
4. Documenting testcase for placement course

**Note:** This is PLAN ONLY. Implementation will follow this plan step by step.

---

**END OF PLAN - req-6**
