#!/bin/bash

# ============================================
# Pronunciation API Automated Test Suite
# ============================================
#
# Description:
#   This script automatically tests the Pronunciation Assessment API
#   according to the test cases defined in:
#     documents/output/Testcase_API_AssessPronunciation.md
#
# Test Coverage:
#   - Total: 30 test cases
#   - Automated: 20 test cases
#   - Manual: 10 test cases
#
# Test Categories:
#   1. Validate (ID-001 ~ ID-010): Input validation tests
#   2. Logic (ID-011 ~ ID-015): Business logic tests
#   3. Error Code (ID-016 ~ ID-025): Error handling tests
#   4. Format Response (ID-026 ~ ID-030): Response structure tests
#
# Prerequisites:
#   - Backend service running on http://localhost:8080
#   - Pronunciation assessment service configured
#   - Test audio file: documents/input/audio_sample.mp3
#   - Valid user account: user@example.com / Abc@123456
#
# Usage:
#   ./test-pronunciation-testcase.sh
#
# Output:
#   - Console output with colored results (✓ PASSED, ✗ FAILED, ⊙ PENDING)
#   - Test report file: test-report-YYYYMMDD-HHMMSS.txt
#
# Exit Codes:
#   0 - All tests completed (check report for pass/fail)
#   1 - Prerequisites not met (auth failed, file missing, etc.)
#
# Author: Generated based on req-5 task plan
# Date: 2025-11-04
# ============================================

# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
PRONUNCIATION_API="/api/v1/pronunciation"
AUTH_API="/api/v1/auth"

# Test credentials (from AdminDataInitializer)
DEFAULT_EMAIL="${DEFAULT_EMAIL:-user@example.com}"
DEFAULT_PASSWORD="${DEFAULT_PASSWORD:-Abc@123456}"

# Test data
AUDIO_FILE="${AUDIO_FILE:-documents/input/audio_sample.mp3}"
REFERENCE_TEXT="Most of my peers go crazy about Vietnamese rap music 'cause it's in vogue, you know? I do listen to some Vietnamese rappers once in a while, but I gotta say my affinity with this type of music is not on par with that of my friends."

# Global variables
ACCESS_TOKEN=""
TEST_RESULTS=()
PASSED_COUNT=0
FAILED_COUNT=0
PENDING_COUNT=0

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# Helper Functions
# ============================================

