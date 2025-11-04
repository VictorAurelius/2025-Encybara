# Pronunciation API Test Suite

Automated test script for Pronunciation Assessment API based on testcase document.

## 📋 Overview

This test suite automates 30 test cases defined in `documents/output/Testcase_API_AssessPronunciation.md`.

**Test Coverage:**
- **Total:** 30 test cases
- **Automated:** 20 test cases
- **Manual:** 10 test cases (skipped during automation)

## 📊 Test Categories

| Category | Test IDs | Count | Auto | Manual | Description |
|----------|----------|-------|------|--------|-------------|
| **Validate** | ID-001 ~ ID-010 | 10 | 9 | 1 | Input validation (method, params, auth) |
| **Logic** | ID-011 ~ ID-015 | 5 | 2 | 3 | Business logic and edge cases |
| **Error Code** | ID-016 ~ ID-025 | 10 | 3 | 7 | Error handling and scenarios |
| **Format Response** | ID-026 ~ ID-030 | 5 | 5 | 0 | Response structure validation |

## 🚀 Prerequisites

### 1. Backend Service
```bash
# Backend must be running on port 8080
curl http://localhost:8080/actuator/health
```

### 2. Pronunciation Service
- Service must be configured via `PRONUNCIATION_SERVICE_URL`
- Default: `http://localhost:5000`

### 3. Test Audio File
```bash
# Ensure audio file exists
ls -lh documents/input/audio_sample.mp3
```

### 4. Test User Account
- Email: `user@example.com`
- Password: `Abc@123456`
- (Created by AdminDataInitializer on startup)

## 📖 Usage

### Basic Usage
```bash
cd backend-service
chmod +x test-pronunciation-testcase.sh
./test-pronunciation-testcase.sh
```

### With Custom Configuration
```bash
# Override default settings via environment variables
BACKEND_URL=http://localhost:8080 \
DEFAULT_EMAIL=test@example.com \
DEFAULT_PASSWORD=password123 \
AUDIO_FILE=path/to/audio.mp3 \
./test-pronunciation-testcase.sh
```

## 📄 Output

### Console Output
The script provides colored console output:
- ✓ **Green:** Test PASSED
- ✗ **Red:** Test FAILED
- ⊙ **Yellow:** Test PENDING (Manual test)

### Example Output
```
============================================
  PRONUNCIATION API TEST SUITE
============================================

Based on: documents/output/Testcase_API_AssessPronunciation.md
Total Test Cases: 30 (20 AUTOMATION + 10 MANUAL)

Configuration:
  Backend URL: http://localhost:8080
  Audio file: documents/input/audio_sample.mp3
  Reference text: Most of my peers go crazy about Vietnamese rap music...

✓ Audio file found

Getting authentication token...
✓ Authentication successful
Access token: eyJhbGciOiJIUzI1NiI...
----------------------------------------

========================================
  VALIDATE CATEGORY (10 TCs)
========================================

[ID-001] Testing: Gọi API với method POST
  ✓ PASSED - HTTP 200

[ID-002] Testing: Gọi API với method GET
  ✓ PASSED - HTTP 405

...
```

### Test Report File
A detailed report is saved to: `test-report-YYYYMMDD-HHMMSS.txt`

**Report Contents:**
- Summary (Passed/Failed/Pending counts)
- Detailed results for each test case
- Success rate for automated tests
- Timestamp and configuration used

## 🔧 Configuration

### Default Configuration
```bash
BACKEND_URL="http://localhost:8080"
DEFAULT_EMAIL="user@example.com"
DEFAULT_PASSWORD="Abc@123456"
AUDIO_FILE="documents/input/audio_sample.mp3"
REFERENCE_TEXT="Most of my peers go crazy about Vietnamese rap music..."
```

### Modifying Configuration
Edit the script's configuration section (lines 53-64):
```bash
# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
DEFAULT_EMAIL="${DEFAULT_EMAIL:-user@example.com}"
DEFAULT_PASSWORD="${DEFAULT_PASSWORD:-Abc@123456}"
AUDIO_FILE="${AUDIO_FILE:-documents/input/audio_sample.mp3}"
```

