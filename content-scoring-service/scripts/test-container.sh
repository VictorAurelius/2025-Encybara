#!/bin/bash

# Container testing script for Content Scoring Service
set -e

echo "🧪 Testing Content Scoring Service Container"

# Configuration
IMAGE_NAME="content-scoring-service"
TAG="latest"
CONTAINER_NAME="content-scoring-test"
PORT="5001"
TIMEOUT=60

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --optimized)
            TAG="optimized"
            echo "🔬 Testing optimized image"
            shift
            ;;
        --tag)
            TAG="$2"
            shift 2
            ;;
        --port)
            PORT="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--optimized] [--tag TAG] [--port PORT]"
            exit 1
            ;;
    esac
done

# Function to cleanup
cleanup() {
    echo "🧹 Cleaning up test container..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
}

# Set trap for cleanup
trap cleanup EXIT

# Check if image exists
echo "🔍 Checking if image exists: $IMAGE_NAME:$TAG"
if ! docker images $IMAGE_NAME:$TAG | grep -q $TAG; then
    echo "❌ Image $IMAGE_NAME:$TAG not found. Please build it first:"
    echo "   ./scripts/build-docker.sh $([ "$TAG" = "optimized" ] && echo "--optimized")"
    exit 1
fi

# Stop any existing test container
cleanup

# Start the container
echo "🚀 Starting test container: $CONTAINER_NAME"
docker run -d \
    --name $CONTAINER_NAME \
    -p $PORT:5001 \
    $IMAGE_NAME:$TAG

# Wait for container to be ready
echo "⏳ Waiting for container to be ready (timeout: ${TIMEOUT}s)..."
for i in $(seq 1 $TIMEOUT); do
    if curl -f http://localhost:$PORT/health >/dev/null 2>&1; then
        echo "✅ Container is ready after ${i}s"
        break
    fi
    if [ $i -eq $TIMEOUT ]; then
        echo "❌ Container failed to start within ${TIMEOUT}s"
        echo "📝 Container logs:"
        docker logs $CONTAINER_NAME
        exit 1
    fi
    sleep 1
done

# Run health checks
echo "🏥 Running health checks..."

# Test health endpoint
echo "  ✓ Testing health endpoint..."
if ! curl -f http://localhost:$PORT/health; then
    echo "❌ Health check failed"
    exit 1
fi

# Test metrics endpoint
echo "  ✓ Testing metrics endpoint..."
if ! curl -f http://localhost:$PORT/metrics >/dev/null 2>&1; then
    echo "❌ Metrics endpoint failed"
    exit 1
fi

# Test API documentation
echo "  ✓ Testing API docs..."
if ! curl -f http://localhost:$PORT/docs >/dev/null 2>&1; then
    echo "❌ API docs failed"
    exit 1
fi

# Test a simple API call
echo "  ✓ Testing API endpoint..."
RESPONSE=$(curl -s -X POST http://localhost:$PORT/api/content-scoring \
    -H "Content-Type: application/json" \
    -d '{
        "question": "What is machine learning?",
        "answer": "Machine learning is a field of AI."
    }')

if echo "$RESPONSE" | grep -q "success"; then
    echo "✅ API endpoint test passed"
else
    echo "❌ API endpoint test failed"
    echo "Response: $RESPONSE"
    exit 1
fi

# Show container info
echo ""
echo "📊 Container Information:"
docker stats $CONTAINER_NAME --no-stream

echo ""
echo "✅ All tests passed! Container is working correctly."
echo "🔗 Access the service at: http://localhost:$PORT"
echo "📚 API Documentation: http://localhost:$PORT/docs"
echo "💓 Health Check: http://localhost:$PORT/health"
echo "📊 Metrics: http://localhost:$PORT/metrics"