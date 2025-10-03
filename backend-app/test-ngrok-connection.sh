#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  NGROK CONNECTION TEST${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""

# Get current environment variables from host and container
HOST_CONTENT_SCORING_URL=${CONTENT_SCORING_SERVICE_URL}
HOST_PRONUNCIATION_URL=${PRONUNCIATION_SERVICE_URL}

# Try to get environment variables from running container
CONTAINER_CONTENT_URL=""
CONTAINER_PRONUNCIATION_URL=""

cd ../build-docker 2>/dev/null || cd .
backend_container=$(docker-compose ps -q backend 2>/dev/null)
if [[ -n "$backend_container" ]]; then
    CONTAINER_CONTENT_URL=$(docker exec "$backend_container" printenv CONTENT_SCORING_SERVICE_URL 2>/dev/null || echo "")
    CONTAINER_PRONUNCIATION_URL=$(docker exec "$backend_container" printenv PRONUNCIATION_SERVICE_URL 2>/dev/null || echo "")
fi
cd ../backend-app 2>/dev/null || cd .

echo -e "${YELLOW}Current Environment Variables:${NC}"
echo -e "Host - CONTENT_SCORING_SERVICE_URL: ${HOST_CONTENT_SCORING_URL:-'(Not set)'}"
echo -e "Host - PRONUNCIATION_SERVICE_URL: ${HOST_PRONUNCIATION_URL:-'(Not set)'}"
echo -e "Container - CONTENT_SCORING_SERVICE_URL: ${CONTAINER_CONTENT_URL:-'(Not set)'}"
echo -e "Container - PRONUNCIATION_SERVICE_URL: ${CONTAINER_PRONUNCIATION_URL:-'(Not set)'}"

# Use container values if available, otherwise host values
CONTENT_SCORING_URL=${CONTAINER_CONTENT_URL:-$HOST_CONTENT_SCORING_URL}
PRONUNCIATION_URL=${CONTAINER_PRONUNCIATION_URL:-$HOST_PRONUNCIATION_URL}
echo ""
echo -e "${YELLOW}Default Ngrok URL: https://dural-rozanne-inquisitorial.ngrok-free.dev${NC}"
echo ""

# Test if Ngrok URLs are accessible
if [[ -n "$CONTENT_SCORING_URL" ]]; then
    echo -e "${YELLOW}Testing Content Scoring Service URL...${NC}"
    echo -e "URL: $CONTENT_SCORING_URL"
    
    # Test basic connectivity
    echo -n "  Connectivity test: "
    if curl -s --max-time 10 "$CONTENT_SCORING_URL" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Connected${NC}"
    else
        echo -e "${RED}✗ Failed to connect${NC}"
    fi
    
    # Test health endpoint
    echo -n "  Health endpoint: "
    health_response=$(curl -s --max-time 10 "$CONTENT_SCORING_URL/health" 2>/dev/null)
    if [[ $? -eq 0 && -n "$health_response" ]]; then
        echo -e "${GREEN}✓ Health OK${NC}"
        echo "    Response: $health_response"
    else
        echo -e "${RED}✗ Health check failed${NC}"
    fi
    
    # Test API endpoint
    echo -n "  API endpoint: "
    api_response=$(curl -s --max-time 10 -X POST "$CONTENT_SCORING_URL/api/content-scoring" \
        -H "Content-Type: application/json" \
        -d '{"question":"test","answer":"test"}' 2>/dev/null)
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✓ API accessible${NC}"
        echo "    Response: ${api_response:0:100}..."
    else
        echo -e "${RED}✗ API not accessible${NC}"
    fi
    echo ""
fi

if [[ -n "$PRONUNCIATION_URL" ]]; then
    echo -e "${YELLOW}Testing Pronunciation Service URL...${NC}"
    echo -e "URL: $PRONUNCIATION_URL"
    
    # Test basic connectivity
    echo -n "  Connectivity test: "
    if curl -s --max-time 10 "$PRONUNCIATION_URL" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Connected${NC}"
    else
        echo -e "${RED}✗ Failed to connect${NC}"
    fi
    
    # Test health endpoint
    echo -n "  Health endpoint: "
    health_response=$(curl -s --max-time 10 "$PRONUNCIATION_URL/health" 2>/dev/null)
    if [[ $? -eq 0 && -n "$health_response" ]]; then
        echo -e "${GREEN}✓ Health OK${NC}"
        echo "    Response: $health_response"
    else
        echo -e "${RED}✗ Health check failed${NC}"
    fi
    echo ""
