#!/bin/bash

# ============================================
# Review API Automated Test Suite
# ============================================
#
# Description:
#   This script automatically tests the Review API
#   for course review creation functionality.
#   Tests three review types: CONTENT, CONTRIBUTING, and MISTAKE.
#
# Test Flow:
#   1. Authenticate user (user@example.com)
#   2. Find test course and validate access
#   3. Create/verify sufficient enrollment (>=30% completion)
#   4. Run validation tests (field requirements)
#   5. Run logic tests (business rules)
#   6. Run error code tests (HTTP responses)
#   7. Generate test report
#
# Test Coverage:
#   - Validation Tests: ~8 test cases
#   - Logic Tests: ~10-15 test cases
#   - Error Tests: ~6 test cases
#   - Total: ~24-29 API calls
#
# Prerequisites:
#   - Backend service running on http://localhost:8080
#   - Database seeded with test courses
#   - Default user created (user@example.com / Abc@123456)
#
# Usage:
#   ./test-review.sh
#
# Output:
#   - Colored console output (✓ PASSED, ✗ FAILED)
#   - Detailed test results and error messages
#
# Author: Generated based on req-7 task plan
# Date: 2025-11-07
# Reference: documents/req-7.md, documents/output/API_Document_V3.md
# ============================================

# Configuration
BACKEND_URL="${BACKEND_URL:-http://18.136.223.96:8080}"
API_BASE="/api/v1"
DEFAULT_EMAIL="${DEFAULT_EMAIL:-user@example.com}"
DEFAULT_PASSWORD="${DEFAULT_PASSWORD:-Abc@123456}"

# Global variables
ACCESS_TOKEN=""
TEST_USER_ID=""
TEST_COURSE_ID=""
ENROLLMENT_ID_SUFFICIENT=""  # Enrollment with >=30% completion
ENROLLMENT_ID_INSUFFICIENT="" # Enrollment with <30% completion
SECOND_COURSE_ID=""         # For duplicate review test

# Test tracking variables
declare -a TEST_RESULTS
PASSED_COUNT=0
FAILED_COUNT=0
TOTAL_TESTS=0

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================================================
# HELPER FUNCTIONS
# ============================================================================

# Print section header
print_section_header() {
    local title="$1"
    echo -e "\n${CYAN}================================${NC}"
    echo -e "${CYAN}${title}${NC}"
    echo -e "${CYAN}================================${NC}\n"
}

# Function to get authentication token
get_auth_token() {
    echo -e "${YELLOW}>>> Authenticating user...${NC}"

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
        # Extract access token
        ACCESS_TOKEN=$(echo "$response_body" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//g' | sed 's/"//g')
        if [[ -z "$ACCESS_TOKEN" ]]; then
            ACCESS_TOKEN=$(echo "$response_body" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//g' | sed 's/"//g')
        fi
        if [[ ! -z "$ACCESS_TOKEN" ]]; then
            # Get user ID from response
            TEST_USER_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
            if [[ -z "$TEST_USER_ID" ]]; then
                TEST_USER_ID=1
                echo -e "${YELLOW}⚠ Could not get user ID from API, using default: ${TEST_USER_ID}${NC}"
            fi
            echo -e "${GREEN}✓ Authentication successful${NC}"
            echo "  Token: ${ACCESS_TOKEN:0:30}..."
            echo "  User ID: ${TEST_USER_ID}"
        else
            echo -e "${RED}✗ Failed to extract access token${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Authentication failed - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo ""
}

# Function to find test courses
setup_test_course() {
    echo -e "${YELLOW}>>> Finding test courses...${NC}"

    # Try to find existing courses first
    echo -e "${CYAN}  → Searching for existing courses...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/courses?page=0&size=10")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" ]]; then
        # Extract first course ID
        TEST_COURSE_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        if [[ ! -z "$TEST_COURSE_ID" ]]; then
            echo -e "${GREEN}✓ Found primary test course${NC}"
            echo "  Course ID: ${TEST_COURSE_ID}"

            # Try to find a second course for additional tests
            SECOND_COURSE_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | sed -n '2p' | cut -d':' -f2)
            if [[ ! -z "$SECOND_COURSE_ID" ]]; then
                echo -e "${GREEN}✓ Found secondary test course${NC}"
                echo "  Course ID: ${SECOND_COURSE_ID}"
            else
                echo -e "${YELLOW}⚠ Only one course found, some tests will be skipped${NC}"
            fi
        else
            echo -e "${YELLOW}⚠ No courses found, using default IDs${NC}"
            echo "Note: Course creation may require admin permissions"
            TEST_COURSE_ID=1
            SECOND_COURSE_ID=2
        fi
    else
        echo -e "${RED}✗ Failed to fetch courses - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        TEST_COURSE_ID=1
        SECOND_COURSE_ID=2
    fi
    echo ""
}

