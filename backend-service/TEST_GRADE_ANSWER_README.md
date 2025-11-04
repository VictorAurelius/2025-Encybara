# Grade Answer API Test Suite

Automated test script for Grade Answer API based on English Placement Test course.

## 📋 Overview

This test suite automates grading API testing using real data from the English Placement Test course. It tests the complete flow from user authentication to answer grading with both TEXT and CHOICE question types.

**Test Coverage:**
- **Total:** 30 test cases (all automated)
- **TEXT Questions:** 5 questions, ~15 test cases
- **CHOICE Questions:** 9 questions, ~27 test cases
- **Total Grading API Calls:** ~42 per execution

## 📊 Test Categories

| Category | Test IDs | Count | Description |
|----------|----------|-------|-------------|
| **Validate** | ID-001 ~ ID-010 | 10 | HTTP methods, authentication, authorization, path validation |
| **Logic - TEXT** | ID-011 ~ ID-020 | 10 | TEXT question grading with normalization (case, whitespace, punctuation) |
| **Logic - CHOICE** | ID-021 ~ ID-030 | 10 | CHOICE question grading with case-insensitive matching |

## 🚀 Prerequisites

### 1. Backend Service
```bash
# Backend must be running on port 8080
curl http://localhost:8080/actuator/health
```

### 2. Database with Placement Course
The database must be seeded with the English Placement Test course. This happens automatically on application startup via `AdminDataInitializer.java`.

**Verify placement course exists:**
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/courses | grep "English Placement Test"
```

### 3. Test User Account
- Email: `user@example.com`
- Password: `Abc@123456`
- (Created automatically by AdminDataInitializer on startup)

## 📖 Usage

### Basic Usage
```bash
cd backend-service
chmod +x test-grade-answer.sh
./test-grade-answer.sh
```

### With Custom Configuration
```bash
# Override default settings via environment variables
BACKEND_URL=http://localhost:8080 \
DEFAULT_EMAIL=test@example.com \
DEFAULT_PASSWORD=password123 \
./test-grade-answer.sh
```

## 📄 Output

### Console Output
The script provides colored console output showing 4 phases:

#### Phase 1: Setup
- ✓ **Green:** Authentication successful
- ✓ **Green:** Placement course found
- ✓ **Green:** Questions discovered

#### Phase 2: Enrollment
- ✓ **Green:** Enrollment created
- ⚠ **Yellow:** Warning if already enrolled (uses existing enrollment)

#### Phase 3: Testing
- **Cyan:** Answer creation progress
- ✓ **Green:** Test PASSED (grading result matches expected)
- ✗ **Red:** Test FAILED (grading result doesn't match expected)

#### Phase 4: Report
- Summary statistics
- Success rate calculation
- Detailed test results

### Example Output
```
============================================
  GRADE ANSWER API TEST SUITE
============================================

Target: English Placement Test
Test Types: TEXT & CHOICE questions only
User: user@example.com
Backend: http://localhost:8080

Test Flow:
  1. Authenticate user
  2. Find placement course
  3. Create enrollment
  4. Discover questions
  5. Create test answers
  6. Grade all answers
  7. Validate results
  8. Generate report

============================================

>>> PHASE 1: SETUP

>>> Authenticating user...
✓ Authentication successful
  Token: eyJhbGciOiJIUzI1NiIsInR5cCI6...

>>> Finding placement course...
✓ Found placement course
  Course ID: 1
  Course Name: English Placement Test

>>> Discovering placement questions...
✓ Found TEXT lesson
  Lesson ID: 1
  Lesson Name: (PLACEMENT) Text - Reading
  TEXT Questions: 5 found
✓ Found CHOICE lesson
  Lesson ID: 2
  Lesson Name: (PLACEMENT) Choice - Reading
  CHOICE Questions: 9 found

>>> PHASE 2: ENROLLMENT

>>> Creating enrollment...
✓ Enrollment created
  Enrollment ID: 123

>>> PHASE 3: TESTING

========================================
  TEXT QUESTIONS TESTING
========================================

Creating TEXT test answers...

