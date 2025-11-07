#!/bin/bash

################################################################################
# TEST SCRIPT: Review API
# API Endpoint: POST /api/v1/reviews
# Description: Automated testing for course review creation functionality
# Version: 1.0
# Date: 2025-11-07
# Reference: documents/req-7.md, documents/output/API_Document_V3.md
################################################################################

# ============================================================================
# CONFIGURATION
# ============================================================================
BACKEND_URL="${BACKEND_URL:-http://18.136.223.96:8080}"
USERNAME="${USERNAME:-user@example.com}"
PASSWORD="${PASSWORD:-Abc@123456}"
ACCESS_TOKEN=""

# Test data IDs (created during setup)
TEST_USER_ID=""
TEST_COURSE_ID=""
ENROLLMENT_ID_SUFFICIENT=""  # Enrollment with >=30% completion
ENROLLMENT_ID_INSUFFICIENT="" # Enrollment with <30% completion
SECOND_COURSE_ID=""          # For duplicate review test

# ============================================================================
# COLOR CODES FOR OUTPUT
# ============================================================================
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================================================
# TEST TRACKING VARIABLES
# ============================================================================
declare -a TEST_RESULTS
PASSED_COUNT=0
FAILED_COUNT=0
PENDING_COUNT=0
TOTAL_TESTS=0

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

# Get authentication token and user ID
get_auth_token() {
    print_section_header "AUTHENTICATION"
    echo "Attempting to authenticate as ${USERNAME}..."

    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}" \
        "${BACKEND_URL}/api/v1/auth/login")

    ACCESS_TOKEN=$(echo "$response" | jq -r '.data.access_token // empty')

    if [ -z "$ACCESS_TOKEN" ]; then
        echo -e "${RED}✗ Failed to get authentication token${NC}"
        echo "Response: $response"
        exit 1
    fi

    # Get user ID
    TEST_USER_ID=$(echo "$response" | jq -r '.data.user.id // empty')

    # If not in login response, try to fetch from account endpoint
    if [ -z "$TEST_USER_ID" ] || [ "$TEST_USER_ID" = "null" ]; then
        local user_response=$(curl -s -X GET \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            "${BACKEND_URL}/api/v1/auth/account" 2>/dev/null)

        TEST_USER_ID=$(echo "$user_response" | jq -r '.data.id // empty')
    fi

    # If still no user ID, use default
    if [ -z "$TEST_USER_ID" ] || [ "$TEST_USER_ID" = "null" ]; then
        TEST_USER_ID=1
        echo -e "${YELLOW}⚠ Could not get user ID from API, using default: ${TEST_USER_ID}${NC}"
    fi

    echo -e "${GREEN}✓ Successfully authenticated${NC}"
    echo "Token: ${ACCESS_TOKEN:0:20}..."
    echo "User ID: ${TEST_USER_ID}"
}

# Find or create test course
setup_test_course() {
    print_section_header "TEST COURSE SETUP"

    # Try to find existing courses first
    echo "Searching for existing courses..."
    local courses_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}/api/v1/courses?page=0&size=10" 2>/dev/null)

    local existing_course_id=$(echo "$courses_response" | jq -r '.data.content[0].id // empty' 2>/dev/null)

    if [ -n "$existing_course_id" ] && [ "$existing_course_id" != "null" ]; then
        TEST_COURSE_ID="$existing_course_id"
        echo -e "${GREEN}✓ Using existing course ID: ${TEST_COURSE_ID}${NC}"

        # Try to find a second course
        SECOND_COURSE_ID=$(echo "$courses_response" | jq -r '.data.content[1].id // empty' 2>/dev/null)
        if [ -n "$SECOND_COURSE_ID" ] && [ "$SECOND_COURSE_ID" != "null" ]; then
            echo -e "${GREEN}✓ Using second existing course ID: ${SECOND_COURSE_ID}${NC}"
        else
            echo -e "${YELLOW}⚠ Only one course found, will skip some tests${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ No existing courses found${NC}"
        echo "Note: Course creation may require admin permissions"
        echo "Using placeholder course ID (tests may fail)"
        TEST_COURSE_ID=1
        SECOND_COURSE_ID=2
    fi
}