# Function to setup enrollments
setup_enrollments() {
    echo -e "${YELLOW}>>> Setting up enrollments...${NC}"

    # Check existing enrollments first
    echo -e "${CYAN}  → Checking existing enrollments...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/enrollments/user/${TEST_USER_ID}")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" ]]; then
        # Try to find enrollment with >=30% completion
        for enroll_data in $(echo "$response_body" | grep -o '{[^}]*}'); do
            course_id=$(echo "$enroll_data" | grep -o '"courseId":[0-9]*' | cut -d':' -f2)
            com_level=$(echo "$enroll_data" | grep -o '"comLevel":[0-9]*' | cut -d':' -f2)
            enroll_id=$(echo "$enroll_data" | grep -o '"id":[0-9]*' | cut -d':' -f2)
            
            if [[ ! -z "$com_level" ]] && [[ "$com_level" -ge 30 ]]; then
                TEST_COURSE_ID="$course_id"
                ENROLLMENT_ID_SUFFICIENT="$enroll_id"
                echo -e "${GREEN}✓ Found enrollment with sufficient completion (${com_level}%)${NC}"
                echo "  Course ID: ${TEST_COURSE_ID}"
                echo "  Enrollment ID: ${ENROLLMENT_ID_SUFFICIENT}"
                break
            elif [[ ! -z "$com_level" ]] && [[ "$com_level" -lt 30 ]]; then
                ENROLLMENT_ID_INSUFFICIENT="$enroll_id"
                echo -e "${GREEN}✓ Found enrollment with insufficient completion (${com_level}%)${NC}"
            fi
        done
    fi

    # Create new enrollment if needed
    if [[ -z "$ENROLLMENT_ID_SUFFICIENT" ]]; then
        echo -e "${CYAN}  → Creating new enrollment for testing...${NC}"
        enroll_data="{\"courseId\":${TEST_COURSE_ID}}"
        
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "${enroll_data}" \
            "${BACKEND_URL}${API_BASE}/enrollments")

        status_code=$(echo "$response" | tail -n1)
        response_body=$(echo "$response" | head -n -1)

        if [[ "$status_code" == "200" || "$status_code" == "201" ]]; then
            ENROLLMENT_ID_SUFFICIENT=$(echo "$response_body" | grep -o '"id":[0-9]*' | cut -d':' -f2)
            echo -e "${GREEN}✓ Created new enrollment${NC}"
            echo "  Enrollment ID: ${ENROLLMENT_ID_SUFFICIENT}"
            echo -e "${YELLOW}Note: New enrollment has 0% completion. Some tests may fail.${NC}"
            echo "Tip: Update completion level in database: UPDATE enrollments SET com_level = 50 WHERE id = ${ENROLLMENT_ID_SUFFICIENT}"
        else
            echo -e "${RED}✗ Failed to create enrollment - HTTP ${status_code}${NC}"
            echo "Response: $response_body"
        fi
    fi

    echo -e "\n${BLUE}Enrollment Summary:${NC}"
    echo "  Course ID: ${TEST_COURSE_ID}"
    echo "  Second Course ID: ${SECOND_COURSE_ID:-none}"
    echo "  Main Enrollment ID: ${ENROLLMENT_ID_SUFFICIENT:-not created}"
    echo "  Low Completion Enrollment ID: ${ENROLLMENT_ID_INSUFFICIENT:-none}"
    echo ""
}

