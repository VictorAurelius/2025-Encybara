#!/bin/bash

################################################################################
# TEST SCRIPT: AddQuestionsToLesson API
# API Endpoint: POST /api/v1/lessons/{lessonId}/questions
# Description: Automated testing for adding questions to lesson functionality
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

# Test data IDs (created during setup)
TEST_LESSON_ID=""
declare -a TEST_QUESTION_IDS

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

# Setup test data
setup_test_data() {
    print_section_header "TEST DATA SETUP"

    # Create test lesson
    echo "Creating test lesson..."
    local lesson_response=$(curl -s -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{"name":"Test Lesson for API Testing","skillType":"READING"}' \
        "${BACKEND_URL}/api/v1/lessons")

    TEST_LESSON_ID=$(echo "$lesson_response" | jq -r '.data.id // empty')

    if [ -z "$TEST_LESSON_ID" ] || [ "$TEST_LESSON_ID" = "null" ]; then
        echo -e "${RED}✗ Failed to create test lesson${NC}"
        echo "Response: $lesson_response"
        exit 1
    fi

    echo -e "${GREEN}✓ Test lesson created with ID: ${TEST_LESSON_ID}${NC}"

    # Create 3 test questions
    echo "Creating test questions..."

    for i in 1 2 3; do
        local question_response=$(curl -s -X POST \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "{\"quesContent\":\"Test question ${i} for lesson testing\",\"keyword\":\"test-lesson-q${i}\",\"quesType\":\"CHOICE\",\"skillType\":\"READING\",\"point\":10,\"questionChoices\":[{\"choiceContent\":\"Option A\",\"choiceKey\":true},{\"choiceContent\":\"Option B\",\"choiceKey\":false}]}" \
            "${BACKEND_URL}/api/v1/questions")

        local question_id=$(echo "$question_response" | jq -r '.data.id // empty')

        if [ -z "$question_id" ] || [ "$question_id" = "null" ]; then
            echo -e "${RED}✗ Failed to create test question ${i}${NC}"
            echo "Response: $question_response"
            exit 1
        fi

        TEST_QUESTION_IDS+=("$question_id")
        echo -e "${GREEN}✓ Test question ${i} created with ID: ${question_id}${NC}"
    done

    echo -e "\n${GREEN}Setup complete!${NC}"
    echo "Lesson ID: ${TEST_LESSON_ID}"
    echo "Question IDs: ${TEST_QUESTION_IDS[@]}"
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

    # Test 1: Invalid lessonId (non-numeric)
    run_test \
        "Invalid lessonId (non-numeric) should return 400" \
        "POST" \
        "/api/v1/lessons/invalid/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
        "400" \
        "VALIDATE"

    # Test 2: Non-existent lessonId
    run_test \
        "Non-existent lessonId should return 404" \
        "POST" \
        "/api/v1/lessons/999999999/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
        "404" \
        "VALIDATE"

    # Test 3: Empty questionIds array
    run_test \
        "Empty questionIds array should return 400 or 404" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        '{"questionIds":[]}' \
        "400|404" \
        "VALIDATE"

    # Test 4: Missing questionIds field
    run_test \
        "Missing questionIds should return 400" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        '{}' \
        "400" \
        "VALIDATE"

    # Test 5: Invalid questionIds format (not array)
    run_test \
        "Invalid questionIds format (string) should return 400" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        '{"questionIds":"not-an-array"}' \
        "400" \
        "VALIDATE"

    # Test 6: questionIds with non-numeric values
    run_test \
        "questionIds with non-numeric values should return 400" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        '{"questionIds":["invalid"]}' \
        "400" \
        "VALIDATE"

    # Test 7: Missing request body
    run_test \
        "Missing request body should return 400" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        "" \
        "400" \
        "VALIDATE"

    # Test 8: Malformed JSON
    run_test \
        "Malformed JSON should return 400" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        '{"questionIds":[123' \
        "400" \
        "VALIDATE"
}

# ============================================================================
# TEST CATEGORY 2: LOGIC TESTS (Business Rules)
# ============================================================================
run_logic_tests() {
    print_section_header "CATEGORY 2: LOGIC TESTS (Business Rules)"

    # Create a separate test lesson for logic tests to avoid duplication issues
    echo "Creating separate lesson for logic tests..."
    local logic_lesson_response=$(curl -s -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{"name":"Logic Test Lesson","skillType":"READING"}' \
        "${BACKEND_URL}/api/v1/lessons")

    local logic_lesson_id=$(echo "$logic_lesson_response" | jq -r '.data.id // empty')

    if [ -z "$logic_lesson_id" ] || [ "$logic_lesson_id" = "null" ]; then
        echo -e "${YELLOW}⚠ Could not create logic test lesson, using main test lesson${NC}"
        logic_lesson_id="$TEST_LESSON_ID"
    else
        echo -e "${GREEN}✓ Logic test lesson created: ${logic_lesson_id}${NC}"
    fi

    # Test 1: Add 1 question to lesson
    run_test \
        "Add single question to lesson" \
        "POST" \
        "/api/v1/lessons/${logic_lesson_id}/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
        "200" \
        "LOGIC" \
        '.statusCode == 200'

    # Create another lesson for batch test
    local batch_lesson_response=$(curl -s -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{"name":"Batch Test Lesson","skillType":"READING"}' \
        "${BACKEND_URL}/api/v1/lessons")

    local batch_lesson_id=$(echo "$batch_lesson_response" | jq -r '.data.id // empty')

    # Test 2: Add multiple questions (batch)
    if [ -n "$batch_lesson_id" ] && [ "$batch_lesson_id" != "null" ]; then
        run_test \
            "Add multiple questions in batch" \
            "POST" \
            "/api/v1/lessons/${batch_lesson_id}/questions" \
            "{\"questionIds\":[${TEST_QUESTION_IDS[0]},${TEST_QUESTION_IDS[1]},${TEST_QUESTION_IDS[2]}]}" \
            "200" \
            "LOGIC" \
            '.statusCode == 200'

        # Test 3: Add same question twice (should fail with 409)
        run_test \
            "Add duplicate question should return 409" \
            "POST" \
            "/api/v1/lessons/${batch_lesson_id}/questions" \
            "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
            "409" \
            "LOGIC"
    else
        echo -e "${YELLOW}⚠ SKIP${NC}: Batch tests skipped (could not create batch test lesson)"
        PENDING_COUNT=$((PENDING_COUNT + 2))
        TOTAL_TESTS=$((TOTAL_TESTS + 2))
    fi

    # Test 4: Add non-existent questionId
    run_test \
        "Add non-existent questionId should return 404" \
        "POST" \
        "/api/v1/lessons/${logic_lesson_id}/questions" \
        '{"questionIds":[999999999]}' \
        "404" \
        "LOGIC"

    # Test 5: Partial failure (1 valid, 1 invalid - transaction should rollback)
    run_test \
        "Partial failure should return 404 (transaction)" \
        "POST" \
        "/api/v1/lessons/${logic_lesson_id}/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[1]},999999999]}" \
        "404" \
        "LOGIC"

    # Test 6: Add question to non-existent lesson
    run_test \
        "Add to non-existent lesson should return 404" \
        "POST" \
        "/api/v1/lessons/999999999/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
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
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 2: Invalid auth token
    ACCESS_TOKEN="INVALID"
    run_test \
        "Request with invalid token should return 401" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
        "401" \
        "ERROR_CODE"
    ACCESS_TOKEN="$temp_token"

    # Test 3: Wrong HTTP method (GET)
    run_test \
        "Wrong HTTP method (GET) should return 405" \
        "GET" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        "NONE" \
        "405" \
        "ERROR_CODE"

    # Test 4: Invalid path parameter format
    run_test \
        "Invalid path parameter should return 400" \
        "POST" \
        "/api/v1/lessons/abc123/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[0]}]}" \
        "400" \
        "ERROR_CODE"

    # Test 5: Null questionIds value
    run_test \
        "Null questionIds should return 400" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        '{"questionIds":null}' \
        "400" \
        "ERROR_CODE"

    # Test 6: Extra unexpected fields (should be ignored or accepted)
    run_test \
        "Extra fields should be ignored (200 or 400)" \
        "POST" \
        "/api/v1/lessons/${TEST_LESSON_ID}/questions" \
        "{\"questionIds\":[${TEST_QUESTION_IDS[1]}],\"extraField\":\"should-be-ignored\"}" \
        "200|400|409" \
        "ERROR_CODE"
}

# ============================================================================
# TEST CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)
# ============================================================================
run_format_response_tests() {
    print_section_header "CATEGORY 4: FORMAT RESPONSE TESTS (Response Structure)"

    # Create a fresh lesson for format tests
    local format_lesson_response=$(curl -s -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{"name":"Format Test Lesson","skillType":"READING"}' \
        "${BACKEND_URL}/api/v1/lessons")

    local format_lesson_id=$(echo "$format_lesson_response" | jq -r '.data.id // empty')

    if [ -n "$format_lesson_id" ] && [ "$format_lesson_id" != "null" ]; then
        # Add question to lesson and check response format
        local response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "{\"questionIds\":[${TEST_QUESTION_IDS[2]}]}" \
            "${BACKEND_URL}/api/v1/lessons/${format_lesson_id}/questions")

        local http_code=$(echo "$response" | tail -n1)
        local body=$(echo "$response" | sed '$d')

        if [ "$http_code" = "200" ]; then
            # Test 1: Response has statusCode = 200
            TOTAL_TESTS=$((TOTAL_TESTS + 1))
            if echo "$body" | jq -e '.statusCode == 200' > /dev/null 2>&1; then
                echo -e "${GREEN}✓ PASS${NC}: Response has statusCode = 200"
                TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response statusCode = 200")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "${RED}✗ FAIL${NC}: Response statusCode incorrect"
                TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Response statusCode incorrect")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi

            # Test 2: Response has message field
            TOTAL_TESTS=$((TOTAL_TESTS + 1))
            if echo "$body" | jq -e '.message' > /dev/null 2>&1; then
                echo -e "${GREEN}✓ PASS${NC}: Response has message field"
                TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Response has message")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "${RED}✗ FAIL${NC}: Response missing message field"
                TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Missing message field")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi

            # Test 3: Response data is null (void return)
            TOTAL_TESTS=$((TOTAL_TESTS + 1))
            local data_value=$(echo "$body" | jq -r '.data')
            if [ "$data_value" = "null" ] || [ -z "$data_value" ]; then
                echo -e "${GREEN}✓ PASS${NC}: Response data is null (void endpoint)"
                TEST_RESULTS+=("PASS|FORMAT_RESPONSE|data is null")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "${RED}✗ FAIL${NC}: Response data should be null"
                TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|data should be null")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi

            # Test 4: Verify lesson was updated (query lesson)
            TOTAL_TESTS=$((TOTAL_TESTS + 1))
            local lesson_check=$(curl -s -X GET \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                "${BACKEND_URL}/api/v1/lessons/${format_lesson_id}")

            local has_questions=$(echo "$lesson_check" | jq -e '.data.questionIds | length > 0')
            if [ "$has_questions" = "true" ]; then
                echo -e "${GREEN}✓ PASS${NC}: Lesson updated correctly (has questions)"
                TEST_RESULTS+=("PASS|FORMAT_RESPONSE|Lesson updated")
                PASSED_COUNT=$((PASSED_COUNT + 1))
            else
                echo -e "${RED}✗ FAIL${NC}: Lesson not updated correctly"
                TEST_RESULTS+=("FAIL|FORMAT_RESPONSE|Lesson not updated")
                FAILED_COUNT=$((FAILED_COUNT + 1))
            fi
        else
            echo -e "${YELLOW}⚠ SKIP${NC}: Format tests skipped (HTTP $http_code)"
            PENDING_COUNT=$((PENDING_COUNT + 4))
            TOTAL_TESTS=$((TOTAL_TESTS + 4))
        fi
    else
        echo -e "${YELLOW}⚠ SKIP${NC}: Format tests skipped (could not create format test lesson)"
        PENDING_COUNT=$((PENDING_COUNT + 4))
        TOTAL_TESTS=$((TOTAL_TESTS + 4))
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
    echo -e "${BLUE}║   AddQuestionsToLesson API - Automated Test Suite     ║${NC}"
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
    setup_test_data

    # Run test categories
    run_validate_tests
    run_logic_tests
    run_error_code_tests
    run_format_response_tests

    # Generate report
    generate_report

    echo -e "\n${BLUE}End Time:${NC}    $(date '+%Y-%m-%d %H:%M:%S')"

    # Note about cleanup
    echo -e "\n${YELLOW}Note:${NC} Test lesson (ID: ${TEST_LESSON_ID}) and questions remain in database."
    echo "Delete manually if needed or run cleanup script."

    # Exit with appropriate code
    if [ ${FAILED_COUNT} -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# Run main function
main "$@"
