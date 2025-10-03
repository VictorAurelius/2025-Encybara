#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  REBUILD BACKEND CONTAINER${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""

# Check if containers are running
cd ../build-docker || {
    echo -e "${RED}✗ Could not find build-docker directory${NC}"
    exit 1
}

echo -e "${YELLOW}Stopping backend container...${NC}"
if docker-compose stop backend; then
    echo -e "${GREEN}✓ Backend stopped${NC}"
else
    echo -e "${RED}✗ Failed to stop backend${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Rebuilding backend container...${NC}"
if docker-compose build --no-cache backend; then
    echo -e "${GREEN}✓ Backend rebuilt successfully${NC}"
else
    echo -e "${RED}✗ Failed to rebuild backend${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Starting backend container...${NC}"
if docker-compose up -d backend; then
    echo -e "${GREEN}✓ Backend started${NC}"
else
    echo -e "${RED}✗ Failed to start backend${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Waiting for backend to be ready...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend is ready!${NC}"
        break
    fi
    echo -n "."
    sleep 2
    if [[ $i -eq 30 ]]; then
        echo -e " ${RED}Timeout${NC}"
        echo "Check logs with: docker-compose logs -f backend"
        exit 1
    fi
done

echo ""
echo -e "${GREEN}Backend rebuilt and ready!${NC}"
echo ""
echo "Testing configuration..."
cd ../backend-app
./test-ngrok-connection.sh