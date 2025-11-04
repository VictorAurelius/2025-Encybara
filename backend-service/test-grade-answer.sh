#!/bin/bash

# ============================================
# Grade Answer API Automated Test Suite
# ============================================
#
# Description:
#   This script automatically tests the Grade Answer API
#   using real data from English Placement Test course.
#   Tests TEXT and CHOICE question types only.
#
# Test Flow:
#   1. Authenticate user (user@example.com)
#   2. Find placement course
#   3. Create enrollment
#   4. Discover questions (TEXT & CHOICE)
#   5. Create test answers (correct + incorrect)
#   6. Grade all answers via API
#   7. Validate results
#   8. Generate report
#
# Test Coverage:
#   - TEXT questions: ~5 questions, 10-15 test cases
#   - CHOICE questions: ~9 questions, 18-27 test cases
#   - Total: ~28-42 grading API calls
#
# Prerequisites:
#   - Backend service running on http://localhost:8080
#   - Database seeded with placement course
#   - Default user created (user@example.com / Abc@123456)
#
# Usage:
#   ./test-grade-answer.sh
#
# Output:
#   - Colored console output (✓ PASSED, ✗ FAILED)
#   - Test report file: test-grade-answer-report-YYYYMMDD-HHMMSS.txt
#
# Author: Generated based on req-6 task plan
# Date: 2025-11-04
# ============================================

# Configuration
BACKEND_URL="${BACKEND_URL:-http://18.136.223.96:8080}"
API_BASE="/api/v1"
DEFAULT_EMAIL="${DEFAULT_EMAIL:-user@example.com}"
DEFAULT_PASSWORD="${DEFAULT_PASSWORD:-Abc@123456}"
PLACEMENT_COURSE_NAME="English Placement Test"

# Global variables
ACCESS_TOKEN=""
PLACEMENT_COURSE_ID=""
ENROLLMENT_ID=""
TEXT_LESSON_ID=""
CHOICE_LESSON_ID=""
declare -a TEXT_QUESTION_IDS
declare -a CHOICE_QUESTION_IDS
declare -a TEXT_ANSWERS
declare -a CHOICE_ANSWERS
declare -a TEST_RESULTS
PASSED_COUNT=0
FAILED_COUNT=0

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================
# Helper Functions
# ============================================

# Function to get authentication token
get_auth_token() {
    echo -e "${YELLOW}>>> Authenticating user...${NC}"

    login_data="{
        \"username\": \"${DEFAULT_EMAIL}\",
        \"password\": \"${DEFAULT_PASSWORD}\"
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -d "${login_data}" \
        "${BACKEND_URL}${API_BASE}/auth/login")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" ]]; then
        # Extract access token
        ACCESS_TOKEN=$(echo "$response_body" | grep -o '"access_token":"[^"]*"' | sed 's/"access_token":"//g' | sed 's/"//g')
        if [[ -z "$ACCESS_TOKEN" ]]; then
            ACCESS_TOKEN=$(echo "$response_body" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//g' | sed 's/"//g')
        fi
        if [[ ! -z "$ACCESS_TOKEN" ]]; then
            echo -e "${GREEN}✓ Authentication successful${NC}"
            echo "  Token: ${ACCESS_TOKEN:0:30}..."
        else
            echo -e "${RED}✗ Failed to extract access token${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Authentication failed - HTTP ${status_code}${NC}"
        echo "Response: $response_body"
        exit 1
    fi
    echo ""
}

# Function to find placement course
find_placement_course() {
    echo -e "${YELLOW}>>> Finding placement course...${NC}"

    response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/courses")

    # Extract course ID - look for "English Placement Test"
    # Parse JSON to find the course with matching name
    PLACEMENT_COURSE_ID=$(echo "$response" | grep -o '"id":[0-9]*,"name":"English Placement Test"' | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)

    if [[ ! -z "$PLACEMENT_COURSE_ID" ]]; then
        echo -e "${GREEN}✓ Found placement course${NC}"
        echo "  Course ID: ${PLACEMENT_COURSE_ID}"
        echo "  Course Name: ${PLACEMENT_COURSE_NAME}"
    else
        echo -e "${RED}✗ Placement course not found${NC}"
        echo "  Please ensure database is seeded with placement course"
        exit 1
    fi
    echo ""
}