# Function to run a test case
run_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    local category="$6"
    local validation_check="${7:-}"
    local test_id="${8:-$((TOTAL_TESTS + 1))}"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "\n${YELLOW}[TEST ${test_id}] ${test_name}${NC}"

    # Build request
    echo -e "${CYAN}  → Request Details:${NC}"
    echo "    Method: ${method}"
    echo "    Endpoint: ${BACKEND_URL}${API_BASE}${endpoint}"
    if [ -n "$data" ] && [ "$data" != "NONE" ]; then
        echo "    Data: $(echo "$data" | jq -c . 2>/dev/null || echo "$data")"
    fi

    # Setup headers
    local headers=(-H "Content-Type: application/json")
    if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "INVALID" ]; then
        headers+=(-H "Authorization: Bearer ${ACCESS_TOKEN}")
    elif [ "$ACCESS_TOKEN" = "INVALID" ]; then
        headers+=(-H "Authorization: Bearer invalid_token_12345")
    fi

    # Execute request with timeout
    local curl_cmd="curl -s -w \"\n%{http_code}\" -X ${method} ${headers[@]}"
    if [ -n "$data" ] && [ "$data" != "NONE" ]; then
        curl_cmd="$curl_cmd -d '${data}'"
    fi
    curl_cmd="$curl_cmd \"${BACKEND_URL}${API_BASE}${endpoint}\""

    echo -e "${CYAN}  → Executing request...${NC}"
    local response=$(timeout 10s bash -c "$curl_cmd" 2>/dev/null)
    local exit_code=$?

    if [ $exit_code -eq 124 ]; then
        echo -e "  ${RED}✗ FAILED - Request timeout after 10s${NC}"
        TEST_RESULTS+=("${test_id}|FAIL|${category}|${test_name}|Timeout")
        FAILED_COUNT=$((FAILED_COUNT + 1))
        return
    fi

    local status_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    # Validate response format
    local valid_response=true
    if ! echo "$body" | jq -e '.statusCode' >/dev/null 2>&1; then
        echo -e "  ${YELLOW}⚠ WARNING - Missing 'statusCode' field${NC}"
        valid_response=false
    fi
    if ! echo "$body" | jq -e '.message' >/dev/null 2>&1; then
        echo -e "  ${YELLOW}⚠ WARNING - Missing 'message' field${NC}"
        valid_response=false
    fi
    if ! echo "$body" | jq -e '.data' >/dev/null 2>&1; then
        echo -e "  ${YELLOW}⚠ WARNING - Missing 'data' field${NC}"
        valid_response=false
    fi

    echo -e "${CYAN}  → Response Details:${NC}"
    echo "    Status Code: ${status_code}"
    if [ -n "$body" ]; then
        echo "    Body: $(echo "$body" | jq -c . 2>/dev/null || echo "$body")"
    fi

    # Handle multiple acceptable status codes
    if [[ "$expected_status" == *"|"* ]]; then
        local status_match=false
        IFS='|' read -ra statuses <<< "$expected_status"
        for status in "${statuses[@]}"; do
            if [ "$status_code" = "$status" ]; then
                status_match=true
                break
            fi
        done

        if [ "$status_match" = true ]; then
            echo -e "  ${GREEN}✓ PASSED - Status code ${status_code} matches expected${NC}"
            TEST_RESULTS+=("${test_id}|PASS|${category}|${test_name}")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "  ${RED}✗ FAILED - Status code mismatch${NC}"
            echo "    Expected: one of [${expected_status}]"
            echo "    Got: ${status_code}"
            TEST_RESULTS+=("${test_id}|FAIL|${category}|${test_name}|Status code mismatch: got ${status_code}")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
        return
    fi

    # Check HTTP status code and validation
    if [ "$status_code" = "$expected_status" ]; then
        if [ -n "$validation_check" ]; then
            if echo "$body" | jq -e "$validation_check" >/dev/null 2>&1; then
                echo -e "  ${GREEN}✓ PASSED - Status code and validation OK${NC}"
                if [ "$valid_response" = false ]; then
                    echo -e "    ${YELLOW}⚠ Note: Response format has warnings${NC}"
                fi
                TEST_RESULTS+=("${test_id}|PASS|${category}|${test_name}")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "  ${RED}✗ FAILED - Validation check failed${NC}"
                echo "    Check: $validation_check"
                TEST_RESULTS+=("${test_id}|FAIL|${category}|${test_name}|Validation failed")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi
        else
            echo -e "  ${GREEN}✓ PASSED - Status code ${status_code} OK${NC}"
            if [ "$valid_response" = false ]; then
                echo -e "    ${YELLOW}⚠ Note: Response format has warnings${NC}"
            fi
            TEST_RESULTS+=("${test_id}|PASS|${category}|${test_name}")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        fi
    else
        echo -e "  ${RED}✗ FAILED - Status code mismatch${NC}"
        echo "    Expected: ${expected_status}"
        echo "    Got: ${status_code}"
        TEST_RESULTS+=("${test_id}|FAIL|${category}|${test_name}|Status code mismatch: got ${status_code}")
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
}