Or use environment variables:
```bash
export BACKEND_URL="http://myserver:8080"
export AUDIO_FILE="path/to/my/audio.mp3"
./test-pronunciation-testcase.sh
```

## 🧪 Test Details

### Automated Tests (20 TCs)
These tests run automatically:
- **ID-001 ~ ID-009:** Input validation (POST/GET/PUT/DELETE, params validation)
- **ID-013 ~ ID-014:** Text format handling (punctuation, special chars)
- **ID-016 ~ ID-018:** Authentication errors (no auth, expired token, invalid token)
- **ID-026 ~ ID-030:** Response format validation (structure, fields, types)

### Manual Tests (10 TCs)
These require manual setup and are skipped during automation:
- **ID-010:** Invalid audio format (service-dependent)
- **ID-011:** Text mismatch (requires specific audio)
- **ID-012:** Long text (requires long audio file)
- **ID-015:** Very short audio (requires specific file)
- **ID-019 ~ ID-025:** Service configuration tests (timeout, 404, 500, etc.)

## 🎯 Expected Results

**Typical Test Run:**
```
Total Test Cases: 30
Passed: 15-18 (depends on environment)
Failed: 0-2 (expected failures for edge cases)
Pending/Manual: 10-12

Success Rate (Automated): 85-95%
```

**Note:** Some tests may fail if:
- Pronunciation service is not configured
- Audio file doesn't match reference text exactly
- Network latency causes timeouts
- Service returns unexpected status codes

## ❗ Troubleshooting

### "Audio file not found"
```bash
# Ensure file exists and path is correct
ls -lh documents/input/audio_sample.mp3

# If missing, check the expected location
pwd
# Should be at project root: /path/to/2025-Encybara
```

### "Authentication failed"
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Verify user credentials
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"Abc@123456"}'

# Check if default user was created
# Look for AdminDataInitializer logs
```

### "Service Unavailable" (503)
```bash
# Check pronunciation service is running
curl http://localhost:5000/health

# Verify PRONUNCIATION_SERVICE_URL is configured in backend
# Check backend logs for service URL
docker logs backend-service 2>&1 | grep "PRONUNCIATION_SERVICE_URL"
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

### Tests Failing with 404 or 405
```bash
# Verify API endpoint is correct
# The endpoint should be: POST /api/v1/pronunciation/assess

# Check backend routing logs
docker logs backend-service 2>&1 | grep "pronunciation"
```

## 🔍 Debugging

### Enable Verbose Output
Modify the script to add `-v` flag to curl commands:
```bash
# Edit test_case function (around line 150)
response=$(curl -v -s -w "\n%{http_code}" -X ${method} \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    ${data} \
    "${BACKEND_URL}${endpoint}")
```

### Run Specific Test Manually
```bash
# Get token first
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"Abc@123456"}' \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

# Test specific endpoint
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@documents/input/audio_sample.mp3" \
  -F "text=Test text" \
  http://localhost:8080/api/v1/pronunciation/assess
```

### View Backend Logs
```bash
# If using Docker
docker-compose logs -f backend

# If running locally
tail -f logs/application.log

# Check for specific errors
docker logs backend-service 2>&1 | grep -i "error\|exception"
```

### Check Test Report
```bash
# View latest test report
ls -lt test-report-*.txt | head -1 | xargs cat

# Or open in editor
vim test-report-$(ls -t test-report-*.txt | head -1)
```

## 📚 Test Case Details

### Validate Category (ID-001 ~ ID-010)

| Test ID | Description | Expected Result |
|---------|-------------|----------------|
| ID-001 | POST method (valid) | 200 OK |
| ID-002 | GET method (invalid) | 405 Method Not Allowed |
| ID-003 | PUT method (invalid) | 405 Method Not Allowed |
| ID-004 | DELETE method (invalid) | 405 Method Not Allowed |
| ID-005 | Valid request with all params | 200 OK with assessment data |
| ID-006 | Missing audio file | 400 Bad Request |
| ID-007 | Empty audio file (0 bytes) | 400 Bad Request |
| ID-008 | Missing text parameter | 400 Bad Request |
| ID-009 | Empty text (whitespace only) | 400 Bad Request |
| ID-010 | Invalid audio format | 400 or 503 (MANUAL) |