# Function to get authentication token
get_auth_token() {
    echo -e "${YELLOW}Getting authentication token...${NC}"

    login_data="{
        \"username\": \"${DEFAULT_EMAIL}\",
        \"password\": \"${DEFAULT_PASSWORD}\"
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "${login_data}" \
        "${BACKEND_URL}${AUTH_API}/login")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" ]]; then
        # Extract access token from response (check both possible field names)
        ACCESS_TOKEN=$(echo "$response_body" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//g' | sed 's/"//g')
        if [[ -z "$ACCESS_TOKEN" ]]; then
            ACCESS_TOKEN=$(echo "$response_body" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//g' | sed 's/"//g')
        fi
        if [[ ! -z "$ACCESS_TOKEN" ]]; then
            echo -e "${GREEN}✓ Authentication successful${NC}"
            echo "Access token: ${ACCESS_TOKEN:0:20}..."
        else
            echo -e "${RED}✗ Failed to extract access token${NC}"
            echo "Response: $response_body"
            exit 1
        fi
    else
        echo -e "${RED}✗ Authentication failed - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo "----------------------------------------"
}

# Generic test case runner with authentication
test_case() {
    local test_id="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    local description="$6"
    local execution_type="${7:-AUTOMATION}"  # Default: AUTOMATION

    echo -e "${YELLOW}[${test_id}] Testing: ${description}${NC}"

    # Skip manual tests
    if [[ "$execution_type" == "MANUAL" ]]; then
        echo -e "${YELLOW}  → Manual test - skipping automation${NC}"
        log_test_result "$test_id" "PENDING" "$description (Manual)"
        PENDING_COUNT=$((PENDING_COUNT + 1))
        echo ""
        return
    fi

    # Execute test
    response=$(curl -s -w "\n%{http_code}" -X ${method} \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        ${data} \
        "${BACKEND_URL}${endpoint}")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    # Check result
    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "${GREEN}  ✓ PASSED - HTTP ${status_code}${NC}"
        log_test_result "$test_id" "PASSED" "$description"
        PASSED_COUNT=$((PASSED_COUNT + 1))
    else
        echo -e "${RED}  ✗ FAILED - Expected ${expected_status}, got ${status_code}${NC}"
        echo "  Response: ${response_body:0:200}..."
        log_test_result "$test_id" "FAILED" "$description (Expected ${expected_status}, got ${status_code})"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""
}

# Test without authentication
test_case_no_auth() {
    local test_id="$1"
    local method="$2"
    local endpoint="$3"
    local data="$4"
    local expected_status="$5"
    local description="$6"

    echo -e "${YELLOW}[${test_id}] Testing: ${description}${NC}"

    response=$(curl -s -w "\n%{http_code}" -X ${method} \
        ${data} \
        "${BACKEND_URL}${endpoint}")

    status_code=$(echo "$response" | tail -n1)

    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "${GREEN}  ✓ PASSED - HTTP ${status_code}${NC}"
        log_test_result "$test_id" "PASSED" "$description"
        PASSED_COUNT=$((PASSED_COUNT + 1))
    else
        echo -e "${RED}  ✗ FAILED - Expected ${expected_status}, got ${status_code}${NC}"
        log_test_result "$test_id" "FAILED" "$description"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""
}

# Test with custom token
test_case_custom_token() {
    local test_id="$1"
    local token="$2"
    local method="$3"
    local endpoint="$4"
    local data="$5"
    local expected_status="$6"
    local description="$7"

    echo -e "${YELLOW}[${test_id}] Testing: ${description}${NC}"

    response=$(curl -s -w "\n%{http_code}" -X ${method} \
        -H "Authorization: Bearer ${token}" \
        ${data} \
        "${BACKEND_URL}${endpoint}")

    status_code=$(echo "$response" | tail -n1)

    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "${GREEN}  ✓ PASSED - HTTP ${status_code}${NC}"
        log_test_result "$test_id" "PASSED" "$description"
        PASSED_COUNT=$((PASSED_COUNT + 1))
    else
        echo -e "${RED}  ✗ FAILED - Expected ${expected_status}, got ${status_code}${NC}"
        log_test_result "$test_id" "FAILED" "$description"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""
}

# Log test result to array
log_test_result() {
    local test_id="$1"
    local result="$2"
    local description="$3"

    TEST_RESULTS+=("${test_id}|${result}|${description}")
}

# ============================================
# Test Category Functions
# ============================================

# Validate Category (ID-001 ~ ID-010)
test_validate_category() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  VALIDATE CATEGORY (10 TCs)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # ID-001: Method POST (valid)
    test_case "ID-001" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"${REFERENCE_TEXT}\"" \
        "200" "Gọi API với method POST"

    # ID-002: Method GET (invalid)
    test_case "ID-002" "GET" "${PRONUNCIATION_API}/assess" \
        "" "405" "Gọi API với method GET"

    # ID-003: Method PUT (invalid)
    test_case "ID-003" "PUT" "${PRONUNCIATION_API}/assess" \
        "" "405" "Gọi API với method PUT"

    # ID-004: Method DELETE (invalid)
    test_case "ID-004" "DELETE" "${PRONUNCIATION_API}/assess" \
        "" "405" "Gọi API với method DELETE"

    # ID-005: Valid request với đầy đủ params
    test_case "ID-005" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"${REFERENCE_TEXT}\"" \
        "200" "Valid request với đầy đủ params"

    # ID-006: Missing audio file
    test_case "ID-006" "POST" "${PRONUNCIATION_API}/assess" \
        "-F text=\"Hello world\"" \
        "400" "Missing audio file"

    # ID-007: Empty audio file
    echo -e "${YELLOW}[ID-007] Creating empty audio file for test...${NC}"
    touch /tmp/empty.mp3
    test_case "ID-007" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@/tmp/empty.mp3 -F text=\"Hello world\"" \
        "400" "Empty audio file"

    # ID-008: Missing text parameter
    test_case "ID-008" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE}" \
        "400" "Missing text parameter"

    # ID-009: Empty text (whitespace only)
    test_case "ID-009" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"   \"" \
        "400" "Empty text parameter (whitespace only)"

    # ID-010: Invalid audio file format (MANUAL)
    echo -e "${YELLOW}[ID-010] Creating invalid file for test...${NC}"
    echo "This is not an audio file" > /tmp/document.txt
    test_case "ID-010" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@/tmp/document.txt -F text=\"Hello world\"" \
        "400" "Invalid audio file format" \
        "MANUAL"
}

