# API Test Suite Documentation

**Version:** 1.0
**Date:** 2025-11-07
**Related:** documents/req-7.md, documents/output/API_Document_V3.md

---

## Table of Contents

1. [Overview](#overview)
2. [Test Scripts](#test-scripts)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [Running Individual Tests](#running-individual-tests)
6. [Test Categories](#test-categories)
7. [Understanding Test Results](#understanding-test-results)
8. [Troubleshooting](#troubleshooting)
9. [Database Setup](#database-setup)
10. [CI/CD Integration](#cicd-integration)

---

## Overview

This test suite provides comprehensive automated testing for 4 core APIs in the Encybara backend service:

| Script | API | Endpoint | Tests | Complexity |
|--------|-----|----------|-------|------------|
| test-create-question.sh | CreateQuestion | POST /api/v1/questions | ~31 | Medium |
| test-notification.sh | Notification | POST /api/v1/notifications | ~26 | Low |
| test-add-questions-to-lesson.sh | AddQuestionsToLesson | POST /api/v1/lessons/{id}/questions | ~24 | Medium |
| test-review.sh | Review | POST /api/v1/reviews | ~28 | High |

**Total Test Coverage:** ~109 tests across all 4 APIs

### Test Categories

All scripts test the following categories:
- **VALIDATE** - Field validation (required, format, type)
- **LOGIC** - Business logic and rules
- **ERROR_CODE** - HTTP status codes (400, 401, 403, 404, 409, 500)
- **FORMAT_RESPONSE** - Response structure validation

---

## Test Scripts

### 1. test-create-question.sh

**Purpose:** Tests question creation with all question types and validation rules.

**Key Tests:**
- All question types: CHOICE, MULTIPLE, TEXT, LISTENING, WRITING, SPEAKING
- WRITING questions must have WRITING skillType
- SPEAKING questions must have SPEAKING skillType
- WRITING/SPEAKING questions cannot have choices
- Field validation (quesContent, point, keyword)

**Dependencies:** None (standalone)

**Run Time:** ~30-45 seconds

### 2. test-notification.sh

**Purpose:** Tests notification creation with all notification types.

**Key Tests:**
- All img ENUM values: STUDY, FLASHCARD, SCHEDULE, ACCOUNT
- Auto-set defaults (isRead=false, createdAt)
- Optional fields (entityId, entityType)
- Field validation

**Dependencies:** Valid user ID (auto-detected from login)

**Run Time:** ~25-35 seconds

### 3. test-add-questions-to-lesson.sh

**Purpose:** Tests adding questions to lessons with duplicate prevention.

**Key Tests:**
- Single and batch question addition
- Duplicate prevention (409 error)
- Transaction rollback on partial failure
- Lesson and question validation

**Dependencies:**
- Creates test lesson (auto-setup)
- Creates 3 test questions (auto-setup)

**Run Time:** ~40-60 seconds (includes setup)

### 4. test-review.sh

**Purpose:** Tests course review creation with enrollment validation.

**Key Tests:**
- All status types: CONTRIBUTING, CONTENT, MISTAKE
- Enrollment requirement validation
- 30% completion requirement
- Duplicate review prevention (409 error)
- Notification creation

**Dependencies:**
- User must be enrolled in a course (auto-detected or manual setup)
- Enrollment must have ≥30% completion (may require manual setup)

**Run Time:** ~45-70 seconds

**Note:** This is the most complex test. See [Review API Special Setup](#review-api-special-setup) for details.

---

## Prerequisites

### Required Software

1. **bash** (v4.0+)
   ```bash
   bash --version
   ```

2. **curl** (for API requests)
   ```bash
   curl --version
   ```

3. **jq** (JSON processor)
   ```bash
   jq --version
   ```

   **Install jq:**
   - Ubuntu/Debian: `sudo apt-get install jq`
   - macOS: `brew install jq`
   - CentOS/RHEL: `sudo yum install jq`

4. **Running Backend Server**
   - Backend must be running on `localhost:8080` (or configure with env vars)
   - Database must be accessible and populated

### Database Requirements

- Admin user must exist with credentials:
  - Email: `admin@gmail.com`
  - Password: `123456`
  - (Or configure with `USERNAME` and `PASSWORD` env vars)

- For test-review.sh:
  - At least one course must exist
  - User should be enrolled with ≥30% completion

---

## Quick Start

### Run All Tests

```bash
cd /mnt/e/person/project-1/2025-Encybara/backend-service

# Run all tests sequentially
./test-create-question.sh
./test-notification.sh
./test-add-questions-to-lesson.sh
./test-review.sh
```

### Run with Custom Backend URL

```bash
export BACKEND_URL="http://localhost:8080"
export USERNAME="admin@gmail.com"
export PASSWORD="123456"

./test-create-question.sh
```

### Run in One Command

```bash
for script in test-create-question.sh test-notification.sh test-add-questions-to-lesson.sh test-review.sh; do
    echo "Running $script..."
    ./$script
    echo ""
done
```

---

## Running Individual Tests

### Test 1: CreateQuestion API

```bash
./test-create-question.sh
```

**Expected Output:**
```
╔════════════════════════════════════════════════════════╗
║       CreateQuestion API - Automated Test Suite       ║
╚════════════════════════════════════════════════════════╝

Backend URL: http://localhost:8080
Username:    admin@gmail.com
Start Time:  2025-11-07 10:30:00

================================
AUTHENTICATION
================================
✓ Successfully authenticated
Token: eyJhbGciOiJIUzI1NiIs...

================================
CATEGORY 1: VALIDATE TESTS (Field Validation)
================================
✓ PASS: Empty quesContent should return 400
✓ PASS: Missing quesContent should return 400
...

TEST EXECUTION SUMMARY
Total Tests:   31
Passed:        28
Failed:        0
Pending:       3

Pass Rate:     90%

═══════════════════════════════════════
   ALL TESTS PASSED! ✓
═══════════════════════════════════════
```

### Test 2: Notification API

```bash
./test-notification.sh
```

**What to Expect:**
- ~26 tests
- Auto-detects user ID from login
- Tests all img ENUM values
- Validates response structure

### Test 3: AddQuestionsToLesson API

```bash
./test-add-questions-to-lesson.sh
```

**What to Expect:**
- Setup phase creates test lesson and 3 questions
- ~24 tests including duplicate prevention
- Leaves test data in database (see note at end)

**Cleanup (Optional):**
```bash
# After running, you can manually delete test data
# Lesson ID and Question IDs are displayed in the output
```

### Test 4: Review API

```bash
./test-review.sh
```

**What to Expect:**
- Auto-detects existing courses and enrollments
- May show warnings if enrollment doesn't meet requirements
- ~28 tests including all review status types

**If Tests Fail:**
See [Review API Special Setup](#review-api-special-setup)

---

## Test Categories

### VALIDATE Tests

Tests for field validation:
- Required fields (null, missing, empty)
- Data types (string, number, ENUM)
- Format validation
- Invalid ENUM values

**Example:**
```
✓ PASS: Empty quesContent should return 400
✓ PASS: Missing userId should return 400
✓ PASS: Invalid img ENUM should return 400
```

### LOGIC Tests

Tests for business logic:
- Valid operations
- Business rule enforcement
- ENUM value variations
- Special validation rules

**Example:**
```
✓ PASS: Valid CHOICE question with multiple choices
✓ PASS: WRITING question with READING skill should return 400
✓ PASS: Add duplicate question should return 409
```

### ERROR_CODE Tests

Tests for HTTP status codes:
- 400 (Bad Request)
- 401 (Unauthorized)
- 403 (Forbidden)
- 404 (Not Found)
- 405 (Method Not Allowed)
- 409 (Conflict)
- 500 (Internal Server Error)

**Example:**
```
✓ PASS: Request without auth token should return 401
✓ PASS: Wrong HTTP method (GET) should return 405
✓ PASS: Malformed JSON should return 400
```

### FORMAT_RESPONSE Tests

Tests for response structure:
- Response wrapper (statusCode, message, data)
- Required fields in data
- Auto-generated fields
- Data type consistency

**Example:**
```
✓ PASS: Response has statusCode = 200
✓ PASS: Response has data.id field
✓ PASS: Response data.isRead = false (default)
```

---

## Understanding Test Results

### Success Output

```
✓ PASS: Test name
```
- Green checkmark
- Test passed as expected

### Failure Output

```
✗ FAIL: Test name (Expected: 200, Got: 400)
  Response: {"statusCode":400,"message":"Validation error","data":null}
```
- Red X
- Shows expected vs actual status code
- Shows response body for debugging

### Pending/Skipped Output

```
⚠ SKIP: Test name (could not create prerequisite data)
```
- Yellow warning
- Test couldn't run due to missing prerequisites

### Summary Report

```
TEST EXECUTION SUMMARY
Test Statistics:
  Total Tests:   31
  Passed:        28
  Failed:        2
  Pending:       1

  Pass Rate:     90%

Results by Category:
  VALIDATE: 10 passed, 0 failed (10 total)
  LOGIC: 8 passed, 1 failed (9 total)
  ERROR_CODE: 7 passed, 1 failed (8 total)
  FORMAT_RESPONSE: 3 passed, 0 failed (3 total)

Failed Tests:
  ✗ [LOGIC] WRITING question with READING skill should return 400
     Expected: 400, Got: 200
  ✗ [ERROR_CODE] Non-existent userId should return 404
     Expected: 404, Got: 500
```

---

## Troubleshooting

### Common Issues

#### 1. "jq: command not found"

**Problem:** jq is not installed

**Solution:**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install jq

# macOS
brew install jq

# CentOS/RHEL
sudo yum install jq
```

#### 2. "Failed to get authentication token"

**Problem:** Invalid credentials or backend not running

**Solution:**
```bash
# Check if backend is running
curl http://localhost:8080/api/v1/health

# Verify credentials
export USERNAME="admin@gmail.com"
export PASSWORD="123456"

# Check admin user exists in database
SELECT * FROM users WHERE email = 'admin@gmail.com';
```

#### 3. "Request timeout after 10s"

**Problem:** Backend not responding or very slow

**Solution:**
- Check backend logs
- Verify database connection
- Check network connectivity
- Increase timeout in script if needed

#### 4. test-review.sh: "Must complete at least 30% of the course to review"

**Problem:** User enrollment doesn't have sufficient completion

**Solution:**
```sql
-- Find user enrollments
SELECT * FROM enrollments WHERE user_id = 1;

-- Update completion level
UPDATE enrollments
SET com_level = 50
WHERE user_id = 1 AND course_id = 1;
```

#### 5. test-add-questions-to-lesson.sh: "Question with ID X already exists in the lesson"

**Problem:** Re-running test without cleanup

**Solution:**
```bash
# Delete test data from previous run
# Or use a fresh database
# Or manually remove lesson-question associations
```

---

## Database Setup

### Minimum Database State

For all tests to pass, ensure:

1. **Admin User Exists:**
```sql
INSERT INTO users (email, password, role)
VALUES ('admin@gmail.com', 'hashed_password', 'ADMIN');
```

2. **At Least One Course Exists:** (for test-review.sh)
```sql
SELECT * FROM courses LIMIT 1;
```

3. **User Enrolled in Course with ≥30% Completion:** (for test-review.sh)
```sql
INSERT INTO enrollments (user_id, course_id, com_level, status)
VALUES (1, 1, 50, 'ACTIVE');
```

### Review API Special Setup

The Review API has the most complex requirements:

**Option 1: Use Existing Data**
```sql
-- Find user with enrollment ≥30%
SELECT e.*, c.name as course_name
FROM enrollments e
JOIN courses c ON e.course_id = c.id
WHERE e.user_id = 1 AND e.com_level >= 30;
```

**Option 2: Create Test Enrollment**
```sql
-- Create enrollment
INSERT INTO enrollments (user_id, course_id, com_level, status, create_at)
VALUES (1, 1, 50, 'ACTIVE', NOW());

-- Verify
SELECT * FROM enrollments WHERE user_id = 1;
```

**Option 3: Skip Review Tests**
```bash
# Run only the first 3 test scripts
./test-create-question.sh
./test-notification.sh
./test-add-questions-to-lesson.sh
```

### Cleanup After Testing

**Warning:** Test scripts create data in the database:
- test-add-questions-to-lesson.sh: Creates lesson and questions
- test-review.sh: Creates reviews
- test-notification.sh: Creates notifications
- test-create-question.sh: Creates questions

**Manual Cleanup:**
```sql
-- Find test data (look for keywords like "test", "format", "logic")
SELECT * FROM lessons WHERE name LIKE '%Test%';
SELECT * FROM questions WHERE keyword LIKE '%test%';
SELECT * FROM reviews WHERE re_subject LIKE '%Test%';
SELECT * FROM notifications WHERE message LIKE '%test%';

-- Delete if needed
-- DELETE FROM lessons WHERE name LIKE '%Test%';
-- etc.
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: API Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v2

      - name: Install jq
        run: sudo apt-get install -y jq

      - name: Start Backend
        run: |
          cd backend-service
          ./gradlew bootRun &
          sleep 30  # Wait for backend to start

      - name: Run Tests
        run: |
          cd backend-service
          ./test-create-question.sh
          ./test-notification.sh
          ./test-add-questions-to-lesson.sh
          ./test-review.sh
        env:
          BACKEND_URL: http://localhost:8080
          USERNAME: admin@gmail.com
          PASSWORD: 123456
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any

    environment {
        BACKEND_URL = 'http://localhost:8080'
        USERNAME = 'admin@gmail.com'
        PASSWORD = credentials('admin-password')
    }

    stages {
        stage('Start Backend') {
            steps {
                sh './gradlew bootRun &'
                sleep 30
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                    cd backend-service
                    ./test-create-question.sh
                    ./test-notification.sh
                    ./test-add-questions-to-lesson.sh
                    ./test-review.sh
                '''
            }
        }
    }

    post {
        always {
            sh 'pkill -f bootRun || true'
        }
    }
}
```

---

## Configuration

### Environment Variables

All test scripts support the following environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| BACKEND_URL | http://localhost:8080 | Backend server URL |
| USERNAME | admin@gmail.com | Admin username |
| PASSWORD | 123456 | Admin password |

**Usage:**
```bash
export BACKEND_URL="http://staging.example.com:8080"
export USERNAME="test.admin@example.com"
export PASSWORD="secure_password"

./test-create-question.sh
```

### Script Configuration

Each script has a configuration section at the top:

```bash
# ============================================================================
# CONFIGURATION
# ============================================================================
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-admin@gmail.com}"
PASSWORD="${PASSWORD:-123456}"
```

You can modify defaults directly in the script or use environment variables.

---

## Advanced Usage

### Running Specific Test Categories

To run only specific categories, modify the main() function in each script:

```bash
# Edit script
vim test-create-question.sh

# Comment out unwanted categories
main() {
    # ...
    run_validate_tests
    run_logic_tests
    # run_error_code_tests  # Commented out
    # run_format_response_tests  # Commented out
    # ...
}
```

### Adding Custom Tests

Add tests to any category function:

```bash
run_logic_tests() {
    print_section_header "CATEGORY 2: LOGIC TESTS"

    # Existing tests...

    # Add your custom test
    run_test \
        "Your custom test description" \
        "POST" \
        "/api/v1/your-endpoint" \
        '{"your":"data"}' \
        "200" \
        "LOGIC" \
        '.data.customField'  # Optional validation
}
```

### Debugging Failed Tests

Enable debug output:

```bash
# Add after shebang
set -x  # Enable bash debug mode

# Run script
./test-create-question.sh
```

Or check individual requests:

```bash
# Extract curl command from script and run manually
curl -s -w "\n%{http_code}" -X POST \
    -H "Authorization: Bearer YOUR_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"your":"data"}' \
    "http://localhost:8080/api/v1/endpoint"
```

---

## Performance Benchmarks

Typical execution times on a standard development machine:

| Script | Tests | Time | Rate |
|--------|-------|------|------|
| test-create-question.sh | 31 | ~35s | 0.9 tests/s |
| test-notification.sh | 26 | ~30s | 0.9 tests/s |
| test-add-questions-to-lesson.sh | 24 | ~50s | 0.5 tests/s |
| test-review.sh | 28 | ~60s | 0.5 tests/s |
| **Total** | **109** | **~175s** | **0.6 tests/s** |

Factors affecting performance:
- Network latency to backend
- Database query performance
- Backend server load
- Number of concurrent requests

---

## Best Practices

1. **Run tests in order:**
   - test-create-question.sh (standalone)
   - test-notification.sh (standalone)
   - test-add-questions-to-lesson.sh (creates data)
   - test-review.sh (needs enrollment)

2. **Clean database between runs** for consistent results

3. **Check backend logs** if tests fail unexpectedly

4. **Don't run tests on production** databases

5. **Update test data** if API contracts change

6. **Review failed test responses** - they often indicate real issues

---

## Support

For issues or questions:

1. Check this README
2. Review documents/req-7.md for detailed test specifications
3. Check documents/output/API_Document_V3.md for API specs
4. Check backend logs for server errors
5. Open an issue with:
   - Script name
   - Error output
   - Backend logs
   - Database state

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-07 | Initial release with 4 test scripts covering 109 tests |

---

## Related Documents

- **documents/req-7.md** - Requirements and detailed test plan
- **documents/output/API_Document_V3.md** - Corrected API documentation
- **documents/input/Testcase API V2.pdf** - Original test case specifications
- **backend-service/test-pronunciation-testcase.sh** - Reference pattern
- **backend-service/test-grade-answer.sh** - Complex test pattern reference

---

**Happy Testing! 🧪**
