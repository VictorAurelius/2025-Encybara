#!/bin/bash

# Test Pronunciation Assessment Workflow through Backend-App
# This script tests the complete pronunciation assessment flow via backend-app integration

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
BACKEND_URL="http://localhost:8080"
PRONUNCIATION_API="/api/v1/pronunciation"

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  PRONUNCIATION ASSESSMENT WORKFLOW TEST${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "${YELLOW}Testing Pronunciation Assessment through Backend-App${NC}"
echo -e "Backend URL: ${BACKEND_URL}"
echo -e "API Endpoint: ${PRONUNCIATION_API}"
echo ""

# Function to check if file exists
check_file() {
    local file_path="$1"
    if [[ ! -f "$file_path" ]]; then
        echo -e "${RED}Error: Test file not found: $file_path${NC}"
        echo "Creating a dummy test file..."
        
        # Create a small dummy audio file (empty file for testing)
        echo "This is a dummy audio file for testing" > "$file_path"
        echo -e "${YELLOW}Created dummy file: $file_path${NC}"
    fi
}

# Function to make API calls with file upload
test_file_upload() {
    local endpoint="$1"
    local file_path="$2"
    local description="$3"
    local expected_status="$4"
    
    echo -e "${YELLOW}Testing: ${description}${NC}"
    echo -e "URL: ${BACKEND_URL}${endpoint}"
    echo -e "File: ${file_path}"
    
    # Check if file exists
    check_file "$file_path"
    
    # Make the request
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -F "file=@${file_path}" \
        "${BACKEND_URL}${endpoint}")
    
    # Split response and status code
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    # Check status code
    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "${GREEN}✓ ${description} - HTTP ${status_code}${NC}"
        if [[ ! -z "$response_body" && "$response_body" != "null" ]]; then
            echo "Response: $response_body" | head -c 300
            echo "..."
        fi
    else
        echo -e "${RED}✗ ${description} - HTTP ${status_code}${NC}"
        echo "Expected: HTTP ${expected_status}"
        echo "Response: $response_body"
    fi
    
    echo "----------------------------------------"
}

# Function to make regular API calls
test_endpoint() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local description="$4"
    local expected_status="$5"
    
    echo -e "${YELLOW}Testing: ${description}${NC}"
    echo -e "URL: ${BACKEND_URL}${endpoint}"
    
    if [[ "$method" == "GET" ]]; then
        response=$(curl -s -w "\n%{http_code}" "${BACKEND_URL}${endpoint}")
    else
        response=$(curl -s -w "\n%{http_code}" -X ${method} \
            -H "Content-Type: application/json" \
            -d "${data}" \
            "${BACKEND_URL}${endpoint}")
    fi
    
    # Split response and status code
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    # Check status code
    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "${GREEN}✓ ${description} - HTTP ${status_code}${NC}"
        if [[ ! -z "$response_body" && "$response_body" != "null" ]]; then
            echo "Response: $response_body" | head -c 200
            echo "..."
        fi
    else
        echo -e "${RED}✗ ${description} - HTTP ${status_code}${NC}"
        echo "Expected: HTTP ${expected_status}"
        echo "Response: $response_body"
    fi
    
    echo "----------------------------------------"
}

# Create test audio files directory
mkdir -p test-audio

# Test 1: Backend Health Check
test_endpoint "GET" "/actuator/health" "" "Backend Health Check" "200"

# Test 2: Create test audio files if they don't exist
echo -e "${YELLOW}Preparing test audio files...${NC}"

# Create different types of test files
echo "Hello world, this is a pronunciation test" > test-audio/test-speech.txt
echo "The quick brown fox jumps over the lazy dog" > test-audio/test-speech-2.txt
echo "Artificial intelligence is transforming technology" > test-audio/test-speech-3.txt

# Create dummy audio files (in real scenario, these would be actual audio files)
cp test-audio/test-speech.txt test-audio/test-speech.wav
cp test-audio/test-speech-2.txt test-audio/test-speech-2.mp3
cp test-audio/test-speech-3.txt test-audio/test-speech-3.wav

echo -e "${GREEN}✓ Test files prepared${NC}"
echo "----------------------------------------"

# Test 3: Pronunciation Assessment - Valid WAV File
test_file_upload "${PRONUNCIATION_API}/assess" "test-audio/test-speech.wav" "Pronunciation Assessment - WAV File" "200"

# Test 4: Pronunciation Assessment - Valid MP3 File  
test_file_upload "${PRONUNCIATION_API}/assess" "test-audio/test-speech-2.mp3" "Pronunciation Assessment - MP3 File" "200"

# Test 5: Pronunciation Assessment - Another WAV File
test_file_upload "${PRONUNCIATION_API}/assess" "test-audio/test-speech-3.wav" "Pronunciation Assessment - Long Text WAV" "200"