# Logic Category (ID-011 ~ ID-015)
test_logic_category() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  LOGIC CATEGORY (5 TCs)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # ID-011: Text mismatch với audio content (MANUAL)
    test_case "ID-011" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"This is completely different text that does not match the audio content at all\"" \
        "200" "Text mismatch với audio content" \
        "MANUAL"

    # ID-012: Long text assessment (paragraph-length) (MANUAL)
    LONG_TEXT="The quick brown fox jumps over the lazy dog. This is a very long paragraph that contains multiple sentences. It is designed to test the system's ability to handle long text inputs and process them correctly. The paragraph continues with more sentences to make it even longer. We want to ensure that the API can handle lengthy transcripts without any issues or performance degradation."
    test_case "ID-012" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"${LONG_TEXT}\"" \
        "200" "Long text assessment (paragraph-length)" \
        "MANUAL"

    # ID-013: Multiple punctuation and special characters in text
    test_case "ID-013" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"Hello! How are you? I'm fine, thank you.\"" \
        "200" "Multiple punctuation in text"

    # ID-014: Special characters and numbers in text
    test_case "ID-014" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"Call me at 123-456-7890!\"" \
        "200" "Special characters and numbers in text"

    # ID-015: Very short audio (< 1 second) (MANUAL)
    test_case "ID-015" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"Hi\"" \
        "200" "Very short audio test" \
        "MANUAL"
}

# Error Code Category (ID-016 ~ ID-025)
test_error_category() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  ERROR CODE CATEGORY (10 TCs)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # ID-016: Request without authentication token
    test_case_no_auth "ID-016" "POST" "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"${REFERENCE_TEXT}\"" \
        "401" "Request without authentication token"

    # ID-017: Request with expired token
    EXPIRED_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MDAwMDAwMDB9.expired"
    test_case_custom_token "ID-017" "${EXPIRED_TOKEN}" "POST" \
        "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"${REFERENCE_TEXT}\"" \
        "401" "Request with expired token"

    # ID-018: Request with invalid token
    INVALID_TOKEN="invalid.token.format"
    test_case_custom_token "ID-018" "${INVALID_TOKEN}" "POST" \
        "${PRONUNCIATION_API}/assess" \
        "-F file=@${AUDIO_FILE} -F text=\"${REFERENCE_TEXT}\"" \
        "401" "Request with invalid token"

    # ID-019 ~ ID-025: Manual test cases (require environment setup)
    echo -e "${YELLOW}[ID-019] Service not configured - MANUAL TEST${NC}"
    log_test_result "ID-019" "PENDING" "Pronunciation service not configured (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""

    echo -e "${YELLOW}[ID-020] Service timeout - MANUAL TEST${NC}"
    log_test_result "ID-020" "PENDING" "Pronunciation service timeout (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""

    echo -e "${YELLOW}[ID-021] Service returns 404 - MANUAL TEST${NC}"
    log_test_result "ID-021" "PENDING" "Pronunciation service returns 404 (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""

    echo -e "${YELLOW}[ID-022] Service returns 500 - MANUAL TEST${NC}"
    log_test_result "ID-022" "PENDING" "Pronunciation service returns 500 (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""

    echo -e "${YELLOW}[ID-023] Service returns invalid response - MANUAL TEST${NC}"
    log_test_result "ID-023" "PENDING" "Service returns invalid response (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""

    echo -e "${YELLOW}[ID-024] Large file exceeds size limit - MANUAL TEST${NC}"
    log_test_result "ID-024" "PENDING" "Large file exceeds size limit (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""

    echo -e "${YELLOW}[ID-025] Concurrent requests handling - MANUAL TEST${NC}"
    log_test_result "ID-025" "PENDING" "Concurrent requests handling (Manual)"
    PENDING_COUNT=$((PENDING_COUNT + 1))
    echo ""
}

