#!/bin/bash

# Pronunciation Assessment Service - Test Script
# This script tests the pronunciation assessment microservice endpoints

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_URL="http://localhost:5000"
TEST_AUDIO_URL="https://www2.cs.uic.edu/~i101/SoundFiles/CantinaBand3.wav"
TEST_TRANSCRIPT="hello world this is a test"

echo -e "${BLUE}=== Pronunciation Assessment Service Test Script ===${NC}"
echo -e "${YELLOW}Service URL:${NC} $SERVICE_URL"
echo ""

# Function to test endpoint
test_endpoint() {
    local endpoint=$1
    local method=$2
    local description=$3
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo -e "${YELLOW}Endpoint:${NC} $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" $SERVICE_URL$endpoint)
        http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
        body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    else
        echo -e "${YELLOW}Method $method not implemented in basic test${NC}"
        return
    fi
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✓ Success (HTTP $http_code)${NC}"
        echo -e "${YELLOW}Response:${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}✗ Failed (HTTP $http_code)${NC}"
        echo -e "${YELLOW}Response:${NC}"
        echo "$body"
    fi
    echo ""
}

# Function to check if service is running
check_service() {
    echo -e "${BLUE}Checking if service is running...${NC}"
    if curl -s -f $SERVICE_URL/health &> /dev/null; then
        echo -e "${GREEN}✓ Service is running${NC}"
        return 0
    else
        echo -e "${RED}✗ Service is not responding${NC}"
        echo -e "${YELLOW}Please ensure the service is running with: ./scripts/run.sh${NC}"
        return 1
    fi
}

# Function to test pronunciation assessment endpoint
test_pronunciation_assessment() {
    echo -e "${BLUE}Testing: Pronunciation Assessment Endpoint${NC}"
    
    # Create a simple test WAV file using sox if available
    if command -v sox &> /dev/null; then
        echo -e "${YELLOW}Generating test audio file...${NC}"
        sox -n -r 16000 -c 1 test_audio.wav synth 2.0 sine 440 vol 0.5
        
        if [ -f "test_audio.wav" ]; then
            echo -e "${YELLOW}Sending pronunciation assessment request...${NC}"
            
            response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
                -X POST \
                -F "audio=@test_audio.wav" \
                -F "transcript=$TEST_TRANSCRIPT" \
                $SERVICE_URL/api/pronunciation-assessment)
            
            http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
            body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
            
            if [ "$http_code" -eq 200 ]; then
                echo -e "${GREEN}✓ Pronunciation assessment successful (HTTP $http_code)${NC}"
                echo -e "${YELLOW}Response:${NC}"
                echo "$body" | jq '.' 2>/dev/null || echo "$body"
            else
                echo -e "${YELLOW}⚠ Assessment response (HTTP $http_code):${NC}"
                echo "$body"
            fi
            
            # Cleanup
            rm -f test_audio.wav
        else
            echo -e "${YELLOW}⚠ Could not generate test audio file${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ Sox not available - skipping audio generation test${NC}"
        echo -e "${YELLOW}You can test manually with:${NC}"
        echo -e "${BLUE}curl -X POST -F \"audio=@your_audio.wav\" -F \"transcript=hello world\" $SERVICE_URL/api/pronunciation-assessment${NC}"
    fi
    echo ""
}

# Function to run comprehensive tests
run_comprehensive_tests() {
    echo -e "${BLUE}=== Running Comprehensive Tests ===${NC}"
    echo ""
    
    # Test 1: Health check
    test_endpoint "/health" "GET" "Health Check Endpoint"
    
    # Test 2: Root endpoint
    test_endpoint "/" "GET" "Root Endpoint"
    
    # Test 3: Service info
    test_endpoint "/api/info" "GET" "Service Info Endpoint"
    
    # Test 4: Pronunciation assessment
    test_pronunciation_assessment
    
    echo -e "${GREEN}=== Test Summary ===${NC}"
    echo -e "${YELLOW}All basic endpoint tests completed${NC}"
    echo ""
}

# Function to show manual test examples
show_manual_examples() {
    echo -e "${BLUE}=== Manual Testing Examples ===${NC}"
    echo ""
    
    echo -e "${YELLOW}1. Basic health check:${NC}"
    echo -e "${BLUE}curl $SERVICE_URL/health${NC}"
    echo ""
    
    echo -e "${YELLOW}2. Service information:${NC}"
    echo -e "${BLUE}curl $SERVICE_URL/api/info${NC}"
    echo ""
    
    echo -e "${YELLOW}3. Pronunciation assessment (replace with your audio file):${NC}"
    echo -e "${BLUE}curl -X POST \\${NC}"
    echo -e "${BLUE}  -F \"audio=@your_audio.wav\" \\${NC}"
    echo -e "${BLUE}  -F \"transcript=hello world\" \\${NC}"
    echo -e "${BLUE}  $SERVICE_URL/api/pronunciation-assessment${NC}"
    echo ""
    
    echo -e "${YELLOW}4. Test with invalid file:${NC}"
    echo -e "${BLUE}curl -X POST \\${NC}"
    echo -e "${BLUE}  -F \"audio=@invalid.txt\" \\${NC}"
    echo -e "${BLUE}  -F \"transcript=test\" \\${NC}"
    echo -e "${BLUE}  $SERVICE_URL/api/pronunciation-assessment${NC}"
    echo ""
    
    echo -e "${YELLOW}5. Test without transcript:${NC}"
    echo -e "${BLUE}curl -X POST \\${NC}"
    echo -e "${BLUE}  -F \"audio=@test.wav\" \\${NC}"
    echo -e "${BLUE}  $SERVICE_URL/api/pronunciation-assessment${NC}"
    echo ""
}

# Main execution
echo -e "${YELLOW}Checking prerequisites...${NC}"

# Check if curl is available
if ! command -v curl &> /dev/null; then
    echo -e "${RED}Error: curl is required but not installed${NC}"
    exit 1
fi

# Check if jq is available (optional, for pretty JSON)
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}Note: jq not found - JSON responses will not be formatted${NC}"
fi

# Check service availability
if ! check_service; then
    exit 1
fi

echo ""

# Parse command line arguments
case "${1:-comprehensive}" in
    "comprehensive"|"all")
        run_comprehensive_tests
        ;;
    "health")
        test_endpoint "/health" "GET" "Health Check Only"
        ;;
    "info")
        test_endpoint "/api/info" "GET" "Service Info Only"
        ;;
    "manual")
        show_manual_examples
        ;;
    "assessment")
        test_pronunciation_assessment
        ;;
    *)
        echo -e "${YELLOW}Usage: $0 [comprehensive|health|info|assessment|manual]${NC}"
        echo -e "${YELLOW}Default: comprehensive${NC}"
        echo ""
        show_manual_examples
        ;;
esac

echo -e "${GREEN}Testing completed!${NC}"