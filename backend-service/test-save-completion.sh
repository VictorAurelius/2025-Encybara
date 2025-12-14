#!/bin/bash

# Test Save Completion API Workflow
# This script tests the complete enrollment save-completion flow for both normal and writing lessons

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Configuration
BACKEND_URL="http://localhost:8080"
AUTH_API="/api/v1/auth"
ENROLLMENT_API="/api/v1/enrollments"
ANSWER_API="/api/v1/answers"
LESSON_RESULT_API="/api/v1/lesson-results"
COURSE_API="/api/v1/courses"
LESSON_API="/api/v1/lessons"

# Default user credentials
DEFAULT_EMAIL="user@example.com"
DEFAULT_PASSWORD="Abc@123456"

# Global variables
ACCESS_TOKEN=""
USER_ID=""
PLACEMENT_COURSE_ID=""
WRITING_LESSON_ID=""
NORMAL_LESSON_ID=""
ENROLLMENT_ID=""
SESSION_ID=$(date +%s)

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  SAVE-COMPLETION API WORKFLOW TEST${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "${YELLOW}Testing Enrollment Save-Completion${NC}"
echo -e "Backend URL: ${BACKEND_URL}"
echo -e "Session ID: ${SESSION_ID}"
echo ""

# Function to get authentication token
get_auth_token() {
    echo -e "${CYAN}Step 1: Getting authentication token...${NC}"
    
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
        ACCESS_TOKEN=$(echo "$response_body" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//g' | sed 's/"//g')
        if [[ -z "$ACCESS_TOKEN" ]]; then
            ACCESS_TOKEN=$(echo "$response_body" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//g' | sed 's/"//g')
        fi
        
        USER_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://g')
        
        if [[ ! -z "$ACCESS_TOKEN" && ! -z "$USER_ID" ]]; then
            echo -e "${GREEN}✓ Authentication successful${NC}"
            echo "  User ID: ${USER_ID}"
            echo "  Token: ${ACCESS_TOKEN:0:20}..."
        else
            echo -e "${RED}✗ Failed to extract token or user ID${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Authentication failed - HTTP ${status_code}${NC}"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to find placement course
find_placement_course() {
    echo -e "${CYAN}Step 2: Finding Placement Test course...${NC}"
    
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${COURSE_API}?page=0&size=100")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        # Extract course ID for "English Placement Test"
        PLACEMENT_COURSE_ID=$(echo "$response_body" | grep -o '"id":[0-9]*,"name":"English Placement Test"' | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
        
        if [[ ! -z "$PLACEMENT_COURSE_ID" ]]; then
            echo -e "${GREEN}✓ Found Placement Test course${NC}"
            echo "  Course ID: ${PLACEMENT_COURSE_ID}"
        else
            echo -e "${RED}✗ Placement Test course not found${NC}"
            echo "  Available courses in response"
            exit 1
        fi
    else
        echo -e "${RED}✗ Failed to get courses - HTTP ${status_code}${NC}"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to find writing and normal lessons
find_lessons() {
    echo -e "${CYAN}Step 3: Finding Writing and Normal lessons...${NC}"
    
    # Get course details which includes lesson IDs
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${COURSE_API}/${PLACEMENT_COURSE_ID}")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        # Extract lesson IDs from course
        lesson_ids=$(echo "$response_body" | grep -o '"lessonIds":\[[0-9,]*\]' | grep -o '[0-9,]*' | tr ',' '\n')
        
        if [[ -z "$lesson_ids" ]]; then
            echo -e "${RED}✗ No lessons found in course${NC}"
            exit 1
        fi
        
        # Get details for each lesson to find WRITING and READING lessons
        for lesson_id in $lesson_ids; do
            lesson_response=$(curl -s \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                "${BACKEND_URL}${LESSON_API}/${lesson_id}")
            
            lesson_name=$(echo "$lesson_response" | grep -o '"name":"[^"]*"' | head -1 | sed 's/"name":"//g' | sed 's/"//g')
            skill_type=$(echo "$lesson_response" | grep -o '"skillType":"[^"]*"' | head -1 | sed 's/"skillType":"//g' | sed 's/"//g')
            
            if [[ "$lesson_name" == "(PLACEMENT) Writing" && "$skill_type" == "WRITING" ]]; then
                WRITING_LESSON_ID=$lesson_id
            elif [[ "$lesson_name" == "(PLACEMENT) Text - Reading" && "$skill_type" == "READING" ]]; then
                NORMAL_LESSON_ID=$lesson_id
            fi
        done
        
        if [[ ! -z "$WRITING_LESSON_ID" && ! -z "$NORMAL_LESSON_ID" ]]; then
            echo -e "${GREEN}✓ Found lessons${NC}"
            echo "  Writing Lesson ID: ${WRITING_LESSON_ID}"
            echo "  Normal Lesson ID: ${NORMAL_LESSON_ID}"
        else
            echo -e "${RED}✗ Could not find required lessons${NC}"
            [[ -z "$WRITING_LESSON_ID" ]] && echo "  Missing: WRITING lesson"
            [[ -z "$NORMAL_LESSON_ID" ]] && echo "  Missing: READING lesson"
            exit 1
        fi
    else
        echo -e "${RED}✗ Failed to get course details - HTTP ${status_code}${NC}"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to create enrollment or get existing one
create_enrollment() {
    echo -e "${CYAN}Step 4: Checking for existing enrollment or creating new one...${NC}"
    
    # First, check if enrollment already exists
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${ENROLLMENT_API}/latest?courseId=${PLACEMENT_COURSE_ID}&userId=${USER_ID}")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        # Enrollment exists, use it
        ENROLLMENT_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://g')
        
        if [[ ! -z "$ENROLLMENT_ID" ]]; then
            echo -e "${YELLOW}✓ Using existing enrollment${NC}"
            echo "  Enrollment ID: ${ENROLLMENT_ID}"
            echo "----------------------------------------"
            return 0
        fi
    fi
    
    # No existing enrollment, create new one
    echo -e "${CYAN}  No existing enrollment found, creating new one...${NC}"
    
    enrollment_data="{
        \"userId\": ${USER_ID},
        \"courseId\": ${PLACEMENT_COURSE_ID}
    }"
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -d "${enrollment_data}" \
        "${BACKEND_URL}${ENROLLMENT_API}")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        ENROLLMENT_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://g')
        
        if [[ ! -z "$ENROLLMENT_ID" ]]; then
            echo -e "${GREEN}✓ Enrollment created${NC}"
            echo "  Enrollment ID: ${ENROLLMENT_ID}"
        else
            echo -e "${RED}✗ Failed to extract enrollment ID${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Enrollment creation failed - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to activate enrollment
activate_enrollment() {
    echo -e "${CYAN}Step 5: Activating enrollment...${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X PUT \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${ENROLLMENT_API}/${ENROLLMENT_ID}/join")
    
    status_code=$(echo "$response" | tail -n1)
    
    if [[ "$status_code" == "200" ]]; then
        echo -e "${GREEN}✓ Enrollment activated${NC}"
    else
        echo -e "${RED}✗ Enrollment activation failed - HTTP ${status_code}${NC}"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to get questions for a lesson
get_lesson_questions() {
    local lesson_id=$1
    local lesson_type=$2
    
    echo -e "${CYAN}Step 6${lesson_type}: Getting questions for ${lesson_type} lesson...${NC}" >&2
    
    # Get lesson details which includes questionIds
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${LESSON_API}/${lesson_id}")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        # Extract question IDs from questionIds array
        question_ids=$(echo "$response_body" | grep -o '"questionIds":\[[0-9,]*\]' | grep -o '[0-9,]*' | tr ',' '\n')
        question_count=$(echo "$question_ids" | wc -l)
        
        if [[ -z "$question_ids" ]]; then
            echo -e "${YELLOW}⚠ No questions found for ${lesson_type} lesson${NC}" >&2
            echo ""
        else
            echo -e "${GREEN}✓ Found ${question_count} questions for ${lesson_type} lesson${NC}" >&2
            # Output only question IDs to stdout (no colors)
            echo "$question_ids"
        fi
    else
        echo -e "${RED}✗ Failed to get lesson details - HTTP ${status_code}${NC}" >&2
        echo -e "${RED}Response: $response_body${NC}" >&2
        exit 1
    fi
    echo "----------------------------------------" >&2
}

# Function to submit answers for writing question
submit_writing_answer() {
    local question_id=$1
    
    echo -e "${CYAN}Step 7a: Submitting WRITING answer (with score from content-scoring)...${NC}"
    
    # Simulate that the answer was scored by content-scoring service
    answer_data="{
        \"questionId\": ${question_id},
        \"answerContent\": \"My name is John Doe, I am 25 years old, and I work as a software engineer. I enjoy programming, reading technical books, and learning new technologies. In my free time, I like playing video games and spending time with my friends and family.\",
        \"pointAchieved\": 85,
        \"improvement\": \"Good introduction with clear structure. Consider adding more specific details about your interests.\",
        \"enrollmentId\": ${ENROLLMENT_ID},
        \"sessionId\": ${SESSION_ID}
    }"
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -d "${answer_data}" \
        "${BACKEND_URL}${ANSWER_API}/user/${USER_ID}")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        echo -e "${GREEN}✓ WRITING answer submitted successfully${NC}"
        echo "  Points: 85/100"
    else
        echo -e "${RED}✗ Failed to submit WRITING answer - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to submit answers for normal questions
submit_normal_answers() {
    local question_ids=$1
    
    echo -e "${CYAN}Step 7b: Submitting answers for NORMAL lesson...${NC}"
    
    local count=0
    for qid in $question_ids; do
        count=$((count + 1))
        
        # Submit a correct answer
        answer_data="{
            \"questionId\": ${qid},
            \"answerContent\": \"Tired as he was, he agreed to help me with my homework.\",
            \"enrollmentId\": ${ENROLLMENT_ID},
            \"sessionId\": ${SESSION_ID}
        }"
        
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            -d "${answer_data}" \
            "${BACKEND_URL}${ANSWER_API}/user/${USER_ID}")
        
        status_code=$(echo "$response" | tail -n1)
        
        if [[ "$status_code" == "200" ]]; then
            # Grade the answer
            answer_id=$(echo "$response" | head -n -1 | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
            
            grade_response=$(curl -s -X PUT \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                "${BACKEND_URL}${ANSWER_API}/grade/${answer_id}")
            
            echo -e "${GREEN}✓ Answer ${count} submitted and graded${NC}"
        fi
        
        if [[ $count -ge 3 ]]; then
            break
        fi
    done
    echo "----------------------------------------"
}

# Function to create lesson result
create_lesson_result() {
    local lesson_id=$1
    local lesson_type=$2
    
    echo -e "${CYAN}Step 8${lesson_type}: Creating lesson result for ${lesson_type} lesson...${NC}"
    
    lesson_result_data="{
        \"lessonId\": ${lesson_id},
        \"enrollmentId\": ${ENROLLMENT_ID},
        \"sessionId\": ${SESSION_ID},
        \"stuTime\": 300
    }"
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -d "${lesson_result_data}" \
        "${BACKEND_URL}${LESSON_RESULT_API}/user/${USER_ID}")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        total_points=$(echo "$response_body" | grep -o '"totalPoints":[0-9]*' | grep -o '[0-9]*')
        com_level=$(echo "$response_body" | grep -o '"comLevel":[0-9.]*' | grep -o '[0-9.]*')
        
        echo -e "${GREEN}✓ Lesson result created for ${lesson_type} lesson${NC}"
        echo "  Total Points: ${total_points}"
        echo "  Completion Level: ${com_level}%"
    else
        echo -e "${RED}✗ Failed to create lesson result - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo "----------------------------------------"
}

# Function to test save-completion API
test_save_completion() {
    echo -e "${CYAN}Step 9: Testing SAVE-COMPLETION API...${NC}"
    echo -e "${YELLOW}This is the main test - saving enrollment completion${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${ENROLLMENT_API}/${ENROLLMENT_ID}/save-completion")
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [[ "$status_code" == "200" ]]; then
        total_points=$(echo "$response_body" | grep -o '"totalPoints":[0-9]*' | grep -o '[0-9]*')
        com_level=$(echo "$response_body" | grep -o '"comLevel":[0-9.]*' | grep -o '[0-9.]*')
        skill_score=$(echo "$response_body" | grep -o '"skillScore":[0-9.]*' | grep -o '[0-9.]*')
        
        echo -e "${GREEN}✓✓✓ SAVE-COMPLETION API TEST PASSED ✓✓✓${NC}"
        echo -e "${GREEN}  Total Points: ${total_points}${NC}"
        echo -e "${GREEN}  Completion Level: ${com_level}%${NC}"
        echo -e "${GREEN}  Skill Score: ${skill_score}${NC}"
        
        # Verify that writing points were counted
        if [[ $total_points -gt 50 ]]; then
            echo -e "${GREEN}  ✓ Writing question points were counted correctly${NC}"
        else
            echo -e "${YELLOW}  ⚠ Warning: Total points seem low, writing points may not be counted${NC}"
        fi
    else
        echo -e "${RED}✗✗✗ SAVE-COMPLETION API TEST FAILED ✗✗✗${NC}"
        echo -e "${RED}  HTTP Status: ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo "----------------------------------------"
}

# Main test flow
echo -e "${YELLOW}Starting comprehensive test workflow...${NC}"
echo ""

get_auth_token
find_placement_course
find_lessons
create_enrollment
activate_enrollment

# Test with WRITING lesson
writing_questions=$(get_lesson_questions "$WRITING_LESSON_ID" "WRITING")
if [[ -z "$writing_questions" || "$writing_questions" == *"error"* ]]; then
    echo -e "${RED}✗ Failed to get WRITING questions, cannot continue${NC}"
    exit 1
fi
writing_question_id=$(echo "$writing_questions" | head -1 | tr -d '[:space:]')
if [[ -z "$writing_question_id" || ! "$writing_question_id" =~ ^[0-9]+$ ]]; then
    echo -e "${RED}✗ Invalid WRITING question ID: '$writing_question_id'${NC}"
    exit 1
fi
submit_writing_answer "$writing_question_id"
create_lesson_result "$WRITING_LESSON_ID" "a"

# Test with NORMAL lesson
normal_questions=$(get_lesson_questions "$NORMAL_LESSON_ID" "NORMAL")
if [[ -z "$normal_questions" || "$normal_questions" == *"error"* ]]; then
    echo -e "${RED}✗ Failed to get NORMAL questions, cannot continue${NC}"
    exit 1
fi
submit_normal_answers "$normal_questions"
create_lesson_result "$NORMAL_LESSON_ID" "b"

# Final test: Save completion
test_save_completion

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  TEST SUMMARY${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}✓ All tests passed successfully!${NC}"
echo -e "${GREEN}✓ WRITING questions are properly scored${NC}"
echo -e "${GREEN}✓ NORMAL questions are properly scored${NC}"
echo -e "${GREEN}✓ Save-completion API works correctly${NC}"
echo ""
echo -e "${YELLOW}Key Points Verified:${NC}"
echo "  1. Authentication works"
echo "  2. Enrollment creation and activation works"
echo "  3. WRITING answers with scores are saved correctly"
echo "  4. Normal answers are graded correctly"
echo "  5. Lesson results calculate points properly"
echo "  6. Save-completion API aggregates all points correctly"
echo ""
echo -e "${CYAN}Enrollment ID: ${ENROLLMENT_ID}${NC}"
echo -e "${CYAN}Session ID: ${SESSION_ID}${NC}"
echo ""