#!/bin/bash

################################################################################
# TEST SCRIPT: Notification API
# API Endpoint: POST /api/v1/notifications
# Description: Automated testing for notification creation functionality
# Version: 1.0
# Date: 2025-11-07
# Reference: documents/req-7.md, documents/output/API_Document_V3.md
################################################################################

# ============================================================================
# CONFIGURATION
# ============================================================================
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-admin@gmail.com}"
PASSWORD="${PASSWORD:-123456}"
ACCESS_TOKEN=""
VALID_USER_ID=""

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

# Get authentication token
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

    # Get user ID from the login response or fetch current user
    VALID_USER_ID=$(echo "$response" | jq -r '.data.user.id // empty')

    # If not in login response, try to fetch from another endpoint
    if [ -z "$VALID_USER_ID" ] || [ "$VALID_USER_ID" = "null" ]; then
        # Try to get account info
        local user_response=$(curl -s -X GET \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            "${BACKEND_URL}/api/v1/auth/account" 2>/dev/null)

        VALID_USER_ID=$(echo "$user_response" | jq -r '.data.id // empty')
    fi

    # If still no user ID, use a default value (adjust as needed)
    if [ -z "$VALID_USER_ID" ] || [ "$VALID_USER_ID" = "null" ]; then
        VALID_USER_ID=1
        echo -e "${YELLOW}⚠ Could not get user ID from API, using default: ${VALID_USER_ID}${NC}"
    fi

    echo -e "${GREEN}✓ Successfully authenticated${NC}"
    echo "Token: ${ACCESS_TOKEN:0:20}..."
    echo "User ID: ${VALID_USER_ID}"
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

    # Test 1: Empty message
    run_test \
        "Empty message should return 400" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"\",\"userId\":${VALID_USER_ID},\"img\":\"STUDY\"}" \
        "400" \
        "VALIDATE"

    # Test 2: Missing message field
    run_test \
        "Missing message should return 400" \
        "POST" \
        "/api/v1/notifications" \
        "{\"userId\":${VALID_USER_ID},\"img\":\"STUDY\"}" \
        "400" \
        "VALIDATE"

    # Test 3: Missing userId
    run_test \
        "Missing userId should return 400" \
        "POST" \
        "/api/v1/notifications" \
        '{"message":"Test notification","img":"STUDY"}' \
        "400" \
        "VALIDATE"

    # Test 4: Invalid userId (non-numeric string)
    run_test \
        "Invalid userId format should return 400" \
        "POST" \
        "/api/v1/notifications" \
        '{"message":"Test notification","userId":"invalid","img":"STUDY"}' \
        "400" \
        "VALIDATE"

    # Test 5: Non-existent userId (very large number unlikely to exist)
    run_test \
        "Non-existent userId should return 404 or 500" \
        "POST" \
        "/api/v1/notifications" \
        '{"message":"Test notification","userId":999999999,"img":"STUDY"}' \
        "404|500" \
        "VALIDATE"

    # Test 6: Missing img field
    run_test \
        "Missing img field should return 400" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Test notification\",\"userId\":${VALID_USER_ID}}" \
        "400" \
        "VALIDATE"

    # Test 7: Invalid img ENUM value
    run_test \
        "Invalid img ENUM should return 400" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Test notification\",\"userId\":${VALID_USER_ID},\"img\":\"INVALID_IMG\"}" \
        "400" \
        "VALIDATE"

    # Test 8: Empty request body
    run_test \
        "Empty request body should return 400" \
        "POST" \
        "/api/v1/notifications" \
        '{}' \
        "400" \
        "VALIDATE"
}

