#!/bin/bash

# Pronunciation Assessment Service - Run Script
# This script runs the Docker container for the pronunciation assessment microservice

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_NAME="pronunciation-assessment"
CONTAINER_NAME="pronunciation-assessment-container"
IMAGE_TAG="latest"
HOST_PORT="5000"
CONTAINER_PORT="5000"

echo -e "${BLUE}=== Pronunciation Assessment Service Run Script ===${NC}"
echo -e "${YELLOW}Service Name:${NC} $SERVICE_NAME"
echo -e "${YELLOW}Container Name:${NC} $CONTAINER_NAME"
echo -e "${YELLOW}Port Mapping:${NC} $HOST_PORT:$CONTAINER_PORT"
echo ""

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo -e "${RED}Error: Docker daemon is not running${NC}"
    exit 1
fi

# Check if image exists
if ! docker images -q $SERVICE_NAME:$IMAGE_TAG &> /dev/null; then
    echo -e "${RED}Error: Docker image $SERVICE_NAME:$IMAGE_TAG not found${NC}"
    echo -e "${YELLOW}Please run ./scripts/build.sh first${NC}"
    exit 1
fi

# Stop existing container if running
if docker ps -q -f name=$CONTAINER_NAME &> /dev/null; then
    echo -e "${YELLOW}Stopping existing container...${NC}"
    docker stop $CONTAINER_NAME || echo -e "${YELLOW}Warning: Could not stop container${NC}"
fi

# Remove existing container if exists
if docker ps -aq -f name=$CONTAINER_NAME &> /dev/null; then
    echo -e "${YELLOW}Removing existing container...${NC}"
    docker rm $CONTAINER_NAME || echo -e "${YELLOW}Warning: Could not remove container${NC}"
fi

echo -e "${BLUE}Starting new container...${NC}"

# Run the container
docker run -d \
    --name $CONTAINER_NAME \
    --publish $HOST_PORT:$CONTAINER_PORT \
    --memory="3g" \
    --memory-swap="3g" \
    --restart=unless-stopped \
    --env SECRET_KEY="pronunciation-$(date +%s)" \
    $SERVICE_NAME:$IMAGE_TAG || {
        echo -e "${RED}Error: Failed to start container${NC}"
        exit 1
    }

echo -e "${GREEN}✓ Container started successfully${NC}"

# Wait a moment for startup
echo -e "${YELLOW}Waiting for service to start...${NC}"
sleep 5

# Check if container is running
if docker ps -q -f name=$CONTAINER_NAME &> /dev/null; then
    echo -e "${GREEN}✓ Service is running${NC}"
    
    # Get container details
    CONTAINER_ID=$(docker ps -q -f name=$CONTAINER_NAME)
    echo -e "${YELLOW}Container ID:${NC} $CONTAINER_ID"
    
    # Test health endpoint
    echo -e "${BLUE}Testing health endpoint...${NC}"
    sleep 2
    
    if curl -s -f http://localhost:$HOST_PORT/health &> /dev/null; then
        echo -e "${GREEN}✓ Health check passed${NC}"
    else
        echo -e "${YELLOW}⚠ Health check failed (service may still be starting)${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}=== Service Started Successfully ===${NC}"
    echo -e "${YELLOW}Service Details:${NC}"
    echo -e "  URL: ${BLUE}http://localhost:$HOST_PORT${NC}"
    echo -e "  Health: ${BLUE}http://localhost:$HOST_PORT/health${NC}"
    echo -e "  API Info: ${BLUE}http://localhost:$HOST_PORT/api/info${NC}"
    echo -e "  Assessment: ${BLUE}http://localhost:$HOST_PORT/api/pronunciation-assessment${NC}"
    echo ""
    echo -e "${YELLOW}Quick Commands:${NC}"
    echo -e "  View logs: ${BLUE}docker logs -f $CONTAINER_NAME${NC}"
    echo -e "  Stop service: ${BLUE}docker stop $CONTAINER_NAME${NC}"
    echo -e "  Test service: ${BLUE}./scripts/test.sh${NC}"
    
else
    echo -e "${RED}Error: Container failed to start${NC}"
    echo -e "${YELLOW}Check logs with: docker logs $CONTAINER_NAME${NC}"
    exit 1
fi