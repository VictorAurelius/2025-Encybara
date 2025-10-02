#!/bin/bash

# Start Ngrok tunnel manually for pronunciation-assessment-service
# Usage: ./start-ngrok.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[NGROK]${NC} $1"
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

print_status "=========================================="
print_status "Starting Ngrok Tunnel"
print_status "=========================================="

# Use docker compose if available, otherwise use docker-compose
COMPOSE_CMD="docker-compose"
if docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
fi

print_status "Using compose command: $COMPOSE_CMD"

# Check if main service is running
if ! docker ps --format "{{.Names}}" | grep -q "pronunciation-assessment-service"; then
    print_error "Main service is not running. Please start it first with:"
    print_error "./build.sh"
    exit 1
fi

# Stop existing ngrok container if running
print_status "Stopping existing Ngrok container..."
docker stop pronunciation-ngrok 2>/dev/null || true
docker rm pronunciation-ngrok 2>/dev/null || true

# Start ngrok service
print_status "Starting Ngrok tunnel with updated config..."
$COMPOSE_CMD up -d ngrok

# Wait for ngrok to start
print_status "Waiting for Ngrok to initialize..."
sleep 5

# Check if ngrok is running
if docker ps --format "{{.Names}}" | grep -q "pronunciation-ngrok"; then
    print_success "Ngrok tunnel started successfully!"
    print_status "=========================================="
    print_status "Ngrok Information:"
    print_status "• Web Interface: http://localhost:4041"
    print_status "• Service Target: pronunciation-assessment-service:5000"
    print_status "• View tunnel URL at: http://localhost:4041"
    print_status "=========================================="
    
    # Try to get tunnel info
    print_status "Attempting to retrieve tunnel URL..."
    sleep 3
    if curl -s http://localhost:4041/api/tunnels > /dev/null 2>&1; then
        print_success "Ngrok API accessible at http://localhost:4041/api/tunnels"
    else
        print_warning "Ngrok API not yet ready. Check web interface manually."
    fi
    
    print_status "To stop tunnel: docker stop pronunciation-ngrok"
    print_status "To view logs: $COMPOSE_CMD logs -f ngrok"
else
    print_error "Failed to start Ngrok tunnel"
    print_status "Check logs with: $COMPOSE_CMD logs ngrok"
    exit 1
fi