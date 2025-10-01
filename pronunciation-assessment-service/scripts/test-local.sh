#!/bin/bash

# Pronunciation Assessment Service - Local Test Script
# This script tests the locally running service

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_URL="http://localhost:5000"
TEST_RESULTS=()

echo -e "${BLUE}=== Pronunciation Assessment Service - Local Test ===${NC}"
echo -e "${YELLOW}Testing service at:${NC} $SERVICE_URL"
echo ""

# Function to add test result
add_test_result() {
    local test_name="$1"
    local status="$2"
    TEST_RESULTS+=("$test_name:$status")
}

# Function to print test result
print_test_result() {
    local test_name="$1"
    local status="$2"
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓ $test_name${NC}"
    else
        echo -e "${RED}✗ $test_name${NC}"
    fi
}

# Test 1: Service availability
echo -e "${YELLOW}Test 1: Service Availability${NC}"
if curl -s -f "$SERVICE_URL" > /dev/null 2>&1; then
    add_test_result "Service Availability" "PASS"
    print_test_result "Service is running" "PASS"
else
    add_test_result "Service Availability" "FAIL"
    print_test_result "Service is NOT running" "FAIL"
    echo -e "${RED}Error: Service is not accessible${NC}"
    echo -e "${YELLOW}Make sure the service is running with: ${BLUE}./scripts/run-local.sh${NC}"
    exit 1
fi

# Test 2: Health check
echo -e "${YELLOW}Test 2: Health Check${NC}"
HEALTH_RESPONSE=$(curl -s "$SERVICE_URL/health" 2>/dev/null || echo "ERROR")
if echo "$HEALTH_RESPONSE" | grep -q "healthy"; then
    add_test_result "Health Check" "PASS"
    print_test_result "Health check passed" "PASS"
    echo -e "${BLUE}Health Response:${NC} $HEALTH_RESPONSE"
else
    add_test_result "Health Check" "FAIL"
    print_test_result "Health check failed" "FAIL"
    echo -e "${RED}Health Response:${NC} $HEALTH_RESPONSE"
fi

# Test 3: Service info
echo -e "${YELLOW}Test 3: Service Info${NC}"
INFO_RESPONSE=$(curl -s "$SERVICE_URL/api/info" 2>/dev/null || echo "ERROR")
if echo "$INFO_RESPONSE" | grep -q "Pronunciation Assessment Microservice"; then
    add_test_result "Service Info" "PASS"
    print_test_result "Service info endpoint working" "PASS"
else
    add_test_result "Service Info" "FAIL"
    print_test_result "Service info endpoint failed" "FAIL"
fi

# Test 4: Root endpoint
echo -e "${YELLOW}Test 4: Root Endpoint${NC}"
ROOT_RESPONSE=$(curl -s "$SERVICE_URL/" 2>/dev/null || echo "ERROR")
if echo "$ROOT_RESPONSE" | grep -q "Pronunciation Assessment Microservice"; then
    add_test_result "Root Endpoint" "PASS"
    print_test_result "Root endpoint working" "PASS"
else
    add_test_result "Root Endpoint" "FAIL"
    print_test_result "Root endpoint failed" "FAIL"
fi

# Test 5: Assessment endpoint (without file - should return error)
echo -e "${YELLOW}Test 5: Assessment Endpoint Validation${NC}"
ASSESSMENT_RESPONSE=$(curl -s -X POST "$SERVICE_URL/api/pronunciation-assessment" 2>/dev/null || echo "ERROR")
if echo "$ASSESSMENT_RESPONSE" | grep -q "Missing.*audio.*file"; then
    add_test_result "Assessment Validation" "PASS"
    print_test_result "Assessment endpoint validation working" "PASS"
else
    add_test_result "Assessment Validation" "FAIL"
    print_test_result "Assessment endpoint validation failed" "FAIL"
    echo -e "${RED}Response:${NC} $ASSESSMENT_RESPONSE"
fi

# Summary
echo ""
echo -e "${BLUE}=== Test Summary ===${NC}"
TOTAL_TESTS=0
PASSED_TESTS=0

for result in "${TEST_RESULTS[@]}"; do
    IFS=':' read -r test_name status <<< "$result"
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ "$status" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
    fi
    print_test_result "$test_name" "$status"
done

echo ""
echo -e "${YELLOW}Total Tests:${NC} $TOTAL_TESTS"
echo -e "${GREEN}Passed:${NC} $PASSED_TESTS"
echo -e "${RED}Failed:${NC} $((TOTAL_TESTS - PASSED_TESTS))"

if [ $PASSED_TESTS -eq $TOTAL_TESTS ]; then
    echo ""
    echo -e "${GREEN}🎉 All tests passed! Service is working correctly.${NC}"
    echo ""
    echo -e "${YELLOW}Available Endpoints:${NC}"
    echo -e "  Root: ${BLUE}$SERVICE_URL/${NC}"
    echo -e "  Health: ${BLUE}$SERVICE_URL/health${NC}"
    echo -e "  Info: ${BLUE}$SERVICE_URL/api/info${NC}"
    echo -e "  Assessment: ${BLUE}$SERVICE_URL/api/pronunciation-assessment${NC}"
    echo ""
    echo -e "${YELLOW}To test with audio file:${NC}"
    echo -e "${BLUE}curl -X POST $SERVICE_URL/api/pronunciation-assessment \\${NC}"
    echo -e "${BLUE}  -F 'audio=@your_audio_file.wav' \\${NC}"
    echo -e "${BLUE}  -F 'transcript=your text here'${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some tests failed. Please check the service.${NC}"
    exit 1
fi