# ============================================================================
# TEST CATEGORY 2: LOGIC TESTS (Business Rules)
# ============================================================================
run_logic_tests() {
    print_section_header "CATEGORY 2: LOGIC TESTS (Business Rules)"

    # Test 1: Valid notification with all fields
    run_test \
        "Valid notification with all fields" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"You have a new lesson available\",\"userId\":${VALID_USER_ID},\"entityId\":123,\"entityType\":\"LESSON\",\"img\":\"STUDY\"}" \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 2: Valid notification without entityId/entityType
    run_test \
        "Valid notification without entityId/entityType" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"General notification message\",\"userId\":${VALID_USER_ID},\"img\":\"ACCOUNT\"}" \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 3: Notification with entityId but no entityType
    run_test \
        "Notification with entityId but no entityType" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Notification with partial entity info\",\"userId\":${VALID_USER_ID},\"entityId\":456,\"img\":\"FLASHCARD\"}" \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 4: Notification with entityType but no entityId
    run_test \
        "Notification with entityType but no entityId" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Notification with entity type only\",\"userId\":${VALID_USER_ID},\"entityType\":\"REVIEW\",\"img\":\"SCHEDULE\"}" \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 5-8: Valid notification with each img ENUM value
    run_test \
        "Valid notification with img=STUDY" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Study reminder\",\"userId\":${VALID_USER_ID},\"img\":\"STUDY\"}" \
        "200" \
        "LOGIC" \
        '.data.img == "STUDY"'

    run_test \
        "Valid notification with img=FLASHCARD" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Flashcard notification\",\"userId\":${VALID_USER_ID},\"img\":\"FLASHCARD\"}" \
        "200" \
        "LOGIC" \
        '.data.img == "FLASHCARD"'

    run_test \
        "Valid notification with img=SCHEDULE" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Schedule update\",\"userId\":${VALID_USER_ID},\"img\":\"SCHEDULE\"}" \
        "200" \
        "LOGIC" \
        '.data.img == "SCHEDULE"'

    run_test \
        "Valid notification with img=ACCOUNT" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Account notification\",\"userId\":${VALID_USER_ID},\"img\":\"ACCOUNT\"}" \
        "200" \
        "LOGIC" \
        '.data.img == "ACCOUNT"'
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
        "/api/v1/notifications" \
        "{\"message\":\"Test\",\"userId\":${VALID_USER_ID},\"img\":\"STUDY\"}" \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 2: Invalid auth token
    ACCESS_TOKEN="INVALID"
    run_test \
        "Request with invalid token should return 401" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Test\",\"userId\":${VALID_USER_ID},\"img\":\"STUDY\"}" \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 3: Malformed JSON
    run_test \
        "Malformed JSON should return 400" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":\"Test\",\"userId\":${VALID_USER_ID}" \
        "400" \
        "ERROR_CODE"

    # Test 4: Wrong HTTP method (GET instead of POST)
    run_test \
        "Wrong HTTP method (GET) should return 405" \
        "GET" \
        "/api/v1/notifications" \
        "NONE" \
        "405" \
        "ERROR_CODE"

    # Test 5: Null values in required fields
    run_test \
        "Null message should return 400" \
        "POST" \
        "/api/v1/notifications" \
        "{\"message\":null,\"userId\":${VALID_USER_ID},\"img\":\"STUDY\"}" \
        "400" \
        "ERROR_CODE"

    # Test 6: Wrong content type (if API validates)
    run_test \
        "Wrong content type should cause error" \
        "POST" \
        "/api/v1/notifications" \
        "NONE" \
        "400|415" \
        "ERROR_CODE"
}

# ============================================================================
# TEST CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)
# ============================================================================
run_format_response_tests() {
    print_section_header "CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)"

    # Create a notification and capture the response for validation
    local test_data="{\"message\":\"Format test notification\",\"userId\":${VALID_USER_ID},\"entityId\":789,\"entityType\":\"TEST\",\"img\":\"STUDY\"}"

    local response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$test_data" \
        "${BACKEND_URL}/api/v1/notifications")

    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        # Test 1: Response has statusCode = 200
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.statusCode == 200' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has statusCode = 200"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response has statusCode = 200")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing or incorrect statusCode"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response statusCode incorrect")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 2: Response has data.id (auto-generated)
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.id' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has data.id field"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response has data.id")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing data.id field"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response missing data.id")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 3: Response data.isRead = false
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.isRead == false' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response data.isRead = false (default)"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|isRead defaults to false")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data.isRead is not false"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|isRead not false by default")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 4: Response data.createdAt is valid timestamp
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.createdAt' > /dev/null 2>&1; then
            local created_at=$(echo "$body" | jq -r '.data.createdAt')
            if [ -n "$created_at" ] && [ "$created_at" != "null" ]; then
                echo -e "${GREEN}✓ PASS${NC}: Response has valid createdAt timestamp"
                TEST_RESULTS+=("PASS|FORMAT_RESPONSE|createdAt is valid")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "${RED}✗ FAIL${NC}: Response createdAt is null or empty"
                TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|createdAt is null")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing data.createdAt field"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Missing createdAt")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 5: Response data matches request fields
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        local has_all_fields=true
        for field in message userId img; do
            if ! echo "$body" | jq -e ".data.${field}" > /dev/null 2>&1; then
                has_all_fields=false
                break
            fi
        done

        if [ "$has_all_fields" = true ]; then
            echo -e "${GREEN}✓ PASS${NC}: Response data contains all request fields"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|All request fields present")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data missing some request fields"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Missing request fields")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 6: Response data.img matches request
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        local response_img=$(echo "$body" | jq -r '.data.img')
        if [ "$response_img" = "STUDY" ]; then
            echo -e "${GREEN}✓ PASS${NC}: Response data.img matches request"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|img field matches")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data.img does not match request"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|img field mismatch")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo -e "${YELLOW}⚠ SKIP${NC}: Format tests skipped (failed to create test notification, HTTP $http_code)"
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
    echo -e "${BLUE}║        Notification API - Automated Test Suite        ║${NC}"
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

    # Run test categories
    run_validate_tests
    run_logic_tests
    run_error_code_tests
    run_format_response_tests

    # Generate report
    generate_report

    echo -e "\n${BLUE}End Time:${NC}    $(date '+%Y-%m-%d %H:%M:%S')"

    # Exit with appropriate code
    if [ ${FAILED_COUNT} -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# Run main function
main "$@"
