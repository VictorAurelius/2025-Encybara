#!/bin/bash

# Diagnostic script to check lesson and questions
# This helps identify why getting questions fails

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

BACKEND_URL="http://localhost:8080"
DEFAULT_EMAIL="user@example.com"
DEFAULT_PASSWORD="Abc@123456"

echo -e "${CYAN}=== Lesson Diagnostic Tool ===${NC}"
echo ""

# Get token
echo -e "${YELLOW}Getting authentication token...${NC}"
login_response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${DEFAULT_EMAIL}\",\"password\":\"${DEFAULT_PASSWORD}\"}" \
    "${BACKEND_URL}/api/v1/auth/login")

ACCESS_TOKEN=$(echo "$login_response" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//g' | sed 's/"//g')
if [[ -z "$ACCESS_TOKEN" ]]; then
    ACCESS_TOKEN=$(echo "$login_response" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//g' | sed 's/"//g')
fi

if [[ -z "$ACCESS_TOKEN" ]]; then
    echo -e "${RED}Failed to get token${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Got token${NC}"
echo ""

# Test lesson 5
echo -e "${CYAN}Testing Lesson ID 5 (WRITING lesson)${NC}"
echo "GET ${BACKEND_URL}/api/v1/lessons/5"
lesson_response=$(curl -s -w "\n%{http_code}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${BACKEND_URL}/api/v1/lessons/5")

lesson_status=$(echo "$lesson_response" | tail -n1)
lesson_body=$(echo "$lesson_response" | head -n -1)

echo "Status: $lesson_status"
if [[ "$lesson_status" == "200" ]]; then
    echo -e "${GREEN}✓ Lesson exists${NC}"
    echo "$lesson_body" | head -c 200
    echo "..."
else
    echo -e "${RED}✗ Lesson fetch failed${NC}"
    echo "$lesson_body"
fi
echo ""

# Test getting questions for lesson 5
echo -e "${CYAN}Testing Questions for Lesson ID 5${NC}"
echo "GET ${BACKEND_URL}/api/v1/questions/lesson/5"
questions_response=$(curl -s -w "\n%{http_code}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${BACKEND_URL}/api/v1/questions/lesson/5")

questions_status=$(echo "$questions_response" | tail -n1)
questions_body=$(echo "$questions_response" | head -n -1)

echo "Status: $questions_status"
if [[ "$questions_status" == "200" ]]; then
    echo -e "${GREEN}✓ Questions fetched successfully${NC}"
    question_count=$(echo "$questions_body" | grep -o '"id":' | wc -l)
    echo "Found $question_count questions"
    echo "$questions_body" | head -c 300
    echo "..."
else
    echo -e "${RED}✗ Questions fetch failed${NC}"
    echo "Full error response:"
    echo "$questions_body"
fi
echo ""

# List all lessons in course 1
echo -e "${CYAN}All lessons in Course 1 (Placement Test)${NC}"
course_response=$(curl -s \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    "${BACKEND_URL}/api/v1/courses/1")

lesson_ids=$(echo "$course_response" | grep -o '"lessonIds":\[[0-9,]*\]' | grep -o '[0-9,]*' | tr ',' '\n')

echo "Lesson IDs in course: $lesson_ids"
echo ""

for lid in $lesson_ids; do
    echo -e "${YELLOW}Checking Lesson $lid...${NC}"
    l_resp=$(curl -s -H "Authorization: Bearer ${ACCESS_TOKEN}" "${BACKEND_URL}/api/v1/lessons/$lid")
    l_name=$(echo "$l_resp" | grep -o '"name":"[^"]*"' | head -1 | sed 's/"name":"//g' | sed 's/"//g')
    l_skill=$(echo "$l_resp" | grep -o '"skillType":"[^"]*"' | head -1 | sed 's/"skillType":"//g' | sed 's/"//g')
    
    # Try to get questions
    q_resp=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer ${ACCESS_TOKEN}" "${BACKEND_URL}/api/v1/questions/lesson/$lid")
    q_status=$(echo "$q_resp" | tail -n1)
    
    if [[ "$q_status" == "200" ]]; then
        q_count=$(echo "$q_resp" | head -n -1 | grep -o '"id":' | wc -l)
        echo "  ID: $lid | Name: $l_name | Skill: $l_skill | Questions: $q_count ✓"
    else
        echo "  ID: $lid | Name: $l_name | Skill: $l_skill | Questions: ERROR ✗"
    fi
done

echo ""
echo -e "${CYAN}=== Diagnostic Complete ===${NC}"
echo ""
echo -e "${YELLOW}Recommendations:${NC}"
echo "1. Check backend logs for detailed error stack trace"
echo "2. Verify lesson-question relationships in database"
echo "3. If lesson 5 has issues, use a different WRITING lesson from the list above"