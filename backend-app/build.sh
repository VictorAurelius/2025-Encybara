#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
PRONUNCIATION_SERVICE_URL=${PRONUNCIATION_SERVICE_URL:-"http://localhost:5000"}

# Print build info
echo -e "${YELLOW}Building backend-app with configuration:${NC}"
echo -e "Pronunciation Service URL: ${PRONUNCIATION_SERVICE_URL}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running${NC}"
    exit 1
fi

# Build the application
echo -e "\n${YELLOW}Building application...${NC}"

# Navigate to directory containing docker-compose.yml
cd ../build-docker

# Export environment variables
export PRONUNCIATION_SERVICE_URL

# Build and start the services
docker-compose down
docker-compose build --no-cache backend
docker-compose up -d

# Wait for backend to be healthy
echo -e "\n${YELLOW}Waiting for backend service to be healthy...${NC}"
attempt=1
max_attempts=30

while [ $attempt -le $max_attempts ]; do
    status=$(docker-compose ps backend | grep healthy || echo "")
    if [[ $status == *"healthy"* ]]; then
        echo -e "${GREEN}Backend service is healthy!${NC}"
        break
    fi
    echo -n "."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "\n${RED}Error: Backend service failed to become healthy${NC}"
    docker-compose logs backend
    exit 1
fi

# Print service URLs
echo -e "\n${GREEN}Build completed successfully!${NC}"
echo -e "Backend API URL: http://localhost:8080"
echo -e "Swagger UI: http://localhost:8080/swagger-ui.html"
echo -e "API Docs: http://localhost:8080/v3/api-docs"

# Print how to access logs
echo -e "\n${YELLOW}To view logs:${NC}"
echo "docker-compose logs -f backend"