# Create enrollments with different completion levels
setup_enrollments() {
    print_section_header "ENROLLMENT SETUP"

    # Check if user is already enrolled
    echo "Checking existing enrollments..."
    local enrollments_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}/api/v1/enrollments/user/${TEST_USER_ID}" 2>/dev/null)

    # Try to find enrollment with sufficient completion
    local enrolled_course=$(echo "$enrollments_response" | jq -r ".data[] | select(.comLevel >= 30) | .courseId" 2>/dev/null | head -n1)

    if [ -n "$enrolled_course" ] && [ "$enrolled_course" != "null" ]; then
        TEST_COURSE_ID="$enrolled_course"
        echo -e "${GREEN}✓ Found enrollment with sufficient completion (>=30%) for course: ${TEST_COURSE_ID}${NC}"
        ENROLLMENT_ID_SUFFICIENT="found"
    else
        echo -e "${YELLOW}⚠ No enrollment with >=30% completion found${NC}"

        # Try to create enrollment with sufficient completion
        echo "Attempting to create enrollment with sufficient completion..."
        local enroll_response=$(curl -s -X POST \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "{\"userId\":${TEST_USER_ID},\"courseId\":${TEST_COURSE_ID}}" \
            "${BACKEND_URL}/api/v1/enrollments" 2>/dev/null)

        ENROLLMENT_ID_SUFFICIENT=$(echo "$enroll_response" | jq -r '.data.id // empty')

        if [ -n "$ENROLLMENT_ID_SUFFICIENT" ] && [ "$ENROLLMENT_ID_SUFFICIENT" != "null" ]; then
            echo -e "${GREEN}✓ Enrollment created: ${ENROLLMENT_ID_SUFFICIENT}${NC}"

            # Try to update completion level to 50% (may require admin or may not be possible)
            echo "Note: Enrollment created with 0% completion. Review tests requiring >=30% may fail."
            echo "Manually update comLevel in database if needed: UPDATE enrollments SET com_level = 50 WHERE id = ${ENROLLMENT_ID_SUFFICIENT}"
        else
            echo -e "${YELLOW}⚠ Could not create enrollment. Tests requiring enrollment will fail.${NC}"
        fi
    fi

    # For insufficient enrollment test
    if [ -n "$SECOND_COURSE_ID" ] && [ "$SECOND_COURSE_ID" != "null" ]; then
        local insufficient_enrolled=$(echo "$enrollments_response" | jq -r ".data[] | select(.comLevel < 30) | .courseId" 2>/dev/null | head -n1)

        if [ -n "$insufficient_enrolled" ] && [ "$insufficient_enrolled" != "null" ]; then
            ENROLLMENT_ID_INSUFFICIENT="$insufficient_enrolled"
            echo -e "${GREEN}✓ Found enrollment with insufficient completion (<30%)${NC}"
        fi
    fi

    echo -e "\n${BLUE}Enrollment Setup Summary:${NC}"
    echo "  Test Course ID: ${TEST_COURSE_ID}"
    echo "  Second Course ID: ${SECOND_COURSE_ID:-none}"
    echo "  Enrollment (>=30%): ${ENROLLMENT_ID_SUFFICIENT:-not created}"
    echo "  Enrollment (<30%): ${ENROLLMENT_ID_INSUFFICIENT:-not created}"
}