# Function to create enrollment
create_enrollment() {
    echo -e "${YELLOW}>>> Creating enrollment...${NC}"

    # First check if user is already enrolled
    echo -e "${CYAN}  → Checking existing enrollments...${NC}"
    enrollment_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/enrollments")
    
    # Try to find existing enrollment for this course
    ENROLLMENT_ID=$(echo "$enrollment_response" | grep -o '"courseId":'${PLACEMENT_COURSE_ID}'[^}]*"id":[0-9]*' | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
    
    if [[ ! -z "$ENROLLMENT_ID" ]]; then
        echo -e "${GREEN}✓ Found existing enrollment${NC}"
        echo "  Enrollment ID: ${ENROLLMENT_ID}"
        echo ""
        return
    fi

    # If not enrolled, try to create enrollment
    echo -e "${CYAN}  → Creating new enrollment...${NC}"
    enrollment_data="{
        \"courseId\": ${PLACEMENT_COURSE_ID}
    }"

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "${enrollment_data}" \
        "${BACKEND_URL}${API_BASE}/enrollments")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" || "$status_code" == "201" ]]; then
        ENROLLMENT_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        echo -e "${GREEN}✓ Enrollment created${NC}"
        echo "  Enrollment ID: ${ENROLLMENT_ID}"
    else
        # Check if already enrolled (different error message)
        if echo "$response_body" | grep -q "already enrolled"; then
            echo -e "${YELLOW}⚠ User already enrolled, fetching existing enrollment...${NC}"
            # Get existing enrollment ID
            enrollment_response=$(curl -s -X GET \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                "${BACKEND_URL}${API_BASE}/enrollments")
            ENROLLMENT_ID=$(echo "$enrollment_response" | grep -o '"courseId":'${PLACEMENT_COURSE_ID}'[^}]*"id":[0-9]*' | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
            if [[ ! -z "$ENROLLMENT_ID" ]]; then
                echo -e "${GREEN}✓ Using existing enrollment${NC}"
                echo "  Enrollment ID: ${ENROLLMENT_ID}"
            else
                echo -e "${RED}✗ Failed to get enrollment ID${NC}"
                exit 1
            fi
        else
            echo -e "${RED}✗ Enrollment creation failed - HTTP ${status_code}${NC}"
            echo "Response: $response_body"
            echo -e "${YELLOW}Debug - Request data:${NC}"
            echo "$enrollment_data"
            echo ""
            echo -e "${YELLOW}⚠ Continuing without enrollment - will try to create answers directly${NC}"
            ENROLLMENT_ID=""
        fi
    fi
    echo ""
}