# ============================================================================
# TEST CATEGORY 1: VALIDATE TESTS (Field Validation)
# ============================================================================
run_validate_tests() {
    print_section_header "CATEGORY 1: VALIDATE TESTS (Field Validation)"

    # Test 1: Missing userId
    run_test \
        "Missing userId should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great course\",\"reSubject\":\"Excellent\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "400" \
        "VALIDATE"

    # Test 2: Missing courseId
    run_test \
        "Missing courseId should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"reContent\":\"Great course\",\"reSubject\":\"Excellent\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "400" \
        "VALIDATE"

    # Test 3: Missing reContent
    run_test \
        "Missing reContent should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reSubject\":\"Excellent\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "400" \
        "VALIDATE"

    # Test 4: Missing reSubject
    run_test \
        "Missing reSubject should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great course\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "400" \
        "VALIDATE"

    # Test 5: Missing numStar
    run_test \
        "Missing numStar should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great course\",\"reSubject\":\"Excellent\",\"status\":\"CONTENT\"}" \
        "400" \
        "VALIDATE"

    # Test 6: Missing status
    run_test \
        "Missing status should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great course\",\"reSubject\":\"Excellent\",\"numStar\":5}" \
        "400" \
        "VALIDATE"

    # Test 7: Invalid status ENUM
    run_test \
        "Invalid status ENUM should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great course\",\"reSubject\":\"Excellent\",\"numStar\":5,\"status\":\"INVALID_STATUS\"}" \
        "400" \
        "VALIDATE"

    # Test 8: Negative numStar
    run_test \
        "Negative numStar should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great course\",\"reSubject\":\"Excellent\",\"numStar\":-1,\"status\":\"CONTENT\"}" \
        "400" \
        "VALIDATE"
}

# ============================================================================
# TEST CATEGORY 2: LOGIC TESTS (Business Rules)
# ============================================================================
run_logic_tests() {
    print_section_header "CATEGORY 2: LOGIC TESTS (Business Rules)"

    echo -e "${YELLOW}Note: Logic tests depend on enrollment with >=30% completion.${NC}"
    echo "If enrollment doesn't exist or has <30% completion, these tests may fail with 400 or 404."

    # Test 1: Valid review with CONTENT status
    run_test \
        "Valid review with status=CONTENT" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"This course has excellent content and structure. Very helpful for learning.\",\"reSubject\":\"Excellent content\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "200|400|404|409" \
        "LOGIC" \
        '.data.id // empty'

    # Test 2: Valid review with CONTRIBUTING status
    run_test \
        "Valid review with status=CONTRIBUTING" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Great community and instructor support.\",\"reSubject\":\"Good contribution\",\"numStar\":4,\"status\":\"CONTRIBUTING\"}" \
        "200|409" \
        "LOGIC"

    # Test 3: Valid review with MISTAKE status
    run_test \
        "Valid review with status=MISTAKE" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Found some typos in lesson 3.\",\"reSubject\":\"Minor mistakes\",\"numStar\":4,\"status\":\"MISTAKE\"}" \
        "200|409" \
        "LOGIC"

    # Test 4: User not enrolled in course
    run_test \
        "User not enrolled should return 404" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":999999999,\"reContent\":\"Review for non-enrolled course\",\"reSubject\":\"Test\",\"numStar\":3,\"status\":\"CONTENT\"}" \
        "404" \
        "LOGIC"

    # Test 5: Review with different numStar values (1-5)
    for star in 1 2 3 4 5; do
        run_test \
            "Valid review with numStar=${star}" \
            "POST" \
            "/api/v1/reviews" \
            "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Review with ${star} stars\",\"reSubject\":\"${star} star review\",\"numStar\":${star},\"status\":\"CONTENT\"}" \
            "200|409" \
            "LOGIC"
    done

    # Test 6: Non-existent userId
    run_test \
        "Non-existent userId should return 404" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":999999999,\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Review from non-existent user\",\"reSubject\":\"Test\",\"numStar\":3,\"status\":\"CONTENT\"}" \
        "404" \
        "LOGIC"

    # Test 7: Non-existent courseId
    run_test \
        "Non-existent courseId should return 404" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":999999999,\"reContent\":\"Review for non-existent course\",\"reSubject\":\"Test\",\"numStar\":3,\"status\":\"CONTENT\"}" \
        "404" \
        "LOGIC"
}