TEXT Question 0: ID 45
  → Created TEXT answer (ID: 201, Expected: correct)
  → Created TEXT answer (ID: 202, Expected: incorrect)
  → Created TEXT answer (ID: 203, Expected: correct)

Grading TEXT answers...

[TEXT-001] Grading TEXT answer 201 (expected: correct)
  ✓ PASSED - Correct answer got full points (10)

[TEXT-002] Grading TEXT answer 202 (expected: incorrect)
  ✓ PASSED - Incorrect answer got zero points (0)

[TEXT-003] Grading TEXT answer 203 (expected: correct)
  ✓ PASSED - Correct answer got full points (10)

========================================
  CHOICE QUESTIONS TESTING
========================================

...

>>> PHASE 4: REPORT

========================================
  TEST EXECUTION SUMMARY
========================================

Course: English Placement Test
User: user@example.com
Enrollment ID: 123

Questions Tested:
  TEXT Questions: 5 questions, 15 test cases
  CHOICE Questions: 9 questions, 27 test cases

Test Statistics:
  Total Grading Calls: 42
  Passed: 42
  Failed: 0

Success Rate: 100.0%

Report saved to: test-grade-answer-report-20251104-143022.txt

============================================
  TEST SUITE COMPLETED
============================================

✓ All tests passed successfully!
```

### Test Report File
A detailed report is saved to: `test-grade-answer-report-YYYYMMDD-HHMMSS.txt`

**Report Contents:**
- Execution timestamp and configuration
- Course and enrollment details
- Summary statistics (total tests, passed, failed, success rate)
- Detailed results for each test case
- Question IDs discovered during execution

## 🔧 Configuration

### Default Configuration
```bash
BACKEND_URL="http://localhost:8080"
API_BASE="/api/v1"
DEFAULT_EMAIL="user@example.com"
DEFAULT_PASSWORD="Abc@123456"
PLACEMENT_COURSE_NAME="English Placement Test"
```

### Modifying Configuration
Edit the script's configuration section (lines 44-48):
```bash
# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
API_BASE="/api/v1"
DEFAULT_EMAIL="${DEFAULT_EMAIL:-user@example.com}"
DEFAULT_PASSWORD="${DEFAULT_PASSWORD:-Abc@123456}"
```

Or use environment variables:
```bash
export BACKEND_URL="http://myserver:8080"
export DEFAULT_EMAIL="admin@example.com"
./test-grade-answer.sh
```

## 🧪 Test Details

### Test Flow

```
┌─────────────────────────────────────────────────┐
│  Phase 1: Setup                                 │
├─────────────────────────────────────────────────┤
│  1. Authenticate user (POST /auth/login)        │
│     → Get JWT access token                      │
│  2. Find placement course (GET /courses)        │
│     → Extract course ID                         │
│  3. Discover questions (GET /lessons/{id})      │
│     → Get TEXT lesson → Extract question IDs    │
│     → Get CHOICE lesson → Extract question IDs  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  Phase 2: Enrollment                            │
├─────────────────────────────────────────────────┤
│  1. Create enrollment (POST /enrollments)       │
│     → If already enrolled, fetch existing       │
│     → Store enrollment ID                       │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  Phase 3: Testing                               │
├─────────────────────────────────────────────────┤
│  For each TEXT question:                        │
│    1. Create answer: correct                    │
│    2. Create answer: incorrect                  │
│    3. Create answer: correct (uppercase)        │
│  For each CHOICE question:                      │
│    1. Create answer: correct                    │
│    2. Create answer: incorrect                  │
│    3. Create answer: correct (uppercase)        │
│                                                  │
│  Then grade all created answers:                │
│    - Call PUT /answers/grade/{answerId}         │
│    - Validate pointAchieved value               │
│    - Validate response structure                │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  Phase 4: Report                                │
├─────────────────────────────────────────────────┤
│  1. Display summary statistics                  │
│  2. Show detailed test results                  │
│  3. Calculate success rate                      │
│  4. Save report to file                         │
└─────────────────────────────────────────────────┘
```

### TEXT Question Test Cases

For each TEXT question, the script creates and grades 3 answers:

1. **Correct answer (exact)** → Expected: 10 points
   ```
   Answer: "Tired as he was, he agreed to help me with my homework"
   ```

2. **Incorrect answer** → Expected: 0 points
   ```
   Answer: "This is an incorrect answer"
   ```

3. **Correct answer (uppercase)** → Expected: 10 points (tests normalization)
   ```
   Answer: "TIRED AS HE WAS, HE AGREED TO HELP ME WITH MY HOMEWORK"
   ```

**TEXT Questions in Placement:**
- Q1: Rewrite with "Tired" → "Tired as he was, he agreed to help me with my homework"
- Q2: Relative clauses → "The man whose daughter is fond of dancing works for my father's company"
- Q3: Apologize → "Martin apologized to Angela for having damaged her car"
- Q4: Passive voice → "Maradona is thought to be the best football player in the 20th century"
- Q5: Conditional → "If it hadn't been for your help, I couldn't overcome the problem"

### CHOICE Question Test Cases

For each CHOICE question, the script creates and grades 3 answers:

1. **Correct choice** → Expected: 10 points
   ```
   Answer: "supportive"
   ```

2. **Incorrect choice** → Expected: 0 points
   ```
   Answer: "intolerant"
   ```

3. **Correct choice (uppercase)** → Expected: 10 points (tests case-insensitivity)
   ```
   Answer: "SUPPORTIVE"
   ```

**CHOICE Questions in Placement:**
- Q1: OPPOSITE "critical" → "supportive"
- Q2: OPPOSITE "post" → "displace"
- Q3: CLOSEST "efficient" → "successful"
- Q4: CLOSEST "endeavoring" → "trying"
- Q5: Grammar → "narrow-minded"
- Q6: Dialogue → "OK, let me just check the diary."
- Q7: Verb tense → "went / have not been"
- Q8: Phrasal verb → "look up"
- Q9: Comparative → "The harder/ the better"

## 🎯 Expected Results

**Typical Test Run:**
```
Total Test Cases: 42
  TEXT test cases: 15 (5 questions × 3 test cases each)
  CHOICE test cases: 27 (9 questions × 3 test cases each)