fi

# Test via Backend-App if it's running
echo -e "${YELLOW}Testing via Backend-App...${NC}"
backend_health=$(curl -s --max-time 5 "http://localhost:8080/actuator/health" 2>/dev/null)
if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✓ Backend-App is running${NC}"
    
    # Check what URLs the backend is actually using
    echo ""
    echo -e "${YELLOW}Backend Configuration:${NC}"
    
    # Test content scoring service info endpoint to see actual URL
    echo -n "  Content Scoring Service: "
    CONTENT_INFO=$(curl -s --max-time 10 "http://localhost:8080/api/v1/content-scoring/info" 2>/dev/null)
    if [[ $? -eq 0 && -n "$CONTENT_INFO" ]]; then
        SERVICE_URL=$(echo "$CONTENT_INFO" | grep -o '"service_url":"[^"]*"' | sed 's/"service_url":"\([^"]*\)"/\1/')
        if [[ -n "$SERVICE_URL" ]]; then
            echo -e "${GREEN}$SERVICE_URL${NC}"
        else
            echo -e "${RED}Could not parse service URL${NC}"
            echo "    Response: $CONTENT_INFO"
        fi
    else
        echo -e "${RED}Could not get service info${NC}"
    fi
    
    # Test pronunciation service info endpoint to see actual URL
    echo -n "  Pronunciation Service: "
    PRONUNCIATION_INFO=$(curl -s --max-time 10 "http://localhost:8080/api/v1/pronunciation/info" 2>/dev/null)
    if [[ $? -eq 0 && -n "$PRONUNCIATION_INFO" ]]; then
        SERVICE_URL=$(echo "$PRONUNCIATION_INFO" | grep -o '"service_url":"[^"]*"' | sed 's/"service_url":"\([^"]*\)"/\1/')
        if [[ -n "$SERVICE_URL" ]]; then
            echo -e "${GREEN}$SERVICE_URL${NC}"
        else
            echo -e "${RED}Could not parse service URL${NC}"
            echo "    Response: $PRONUNCIATION_INFO"
        fi
    else
        echo -e "${RED}Could not get service info${NC}"
    fi
    
    echo ""
    
    # Test content scoring via backend
    echo -n "  Content scoring health: "
    backend_content=$(curl -s --max-time 10 "http://localhost:8080/api/v1/content-scoring/health" 2>/dev/null)
    if [[ $? -eq 0 ]]; then
        echo -e "${GREEN}✓ Accessible${NC}"
    else
        echo -e "${RED}✗ Not accessible${NC}"
    fi
    
    # Test pronunciation via backend  
    if [[ -n "$PRONUNCIATION_URL" ]]; then
        echo -n "  Pronunciation via backend: "
        # Create a small test audio file
        echo "test" | text2wav > /tmp/test.wav 2>/dev/null || echo "test audio" > /tmp/test.wav
        backend_pronunciation=$(curl -s --max-time 10 -X POST "http://localhost:8080/api/v1/pronunciation/assess" \
            -F "file=@/tmp/test.wav" 2>/dev/null)
        if [[ $? -eq 0 ]]; then
            echo -e "${GREEN}✓ Accessible${NC}"
        else
            echo -e "${RED}✗ Not accessible${NC}"
        fi
        rm -f /tmp/test.wav
    fi
else
    echo -e "${RED}✗ Backend-App is not running${NC}"
fi

echo ""
echo -e "${YELLOW}Recommendations:${NC}"
if [[ -z "$CONTENT_SCORING_URL" && -z "$PRONUNCIATION_URL" ]]; then
    echo -e "- Set environment variables: export NGROK_URL=https://your-ngrok-url.ngrok.io"
    echo -e "- Then rebuild: ./build.sh --auto"
elif [[ -n "$CONTENT_SCORING_URL" || -n "$PRONUNCIATION_URL" ]]; then
    echo -e "- Check if Ngrok tunnel is still active"
    echo -e "- Verify local services are running on correct ports"
    echo -e "- Try regenerating Ngrok tunnel if connections fail"
fi

echo -e "- Check Ngrok status: curl http://localhost:4040/api/tunnels"
echo -e "- Restart services if needed: docker-compose restart"