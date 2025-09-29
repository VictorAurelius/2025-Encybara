#!/bin/bash

# Docker build script for Content Scoring Service
set -e

echo "🐳 Building Content Scoring Service Docker Image"

# Change to the project root directory
cd "$(dirname "$0")/.."

# Build options
DOCKERFILE="Dockerfile"
IMAGE_NAME="content-scoring-service"
TAG="latest"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --optimized)
            DOCKERFILE="Dockerfile.optimized"
            TAG="optimized"
            echo "📦 Using optimized multi-stage build"
            shift
            ;;
        --tag)
            TAG="$2"
            shift 2
            ;;
        --no-cache)
            NO_CACHE="--no-cache"
            echo "🔄 Building without cache"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--optimized] [--tag TAG] [--no-cache]"
            exit 1
            ;;
    esac
done

# Verify required files exist
echo "🔍 Checking required files..."
if [ ! -f "$DOCKERFILE" ]; then
    echo "❌ $DOCKERFILE not found"
    exit 1
fi

if [ ! -f "requirements-prod.txt" ]; then
    echo "❌ requirements-prod.txt not found"
    exit 1
fi

if [ ! -f "scripts/docker-entrypoint.sh" ]; then
    echo "❌ scripts/docker-entrypoint.sh not found"
    exit 1
fi

# Build the Docker image
echo "🔨 Building Docker image: $IMAGE_NAME:$TAG"
docker build \
    $NO_CACHE \
    -f $DOCKERFILE \
    -t $IMAGE_NAME:$TAG \
    .

echo "✅ Docker image built successfully: $IMAGE_NAME:$TAG"

# Display image info
echo ""
echo "📊 Image Information:"
docker images $IMAGE_NAME:$TAG

echo ""
echo "🚀 To run the container:"
echo "  docker run -p 5001:5001 $IMAGE_NAME:$TAG"
echo ""
echo "🐙 To run with docker-compose:"
echo "  docker-compose up"