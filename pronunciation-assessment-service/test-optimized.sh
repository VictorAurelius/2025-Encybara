#!/bin/bash

# Test script for optimized pronunciation assessment service

echo "=========================================="
echo "Testing Pronunciation Assessment Service"
echo "Version: 1.0.0-optimized"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SERVICE_URL="${SERVICE_URL:-http://localhost:5000}"

# Test 1: Health check
echo "Test 1: Health check"
echo "-------------------"
response=$(curl -s "${SERVICE_URL}/health")
if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ Health check passed${NC}"
    echo "Response: $response"
else
    echo -e "${RED}✗ Health check failed${NC}"
    exit 1
fi
echo ""

# Test 2: Service info
echo "Test 2: Service info"
echo "-------------------"
response=$(curl -s "${SERVICE_URL}/api/info")
if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ Service info passed${NC}"
    echo "Response: $response" | python3 -m json.tool 2>/dev/null || echo "$response"
else
    echo -e "${RED}✗ Service info failed${NC}"
    exit 1
fi
echo ""

# Test 3: Root endpoint
echo "Test 3: Root endpoint"
echo "-------------------"
response=$(curl -s "${SERVICE_URL}/")
if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ Root endpoint passed${NC}"
    echo "Response: $response"
else
    echo -e "${RED}✗ Root endpoint failed${NC}"
    exit 1
fi
echo ""

# Test 4: Assessment endpoint (if test audio exists)
if [ -f "tests/sample_audio.wav" ] || [ -f "test.wav" ]; then
    echo "Test 4: Pronunciation assessment"
    echo "-------------------------------"

    AUDIO_FILE=""
    if [ -f "tests/sample_audio.wav" ]; then
        AUDIO_FILE="tests/sample_audio.wav"
    elif [ -f "test.wav" ]; then
        AUDIO_FILE="test.wav"
    fi

    echo -e "${YELLOW}Testing with audio file: $AUDIO_FILE${NC}"
    echo "Transcript: 'hello world'"
    echo ""

    start_time=$(date +%s)

    response=$(curl -s -X POST "${SERVICE_URL}/api/pronunciation-assessment" \
        -F "audio=@${AUDIO_FILE}" \
        -F "transcript=hello world")

    end_time=$(date +%s)
    duration=$((end_time - start_time))

    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✓ Assessment passed${NC}"
        echo "Response time: ${duration}s"
        echo "Response: $response" | python3 -m json.tool 2>/dev/null || echo "$response"

        # Check if response time is improved
        if [ $duration -lt 15 ]; then
            echo -e "${GREEN}✓ Performance OK (<15s)${NC}"
        elif [ $duration -lt 20 ]; then
            echo -e "${YELLOW}⚠ Performance acceptable (15-20s)${NC}"
        else
            echo -e "${YELLOW}⚠ Performance slow (>20s) - may need further optimization${NC}"
        fi
    else
        echo -e "${RED}✗ Assessment failed${NC}"
    fi
    echo ""
else
    echo -e "${YELLOW}⚠ Test 4: Skipped (no test audio file found)${NC}"
    echo "Create a test.wav file or tests/sample_audio.wav to test assessment"
    echo ""
fi

echo "=========================================="
echo "All basic tests completed!"
echo "=========================================="