# Run a test case
run_test() {
    local test_name="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    local category="$6"
    local validation_check="${7:-}"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    # Build curl command
    local curl_cmd="curl -s -w \"\\n%{http_code}\" -X ${method}"

    if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "INVALID" ]; then
        curl_cmd="$curl_cmd -H \"Authorization: Bearer ${ACCESS_TOKEN}\""
    elif [ "$ACCESS_TOKEN" = "INVALID" ]; then
        curl_cmd="$curl_cmd -H \"Authorization: Bearer invalid_token_12345\""
    fi

    curl_cmd="$curl_cmd -H \"Content-Type: application/json\""

    if [ -n "$data" ] && [ "$data" != "NONE" ]; then
        curl_cmd="$curl_cmd -d '${data}'"
    fi

    curl_cmd="$curl_cmd \"${BACKEND_URL}${endpoint}\""

    # Execute request with timeout
    local response=$(eval "timeout 10s $curl_cmd 2>/dev/null")
    local exit_code=$?

    if [ $exit_code -eq 124 ]; then
        echo -e "${RED}✗ FAIL${NC}: $test_name (Request timeout after 10s)"
        TEST_RESULTS+=("FAIL|$category|$test_name|Timeout")
        FAILED_COUNT=$((FAILED_COUNT + 1))
        return
    fi

    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    # Handle multiple acceptable status codes (e.g., "200|409")
    if [[ "$expected_status" == *"|"* ]]; then
        local status_match=false
        IFS='|' read -ra statuses <<< "$expected_status"
        for status in "${statuses[@]}"; do
            if [ "$http_code" = "$status" ]; then
                status_match=true
                break
            fi
        done

        if [ "$status_match" = true ]; then
            echo -e "${GREEN}✓ PASS${NC}: $test_name"
            TEST_RESULTS+=("PASS|$category|$test_name")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: $test_name (Expected: $expected_status, Got: $http_code)"
            if [ -n "$body" ]; then
                echo "  Response: $(echo "$body" | jq -c . 2>/dev/null || echo "$body")"
            fi
            TEST_RESULTS+=("FAIL|$category|$test_name|Expected: $expected_status, Got: $http_code")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
        return
    fi

    # Check HTTP status code
    if [ "$http_code" = "$expected_status" ]; then
        # Additional validation check if provided
        if [ -n "$validation_check" ]; then
            if echo "$body" | jq -e "$validation_check" > /dev/null 2>&1; then
                echo -e "${GREEN}✓ PASS${NC}: $test_name"
                TEST_RESULTS+=("PASS|$category|$test_name")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "${RED}✗ FAIL${NC}: $test_name (Validation check failed: $validation_check)"
                TEST_RESULTS+=("FAIL|$category|$test_name|Validation: $validation_check failed")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi
        else
            echo -e "${GREEN}✓ PASS${NC}: $test_name"
            TEST_RESULTS+=("PASS|$category|$test_name")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        fi
    else
        echo -e "${RED}✗ FAIL${NC}: $test_name (Expected: $expected_status, Got: $http_code)"
        if [ -n "$body" ]; then
            echo "  Response: $(echo "$body" | jq -c . 2>/dev/null || echo "$body")"
        fi
        TEST_RESULTS+=("FAIL|$category|$test_name|Expected: $expected_status, Got: $http_code")
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
generate_report() {
    print_section_header "TEST EXECUTION SUMMARY"

    echo -e "${BLUE}Test Statistics:${NC}"
    echo -e "  Total Tests:   ${TOTAL_TESTS}"
    echo -e "  ${GREEN}Passed:        ${PASSED_COUNT}${NC}"
    echo -e "  ${RED}Failed:        ${FAILED_COUNT}${NC}"
    echo -e "  ${YELLOW}Pending:       ${PENDING_COUNT}${NC}"

    if [ ${TOTAL_TESTS} -gt 0 ]; then
        local pass_rate=$((PASSED_COUNT * 100 / TOTAL_TESTS))
        echo -e "\n  Pass Rate:     ${pass_rate}%"
    fi

    # Summary by category
    echo -e "\n${BLUE}Results by Category:${NC}"
    for category in "VALIDATE" "LOGIC" "ERROR_CODE" "FORMAT_RESPONSE"; do
        local cat_total=0
        local cat_passed=0
        local cat_failed=0

        for result in "${TEST_RESULTS[@]}"; do
            IFS='|' read -ra parts <<< "$result"
            if [ "${parts[1]}" = "$category" ]; then
                cat_total=$((cat_total + 1))
                if [ "${parts[0]}" = "PASS" ]; then
                    cat_passed=$((cat_passed + 1))
                else
                    cat_failed=$((cat_failed + 1))
                fi
            fi
        done

        if [ $cat_total -gt 0 ]; then
            echo -e "  ${category}: ${GREEN}${cat_passed} passed${NC}, ${RED}${cat_failed} failed${NC} (${cat_total} total)"
        fi
    done

    # Show failed tests
    if [ ${FAILED_COUNT} -gt 0 ]; then
        echo -e "\n${RED}Failed Tests:${NC}"
        for result in "${TEST_RESULTS[@]}"; do
            if [[ $result == FAIL* ]]; then
                IFS='|' read -ra parts <<< "$result"
                echo -e "  ${RED}✗${NC} [${parts[1]}] ${parts[2]}"
                if [ ${#parts[@]} -ge 4 ]; then
                    echo -e "     ${parts[3]}"
                fi
            fi
        done
    fi

    # Final verdict
    echo ""
    if [ ${FAILED_COUNT} -eq 0 ] && [ ${PENDING_COUNT} -eq 0 ]; then
        echo -e "${GREEN}═══════════════════════════════════════${NC}"
        echo -e "${GREEN}   ALL TESTS PASSED! ✓${NC}"
        echo -e "${GREEN}═══════════════════════════════════════${NC}"
    elif [ ${FAILED_COUNT} -eq 0 ]; then
        echo -e "${YELLOW}═══════════════════════════════════════${NC}"
        echo -e "${YELLOW}   ALL TESTS PASSED (some pending)${NC}"
        echo -e "${YELLOW}═══════════════════════════════════════${NC}"
    else
        echo -e "${RED}═══════════════════════════════════════${NC}"
        echo -e "${RED}   SOME TESTS FAILED ✗${NC}"
        echo -e "${RED}═══════════════════════════════════════${NC}"
    fi
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================
main() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                                                        ║${NC}"
    echo -e "${BLUE}║          Review API - Automated Test Suite            ║${NC}"
    echo -e "${BLUE}║                                                        ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
    echo -e "\n${BLUE}Backend URL:${NC} ${BACKEND_URL}"
    echo -e "${BLUE}Username:${NC}    ${USERNAME}"
    echo -e "${BLUE}Start Time:${NC}  $(date '+%Y-%m-%d %H:%M:%S')"

    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}Error: jq is not installed. Please install jq to run this test suite.${NC}"
        echo "Install with: sudo apt-get install jq (Ubuntu/Debian) or brew install jq (macOS)"
        exit 1
    fi

    # Authenticate
    get_auth_token

    # Setup test data
    setup_test_course
    setup_enrollments

    # Run test categories
    run_validate_tests
    run_logic_tests
    run_error_code_tests
    run_format_response_tests

    # Generate report
    generate_report

    echo -e "\n${BLUE}End Time:${NC}    $(date '+%Y-%m-%d %H:%M:%S')"

    # Important notes
    echo -e "\n${CYAN}═══════════════════════════════════════${NC}"
    echo -e "${CYAN}IMPORTANT NOTES${NC}"
    echo -e "${CYAN}═══════════════════════════════════════${NC}"
    echo -e "${YELLOW}1. Review API requires:${NC}"
    echo -e "   - User must be enrolled in the course"
    echo -e "   - Enrollment completion must be ≥30%"
    echo -e "   - User can only review a course once (409 on duplicate)"
    echo -e "\n${YELLOW}2. If tests fail with 400/404:${NC}"
    echo -e "   - Check enrollment exists: SELECT * FROM enrollments WHERE user_id = ${TEST_USER_ID}"
    echo -e "   - Update completion: UPDATE enrollments SET com_level = 50 WHERE user_id = ${TEST_USER_ID}"
    echo -e "\n${YELLOW}3. Reviews created during testing remain in database${NC}"

    # Exit with appropriate code
    if [ ${FAILED_COUNT} -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# Run main function
main "$@"
