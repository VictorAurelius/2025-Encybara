#!/bin/bash

# Test Content Scoring Workflow through Backend-App
# This script tests the complete content scoring flow via backend-app integration

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
BACKEND_URL="http://localhost:8080"
CONTENT_SCORING_API="/api/v1/content-scoring"

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  CONTENT SCORING WORKFLOW TEST${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "${YELLOW}Testing Content Scoring through Backend-App${NC}"
echo -e "Backend URL: ${BACKEND_URL}"
echo -e "API Endpoint: ${CONTENT_SCORING_API}"
echo ""

# Function to make API calls with proper error handling
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

# Test 1: Backend Health Check
test_endpoint "GET" "/actuator/health" "" "Backend Health Check" "200"

# Test 2: Content Scoring Service Health Check
test_endpoint "GET" "${CONTENT_SCORING_API}/health" "" "Content Scoring Service Health Check" "200"

# Test 3: Content Scoring Service Info
test_endpoint "GET" "${CONTENT_SCORING_API}/info" "" "Content Scoring Service Info" "200"

# Test 4: Content Scoring - Valid Request
scoring_data='{
  "question": "What is artificial intelligence?",
  "userAnswer": "AI is a technology that enables machines to learn and make decisions",
  "prompt": "Evaluate this answer about artificial intelligence"
}'

test_endpoint "POST" "${CONTENT_SCORING_API}/evaluate" "$scoring_data" "Content Scoring - Valid Request" "200"

# Test 5: Content Scoring - Empty Answer
scoring_data_empty='{
  "question": "What is machine learning?",
  "userAnswer": "",
  "prompt": "Evaluate this answer"
}'

test_endpoint "POST" "${CONTENT_SCORING_API}/evaluate" "$scoring_data_empty" "Content Scoring - Empty Answer (should fail)" "400"

# Test 6: Content Scoring - Missing Question
scoring_data_no_question='{
  "userAnswer": "This is a good answer",
  "prompt": "Evaluate this answer"
}'

test_endpoint "POST" "${CONTENT_SCORING_API}/evaluate" "$scoring_data_no_question" "Content Scoring - Missing Question (should fail)" "400"

# Test 7: Content Scoring - Long Answer
scoring_data_long='{
  "question": "Explain the concept of neural networks in detail.",
  "userAnswer": "Neural networks are computational models inspired by biological neural networks. They consist of interconnected nodes called neurons arranged in layers. Each connection has a weight that adjusts during training. The network learns by adjusting these weights based on the error between predicted and actual outputs. Deep neural networks have multiple hidden layers, enabling them to learn complex patterns and representations. They are used in various applications including image recognition, natural language processing, and predictive modeling.",
  "prompt": "Evaluate this comprehensive answer about neural networks"
}'

test_endpoint "POST" "${CONTENT_SCORING_API}/evaluate" "$scoring_data_long" "Content Scoring - Long Answer" "200"

# Test 8: Content Scoring - Vietnamese Question
scoring_data_vietnamese='{
  "question": "Trí tuệ nhân tạo là gì?",
  "userAnswer": "Trí tuệ nhân tạo là công nghệ cho phép máy tính học hỏi và đưa ra quyết định",
  "prompt": "Đánh giá câu trả lời về trí tuệ nhân tạo"
}'

test_endpoint "POST" "${CONTENT_SCORING_API}/evaluate" "$scoring_data_vietnamese" "Content Scoring - Vietnamese Question" "200"

# Test 9: Suggestions Endpoint (should be disabled)
suggestion_data='{
  "question": "What is machine learning?",
  "context": "Basic AI concepts"
}'

test_endpoint "POST" "${CONTENT_SCORING_API}/suggest" "$suggestion_data" "Suggestions Endpoint (should be disabled)" "503"

# Test 10: Invalid HTTP Method
test_endpoint "GET" "${CONTENT_SCORING_API}/evaluate" "" "Content Scoring - GET Method (should fail)" "405"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  PERFORMANCE TESTS${NC}"
echo -e "${BLUE}============================================${NC}"

# Performance Test: Multiple Concurrent Requests
echo -e "${YELLOW}Testing: Multiple Concurrent Scoring Requests${NC}"
echo "Sending 5 concurrent requests..."

start_time=$(date +%s.%N)
for i in {1..5}; do
    (
        curl -s -X POST \
            -H "Content-Type: application/json" \
            -d "${scoring_data}" \
            "${BACKEND_URL}${CONTENT_SCORING_API}/evaluate" > /dev/null
    ) &
done
wait
end_time=$(date +%s.%N)

duration=$(echo "$end_time - $start_time" | bc -l)
echo -e "${GREEN}✓ Concurrent requests completed in ${duration} seconds${NC}"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  INTEGRATION TEST SUMMARY${NC}"
echo -e "${BLUE}============================================${NC}"

# Final Integration Test
echo -e "${YELLOW}Running end-to-end integration test...${NC}"

# 1. Check if backend is healthy
backend_health=$(curl -s "${BACKEND_URL}/actuator/health" | grep -o '"status":"UP"' || echo "")

# 2. Check if content scoring service is accessible
scoring_health=$(curl -s "${BACKEND_URL}${CONTENT_SCORING_API}/health" | grep -o '"statusCode":200' || echo "")

# 3. Test actual scoring
scoring_test=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "${scoring_data}" \
    "${BACKEND_URL}${CONTENT_SCORING_API}/evaluate" | grep -o '"statusCode":200' || echo "")

echo ""
if [[ ! -z "$backend_health" && ! -z "$scoring_health" && ! -z "$scoring_test" ]]; then
    echo -e "${GREEN}✓ INTEGRATION TEST PASSED${NC}"
    echo -e "${GREEN}  - Backend is healthy${NC}"
    echo -e "${GREEN}  - Content scoring service is accessible${NC}"
    echo -e "${GREEN}  - Scoring functionality works${NC}"
else
    echo -e "${RED}✗ INTEGRATION TEST FAILED${NC}"
    [[ -z "$backend_health" ]] && echo -e "${RED}  - Backend health check failed${NC}"
    [[ -z "$scoring_health" ]] && echo -e "${RED}  - Content scoring service not accessible${NC}"
    [[ -z "$scoring_test" ]] && echo -e "${RED}  - Scoring functionality failed${NC}"
fi

echo ""
echo -e "${YELLOW}Test completed. Check above for any failures.${NC}"
echo -e "${YELLOW}For debugging:${NC}"
echo -e "Backend logs: docker-compose logs -f backend"
echo -e "Backend health: curl ${BACKEND_URL}/actuator/health"
echo -e "Scoring health: curl ${BACKEND_URL}${CONTENT_SCORING_API}/health"
echo -e "Manual test: curl -X POST -H 'Content-Type: application/json' -d '${scoring_data}' ${BACKEND_URL}${CONTENT_SCORING_API}/evaluate"