Expected Results:
  Passed: 42
  Failed: 0
  Success Rate: 100%
```

**Test Validation:**
- ✅ Correct answers (exact match) → 10 points
- ✅ Correct answers (normalized: uppercase, whitespace) → 10 points
- ✅ Incorrect answers → 0 points
- ✅ Response structure contains required fields
- ✅ HTTP 200 OK for successful grading

**Note:** Some tests may fail if:
- Database is not properly seeded
- Placement course structure has changed
- Correct answers in code don't match database
- API response structure has changed

## ❗ Troubleshooting

### "Authentication failed"
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Verify user credentials
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"Abc@123456"}'

# Check AdminDataInitializer logs for user creation
```

### "Placement course not found"
```bash
# Check if course exists in database
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/courses | grep "English Placement Test"

# Verify course seeding in logs
docker logs backend-service 2>&1 | grep "Loading PLACEMENT course"

# Check data files exist
ls -la backend-service/src/main/resources/data/placement/json/test1/paper1/
```

### "No questions found in lessons"
```bash
# Verify lesson structure
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/lessons/{lessonId}

# Check question files are loaded
docker logs backend-service 2>&1 | grep "Loaded.*questions"

# Verify question data files
cat backend-service/src/main/resources/data/placement/json/test1/paper1/question-1.json
cat backend-service/src/main/resources/data/placement/json/test1/paper1/question-2.json
```

### "Enrollment creation failed"
```bash
# Check if user is already enrolled
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/enrollments

# Try to delete existing enrollment (if needed)
curl -X DELETE -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/enrollments/{enrollmentId}
```

### "Grading API returns unexpected points"
```bash
# Verify correct answers in database
# Check question details
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/questions/{questionId}

# Check if answers match expected format
# TEXT: normalized (lowercase, trim, no extra spaces, no trailing punctuation)
# CHOICE: case-insensitive exact match
```