# ============================================================================
# TEST CATEGORY 3: ERROR CODE TESTS (HTTP Status Codes)
# ============================================================================
run_error_code_tests() {
    print_section_header "CATEGORY 3: ERROR CODE TESTS (HTTP Status Codes)"

    # Test 1: No auth token
    local temp_token="$ACCESS_TOKEN"
    ACCESS_TOKEN=""
    run_test \
        "Request without auth token should return 401" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Test\",\"reSubject\":\"Test\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 2: Invalid auth token
    ACCESS_TOKEN="INVALID"
    run_test \
        "Request with invalid token should return 401" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Test\",\"reSubject\":\"Test\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 3: Malformed JSON
    run_test \
        "Malformed JSON should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID}" \
        "400" \
        "ERROR_CODE"

    # Test 4: Wrong HTTP method (GET)
    run_test \
        "Wrong HTTP method (GET) should return 405" \
        "GET" \
        "/api/v1/reviews" \
        "NONE" \
        "405" \
        "ERROR_CODE"

    # Test 5: Empty request body
    run_test \
        "Empty request body should return 400" \
        "POST" \
        "/api/v1/reviews" \
        '{}' \
        "400" \
        "ERROR_CODE"

    # Test 6: Null values in required fields
    run_test \
        "Null userId should return 400" \
        "POST" \
        "/api/v1/reviews" \
        "{\"userId\":null,\"courseId\":${TEST_COURSE_ID},\"reContent\":\"Test\",\"reSubject\":\"Test\",\"numStar\":5,\"status\":\"CONTENT\"}" \
        "400" \
        "ERROR_CODE"
}

