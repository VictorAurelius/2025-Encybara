#!/bin/bash

# Test SpeakingSampleAnswer API
# This script tests the SpeakingSampleAnswer functionality to verify audioLink persistence

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
BACKEND_URL="http://localhost:8080"
API_BASE="/api/v1/speaking-sample-answers"
AUTH_API="/api/v1/auth"

# Default user credentials (from AdminDataInitializer)
DEFAULT_EMAIL="user@example.com"
DEFAULT_PASSWORD="Abc@123456"

# Global variables
ACCESS_TOKEN=""

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  SPEAKING SAMPLE ANSWER API TEST${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "${YELLOW}Testing SpeakingSampleAnswer AudioLink functionality${NC}"
echo -e "Backend URL: ${BACKEND_URL}"
echo -e "API Endpoint: ${API_BASE}"
echo ""

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

# Function to make API calls
test_endpoint() {
    local method="$1"
    local endpoint="$2" 
    local data="$3"
    local description="$4"
    local expected_status="$5"
    
    echo -e "${YELLOW}Testing: ${description}${NC}"
    echo -e "URL: ${BACKEND_URL}${endpoint}"
    
    # Prepare headers
    local headers=()
    headers+=("-H" "Content-Type: application/json")
    if [[ ! -z "$ACCESS_TOKEN" ]]; then
        headers+=("-H" "Authorization: Bearer ${ACCESS_TOKEN}")
    fi
    
    if [[ "$method" == "GET" ]]; then
        response=$(curl -s -w "\n%{http_code}" "${headers[@]}" "${BACKEND_URL}${endpoint}")
    else
        response=$(curl -s -w "\n%{http_code}" -X ${method} \
            "${headers[@]}" \
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
            echo "Response: $response_body"
        fi
    else
        echo -e "${RED}✗ ${description} - HTTP ${status_code}${NC}"
        echo "Expected: HTTP ${expected_status}"
        echo "Response: $response_body"
    fi
    
    # Return response body for further processing
    echo "$response_body"
    echo "----------------------------------------"
}

# Function to extract question ID from speaking questions
get_speaking_question_id() {
    echo -e "${YELLOW}Finding a speaking question...${NC}" >&2
    
    response=$(curl -s -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}/api/v1/questions?page=0&size=50")
    
    # Extract question ID where quesType is SPEAKING
    question_id=$(echo "$response" | grep -B5 -A5 '"quesType":"SPEAKING"' | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)
    
    if [[ ! -z "$question_id" ]]; then
        echo -e "${GREEN}✓ Found speaking question ID: ${question_id}${NC}" >&2
        echo "$question_id"
    else
        echo -e "${RED}✗ No speaking questions found${NC}" >&2
        echo ""
    fi
}

# Get authentication token first
get_auth_token

# Test 1: Backend Health Check
test_endpoint "GET" "/actuator/health" "" "Backend Health Check" "200" > /dev/null

# Test 2: Find a speaking question
QUESTION_ID=$(get_speaking_question_id)

if [[ -z "$QUESTION_ID" ]]; then
    echo -e "${RED}Cannot proceed without a speaking question ID${NC}"
    exit 1
fi

echo "----------------------------------------"

# Test 3: Get all sample answers for the speaking question
echo -e "${YELLOW}Testing: Get all sample answers for question ${QUESTION_ID}${NC}"
response=$(test_endpoint "GET" "${API_BASE}/question/${QUESTION_ID}" "" "Get sample answers by question ID" "200")

# Extract sample answer data to check audioLink values
echo -e "${YELLOW}Checking audioLink values in sample answers...${NC}"
if [[ "$response" == *"audioLink"* ]]; then
    echo -e "${GREEN}✓ Response contains audioLink field${NC}"
    
    # Extract and display audioLink values
    audio_links=$(echo "$response" | grep -o '"audioLink":"[^"]*"' | sed 's/"audioLink":"//g' | sed 's/"//g')
    null_audio_links=$(echo "$response" | grep -o '"audioLink":null' | wc -l)
    
    echo "Audio links found:"
    if [[ ! -z "$audio_links" ]]; then
        echo "$audio_links" | while read -r link; do
            if [[ ! -z "$link" ]]; then
                echo -e "${GREEN}  ✓ $link${NC}"
            fi
        done
    fi
    
    if [[ "$null_audio_links" -gt 0 ]]; then
        echo -e "${YELLOW}  ! Found ${null_audio_links} null audioLink(s)${NC}"
    fi
    
    # Check if we have the expected audioLink with ssa-1.mp3
    if [[ "$response" == *"ssa-1.mp3"* ]]; then
        echo -e "${GREEN}✓ Found expected ssa-1.mp3 audio file link${NC}"
    else
        echo -e "${RED}✗ Expected ssa-1.mp3 audio file link not found${NC}"
        echo -e "${YELLOW}This indicates the audioLink import may have failed${NC}"
    fi
else
    echo -e "${RED}✗ No audioLink field found in response${NC}"
fi

# Test 4: Get sample answers by difficulty level (specifically level 2 which should have audio)
echo "----------------------------------------"
test_endpoint "GET" "${API_BASE}/question/${QUESTION_ID}/difficulty/2" "" "Get sample answers for difficulty level 2" "200"

# Test 5: Get sample answers with audio links only
echo "----------------------------------------"
test_endpoint "GET" "${API_BASE}/question/${QUESTION_ID}/with-audio" "" "Get sample answers with audio links" "200"

# Test 6: Check if question has sample answers
echo "----------------------------------------"
test_endpoint "GET" "${API_BASE}/question/${QUESTION_ID}/exists" "" "Check if question has sample answers" "200"

# Test 7: Get count of sample answers
echo "----------------------------------------"
test_endpoint "GET" "${API_BASE}/question/${QUESTION_ID}/count" "" "Get count of sample answers" "200"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  DATABASE DIRECT CHECK${NC}" 
echo -e "${BLUE}============================================${NC}"

echo -e "${YELLOW}To manually check database audioLink values:${NC}"
echo "1. Connect to your database"
echo "2. Run: SELECT id, difficulty_level, audio_link FROM speaking_sample_answers WHERE question_id = ${QUESTION_ID};"
echo "3. Verify that difficulty_level = 2 has a non-null audio_link value"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  TROUBLESHOOTING${NC}"
echo -e "${BLUE}============================================${NC}"

echo -e "${YELLOW}If audioLink is still NULL:${NC}"
echo "1. Check CourseDataSeeder debug logs during application startup"
echo "2. Verify the audio file exists: backend-service/src/main/resources/data/efit/file/unit1/paper1/ssa-1.mp3"
echo "3. Check file permissions and accessibility"
echo "4. Verify database transaction commit"
echo "5. Check if cascade save is working properly"

echo ""
echo -e "${YELLOW}Expected behavior:${NC}"
echo "- Only difficulty level 2 should have audioLink (from JSON config)"
echo "- Other difficulty levels should have audioLink = null"
echo "- The audioLink should point to: http://localhost:8080/uploadfile/speaking-sample-answers/efit/unit1/paper1/TIMESTAMP-ssa-1.mp3"

echo ""
echo -e "${GREEN}Test completed. Check the results above.${NC}"