### "Connection Refused"
```bash
# Check backend is accessible
netstat -an | grep 8080

# Check if service is running in Docker
docker ps | grep backend

# Try accessing health endpoint
curl -v http://localhost:8080/actuator/health
```

## 🔍 Debugging

### Enable Verbose Output
Modify the script to add `-v` flag to curl commands:
```bash
# Edit grade_answer function (around line 290)
response=$(curl -v -s -w "\n%{http_code}" -X PUT \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json" \
    "${BACKEND_URL}${API_BASE}/answers/grade/${answer_id}")
```

### Test Specific Answer Manually
```bash
# Get token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"Abc@123456"}' \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

# Create an answer
ANSWER_ID=$(curl -s -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"questionId":45,"answerContent":"test answer","enrollmentId":1,"sessionId":1}' \
  http://localhost:8080/api/v1/answers \
  | grep -o '"id":[0-9]*' | cut -d':' -f2)

# Grade the answer
curl -X PUT \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/answers/grade/$ANSWER_ID
```

### View Backend Logs
```bash
# If using Docker
docker-compose logs -f backend

# If running locally
tail -f logs/application.log

# Check for specific errors
docker logs backend-service 2>&1 | grep -i "error\|exception"

# Check grading logic
docker logs backend-service 2>&1 | grep -i "grading\|answer"
```

### Check Test Report
```bash
# View latest test report
ls -lt test-grade-answer-report-*.txt | head -1 | xargs cat

# Or open in editor
vim $(ls -t test-grade-answer-report-*.txt | head -1)
```

## 📚 Grading Algorithm Details

### TEXT Question Normalization

**Steps:**
1. Trim leading/trailing whitespace
2. Convert to lowercase
3. Replace multiple spaces with single space
4. Remove trailing punctuation (`.`, `!`, `?`)

**Example:**
```
User Input:   "  TIRED AS  HE  WAS, HE AGREED...  "
Normalized:   "tired as he was, he agreed"

Correct:      "Tired as he was, he agreed to help me with my homework."
Normalized:   "tired as he was, he agreed to help me with my homework"

Result:       No match → 0 points
```

**Binary Grading:**
- Exact match after normalization → **10 points**
- Any difference → **0 points**
- No partial credit

### CHOICE Question Normalization

**Steps:**
1. Trim leading/trailing whitespace
2. Convert to lowercase
3. Remove trailing punctuation

**Example:**
```
User Input:   "SUPPORTIVE "
Normalized:   "supportive"

Correct:      "supportive"
Normalized:   "supportive"

Result:       Match → 10 points
```

**Binary Grading:**
- Exact match after normalization → **10 points**
- Any difference → **0 points**

## 📖 API Endpoints Reference

### Authentication
```
POST /api/v1/auth/login
Content-Type: application/json

Request:
{
  "username": "user@example.com",
  "password": "Abc@123456"
}

Response:
{
  "statusCode": 200,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "user": { ... }
  }
}
```

### Get Courses
```
GET /api/v1/courses
Authorization: Bearer <token>

Response:
{
  "statusCode": 200,
  "data": [
    {
      "id": 1,
      "name": "English Placement Test",
      ...
    }
  ]
}
```

### Get Course Details
```
GET /api/v1/courses/{courseId}
Authorization: Bearer <token>

Response:
{
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "English Placement Test",
    "lessons": [
      { "lessonId": 1, ... },
      { "lessonId": 2, ... }
    ]
  }
}
```

### Get Lesson Details
```
GET /api/v1/lessons/{lessonId}
Authorization: Bearer <token>

Response:
{
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "(PLACEMENT) Text - Reading",
    "questions": [
      { "questionId": 45, ... },
      { "questionId": 46, ... }
    ]
  }
}
```

### Create Enrollment
```
POST /api/v1/enrollments
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "courseId": 1
}

Response:
{
  "statusCode": 200,
  "data": {
    "id": 123,
    "courseId": 1,
    "userId": 1,
    ...
  }
}
```