# Format Response Category (ID-026 ~ ID-030)
test_format_category() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  FORMAT RESPONSE CATEGORY (5 TCs)${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # ID-026: Verify success response structure
    echo -e "${YELLOW}[ID-026] Testing: Success response structure${NC}"
    response=$(curl -s -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -F "file=@${AUDIO_FILE}" \
        -F "text=${REFERENCE_TEXT}" \
        "${BACKEND_URL}${PRONUNCIATION_API}/assess")

    # Validate response structure
    if echo "$response" | grep -q "statusCode" && \
       echo "$response" | grep -q "error" && \
       echo "$response" | grep -q "message" && \
       echo "$response" | grep -q "data"; then
        echo -e "${GREEN}  ✓ PASSED - All required fields present${NC}"
        log_test_result "ID-026" "PASSED" "Success response structure verified"
        PASSED_COUNT=$((PASSED_COUNT + 1))
    else
        echo -e "${RED}  ✗ FAILED - Missing required fields${NC}"
        log_test_result "ID-026" "FAILED" "Success response structure validation failed"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""

    # ID-027: Verify error response structure
    echo -e "${YELLOW}[ID-027] Testing: Error response structure${NC}"
    error_response=$(curl -s -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -F "text=Hello world" \
        "${BACKEND_URL}${PRONUNCIATION_API}/assess")

    if echo "$error_response" | grep -q "statusCode" && \
       echo "$error_response" | grep -q "error" && \
       echo "$error_response" | grep -q "message"; then
        echo -e "${GREEN}  ✓ PASSED - Error response structure valid${NC}"
        log_test_result "ID-027" "PASSED" "Error response structure verified"
        PASSED_COUNT=$((PASSED_COUNT + 1))
    else
        echo -e "${RED}  ✗ FAILED - Invalid error response structure${NC}"
        log_test_result "ID-027" "FAILED" "Error response structure validation failed"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""

    # ID-028: Verify overall_score field exists and valid
    echo -e "${YELLOW}[ID-028] Testing: overall_score field validation${NC}"
    if echo "$response" | grep -q "overall_score"; then
        overall_score=$(echo "$response" | grep -o '"overall_score":[0-9.]*' | cut -d':' -f2)
        if [[ ! -z "$overall_score" ]]; then
            echo -e "${GREEN}  ✓ PASSED - overall_score exists: ${overall_score}${NC}"
            log_test_result "ID-028" "PASSED" "overall_score field validated"
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - overall_score value invalid${NC}"
            log_test_result "ID-028" "FAILED" "overall_score value invalid"
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo -e "${RED}  ✗ FAILED - overall_score field missing${NC}"
        log_test_result "ID-028" "FAILED" "overall_score field missing"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""

    # ID-029: Verify fluency_score field exists and valid
    echo -e "${YELLOW}[ID-029] Testing: fluency_score field validation${NC}"
    if echo "$response" | grep -q "fluency_score"; then
        fluency_score=$(echo "$response" | grep -o '"fluency_score":[0-9.]*' | cut -d':' -f2)
        if [[ ! -z "$fluency_score" ]]; then
            echo -e "${GREEN}  ✓ PASSED - fluency_score exists: ${fluency_score}${NC}"
            log_test_result "ID-029" "PASSED" "fluency_score field validated"
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - fluency_score value invalid${NC}"
            log_test_result "ID-029" "FAILED" "fluency_score value invalid"
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo -e "${RED}  ✗ FAILED - fluency_score field missing${NC}"
        log_test_result "ID-029" "FAILED" "fluency_score field missing"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""

    # ID-030: Verify phoneme_scores array structure
    echo -e "${YELLOW}[ID-030] Testing: phoneme_scores array structure${NC}"
    if echo "$response" | grep -q "phoneme_scores"; then
        # Check if phoneme_scores is an array with required fields
        if echo "$response" | grep -q "\"phoneme\":" && \
           echo "$response" | grep -q "\"gop_score\":" && \
           echo "$response" | grep -q "\"quality\":"; then
            echo -e "${GREEN}  ✓ PASSED - phoneme_scores array structure valid${NC}"
            log_test_result "ID-030" "PASSED" "phoneme_scores array structure validated"
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - phoneme_scores missing required fields${NC}"
            log_test_result "ID-030" "FAILED" "phoneme_scores structure invalid"
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        echo -e "${RED}  ✗ FAILED - phoneme_scores field missing${NC}"
        log_test_result "ID-030" "FAILED" "phoneme_scores field missing"
        FAILED_COUNT=$((FAILED_COUNT + 1))
    fi
    echo ""
}

