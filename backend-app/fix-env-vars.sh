#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}============================================${NC}"
echo -e "${YELLOW}  FIX ENVIRONMENT VARIABLES${NC}"
echo -e "${YELLOW}============================================${NC}"
echo ""

DEFAULT_NGROK="https://dural-rozanne-inquisitorial.ngrok-free.dev"

# Function to update environment variable in running container
update_container_env() {
    local service_name="$1"
    local env_var="$2"
    local env_value="$3"
    
    echo -e "${YELLOW}Updating $env_var in $service_name container...${NC}"
    
    # Get container ID
    container_id=$(docker-compose ps -q $service_name 2>/dev/null)
    if [[ -z "$container_id" ]]; then
        echo -e "${RED}✗ Container $service_name not found${NC}"
        return 1
    fi
    
    # Set environment variable using docker exec
    if docker exec "$container_id" bash -c "export $env_var='$env_value'" >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Updated $env_var=$env_value${NC}"
        return 0
    else
        echo -e "${RED}✗ Failed to update $env_var${NC}"
        return 1
    fi
}

# Function to restart backend service with new environment
restart_with_env() {
    local content_url="$1"
    local pronunciation_url="$2"
    
    echo -e "${YELLOW}Restarting backend with new environment variables...${NC}"
    
    # Export environment variables
    export CONTENT_SCORING_SERVICE_URL="$content_url"
    export PRONUNCIATION_SERVICE_URL="$pronunciation_url"
    
    echo "CONTENT_SCORING_SERVICE_URL=$content_url"
    echo "PRONUNCIATION_SERVICE_URL=$pronunciation_url"
    
    # Navigate to docker directory and restart
    cd ../build-docker || {
        echo -e "${RED}✗ Could not find build-docker directory${NC}"
        return 1
    }
    
    # Restart backend service
    if docker-compose restart backend; then
        echo -e "${GREEN}✓ Backend restarted successfully${NC}"
        
        # Wait for backend to be ready
        echo -n "Waiting for backend to be ready..."
        for i in {1..30}; do
            if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
                echo -e " ${GREEN}Ready!${NC}"
                return 0
            fi
            echo -n "."
            sleep 2
        done
        echo -e " ${RED}Timeout${NC}"
        return 1
    else
        echo -e "${RED}✗ Failed to restart backend${NC}"
        return 1
    fi
}

# Check if container is running
echo "Checking container status..."
cd ../build-docker 2>/dev/null || cd .
if ! docker-compose ps | grep -q "Up"; then
    echo -e "${RED}✗ Docker containers are not running${NC}"
    echo "Start containers first with: ./build.sh"
    exit 1
fi

echo -e "${GREEN}✓ Containers are running${NC}"
echo ""

# Get current configuration option
echo "Choose environment configuration:"
echo "1) Use default Ngrok URL: $DEFAULT_NGROK"
echo "2) Use custom Ngrok URL"
echo "3) Use individual service URLs"
echo "4) Check current configuration only"
echo ""

read -p "Enter choice (1-4): " choice

case $choice in
    1)
        echo -e "${YELLOW}Using default Ngrok URL: $DEFAULT_NGROK${NC}"
        restart_with_env "$DEFAULT_NGROK" "$DEFAULT_NGROK"
        ;;
    2)
        read -p "Enter your Ngrok URL: " custom_url
        if [[ -z "$custom_url" ]]; then
            echo -e "${RED}✗ No URL provided${NC}"
            exit 1
        fi
        echo -e "${YELLOW}Using custom Ngrok URL: $custom_url${NC}"
        restart_with_env "$custom_url" "$custom_url"
        ;;
    3)
        read -p "Enter Content Scoring Service URL: " content_url
        read -p "Enter Pronunciation Service URL: " pronunciation_url
        
        if [[ -z "$content_url" ]]; then
            content_url="$DEFAULT_NGROK"
            echo "Using default for content scoring: $DEFAULT_NGROK"
        fi
        
        if [[ -z "$pronunciation_url" ]]; then
            pronunciation_url="$DEFAULT_NGROK"
            echo "Using default for pronunciation: $DEFAULT_NGROK"
        fi
        
        restart_with_env "$content_url" "$pronunciation_url"
        ;;
    4)
        echo -e "${YELLOW}Current Configuration:${NC}"
        cd ../backend-app
        ./test-ngrok-connection.sh
        exit 0
        ;;
    *)
        echo -e "${RED}✗ Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${YELLOW}Testing new configuration...${NC}"
cd ../backend-app
./test-ngrok-connection.sh

echo ""
echo -e "${GREEN}Environment variables updated successfully!${NC}"
echo ""
echo "Next steps:"
echo "- Test services: ./test-content-scoring.sh"
echo "- Test pronunciation: ./test-pronunciation.sh"
echo "- Check logs: docker-compose logs -f backend"