### Create Answer
```
POST /api/v1/answers
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "questionId": 45,
  "answerContent": "Tired as he was, he agreed to help me with my homework",
  "enrollmentId": 123,
  "sessionId": 1
}

Response:
{
  "statusCode": 200,
  "data": {
    "id": 201,
    "questionId": 45,
    "answerContent": "Tired as he was, he agreed to help me with my homework",
    "pointAchieved": null,
    "enrollmentId": 123,
    "sessionId": 1
  }
}
```

### Grade Answer
```
PUT /api/v1/answers/grade/{answerId}
Authorization: Bearer <token>

Response:
{
  "statusCode": 200,
  "error": null,
  "message": "Answer graded successfully",
  "data": {
    "id": 201,
    "questionId": 45,
    "answerContent": "Tired as he was, he agreed to help me with my homework",
    "pointAchieved": 10,
    "enrollmentId": 123,
    "sessionId": 1,
    "improvement": null
  }
}
```

## 🛠️ Extending the Test Suite

### Adding More Test Cases

1. **Add test cases to TEXT questions:**
   ```bash
   # Edit test_text_questions() function
   # Add more test variations:

   # Test with extra punctuation
   create_answer "$question_id" "${correct_answer}..." "correct" "TEXT"

   # Test with leading spaces
   create_answer "$question_id" "   ${correct_answer}" "correct" "TEXT"
   ```

2. **Add test cases to CHOICE questions:**
   ```bash
   # Edit test_choice_questions() function
   # Add more incorrect options:

   local another_wrong="${choice_incorrect_answers2[$q_index]}"
   create_answer "$question_id" "$another_wrong" "incorrect" "CHOICE"
   ```

### Testing Additional Question Types

To test MULTIPLE choice questions (currently excluded):

1. Modify `discover_placement_questions()` to find MULTIPLE lessons
2. Add `MULTIPLE_QUESTION_IDS` array
3. Create `test_multiple_questions()` function
4. Implement partial credit validation logic
5. Call in main flow

### Custom Validation Rules

Add custom validation in `grade_answer()` function:

```bash
# Check response time
start_time=$(date +%s%N)
response=$(curl ...)
end_time=$(date +%s%N)
duration=$((($end_time - $start_time) / 1000000))

if [[ $duration -gt 1000 ]]; then
    echo -e "${YELLOW}  ⚠ WARNING - Slow response (${duration}ms)${NC}"
fi

# Validate improvement field
if [[ "$point_achieved" == "0" ]]; then
    if ! echo "$response_body" | grep -q '"improvement"'; then
        echo -e "${YELLOW}  ⚠ WARNING - Missing improvement suggestion${NC}"
    fi
fi
```

## 📖 References

- **Testcase Document:** `documents/output/Testcase_API_GradeAnswer_Placement.md`
- **API Document:** `documents/output/API_Document_GradeAnswer.md`
- **Plan Task:** `documents/req-6.md`
- **Original Testcase:** `documents/output/Testcase_API_GradeAnswer.md`
- **Pronunciation Test Script:** `backend-service/test-pronunciation-testcase.sh` (similar pattern)

## 🤝 Contributing

To contribute to this test suite:
1. Update testcase document with new test cases
2. Add implementation to script functions
3. Update this README
4. Update test count totals
5. Test locally before committing

## 📝 Notes

- **Dynamic Discovery:** Question IDs are discovered dynamically at runtime and cannot be hardcoded
- **Binary Grading:** Both TEXT and CHOICE use binary grading (10 or 0 points)
- **Normalization:** Critical for TEXT and CHOICE matching - script tests this explicitly
- **Session ID:** Currently defaults to 1 for all answers
- **Enrollment Reuse:** Script reuses existing enrollment if user already enrolled in placement course
- **No MULTIPLE Testing:** Script explicitly excludes MULTIPLE choice questions as per requirements

## 📅 Changelog

### 2025-11-04
- Initial release
- 42 test cases implemented (all automated)
- Based on req-6 task plan
- Tests TEXT and CHOICE questions only
- Complete flow: auth → enrollment → answer creation → grading → validation
- Dynamic question discovery
- Normalization testing
- Colored console output
- Detailed report generation

---

**Last Updated:** 2025-11-04
**Version:** 1.0.0
**Author:** Generated based on req-6 task plan