# ============================================
# Report Generation
# ============================================

generate_test_report() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  TEST EXECUTION SUMMARY${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    echo "Total Test Cases: 30"
    echo -e "${GREEN}Passed: ${PASSED_COUNT}${NC}"
    echo -e "${RED}Failed: ${FAILED_COUNT}${NC}"
    echo -e "${YELLOW}Pending/Manual: ${PENDING_COUNT}${NC}"
    echo ""

    # Calculate success rate for automated tests
    if [[ $((PASSED_COUNT + FAILED_COUNT)) -gt 0 ]]; then
        success_rate=$(awk "BEGIN {printf \"%.1f\", ($PASSED_COUNT / ($PASSED_COUNT + $FAILED_COUNT)) * 100}")
        echo "Success Rate (Automated): ${success_rate}%"
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
            "PENDING")
                echo -e "${YELLOW}⊙${NC} ${test_id}: ${description}"
                ;;
        esac
    done

    echo "----------------------------------------"
    echo ""

    # Save report to file
    local report_file="test-report-$(date +%Y%m%d-%H%M%S).txt"
    {
        echo "========================================="
        echo "Pronunciation API Test Execution Report"
        echo "========================================="
        echo ""
        echo "Generated: $(date)"
        echo "Backend URL: ${BACKEND_URL}"
        echo "Audio File: ${AUDIO_FILE}"
        echo ""
        echo "Summary:"
        echo "  Total Test Cases: 30"
        echo "  Passed: ${PASSED_COUNT}"
        echo "  Failed: ${FAILED_COUNT}"
        echo "  Pending/Manual: ${PENDING_COUNT}"
        if [[ $((PASSED_COUNT + FAILED_COUNT)) -gt 0 ]]; then
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
        echo ""
        echo "Test Categories:"
        echo "  Validate:       10 TCs (ID-001 ~ ID-010)"
        echo "  Logic:           5 TCs (ID-011 ~ ID-015)"
        echo "  Error Code:     10 TCs (ID-016 ~ ID-025)"
        echo "  Format Response: 5 TCs (ID-026 ~ ID-030)"
        echo ""
        echo "Execution Types:"
        echo "  AUTOMATION:     20 TCs"
        echo "  MANUAL:         10 TCs"
        echo ""
    } > "$report_file"

    echo "Report saved to: ${report_file}"
    echo ""
}

# ============================================
# Main Execution
# ============================================

main() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  PRONUNCIATION API TEST SUITE${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
    echo "Based on: documents/output/Testcase_API_AssessPronunciation.md"
    echo "Total Test Cases: 30 (20 AUTOMATION + 10 MANUAL)"
    echo ""
    echo "Configuration:"
    echo "  Backend URL: ${BACKEND_URL}"
    echo "  Audio file: ${AUDIO_FILE}"
    echo "  Reference text: ${REFERENCE_TEXT:0:80}..."
    echo ""

    # Check if audio file exists
    if [[ ! -f "${AUDIO_FILE}" ]]; then
        echo -e "${RED}Error: Audio file not found: ${AUDIO_FILE}${NC}"
        echo "Please ensure the audio file exists before running tests."
        echo ""
        echo "Expected location:"
        echo "  ${AUDIO_FILE}"
        echo ""
        exit 1
    fi
    echo -e "${GREEN}✓ Audio file found${NC}"
    echo ""

    # Get authentication token
    get_auth_token

    # Run test categories
    test_validate_category
    test_logic_category
    test_error_category
    test_format_category

    # Generate report
    generate_test_report

    # Cleanup temporary files
    echo "Cleaning up temporary files..."
    rm -f /tmp/empty.mp3 /tmp/document.txt
    echo -e "${GREEN}✓ Cleanup completed${NC}"
    echo ""

    # Final summary
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  TEST SUITE COMPLETED${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""

    if [[ $FAILED_COUNT -eq 0 ]]; then
        echo -e "${GREEN}All automated tests passed!${NC}"
    else
        echo -e "${RED}Some tests failed. Please review the report.${NC}"
    fi
    echo ""
}

# Run main
main