# Function to discover placement questions
discover_placement_questions() {
    echo -e "${YELLOW}>>> Discovering placement questions...${NC}"

    # Get course details with lessons
    echo -e "${CYAN}  → Getting course details...${NC}"
    course_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        "${BACKEND_URL}${API_BASE}/courses/${PLACEMENT_COURSE_ID}")

    # Debug: Show course response structure
    echo -e "${CYAN}  → Course API response received${NC}"
    echo -e "${YELLOW}Debug - Full course response:${NC}"
    echo "$course_response" | head -10
    echo "..."
    
    # Extract lesson IDs from course - try multiple possible JSON structures
    lesson_ids=""
    
    # Try format: "lessonIds": [1,2,3,4,5,6]
    if echo "$course_response" | grep -q '"lessonIds":\['; then
        lesson_ids=$(echo "$course_response" | grep -o '"lessonIds":\[[0-9,]*\]' | sed 's/"lessonIds":\[//g' | sed 's/\]//g' | tr ',' ' ')
        echo -e "${CYAN}  → Found lessonIds array format in course response${NC}"
    # Try format: "lessonId": number
    elif echo "$course_response" | grep -q '"lessonId":[0-9]*'; then
        lesson_ids=$(echo "$course_response" | grep -o '"lessonId":[0-9]*' | cut -d':' -f2)
        echo -e "${CYAN}  → Found lessonId format in course response${NC}"
    # Try format: "id": number within lessons array
    elif echo "$course_response" | grep -q '"lessons"'; then
        lesson_ids=$(echo "$course_response" | grep -A 20 '"lessons"' | grep -o '"id":[0-9]*' | cut -d':' -f2)
        echo -e "${CYAN}  → Found lessons array format in course response${NC}"
    # Try direct lesson objects with id (fallback)
    elif echo "$course_response" | grep -q '"id":[0-9]*'; then
        lesson_ids=$(echo "$course_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        echo -e "${CYAN}  → Found direct id format in course response${NC}"
    fi

    if [[ -z "$lesson_ids" ]]; then
        echo -e "${RED}✗ No lesson IDs found in course response${NC}"
        echo -e "${YELLOW}Debug - Course response sample:${NC}"
        echo "$course_response" | head -5
        exit 1
    fi

    echo -e "${CYAN}  → Found lesson IDs: $lesson_ids${NC}"

    # For each lesson, check its name and get questions
    for lesson_id in $lesson_ids; do
        echo -e "${CYAN}  → Checking lesson ID: $lesson_id${NC}"
        
        lesson_response=$(curl -s -X GET \
            -H "Authorization: Bearer ${ACCESS_TOKEN}" \
            "${BACKEND_URL}${API_BASE}/lessons/${lesson_id}")

        # Extract lesson name - try multiple formats
        lesson_name=""
        if echo "$lesson_response" | grep -q '"name":"[^"]*"'; then
            lesson_name=$(echo "$lesson_response" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
        elif echo "$lesson_response" | grep -q '"lessonName":"[^"]*"'; then
            lesson_name=$(echo "$lesson_response" | grep -o '"lessonName":"[^"]*"' | head -1 | cut -d'"' -f4)
        fi

        echo -e "${CYAN}    → Lesson name: '$lesson_name'${NC}"
        
        # Debug: Show lesson response sample for target lessons
        if [[ "$lesson_name" == *"Text - Reading"* ]] || [[ "$lesson_name" == *"Choice - Reading"* ]]; then
            echo -e "${YELLOW}    → Debug - Lesson $lesson_id response sample:${NC}"
            echo "$lesson_response" | head -3
            echo "    ..."
        fi

        if [[ "$lesson_name" == *"Text - Reading"* ]]; then
            TEXT_LESSON_ID=$lesson_id
            echo -e "${GREEN}✓ Found TEXT lesson${NC}"
            echo "  Lesson ID: ${TEXT_LESSON_ID}"
            echo "  Lesson Name: ${lesson_name}"

            # Extract TEXT question IDs - try multiple formats
            question_ids=""
            if echo "$lesson_response" | grep -q '"questionIds":\['; then
                question_ids=$(echo "$lesson_response" | grep -o '"questionIds":\[[0-9,]*\]' | sed 's/"questionIds":\[//g' | sed 's/\]//g' | tr ',' ' ')
            elif echo "$lesson_response" | grep -q '"questionId":[0-9]*'; then
                question_ids=$(echo "$lesson_response" | grep -o '"questionId":[0-9]*' | cut -d':' -f2)
            elif echo "$lesson_response" | grep -q '"questions".*"id":[0-9]*'; then
                question_ids=$(echo "$lesson_response" | grep -A 50 '"questions"' | grep -o '"id":[0-9]*' | cut -d':' -f2)
            elif echo "$lesson_response" | grep -q '"id":[0-9]*'; then
                # Filter out lesson ID itself
                question_ids=$(echo "$lesson_response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | grep -v "^${lesson_id}$")
            fi
            
            TEXT_QUESTION_IDS=($question_ids)
            echo "  TEXT Questions: ${#TEXT_QUESTION_IDS[@]} found (IDs: $question_ids)"
            
        elif [[ "$lesson_name" == *"Choice - Reading"* ]]; then
            CHOICE_LESSON_ID=$lesson_id
            echo -e "${GREEN}✓ Found CHOICE lesson${NC}"
            echo "  Lesson ID: ${CHOICE_LESSON_ID}"
            echo "  Lesson Name: ${lesson_name}"

            # Extract CHOICE question IDs - try multiple formats
            question_ids=""
            if echo "$lesson_response" | grep -q '"questionIds":\['; then
                question_ids=$(echo "$lesson_response" | grep -o '"questionIds":\[[0-9,]*\]' | sed 's/"questionIds":\[//g' | sed 's/\]//g' | tr ',' ' ')
            elif echo "$lesson_response" | grep -q '"questionId":[0-9]*'; then
                question_ids=$(echo "$lesson_response" | grep -o '"questionId":[0-9]*' | cut -d':' -f2)
            elif echo "$lesson_response" | grep -q '"questions".*"id":[0-9]*'; then
                question_ids=$(echo "$lesson_response" | grep -A 50 '"questions"' | grep -o '"id":[0-9]*' | cut -d':' -f2)
            elif echo "$lesson_response" | grep -q '"id":[0-9]*'; then
                # Filter out lesson ID itself
                question_ids=$(echo "$lesson_response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | grep -v "^${lesson_id}$")
            fi
            
            CHOICE_QUESTION_IDS=($question_ids)
            echo "  CHOICE Questions: ${#CHOICE_QUESTION_IDS[@]} found (IDs: $question_ids)"
        else
            echo -e "${YELLOW}  → Skipping lesson: '$lesson_name' (not target lesson)${NC}"
        fi
    done

    # Validate that we found the required lessons
    if [[ -z "$TEXT_LESSON_ID" ]] || [[ -z "$CHOICE_LESSON_ID" ]]; then
        echo -e "${RED}✗ Failed to discover all required lessons${NC}"
        echo "  TEXT_LESSON_ID: '${TEXT_LESSON_ID}'"
        echo "  CHOICE_LESSON_ID: '${CHOICE_LESSON_ID}'"
        echo ""
        echo -e "${YELLOW}Available lesson names found:${NC}"
        for lesson_id in $lesson_ids; do
            lesson_response=$(curl -s -X GET \
                -H "Authorization: Bearer ${ACCESS_TOKEN}" \
                "${BACKEND_URL}${API_BASE}/lessons/${lesson_id}")
            lesson_name=$(echo "$lesson_response" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
            echo "  Lesson ID $lesson_id: '$lesson_name'"
        done
        exit 1
    fi

    if [[ ${#TEXT_QUESTION_IDS[@]} -eq 0 ]] || [[ ${#CHOICE_QUESTION_IDS[@]} -eq 0 ]]; then
        echo -e "${RED}✗ No questions found in lessons${NC}"
        echo "  TEXT questions found: ${#TEXT_QUESTION_IDS[@]}"
        echo "  CHOICE questions found: ${#CHOICE_QUESTION_IDS[@]}"
        exit 1
    fi

    echo ""
}

# Function to create answer
create_answer() {
    local question_id="$1"
    local answer_content="$2"
    local expected_result="$3"  # "correct" or "incorrect"
    local question_type="$4"    # "TEXT" or "CHOICE"

    # Build answer data - handle missing enrollment ID
    if [[ ! -z "$ENROLLMENT_ID" ]]; then
        answer_data="{
            \"questionId\": ${question_id},
            \"answerContent\": \"${answer_content}\",
            \"enrollmentId\": ${ENROLLMENT_ID},
            \"sessionId\": 1
        }"
    else
        answer_data="{
            \"questionId\": ${question_id},
            \"answerContent\": \"${answer_content}\",
            \"sessionId\": 1
        }"
    fi

    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "${answer_data}" \
        "${BACKEND_URL}${API_BASE}/answers")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    if [[ "$status_code" == "200" || "$status_code" == "201" ]]; then
        answer_id=$(echo "$response_body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        if [[ "$question_type" == "TEXT" ]]; then
            TEXT_ANSWERS+=("${answer_id}|${expected_result}")
        else
            CHOICE_ANSWERS+=("${answer_id}|${expected_result}")
        fi
        echo -e "${CYAN}  → Created ${question_type} answer (ID: ${answer_id}, Expected: ${expected_result})${NC}"
        echo "$answer_id"
    else
        echo -e "${RED}  ✗ Failed to create answer - HTTP ${status_code}${NC}"
        echo ""
    fi
}

# Function to grade answer
grade_answer() {
    local answer_id="$1"
    local expected_result="$2"  # "correct" or "incorrect"
    local question_type="$3"    # "TEXT" or "CHOICE"
    local test_id="$4"          # Test case ID

    echo -e "${YELLOW}[${test_id}] Grading ${question_type} answer ${answer_id} (expected: ${expected_result})${NC}"

    response=$(curl -s -w "\n%{http_code}" -X PUT \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        "${BACKEND_URL}${API_BASE}/answers/grade/${answer_id}")

    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)

    # Validate status code
    if [[ "$status_code" != "200" ]]; then
        echo -e "${RED}  ✗ FAILED - HTTP ${status_code}${NC}"
        TEST_RESULTS+=("${test_id}|FAILED|Grade ${question_type} answer ${answer_id} - HTTP ${status_code}")
        FAILED_COUNT=$((FAILED_COUNT + 1))
        echo ""
        return
    fi

    # Extract pointAchieved from response
    point_achieved=$(echo "$response_body" | grep -o '"pointAchieved":[0-9]*' | cut -d':' -f2)

    # Validate pointAchieved based on expected result
    if [[ "$expected_result" == "correct" ]]; then
        # Expected: full points (10)
        if [[ "$point_achieved" == "10" ]]; then
            echo -e "${GREEN}  ✓ PASSED - Correct answer got full points (10)${NC}"
            TEST_RESULTS+=("${test_id}|PASSED|Grade ${question_type} - Correct answer got 10 points")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - Expected 10 points, got ${point_achieved}${NC}"
            TEST_RESULTS+=("${test_id}|FAILED|Grade ${question_type} - Expected 10, got ${point_achieved}")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    else
        # Expected: zero points (0)
        if [[ "$point_achieved" == "0" ]]; then
            echo -e "${GREEN}  ✓ PASSED - Incorrect answer got zero points (0)${NC}"
            TEST_RESULTS+=("${test_id}|PASSED|Grade ${question_type} - Incorrect answer got 0 points")
            PASSED_COUNT=$((PASSED_COUNT + 1))
        else
            echo -e "${RED}  ✗ FAILED - Expected 0 points, got ${point_achieved}${NC}"
            TEST_RESULTS+=("${test_id}|FAILED|Grade ${question_type} - Expected 0, got ${point_achieved}")
            FAILED_COUNT=$((FAILED_COUNT + 1))
        fi
    fi

    # Validate response structure
    if ! echo "$response_body" | grep -q '"statusCode"' || \
       ! echo "$response_body" | grep -q '"message"' || \
       ! echo "$response_body" | grep -q '"data"'; then
        echo -e "${YELLOW}  ⚠ WARNING - Response structure incomplete${NC}"
    fi

    echo ""
}

# ============================================
# Test Functions
# ============================================

# Test TEXT questions
test_text_questions() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  TEXT QUESTIONS TESTING${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    if [[ ${#TEXT_QUESTION_IDS[@]} -eq 0 ]]; then
        echo -e "${YELLOW}⚠ No TEXT questions found, skipping...${NC}"
        echo ""
        return
    fi

    echo -e "${YELLOW}Creating TEXT test answers...${NC}"
    echo ""

    # Known correct answers from placement course data
    declare -A text_correct_answers=(
        [0]="Tired as he was, he agreed to help me with my homework"
        [1]="The man whose daughter is fond of dancing works for my father's company"
        [2]="Martin apologized to Angela for having damaged her car"
        [3]="Maradona is thought to be the best football player in the 20th century"
        [4]="If it hadn't been for your help, I couldn't overcome the problem"
    )

    # Test each TEXT question
    local q_index=0
    for question_id in "${TEXT_QUESTION_IDS[@]}"; do
        if [[ $q_index -ge ${#text_correct_answers[@]} ]]; then
            break
        fi

        local correct_answer="${text_correct_answers[$q_index]}"

        echo -e "${CYAN}TEXT Question ${q_index}: ID ${question_id}${NC}"

        # Test case 1: Correct answer
        create_answer "$question_id" "$correct_answer" "correct" "TEXT"

        # Test case 2: Incorrect answer
        create_answer "$question_id" "This is an incorrect answer" "incorrect" "TEXT"

        # Test case 3: Correct answer with different case (normalization)
        local uppercase_answer=$(echo "$correct_answer" | tr '[:lower:]' '[:upper:]')
        create_answer "$question_id" "$uppercase_answer" "correct" "TEXT"

        echo ""
        q_index=$((q_index + 1))
    done

    echo -e "${YELLOW}Grading TEXT answers...${NC}"
    echo ""

    # Grade all TEXT answers
    local test_id=1
    for answer in "${TEXT_ANSWERS[@]}"; do
        IFS='|' read -r answer_id expected_result <<< "$answer"
        grade_answer "$answer_id" "$expected_result" "TEXT" "TEXT-$(printf '%03d' $test_id)"
        test_id=$((test_id + 1))
    done
}

# Test CHOICE questions
test_choice_questions() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  CHOICE QUESTIONS TESTING${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    if [[ ${#CHOICE_QUESTION_IDS[@]} -eq 0 ]]; then
        echo -e "${YELLOW}⚠ No CHOICE questions found, skipping...${NC}"
        echo ""
        return
    fi

    echo -e "${YELLOW}Creating CHOICE test answers...${NC}"
    echo ""

    # Known correct answers from placement course data
    declare -A choice_correct_answers=(
        [0]="supportive"
        [1]="displace"
        [2]="successful"
        [3]="trying"
        [4]="narrow-minded"
        [5]="OK, let me just check the diary."
        [6]="went / have not been"
        [7]="look up"
        [8]="The harder/ the better"
    )

    declare -A choice_incorrect_answers=(
        [0]="intolerant"
        [1]="locate"
        [2]="disorganised"
        [3]="requesting"
        [4]="narrow-mind"
        [5]="Not at the moment. He can't be disturbed."
        [6]="went / wasn't"
        [7]="get up"
        [8]="The more / the much"
    )

    # Test each CHOICE question
    local q_index=0
    for question_id in "${CHOICE_QUESTION_IDS[@]}"; do
        if [[ $q_index -ge ${#choice_correct_answers[@]} ]]; then
            break
        fi

        local correct_answer="${choice_correct_answers[$q_index]}"
        local incorrect_answer="${choice_incorrect_answers[$q_index]}"

        echo -e "${CYAN}CHOICE Question ${q_index}: ID ${question_id}${NC}"

        # Test case 1: Correct answer
        create_answer "$question_id" "$correct_answer" "correct" "CHOICE"

        # Test case 2: Incorrect answer
        create_answer "$question_id" "$incorrect_answer" "incorrect" "CHOICE"

        # Test case 3: Correct answer with different case (normalization)
        local uppercase_answer=$(echo "$correct_answer" | tr '[:lower:]' '[:upper:]')
        create_answer "$question_id" "$uppercase_answer" "correct" "CHOICE"

        echo ""
        q_index=$((q_index + 1))
    done

    echo -e "${YELLOW}Grading CHOICE answers...${NC}"
    echo ""

    # Grade all CHOICE answers
    local test_id=1
    for answer in "${CHOICE_ANSWERS[@]}"; do
        IFS='|' read -r answer_id expected_result <<< "$answer"
        grade_answer "$answer_id" "$expected_result" "CHOICE" "CHOICE-$(printf '%03d' $test_id)"
        test_id=$((test_id + 1))
    done
}

# ============================================
# Report Generation
# ============================================

generate_report() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  TEST EXECUTION SUMMARY${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    local total_tests=$((PASSED_COUNT + FAILED_COUNT))

    echo "Course: ${PLACEMENT_COURSE_NAME}"
    echo "User: ${DEFAULT_EMAIL}"
    echo "Enrollment ID: ${ENROLLMENT_ID}"
    echo ""
    echo "Questions Tested:"
    echo "  TEXT Questions: ${#TEXT_QUESTION_IDS[@]} questions, ${#TEXT_ANSWERS[@]} test cases"
    echo "  CHOICE Questions: ${#CHOICE_QUESTION_IDS[@]} questions, ${#CHOICE_ANSWERS[@]} test cases"
    echo ""
    echo "Test Statistics:"
    echo "  Total Grading Calls: ${total_tests}"
    echo -e "  ${GREEN}Passed: ${PASSED_COUNT}${NC}"
    echo -e "  ${RED}Failed: ${FAILED_COUNT}${NC}"
    echo ""

    if [[ $total_tests -gt 0 ]]; then
        success_rate=$(awk "BEGIN {printf \"%.1f\", ($PASSED_COUNT / $total_tests) * 100}")
        echo "Success Rate: ${success_rate}%"
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
        esac
    done

    echo "----------------------------------------"
    echo ""

    # Save report to file
    local report_file="test-grade-answer-report-$(date +%Y%m%d-%H%M%S).txt"
    {
        echo "========================================="
        echo "Grade Answer API Test Execution Report"
        echo "========================================="
        echo ""
        echo "Generated: $(date)"
        echo "Backend URL: ${BACKEND_URL}"
        echo "Course: ${PLACEMENT_COURSE_NAME}"
        echo "Course ID: ${PLACEMENT_COURSE_ID}"
        echo "User: ${DEFAULT_EMAIL}"
        echo "Enrollment ID: ${ENROLLMENT_ID}"
        echo ""
        echo "Questions Tested:"
        echo "  TEXT Questions: ${#TEXT_QUESTION_IDS[@]} questions, ${#TEXT_ANSWERS[@]} test cases"
        echo "  CHOICE Questions: ${#CHOICE_QUESTION_IDS[@]} questions, ${#CHOICE_ANSWERS[@]} test cases"
        echo ""
        echo "Summary:"
        echo "  Total Grading Calls: ${total_tests}"
        echo "  Passed: ${PASSED_COUNT}"
        echo "  Failed: ${FAILED_COUNT}"
        if [[ $total_tests -gt 0 ]]; then
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
        echo "Question Details:"
        echo "  TEXT Lesson ID: ${TEXT_LESSON_ID}"
        echo "  TEXT Question IDs: ${TEXT_QUESTION_IDS[@]}"
        echo "  CHOICE Lesson ID: ${CHOICE_LESSON_ID}"
        echo "  CHOICE Question IDs: ${CHOICE_QUESTION_IDS[@]}"
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
    echo -e "${BLUE}  GRADE ANSWER API TEST SUITE${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
    echo "Target: English Placement Test"
    echo "Test Types: TEXT & CHOICE questions only"
    echo "User: ${DEFAULT_EMAIL}"
    echo "Backend: ${BACKEND_URL}"
    echo ""
    echo "Test Flow:"
    echo "  1. Authenticate user"
    echo "  2. Find placement course"
    echo "  3. Create enrollment"
    echo "  4. Discover questions"
    echo "  5. Create test answers"
    echo "  6. Grade all answers"
    echo "  7. Validate results"
    echo "  8. Generate report"
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo ""

    # SETUP PHASE
    echo -e "${BLUE}>>> PHASE 1: SETUP${NC}"
    echo ""
    get_auth_token
    find_placement_course
    discover_placement_questions

    # ENROLLMENT PHASE
    echo -e "${BLUE}>>> PHASE 2: ENROLLMENT${NC}"
    echo ""
    create_enrollment

    # TESTING PHASE
    echo -e "${BLUE}>>> PHASE 3: TESTING${NC}"
    echo ""
    test_text_questions
    test_choice_questions

    # REPORT PHASE
    echo -e "${BLUE}>>> PHASE 4: REPORT${NC}"
    echo ""
    generate_report

    # COMPLETION
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  TEST SUITE COMPLETED${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""

    if [[ $FAILED_COUNT -eq 0 ]]; then
        echo -e "${GREEN}✓ All tests passed successfully!${NC}"
        exit 0
    else
        echo -e "${RED}✗ Some tests failed. Please review the report.${NC}"
        exit 1
    fi
}

# Run main
main
