#!/bin/bash

################################################################################
# TEST SCRIPT: CreateQuestion API
# API Endpoint: POST /api/v1/questions
# Description: Automated testing for question creation functionality
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

    echo -e "${GREEN}✓ Successfully authenticated${NC}"
    echo "Token: ${ACCESS_TOKEN:0:20}..."
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

    # Test 1: Empty quesContent
    run_test \
        "Empty quesContent should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"","keyword":"test","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 2: Null quesContent (missing field)
    run_test \
        "Missing quesContent should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"keyword":"test","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 3: Zero point
    run_test \
        "Zero point should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","quesType":"CHOICE","skillType":"READING","point":0,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 4: Negative point
    run_test \
        "Negative point should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","quesType":"CHOICE","skillType":"READING","point":-5,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 5: Empty keyword (should auto-fill for WRITING, accept for others)
    run_test \
        "Empty keyword for CHOICE question should be accepted" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"What is 2+2?","keyword":"","quesType":"CHOICE","skillType":"READING","point":5,"questionChoices":[{"choiceContent":"4","choiceKey":true}]}' \
        "200" \
        "VALIDATE" \
        '.data.id'

    # Test 6: Missing quesType
    run_test \
        "Missing quesType should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","skillType":"READING","point":10,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 7: Invalid quesType ENUM
    run_test \
        "Invalid quesType ENUM should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","quesType":"INVALID_TYPE","skillType":"READING","point":10,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 8: Missing skillType
    run_test \
        "Missing skillType should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","quesType":"CHOICE","point":10,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 9: Invalid skillType ENUM
    run_test \
        "Invalid skillType ENUM should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","quesType":"CHOICE","skillType":"INVALID_SKILL","point":10,"questionChoices":[]}' \
        "400" \
        "VALIDATE"

    # Test 10: Missing required field (point)
    run_test \
        "Missing point field should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test question?","keyword":"test","quesType":"CHOICE","skillType":"READING","questionChoices":[]}' \
        "400" \
        "VALIDATE"
}

# ============================================================================
# TEST CATEGORY 2: LOGIC TESTS (Business Rules)
# ============================================================================
run_logic_tests() {
    print_section_header "CATEGORY 2: LOGIC TESTS (Business Rules)"

    # Test 1: Valid CHOICE question with 4 choices
    run_test \
        "Valid CHOICE question with multiple choices" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"What is the capital of France?","keyword":"geography","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[{"choiceContent":"Paris","choiceKey":true},{"choiceContent":"London","choiceKey":false},{"choiceContent":"Berlin","choiceKey":false},{"choiceContent":"Madrid","choiceKey":false}]}' \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 2: Valid MULTIPLE question with choices
    run_test \
        "Valid MULTIPLE question with choices" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Select all prime numbers:","keyword":"mathematics","quesType":"MULTIPLE","skillType":"READING","point":15,"questionChoices":[{"choiceContent":"2","choiceKey":true},{"choiceContent":"3","choiceKey":true},{"choiceContent":"4","choiceKey":false},{"choiceContent":"5","choiceKey":true}]}' \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 3: Valid TEXT question without choices
    run_test \
        "Valid TEXT question without choices" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"What is your favorite color?","keyword":"opinion","quesType":"TEXT","skillType":"WRITING","point":5,"questionChoices":[]}' \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 4: Valid LISTENING question
    run_test \
        "Valid LISTENING question" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Listen and choose the correct answer","keyword":"audio comprehension","quesType":"LISTENING","skillType":"LISTENING","point":10,"questionChoices":[{"choiceContent":"Option A","choiceKey":true},{"choiceContent":"Option B","choiceKey":false}]}' \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 5: Valid WRITING question (WRITING skill, no choices)
    run_test \
        "Valid WRITING question with WRITING skill" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Write an essay about climate change (min 250 words)","keyword":"writing task","quesType":"WRITING","skillType":"WRITING","point":25,"questionChoices":[]}' \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 6: Valid SPEAKING question (SPEAKING skill, no choices)
    run_test \
        "Valid SPEAKING question with SPEAKING skill" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Describe your favorite holiday destination","keyword":"speaking task","quesType":"SPEAKING","skillType":"SPEAKING","point":20,"questionChoices":[]}' \
        "200" \
        "LOGIC" \
        '.data.id'

    # Test 7: WRITING question with wrong skillType (should fail)
    run_test \
        "WRITING question with READING skill should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Write about your experience","keyword":"writing","quesType":"WRITING","skillType":"READING","point":25,"questionChoices":[]}' \
        "400" \
        "LOGIC"

    # Test 8: SPEAKING question with wrong skillType (should fail)
    run_test \
        "SPEAKING question with LISTENING skill should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Talk about your hobbies","keyword":"speaking","quesType":"SPEAKING","skillType":"LISTENING","point":20,"questionChoices":[]}' \
        "400" \
        "LOGIC"
}

