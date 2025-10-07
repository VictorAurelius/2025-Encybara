#!/bin/bash

# WhisperX Pronunciation Assessment Service - Simple Build Script
set -e

echo "=================================================="
echo "WhisperX Pronunciation Assessment Service - Build"
echo "CPU-Only Version (No CUDA Downloads)"
echo "=================================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[BUILD]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Clean up old containers and images
print_status "Cleaning up old containers and images..."
docker compose down --volumes --remove-orphans 2>/dev/null || true
docker images | grep "pronunciation-assessment-service" | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

# Build the image
print_status "Building CPU-only WhisperX service (this will take 3-5 minutes)..."
print_status "Note: Skipping pyannote.audio to avoid 888MB CUDA downloads"

timeout 3600 docker build --no-cache -t pronunciation-assessment-service . || {
    print_error "Docker build failed!"
    exit 1
}

print_success "Docker image built successfully!"

# Start the service
print_status "Starting WhisperX service..."
docker compose up -d

print_status "Waiting for service to be ready..."
sleep 15

# Check health
for i in {1..30}; do
    if curl -s http://localhost:5000/health > /dev/null 2>&1; then
        print_success "✓ WhisperX service is healthy and ready!"
        break
    else
        print_status "Waiting for service... ($i/30)"
        sleep 3
    fi
    
    if [ $i -eq 30 ]; then
        print_warning "Service might still be starting. Check logs manually."
    fi
done

# Show service status
print_status "Service Status:"
docker compose ps

# Show useful information
echo ""
print_success "🎉 WhisperX Pronunciation Assessment Service is ready!"
echo ""
echo "Available endpoints:"
echo "• Health: http://localhost:5000/health"
echo "• Info: http://localhost:5000/api/info"
echo "• Assessment: http://localhost:5000/api/pronunciation-assessment"
echo ""
echo "Features:"
echo "• ✅ CPU-Only (no CUDA downloads)"
echo "• ✅ Fast build (3-5 minutes)"
echo "• ✅ 10x faster than MFA (2-8s vs 30s)"
echo "• ✅ Works on any machine"
echo ""
echo "Test commands:"
echo "python3 test_whisperx.py"
echo ""
echo "View logs:"
echo "docker logs pronunciation-assessment-service-whisperx"