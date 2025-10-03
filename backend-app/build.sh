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
    echo -e "${YELLOW}Unified Ngrok Service Configuration:${NC}"
    echo ""
    
    # Unified Ngrok URL
    echo -e "${YELLOW}Service Configuration Options:${NC}"
    echo -e "   ${YELLOW}1. Nhập Ngrok URL (ví dụ: https://abc123.ngrok-free.app)${NC}"
    echo -e "   ${YELLOW}2. Sử dụng localhost (http://localhost:port)${NC}"
    echo -e "   ${YELLOW}3. Để trống (test configuration errors)${NC}"
    echo -e "   ${YELLOW}(Cả 2 services sẽ dùng chung URL với ports khác nhau)${NC}"
    read -p "   Nhập Ngrok URL (hoặc ENTER để localhost, 'skip' để không set): " ngrok_base_url
    
    if [[ "$ngrok_base_url" == "skip" ]]; then
        # Don't set URLs - let application show configuration errors
        PRONUNCIATION_SERVICE_URL=""
        CONTENT_SCORING_SERVICE_URL=""
        echo -e "   ${YELLOW}Không set URLs - services sẽ báo lỗi configuration${NC}"
    elif [[ -z "$ngrok_base_url" ]]; then
        # Use localhost URLs
        PRONUNCIATION_SERVICE_URL="http://localhost:5000"
        CONTENT_SCORING_SERVICE_URL="http://localhost:5001"
        echo -e "   ${GREEN}Sử dụng localhost:${NC}"
        echo -e "   ${GREEN}  - Pronunciation: $PRONUNCIATION_SERVICE_URL${NC}"
        echo -e "   ${GREEN}  - Content Scoring: $CONTENT_SCORING_SERVICE_URL${NC}"
    else
        # Remove trailing slash if present
        ngrok_base_url=$(echo "$ngrok_base_url" | sed 's|/$||')
        
        # Use same base URL with different ports
        PRONUNCIATION_SERVICE_URL="${ngrok_base_url}:5000"
        CONTENT_SCORING_SERVICE_URL="${ngrok_base_url}:5001"
        
        echo -e "   ${GREEN}Unified Ngrok configuration:${NC}"
        echo -e "   ${GREEN}  - Pronunciation: $PRONUNCIATION_SERVICE_URL${NC}"
        echo -e "   ${GREEN}  - Content Scoring: $CONTENT_SCORING_SERVICE_URL${NC}"
    fi
    
    echo ""
    echo -e "${YELLOW}==================================================${NC}"
}

# Check for non-interactive mode (CI/CD or automated builds)
if [[ "$1" == "--auto" || -n "$CI" || -n "$GITHUB_ACTIONS" ]]; then
    echo -e "${YELLOW}Auto mode detected, using environment variables${NC}"
    
    # Check if unified NGROK_URL is provided
    if [[ -n "$NGROK_URL" ]]; then
        # Remove trailing slash if present
        NGROK_URL=$(echo "$NGROK_URL" | sed 's|/$||')
        PRONUNCIATION_SERVICE_URL="${NGROK_URL}:5000"
        CONTENT_SCORING_SERVICE_URL="${NGROK_URL}:5001"
        echo -e "${GREEN}Using unified Ngrok URL: $NGROK_URL${NC}"
    else
        # Use environment variables only, no defaults
        PRONUNCIATION_SERVICE_URL=${PRONUNCIATION_SERVICE_URL:-""}
        CONTENT_SCORING_SERVICE_URL=${CONTENT_SCORING_SERVICE_URL:-""}
        if [[ -z "$PRONUNCIATION_SERVICE_URL" && -z "$CONTENT_SCORING_SERVICE_URL" ]]; then
            echo -e "${YELLOW}No service URLs configured - services will show configuration error${NC}"
        else
            echo -e "${GREEN}Using individual service URLs from environment${NC}"
        fi
    fi
else
    # Interactive mode - prompt for URLs
    prompt_for_service_urls
fi

# Print build info
echo -e "${YELLOW}Building backend-app with configuration:${NC}"
if [[ -z "$PRONUNCIATION_SERVICE_URL" ]]; then
    echo -e "Pronunciation Service URL: ${RED}(Not configured - will show error)${NC}"
else
    echo -e "Pronunciation Service URL: ${PRONUNCIATION_SERVICE_URL}"
fi

if [[ -z "$CONTENT_SCORING_SERVICE_URL" ]]; then
    echo -e "Content Scoring Service URL: ${RED}(Not configured - will show error)${NC}"
else
    echo -e "Content Scoring Service URL: ${CONTENT_SCORING_SERVICE_URL}"
fi

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
if [[ -z "$PRONUNCIATION_SERVICE_URL" ]]; then
    echo -e "Pronunciation Service: ${RED}Not configured (will show error message)${NC}"
else
    echo -e "Pronunciation Service: ${PRONUNCIATION_SERVICE_URL}"
fi

if [[ -z "$CONTENT_SCORING_SERVICE_URL" ]]; then
    echo -e "Content Scoring Service: ${RED}Not configured (will show error message)${NC}"
else
    echo -e "Content Scoring Service: ${CONTENT_SCORING_SERVICE_URL}"
fi

# Print how to access logs
echo -e "\n${YELLOW}To view logs:${NC}"
echo "docker-compose logs -f backend"

echo -e "\n${YELLOW}Note:${NC}"
if [[ -n "$PRONUNCIATION_SERVICE_URL" || -n "$CONTENT_SCORING_SERVICE_URL" ]]; then
    echo -e "- Nếu sử dụng Ngrok URLs, đảm bảo unified ngrok tunnel đang chạy"
    echo -e "- Start unified ngrok: ./start-ngrok-services.sh"
    echo -e "- Kiểm tra health check của các services trước khi test"
    if [[ -n "$PRONUNCIATION_SERVICE_URL" ]]; then
        echo -e "- Pronunciation: ${PRONUNCIATION_SERVICE_URL}/health"
    fi
    if [[ -n "$CONTENT_SCORING_SERVICE_URL" ]]; then
        echo -e "- Content Scoring: ${CONTENT_SCORING_SERVICE_URL}/health"
    fi
else
    echo -e "- Services chưa được cấu hình - sẽ trả về error messages khi gọi API"
    echo -e "- Để test với Ngrok: export URLs rồi rebuild"
    echo -e "- Ví dụ: export NGROK_URL=https://abc.ngrok.io && ./build.sh --auto"
fi
echo ""
echo -e "${YELLOW}Unified Ngrok Info:${NC}"
echo -e "- Web Interface: http://localhost:4040"
echo -e "- Monitor: cd ngrok-service && ./monitor.sh"
echo -e "- Get URLs: cd ngrok-service && ./get-tunnel-urls.sh"