# ============================================================================
# TEST CATEGORY 3: ERROR CODE TESTS (HTTP Status Codes)
# ============================================================================
run_error_code_tests() {
    print_section_header "CATEGORY 3: ERROR CODE TESTS (HTTP Status Codes)"

    # Test 1: WRITING question with choices (should fail)
    run_test \
        "WRITING question with choices should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Write an essay","keyword":"writing","quesType":"WRITING","skillType":"WRITING","point":25,"questionChoices":[{"choiceContent":"Choice 1","choiceKey":true}]}' \
        "400" \
        "ERROR_CODE"

    # Test 2: SPEAKING question with choices (should fail)
    run_test \
        "SPEAKING question with choices should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Describe something","keyword":"speaking","quesType":"SPEAKING","skillType":"SPEAKING","point":20,"questionChoices":[{"choiceContent":"Choice 1","choiceKey":true}]}' \
        "400" \
        "ERROR_CODE"

    # Test 3: No auth token
    local temp_token="$ACCESS_TOKEN"
    ACCESS_TOKEN=""
    run_test \
        "Request without auth token should return 401" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test?","keyword":"test","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[]}' \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 4: Invalid auth token
    ACCESS_TOKEN="INVALID"
    run_test \
        "Request with invalid token should return 401" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test?","keyword":"test","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[]}' \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 5: Malformed JSON
    run_test \
        "Malformed JSON should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"Test","keyword":"test","quesType":"CHOICE"' \
        "400" \
        "ERROR_CODE"

    # Test 6: Wrong HTTP method (GET instead of POST)
    run_test \
        "Wrong HTTP method (GET) should return 405" \
        "GET" \
        "/api/v1/questions" \
        "NONE" \
        "405" \
        "ERROR_CODE"

    # Test 7: Empty request body
    run_test \
        "Empty request body should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{}' \
        "400" \
        "ERROR_CODE"

    # Test 8: Whitespace-only quesContent
    run_test \
        "Whitespace-only quesContent should return 400" \
        "POST" \
        "/api/v1/questions" \
        '{"quesContent":"   ","keyword":"test","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[]}' \
        "400" \
        "ERROR_CODE"
}

# ============================================================================
# TEST CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)
# ============================================================================
run_format_response_tests() {
    print_section_header "CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)"

    # Create a question and capture the response for validation
    local test_data='{"quesContent":"Sample question for format test?","keyword":"format-test","quesType":"CHOICE","skillType":"READING","point":10,"questionChoices":[{"choiceContent":"Answer A","choiceKey":true},{"choiceContent":"Answer B","choiceKey":false}]}'

    local response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$test_data" \
        "${BACKEND_URL}/api/v1/questions")

    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "200" ]; then
        # Test 1: Response has statusCode field
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.statusCode == 200' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has statusCode = 200"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response has statusCode field")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing statusCode field"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response missing statusCode field")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 2: Response has message field
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.message' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has message field"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response has message field")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing message field"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response missing message field")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 3: Response has data.id field
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.id' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response has data.id field"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response has data.id field")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response missing data.id field"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response missing data.id field")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 4: Response data contains all request fields
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        local has_all_fields=true
        for field in quesContent keyword quesType skillType point; do
            if ! echo "$body" | jq -e ".data.${field}" > /dev/null 2>&1; then
                has_all_fields=false
                break
            fi
        done

        if [ "$has_all_fields" = true ]; then
            echo -e "${GREEN}✓ PASS${NC}: Response data contains all request fields"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response data contains all fields")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data missing some request fields"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response data missing fields")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi

        # Test 5: Response data.questionChoices is array
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if echo "$body" | jq -e '.data.questionChoices | type == "array"' > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PASS${NC}: Response data.questionChoices is array"
            TEST_RESULTS+=("PASS|FORMAT_RESPONSE|questionChoices is array")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}✗ FAIL${NC}: Response data.questionChoices is not array"
            TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|questionChoices is not array")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo -e "${YELLOW}⚠ SKIP${NC}: Format tests skipped (failed to create test question, HTTP $http_code)"
        PENDING_COUNT=$((PENDING_COUNT + 5))
        TOTAL_TESTS=$((TOTAL_TESTS + 5))
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
    echo -e "${BLUE}║       CreateQuestion API - Automated Test Suite       ║${NC}"
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