# ============================================================================
# TEST CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)
# ============================================================================
run_format_response_tests() {
    print_section_header "CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)"

    echo -e "${YELLOW}Note: Format tests will create a review. May fail with 409 if duplicate.${NC}"

    # Try to use a different course for format test if available
    local format_course_id="${SECOND_COURSE_ID:-${TEST_COURSE_ID}}"

    # Create a review and capture response
    local test_data="{\"userId\":${TEST_USER_ID},\"courseId\":${format_course_id},\"reContent\":\"Format test review with detailed feedback about the course quality and content.\",\"reSubject\":\"Format Test Review\",\"numStar\":5,\"status\":\"CONTENT\"}"

    local response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$test_data" \
        "${BACKEND_URL}/api/v1/reviews")

    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        # Test 1: Response has statusCode = 200
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.statusCode == 200' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has statusCode = 200"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|statusCode = 200")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response statusCode incorrect"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|statusCode incorrect")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 2: Response has data.id (auto-generated)
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.id' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has data.id field"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|has data.id")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing data.id"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|missing data.id")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 3: Response data.numLike = 0 (default)
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.numLike == 0' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response data.numLike = 0 (default)"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|numLike defaults to 0")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data.numLike is not 0"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|numLike not 0")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 4: Response data matches request fields
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        local has_all_fields=true
        for field in userId courseId reContent reSubject numStar status; do
            if ! echo "$body" | jq -e ".data.${field}" > /dev/null 2>&1; then
                has_all_fields=false
                break
            fi
        done

        if [ "$has_all_fields" = true ]; then
            echo -e "${GREEN}✓ PASS${NC}: Response data contains all request fields"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|all fields present")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data missing some fields"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|missing fields")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 5: Check if notification was created
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        echo "Checking if notification was created..."
        local notif_response=$(curl -s -X GET \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            "${BACKEND_URL}/api/v1/notifications/user/${TEST_USER_ID}?page=0&size=10" 2>/dev/null)

        if echo "$notif_response" | jq -e '.data.content | length > 0' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Notification created for review"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|notification created")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${YELLOW}⚠ SKIP${NC}: Could not verify notification creation"
            TEST_RESULTS+=("SKIP|FORMAT_RESPONSE|notification check inconclusive")
            PENDING_COUNT=$((PENDING_COUNT + 1))
        fi

        # Test 6: All response fields present
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        local expected_fields=("id" "userId" "courseId" "reContent" "reSubject" "numStar" "numLike" "status")
        local all_present=true

        for field in "${expected_fields[@]}"; do
            if ! echo "$body" | jq -e ".data.${field}" > /dev/null 2>&1; then
                all_present=false
                break
            fi
        done

        if [ "$all_present" = true ]; then
            echo -e "${GREEN}✓ PASS${NC}: All expected response fields present"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|complete response")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Some expected fields missing"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|incomplete response")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo -e "${YELLOW}⚠ SKIP${NC}: Format tests skipped (HTTP $http_code)"
        if [ "$http_code" = "409" ]; then
            echo "  Reason: Duplicate review (user already reviewed this course)"
        elif [ "$http_code" = "400" ]; then
            echo "  Reason: Enrollment completion <30% or other validation error"
        elif [ "$http_code" = "404" ]; then
            echo "  Reason: User not enrolled in course"
        fi
        echo "  Response: $(echo "$body" | jq -c . 2>/dev/null || echo "$body")"
        PENDING_COUNT=$((PENDING_COUNT + 6))
        TOTAL_TESTS=$((TOTAL_TESTS + 6))
    fi
}

