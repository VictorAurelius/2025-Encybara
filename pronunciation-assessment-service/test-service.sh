#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Check if service is running
echo "Testing pronunciation assessment service..."
response=$(curl -s -w "%{http_code}" http://localhost:5000/health)
status=${response: -3}
body=${response:0:${#response}-3}

if [ "$status" == "200" ]; then
    echo -e "${GREEN}✓ Service is running${NC}"
else
    echo -e "${RED}✗ Service is not running${NC}"
    exit 1
fi

# Test audio file assessment
echo -e "\nTesting audio file assessment..."

# Create test audio file if not exists
if [ ! -f "test.wav" ]; then
    echo "Creating test audio file..."
    # Generate 1 second of silence as test audio
    ffmpeg -f lavfi -i anullsrc=r=44100:cl=mono -t 1 -q:a 9 -acodec pcm_s16le test.wav >/dev/null 2>&1
fi

# Test audio file upload
response=$(curl -s -w "%{http_code}" -X POST \
    -F "file=@test.wav" \
    http://localhost:5000/pronunciation/assess)
status=${response: -3}
body=${response:0:${#response}-3}

if [ "$status" == "200" ]; then
    echo -e "${GREEN}✓ Audio assessment endpoint working${NC}"
    echo "Response: $body"
else
    echo -e "${RED}✗ Audio assessment failed${NC}"
    echo "Status: $status"
    echo "Response: $body"
    exit 1
fi

# Clean up
rm -f test.wav

echo -e "\n${GREEN}All tests passed successfully!${NC}"