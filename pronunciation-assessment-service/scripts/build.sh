#!/bin/bash

# Pronunciation Assessment Service - Build Script
# This script builds the Docker image for the pronunciation assessment microservice

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_NAME="pronunciation-assessment"
IMAGE_TAG="latest"
DOCKERFILE_PATH="../Dockerfile"

echo -e "${BLUE}=== Pronunciation Assessment Service Build Script ===${NC}"
echo -e "${YELLOW}Service Name:${NC} $SERVICE_NAME"
echo -e "${YELLOW}Image Tag:${NC} $IMAGE_TAG"
echo ""

# Check if Docker is installed and running
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed or not in PATH${NC}"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}Error: Docker daemon is not running${NC}"
    exit 1
fi

# Navigate to project root
cd "$(dirname "$0")/.."

echo -e "${BLUE}Step 1: Cleaning up old images (optional)${NC}"
if docker images -q $SERVICE_NAME &> /dev/null; then
    read -p "Remove existing Docker image? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker rmi $SERVICE_NAME:$IMAGE_TAG || echo -e "${YELLOW}Warning: Could not remove old image${NC}"
    fi
fi

echo -e "${BLUE}Step 2: Building Docker image...${NC}"
echo -e "${YELLOW}This may take several minutes for first build (downloading MFA models)${NC}"

# Build Docker image with progress
docker build \
    --tag $SERVICE_NAME:$IMAGE_TAG \
    --file Dockerfile \
    --progress=plain \
    . || {
        echo -e "${RED}Error: Docker build failed${NC}"
        exit 1
    }

echo -e "${GREEN}✓ Docker image built successfully${NC}"

# Verify the image
echo -e "${BLUE}Step 3: Verifying image...${NC}"
if docker images -q $SERVICE_NAME:$IMAGE_TAG &> /dev/null; then
    IMAGE_SIZE=$(docker images $SERVICE_NAME:$IMAGE_TAG --format "table {{.Size}}" | tail -n 1)
    echo -e "${GREEN}✓ Image verification successful${NC}"
    echo -e "${YELLOW}Image Size:${NC} $IMAGE_SIZE"
else
    echo -e "${RED}Error: Image verification failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}=== Build Completed Successfully ===${NC}"
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Run the service: ${BLUE}./scripts/run.sh${NC}"
echo -e "  2. Test the service: ${BLUE}./scripts/test.sh${NC}"
echo -e "  3. Check logs: ${BLUE}docker logs pronunciation-assessment-container${NC}"
echo ""
echo -e "${YELLOW}Image Details:${NC}"
echo -e "  Name: $SERVICE_NAME:$IMAGE_TAG"
echo -e "  Size: $IMAGE_SIZE"
echo -e "  Built: $(date)"