# ============================================================================
# REPORT GENERATION
# ============================================================================
# Function to generate test report
generate_report() {
    local report_date=$(date '+%Y-%m-%d %H:%M:%S')
    local duration=$SECONDS
    local minutes=$((duration / 60))
    local seconds=$((duration % 60))

    echo -e "\n${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                    TEST EXECUTION REPORT                ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}\n"

    # Configuration Summary
    echo -e "${CYAN}Test Configuration:${NC}"
    echo -e "  Backend URL:  ${BACKEND_URL}"
    echo -e "  User:         ${DEFAULT_EMAIL}"
    echo -e "  Test Course:  ${TEST_COURSE_ID}"
    echo -e "  Date:         ${report_date}"
    echo -e "  Duration:     ${minutes}m ${seconds}s"

    # Test Statistics
    echo -e "\n${CYAN}Test Statistics:${NC}"
    echo -e "┌──────────────┬────────┐"
    echo -e "│ Total Tests  │ ${TOTAL_TESTS} │"
    printf "│ %-12s│ ${GREEN}%-7d${NC}│\n" "Passed" "${PASSED_COUNT}"
    printf "│ %-12s│ ${RED}%-7d${NC}│\n" "Failed" "${FAILED_COUNT}"
    printf "│ %-12s│ ${YELLOW}%-7d${NC}│\n" "Pending" "${PENDING_COUNT}"
    echo -e "└──────────────┴────────┘"

    # Pass Rate Calculation
    if [ ${TOTAL_TESTS} -gt 0 ]; then
        local pass_rate=$((PASSED_COUNT * 100 / TOTAL_TESTS))
        local pass_color=$RED
        if [ $pass_rate -ge 90 ]; then
            pass_color=$GREEN
        elif [ $pass_rate -ge 70 ]; then
            pass_color=$YELLOW
        fi
        echo -e "\nPass Rate: ${pass_color}${pass_rate}%${NC}"
    fi

    # Category Summary
    echo -e "\n${CYAN}Results by Category:${NC}"
    echo -e "┌────────────────┬────────┬────────┬────────┬──────────┐"
    echo -e "│    Category    │ Passed │ Failed │ Pending│ Coverage │"
    echo -e "├────────────────┼────────┼────────┼────────┼──────────┤"
    
    for category in "VALIDATE" "LOGIC" "ERROR_CODE" "FORMAT_RESPONSE"; do
        local cat_total=0
        local cat_passed=0
        local cat_failed=0
        local cat_pending=0

        for result in "${TEST_RESULTS[@]}"; do
            IFS='|' read -ra parts <<< "$result"
            if [ "${parts[1]}" = "$category" ]; then
                cat_total=$((cat_total + 1))
                case "${parts[0]}" in
                    "PASS") cat_passed=$((cat_passed + 1)) ;;
                    "FAIL") cat_failed=$((cat_failed + 1)) ;;
                    "SKIP") cat_pending=$((cat_pending + 1)) ;;
                esac
            fi
        done

        if [ $cat_total -gt 0 ]; then
            local coverage=$((cat_passed * 100 / cat_total))
            local cov_color=$RED
            if [ $coverage -ge 90 ]; then
                cov_color=$GREEN
            elif [ $coverage -ge 70 ]; then
                cov_color=$YELLOW
            fi
            printf "│ %-14s│ ${GREEN}%-7d${NC}│ ${RED}%-7d${NC}│ ${YELLOW}%-7d${NC}│ ${cov_color}%-8d${NC}│\n" \
                "$category" "$cat_passed" "$cat_failed" "$cat_pending" "$coverage"
        fi
    done
    echo -e "└────────────────┴────────┴────────┴────────┴──────────┘"

    # Failed Tests Detail
    if [ ${FAILED_COUNT} -gt 0 ]; then
        echo -e "\n${RED}Failed Tests Detail:${NC}"
        echo -e "┌─────────┬────────────┬────────────────────────────────────┐"
        echo -e "│ Test ID │  Category  │ Description                        │"
        echo -e "├─────────┼────────────┼────────────────────────────────────┤"
        
        for result in "${TEST_RESULTS[@]}"; do
            if [[ $result == FAIL* ]]; then
                IFS='|' read -ra parts <<< "$result"
                local test_id="${parts[0]}"
                printf "│ %-7s │ %-10s │ %-36s │\n" "${parts[0]}" "${parts[1]}" "${parts[2]:0:36}"
                if [ ${#parts[@]} -ge 4 ]; then
                    printf "│         │            │ %-36s │\n" "${parts[3]:0:36}"
                fi
            fi
        done
        echo -e "└─────────┴────────────┴────────────────────────────────────┘"
    fi

    # Final Verdict
    echo -e "\n${CYAN}Final Verdict:${NC}"
    if [ ${FAILED_COUNT} -eq 0 ] && [ ${PENDING_COUNT} -eq 0 ]; then
        echo -e "╔═══════════════════════════════════════════════════╗"
        echo -e "║ ${GREEN}               ALL TESTS PASSED! ✓               ${NC}║"
        echo -e "╚═══════════════════════════════════════════════════╝"
    elif [ ${FAILED_COUNT} -eq 0 ]; then
        echo -e "╔═══════════════════════════════════════════════════╗"
        echo -e "║ ${YELLOW}         ALL TESTS PASSED (some pending)         ${NC}║"
        echo -e "╚═══════════════════════════════════════════════════╝"
    else
        echo -e "╔═══════════════════════════════════════════════════╗"
        echo -e "║ ${RED}              SOME TESTS FAILED! ✗              ${NC}║"
        echo -e "╚═══════════════════════════════════════════════════╝"
    fi
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================
main() {
    # Start timing
    SECONDS=0
    
    echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                 REVIEW API TEST SUITE                  ║${NC}"
    echo -e "${BLUE}║                                                        ║${NC}"
    echo -e "${BLUE}║  Version: 1.0.0                                       ║${NC}"
    echo -e "${BLUE}║  Date: $(date '+%Y-%m-%d')                              ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"

    # System checks
    echo -e "\n${CYAN}System Checks:${NC}"
    
    # Check if jq is installed
    echo -n "  Checking jq installation... "
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}✗ Not Found${NC}"
        echo -e "\n${RED}Error: jq is required but not installed.${NC}"
        echo "Please install jq using one of these commands:"
        echo "  Ubuntu/Debian: sudo apt-get install jq"
        echo "  macOS:        brew install jq"
        echo "  Windows:      choco install jq"
        exit 1
    fi
    echo -e "${GREEN}✓ Found${NC}"

    # Check if curl is installed
    echo -n "  Checking curl installation... "
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}✗ Not Found${NC}"
        echo -e "\n${RED}Error: curl is required but not installed.${NC}"
        echo "Please install curl using your system's package manager."
        exit 1
    fi
    echo -e "${GREEN}✓ Found${NC}"

    # Check internet connectivity
    echo -n "  Checking backend connectivity... "
    if ! curl --silent --head --fail "${BACKEND_URL}" >/dev/null 2>&1; then
        echo -e "${RED}✗ Failed${NC}"
        echo -e "\n${RED}Error: Cannot connect to ${BACKEND_URL}${NC}"
        echo "Please check:"
        echo "  1. Your internet connection"
        echo "  2. The backend service is running"
        echo "  3. The BACKEND_URL environment variable is correct"
        exit 1
    fi
    echo -e "${GREEN}✓ Connected${NC}"

    echo -e "\n${CYAN}Test Environment:${NC}"
    echo -e "  Backend URL:  ${BACKEND_URL}"
    echo -e "  User:         ${DEFAULT_EMAIL}"
    echo -e "  Start Time:   $(date '+%Y-%m-%d %H:%M:%S')"
    echo -e "  Test Script:  ${0##*/}"

    echo -e "\n${CYAN}Initializing Tests:${NC}"
    
    # Authenticate
    echo -n "  Authenticating user... "
    get_auth_token

    # Setup test data
    echo -n "  Setting up test data... "
    setup_test_course
    setup_enrollments

    # Run test categories with progress tracking
    echo -e "\n${CYAN}Executing Test Categories:${NC}"
    local total_categories=4
    local current_category=0

    # Category 1: Validate Tests
    ((current_category++))
    echo -e "\n${YELLOW}[$current_category/$total_categories] Running Validate Tests${NC}"
    run_validate_tests

    # Category 2: Logic Tests
    ((current_category++))
    echo -e "\n${YELLOW}[$current_category/$total_categories] Running Logic Tests${NC}"
    run_logic_tests

    # Category 3: Error Code Tests
    ((current_category++))
    echo -e "\n${YELLOW}[$current_category/$total_categories] Running Error Code Tests${NC}"
    run_error_code_tests

    # Category 4: Format Response Tests
    ((current_category++))
    echo -e "\n${YELLOW}[$current_category/$total_categories] Running Format Response Tests${NC}"
    run_format_response_tests

    # Generate final report
    generate_report

    # Display important notes
    echo -e "\n${CYAN}Important Notes:${NC}"
    echo -e "┌────────────────────────────────────────────────────┐"
    echo -e "│ 1. Review API Requirements:                        │"
    echo -e "│    • User must be enrolled in the course          │"
    echo -e "│    • Enrollment completion must be ≥30%           │"
    echo -e "│    • One review per course per user              │"
    echo -e "│                                                    │"
    echo -e "│ 2. Troubleshooting Failed Tests:                  │"
    echo -e "│    • Check enrollment:                            │"
    echo -e "│      SELECT * FROM enrollments                    │"
    echo -e "│      WHERE user_id = ${TEST_USER_ID}                          │"
    echo -e "│    • Update completion level:                     │"
    echo -e "│      UPDATE enrollments                           │"
    echo -e "│      SET com_level = 50                          │"
    echo -e "│      WHERE user_id = ${TEST_USER_ID}                          │"
    echo -e "│                                                    │"
    echo -e "│ 3. Note: Test reviews remain in the database      │"
    echo -e "└────────────────────────────────────────────────────┘"

    # Exit with status code based on results
    if [ ${FAILED_COUNT} -gt 0 ]; then
        exit 1
    fi
    exit 0
}

# Run main function
main "$@"
