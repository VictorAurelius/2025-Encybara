#!/bin/bash

# Script để lấy public URL từ Ngrok tunnel
# Usage: ./get-tunnel-url.sh

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_status "Getting Ngrok tunnel URL for pronunciation-assessment-service..."

# Check if ngrok container is running
if ! docker ps --format "{{.Names}}" | grep -q "pronunciation-ngrok"; then
    print_error "Ngrok container is not running!"
    print_error "Start it first with: ./start-ngrok.sh"
    exit 1
fi

# Wait a moment for ngrok to be ready
sleep 2

# Try to get tunnel info from API
print_status "Querying Ngrok API..."

# Get tunnel information
TUNNEL_INFO=$(curl -s http://localhost:4041/api/tunnels 2>/dev/null)

if [ $? -eq 0 ] && [ -n "$TUNNEL_INFO" ]; then
    # Extract public URL using basic text processing (no jq dependency)
    PUBLIC_URL=$(echo "$TUNNEL_INFO" | grep -o '"public_url":"[^"]*"' | head -1 | cut -d'"' -f4)
    
    if [ -n "$PUBLIC_URL" ]; then
        print_success "=========================================="
        print_success "Ngrok Tunnel Information"
        print_success "=========================================="
        print_success "Public URL: $PUBLIC_URL"
        print_success "Local Target: pronunciation-assessment-service:5000"
        print_success "Web Interface: http://localhost:4041"
        print_success "=========================================="
        
        print_status "Test URLs:"
        echo "Health Check: $PUBLIC_URL/health"
        echo "Service Info: $PUBLIC_URL/api/info"
        echo "Assessment API: $PUBLIC_URL/api/pronunciation-assessment"
        
        print_status "Example test command:"
        echo "curl $PUBLIC_URL/health"
        
    else
        print_error "Could not extract public URL from API response"
        print_status "Raw API response:"
        echo "$TUNNEL_INFO"
    fi
else
    print_error "Failed to connect to Ngrok API at http://localhost:4041"
    print_status "Checking container status..."
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep pronunciation
    
    print_status "Try checking manually at: http://localhost:4041"
fi