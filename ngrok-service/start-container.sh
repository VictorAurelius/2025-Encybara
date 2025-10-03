#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🐳 Starting Unified Ngrok Container Service${NC}"
echo "=============================================="

# Navigate to ngrok-service directory
cd "$(dirname "$0")"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running!${NC}"
    echo -e "${YELLOW}Please start Docker Desktop and try again.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker is running${NC}"

# Check if config file exists
if [ ! -f "ngrok.yml" ]; then
    echo -e "${RED}❌ ngrok.yml not found!${NC}"
    echo "Make sure you're running this from the ngrok-service directory"
    exit 1
fi

echo -e "${GREEN}✅ Configuration file found${NC}"

# Stop existing container if running
echo -e "${YELLOW}🛑 Stopping existing containers...${NC}"
docker-compose down > /dev/null 2>&1 || true

# Build and start the container
echo -e "${YELLOW}🔨 Building ngrok container...${NC}"
docker-compose build --no-cache

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to build container${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Container built successfully${NC}"

# Start the service
echo -e "${YELLOW}🚀 Starting ngrok service...${NC}"
docker-compose up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to start container${NC}"
    exit 1
fi

# Wait for service to be ready
echo -e "${BLUE}⏳ Waiting for service to initialize...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
        break
    fi
    printf "."
    sleep 1
done
echo ""

# Check if service is running
if curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Ngrok container started successfully!${NC}"
    echo ""
    echo -e "${BLUE}🌐 Container Information:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    echo -e "  📦 Container: ${GREEN}unified-ngrok-service${NC}"
    echo -e "  🌐 Web Interface: ${BLUE}http://localhost:4040${NC}"
    echo -e "  📊 API Endpoint: ${BLUE}http://localhost:4040/api/tunnels${NC}"
    
    # Get tunnel information
    echo ""
    echo -e "${BLUE}🔗 Active Tunnels:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    
    TUNNELS=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null)
    if command -v jq >/dev/null 2>&1; then
        echo "$TUNNELS" | jq -r '.tunnels[] | "  🌐 \(.name): \(.public_url)"'
    else
        echo "$TUNNELS" | grep -o '"name":"[^"]*"' | sed 's/"name":"//g' | sed 's/"//g' | while read name; do
            url=$(echo "$TUNNELS" | grep -A10 "\"name\":\"$name\"" | grep -o '"public_url":"https://[^"]*"' | head -1 | sed 's/"public_url":"//g' | sed 's/"//g')
            if [ -n "$url" ]; then
                echo "  🌐 $name: $url"
            fi
        done
    fi
    
    echo ""
    echo -e "${BLUE}🔧 Container Management:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    echo -e "  📋 View logs: ${YELLOW}docker-compose logs -f${NC}"
    echo -e "  🛑 Stop service: ${YELLOW}docker-compose down${NC}"
    echo -e "  🔄 Restart: ${YELLOW}docker-compose restart${NC}"
    echo -e "  📊 Status: ${YELLOW}docker-compose ps${NC}"
    
    echo ""
    echo -e "${GREEN}🎉 Unified ngrok service is running in container!${NC}"
    echo -e "${BLUE}Services mapped:${NC}"
    echo -e "  • pronunciation-assessment → port 5000"
    echo -e "  • content-scoring → port 5001"
    
else
    echo -e "${RED}❌ Failed to start ngrok service${NC}"
    echo -e "${YELLOW}📋 Check logs: docker-compose logs${NC}"
    exit 1
fi