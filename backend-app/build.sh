#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Function to prompt for service URLs
prompt_for_service_urls() {
    echo -e "${YELLOW}==================================================${NC}"
    echo -e "${YELLOW}  BACKEND-APP SERVICE CONFIGURATION${NC}"
    echo -e "${YELLOW}==================================================${NC}"
    echo ""
    echo -e "${YELLOW}Vui lòng nhập public URL của các microservices:${NC}"
    echo ""
    
    # Pronunciation Assessment Service URL
    echo -e "${YELLOW}1. Pronunciation Assessment Service URL:${NC}"
    echo -e "   ${YELLOW}Ví dụ: https://abc123.ngrok-free.app${NC}"
    echo -e "   ${YELLOW}Hoặc để trống để sử dụng localhost (http://localhost:5000)${NC}"
    read -p "   Nhập URL: " pronunciation_url
    
    if [[ -z "$pronunciation_url" ]]; then
        PRONUNCIATION_SERVICE_URL="http://localhost:5000"
        echo -e "   ${GREEN}Sử dụng localhost: $PRONUNCIATION_SERVICE_URL${NC}"
    else
        # Remove trailing slash if present
        pronunciation_url=$(echo "$pronunciation_url" | sed 's|/$||')
        PRONUNCIATION_SERVICE_URL="$pronunciation_url"
        echo -e "   ${GREEN}Đã thiết lập: $PRONUNCIATION_SERVICE_URL${NC}"
    fi
    
    echo ""
    
    # Content Scoring Service URL
    echo -e "${YELLOW}2. Content Scoring Service URL:${NC}"
    echo -e "   ${YELLOW}Ví dụ: https://def456.ngrok-free.app${NC}"
    echo -e "   ${YELLOW}Hoặc để trống để sử dụng localhost (http://localhost:5001)${NC}"
    read -p "   Nhập URL: " content_scoring_url
    
    if [[ -z "$content_scoring_url" ]]; then
        CONTENT_SCORING_SERVICE_URL="http://localhost:5001"
        echo -e "   ${GREEN}Sử dụng localhost: $CONTENT_SCORING_SERVICE_URL${NC}"
    else
        # Remove trailing slash if present
        content_scoring_url=$(echo "$content_scoring_url" | sed 's|/$||')
        CONTENT_SCORING_SERVICE_URL="$content_scoring_url"
        echo -e "   ${GREEN}Đã thiết lập: $CONTENT_SCORING_SERVICE_URL${NC}"
    fi
    
    echo ""
    echo -e "${YELLOW}==================================================${NC}"
}

# Check for non-interactive mode (CI/CD or automated builds)
if [[ "$1" == "--auto" || -n "$CI" || -n "$GITHUB_ACTIONS" ]]; then
    echo -e "${YELLOW}Auto mode detected, using environment variables or defaults${NC}"
    PRONUNCIATION_SERVICE_URL=${PRONUNCIATION_SERVICE_URL:-"http://localhost:5000"}
    CONTENT_SCORING_SERVICE_URL=${CONTENT_SCORING_SERVICE_URL:-"http://localhost:5001"}
else
    # Interactive mode - prompt for URLs
    prompt_for_service_urls
fi

# Print build info
echo -e "${YELLOW}Building backend-app with configuration:${NC}"
echo -e "Pronunciation Service URL: ${PRONUNCIATION_SERVICE_URL}"
echo -e "Content Scoring Service URL: ${CONTENT_SCORING_SERVICE_URL}"

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
export CONTENT_SCORING_SERVICE_URL

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
echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}  SERVICE URLS${NC}"
echo -e "${GREEN}==================================================${NC}"
echo -e "Backend API URL: http://localhost:8080"
echo -e "Swagger UI: http://localhost:8080/swagger-ui.html"
echo -e "API Docs: http://localhost:8080/v3/api-docs"
echo ""
echo -e "${YELLOW}Configured Microservices:${NC}"
echo -e "Pronunciation Service: ${PRONUNCIATION_SERVICE_URL}"
echo -e "Content Scoring Service: ${CONTENT_SCORING_SERVICE_URL}"

# Print how to access logs
echo -e "\n${YELLOW}To view logs:${NC}"
echo "docker-compose logs -f backend"

echo -e "\n${YELLOW}Note:${NC}"
echo -e "- Nếu sử dụng Ngrok URLs, đảm bảo các tunnel đang chạy"
echo -e "- Kiểm tra health check của các services trước khi test"
echo -e "- Pronunciation: ${PRONUNCIATION_SERVICE_URL}/health"
echo -e "- Content Scoring: ${CONTENT_SCORING_SERVICE_URL}/health"