### Logic Category (ID-011 ~ ID-015)

| Test ID | Description | Expected Result |
|---------|-------------|----------------|
| ID-011 | Text mismatch with audio | 200 OK with low scores (MANUAL) |
| ID-012 | Long paragraph text | 200 OK (MANUAL) |
| ID-013 | Multiple punctuation | 200 OK |
| ID-014 | Special characters & numbers | 200 OK |
| ID-015 | Very short audio | 200 OK (MANUAL) |

### Error Code Category (ID-016 ~ ID-025)

| Test ID | Description | Expected Result |
|---------|-------------|----------------|
| ID-016 | No authentication token | 401 Unauthorized |
| ID-017 | Expired token | 401 Unauthorized |
| ID-018 | Invalid token | 401 Unauthorized |
| ID-019 | Service not configured | 503 Service Unavailable (MANUAL) |
| ID-020 | Service timeout | 503 Service Unavailable (MANUAL) |
| ID-021 | Service returns 404 | 404 Not Found (MANUAL) |
| ID-022 | Service returns 500 | 500 Internal Server Error (MANUAL) |
| ID-023 | Invalid service response | 503 Service Unavailable (MANUAL) |
| ID-024 | Large file exceeds limit | 400 or 413 (MANUAL) |
| ID-025 | Concurrent requests | 200 OK for all (MANUAL) |

### Format Response Category (ID-026 ~ ID-030)

| Test ID | Description | Expected Result |
|---------|-------------|----------------|
| ID-026 | Success response structure | statusCode, error, message, data fields |
| ID-027 | Error response structure | statusCode, error, message fields |
| ID-028 | overall_score field | Field exists and valid (0-100) |
| ID-029 | fluency_score field | Field exists and valid (0-100) |
| ID-030 | phoneme_scores array | Array with phoneme objects |

## 🛠️ Extending the Test Suite

### Adding New Test Cases

1. Update the testcase document:
   ```bash
   vim documents/output/Testcase_API_AssessPronunciation.md
   ```

2. Add test case to appropriate category function in script:
   ```bash
   # For example, add to test_validate_category()
   test_case "ID-031" "POST" "${PRONUNCIATION_API}/assess" \
       "-F file=@${AUDIO_FILE} -F text=\"New test\"" \
       "200" "New test case description"
   ```

3. Update test count in documentation

### Adding New Test Categories

1. Create new function:
   ```bash
   test_new_category() {
       echo -e "${BLUE}========================================${NC}"
       echo -e "${BLUE}  NEW CATEGORY${NC}"
       echo -e "${BLUE}========================================${NC}"
       echo ""

       # Add test cases here
   }
   ```

2. Call it in main():
   ```bash
   test_new_category
   ```

## 📖 References

- **Testcase Document:** `documents/output/Testcase_API_AssessPronunciation.md`
- **API Document:** `documents/output/API_Document_AssessPronunciation.md`
- **Plan Task:** `documents/req-5.md`
- **Original Script:** `backend-service/test-pronunciation.sh`

## 🤝 Contributing

To contribute to this test suite:
1. Update testcase document with new test cases
2. Add implementation to script
3. Update this README
4. Update test count totals
5. Test locally before committing

## 📝 Notes

- **Audio File:** The test uses `documents/input/audio_sample.mp3` which should contain the reference text being tested
- **Manual Tests:** These tests require specific setup or environment conditions that cannot be easily automated
- **Success Rate:** A success rate of 85-95% is expected for automated tests in a properly configured environment
- **Network Dependency:** Tests depend on backend and pronunciation services being accessible

## 📅 Changelog

### 2025-11-04
- Initial release
- 30 test cases implemented (20 AUTOMATION + 10 MANUAL)
- Based on req-5 task plan
- Supports 4 test categories: Validate, Logic, Error Code, Format Response

---

**Last Updated:** 2025-11-04
**Version:** 1.0.0
**Author:** Generated based on req-5 task plan