# Test 6: Pronunciation Assessment - Missing File
echo -e "${YELLOW}Testing: Missing File Upload${NC}"
response=$(curl -s -w "\n%{http_code}" -X POST "${BACKEND_URL}${PRONUNCIATION_API}/assess" 2>/dev/null)
status_code=$(echo "$response" | tail -n1)
if [[ "$status_code" == "400" || "$status_code" == "415" ]]; then
    echo -e "${GREEN}✓ Missing File Upload - HTTP ${status_code} (Expected error)${NC}"
else
    echo -e "${RED}✗ Missing File Upload - HTTP ${status_code}${NC}"
    echo "Expected: HTTP 400 or 415"
fi
echo "----------------------------------------"

# Test 7: Invalid HTTP Method
test_endpoint "GET" "${PRONUNCIATION_API}/assess" "" "Pronunciation Assessment - GET Method (should fail)" "405"

# Test 8: Wrong Content Type
echo -e "${YELLOW}Testing: Wrong Content Type${NC}"
response=$(curl -s -w "\n%{http_code}" -X POST \
    -H "Content-Type: application/json" \
    -d '{"file": "not a file"}' \
    "${BACKEND_URL}${PRONUNCIATION_API}/assess")
status_code=$(echo "$response" | tail -n1)
if [[ "$status_code" == "400" || "$status_code" == "415" ]]; then
    echo -e "${GREEN}✓ Wrong Content Type - HTTP ${status_code} (Expected error)${NC}"
else
    echo -e "${RED}✗ Wrong Content Type - HTTP ${status_code}${NC}"
fi
echo "----------------------------------------"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  PERFORMANCE TESTS${NC}"
echo -e "${BLUE}============================================${NC}"

# Performance Test: File Upload Speed
echo -e "${YELLOW}Testing: File Upload Performance${NC}"

start_time=$(date +%s.%N)
test_file_upload "${PRONUNCIATION_API}/assess" "test-audio/test-speech.wav" "Performance Test" "200" > /dev/null
end_time=$(date +%s.%N)

duration=$(echo "$end_time - $start_time" | bc -l 2>/dev/null || echo "N/A")
if [[ "$duration" != "N/A" ]]; then
    echo -e "${GREEN}✓ Upload completed in ${duration} seconds${NC}"
else
    echo -e "${YELLOW}! Upload completed (duration calculation not available)${NC}"
fi

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  INTEGRATION TEST SUMMARY${NC}"
echo -e "${BLUE}============================================${NC}"

# Final Integration Test
echo -e "${YELLOW}Running end-to-end integration test...${NC}"

# 1. Check if backend is healthy
backend_health=$(curl -s "${BACKEND_URL}/actuator/health" | grep -o '"status":"UP"' || echo "")

# 2. Test pronunciation assessment
pronunciation_test=""
if [[ -f "test-audio/test-speech.wav" ]]; then
    pronunciation_response=$(curl -s -X POST -F "file=@test-audio/test-speech.wav" "${BACKEND_URL}${PRONUNCIATION_API}/assess")
    if [[ "$pronunciation_response" == *"statusCode"* ]]; then
        pronunciation_test="success"
    fi
fi

echo ""
if [[ ! -z "$backend_health" && ! -z "$pronunciation_test" ]]; then
    echo -e "${GREEN}✓ INTEGRATION TEST PASSED${NC}"
    echo -e "${GREEN}  - Backend is healthy${NC}"
    echo -e "${GREEN}  - Pronunciation assessment works${NC}"
else
    echo -e "${RED}✗ INTEGRATION TEST FAILED${NC}"
    [[ -z "$backend_health" ]] && echo -e "${RED}  - Backend health check failed${NC}"
    [[ -z "$pronunciation_test" ]] && echo -e "${RED}  - Pronunciation assessment failed${NC}"
fi

echo ""
echo -e "${YELLOW}Test completed. Check above for any failures.${NC}"
echo -e "${YELLOW}For debugging:${NC}"
echo -e "Backend logs: docker-compose logs -f backend"
echo -e "Backend health: curl ${BACKEND_URL}/actuator/health"
echo -e "Manual test: curl -X POST -F 'file=@test-audio/test-speech.wav' ${BACKEND_URL}${PRONUNCIATION_API}/assess"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  PRONUNCIATION SERVICE CONFIGURATION${NC}"
echo -e "${BLUE}============================================${NC}"

echo -e "${YELLOW}Current pronunciation service configuration:${NC}"
echo "Backend reads from environment variable: PRONUNCIATION_SERVICE_URL"
echo "Default fallback: http://localhost:5000"
echo ""
echo -e "${YELLOW}To test with different pronunciation service:${NC}"
echo "1. Set PRONUNCIATION_SERVICE_URL environment variable"
echo "2. Rebuild backend-app: cd ../backend-app && ./build.sh"
echo "3. Or use: docker-compose down && PRONUNCIATION_SERVICE_URL=your-url docker-compose up -d"

# Cleanup test files
echo ""
echo -e "${YELLOW}Cleaning up test files...${NC}"
rm -rf test-audio/
echo -e "${GREEN}✓ Cleanup completed${NC}"