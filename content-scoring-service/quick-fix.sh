#!/bin/bash

# Quick Fix Script - Fix permission issues without rebuild
# Sử dụng image đã có và fix permissions bằng environment variables

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[QUICK FIX]${NC} $1"
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

print_status "======================================"
print_status "Content Scoring Service - Quick Fix"
print_status "======================================"

# Check if image exists
if ! docker images | grep -q "content-scoring-service"; then
    print_error "No content-scoring-service image found. Need to build first."
    print_status "Run: docker build -t content-scoring-service ."
    exit 1
fi

print_status "Found existing content-scoring-service image"

# Stop current containers
print_status "Stopping current containers..."
docker compose down 2>/dev/null || true

# Use quick fix compose file
print_status "Starting service with permission fixes..."
print_status "Using /tmp/.cache for HuggingFace cache (writable)"
print_status "Running as root user temporarily"

docker compose -f docker-compose-quick-fix.yml up -d

print_success "Service started with quick fixes applied!"

# Wait a bit then check status
print_status "Waiting for service to initialize..."
sleep 15

# Check if container is running
if docker ps | grep -q "content-scoring-service"; then
    print_success "Container is running!"
    
    # Check logs for errors
    print_status "Checking logs for errors..."
    logs=$(docker compose -f docker-compose-quick-fix.yml logs content-scoring-service --tail=20 2>&1)
    
    if echo "$logs" | grep -q "Permission denied"; then
        print_warning "Still seeing permission errors in logs"
        print_status "Trying alternative fix..."
        
        # Create cache directory with proper permissions
        docker compose -f docker-compose-quick-fix.yml exec content-scoring-service mkdir -p /tmp/.cache/huggingface
        docker compose -f docker-compose-quick-fix.yml exec content-scoring-service chmod -R 777 /tmp/.cache/huggingface
        
        print_status "Restarting service..."
        docker compose -f docker-compose-quick-fix.yml restart content-scoring-service
        
    elif echo "$logs" | grep -q "Content Scoring Service initialized successfully"; then
        print_success "Service initialized successfully!"
        
    elif echo "$logs" | grep -q "uvicorn"; then
        print_success "Uvicorn is starting!"
        
    else
        print_status "Service is starting, checking health..."
    fi
    
else
    print_error "Container failed to start"
    print_status "Checking logs..."
    docker compose -f docker-compose-quick-fix.yml logs content-scoring-service
    exit 1
fi

# Check health endpoint
print_status "Testing health endpoint..."
for i in {1..30}; do
    if curl -s http://localhost:5001/health > /dev/null 2>&1; then
        print_success "Service is healthy and ready!"
        break
    else
        print_status "Waiting for service... ($i/30)"
        sleep 2
    fi
    
    if [ $i -eq 30 ]; then
        print_warning "Health check timeout. Service might still be loading models."
        print_status "Check logs: docker compose -f docker-compose-quick-fix.yml logs -f content-scoring-service"
    fi
done

# Show service info
print_status "======================================"
print_success "Quick Fix completed!"
print_status "======================================"
print_status "Service Status:"
docker compose -f docker-compose-quick-fix.yml ps

print_status ""
print_status "Available endpoints:"
print_status "• Main Service: http://localhost:5001"
print_status "• API Documentation: http://localhost:5001/docs"
print_status "• Health Check: http://localhost:5001/health"
print_status "• Metrics: http://localhost:5001/metrics"

print_status ""
print_status "To check logs: docker compose -f docker-compose-quick-fix.yml logs -f content-scoring-service"
print_status "To stop: docker compose -f docker-compose-quick-fix.yml down"
print_status "======================================"

# Test API if service is healthy
if curl -s http://localhost:5001/health > /dev/null 2>&1; then
    print_status ""
    print_status "Testing API..."
    response=$(curl -s -X POST http://localhost:5001/api/content-scoring \
        -H "Content-Type: application/json" \
        -d '{"question": "What is ML?", "answer": "Machine learning uses algorithms."}' 2>/dev/null || echo "API not ready")
    
    if echo "$response" | grep -q "score"; then
        print_success "API is working! Response: $response"
    else
        print_status "API might still be loading models. Try again in a few minutes."
    fi
fi