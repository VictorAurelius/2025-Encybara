#!/bin/bash

# Test script for Ngrok public URL of pronunciation-assessment-service
# Usage: ./test-ngrok-public.sh [public_url]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[TEST]${NC} $1"
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

# Function to test endpoint
test_endpoint() {
    local url="$1"
    local description="$2"
    local expected_status="${3:-200}"
    
    print_status "Testing: $description"
    print_status "URL: $url"
    
    # Test with curl
    response=$(curl -s -w "%{http_code}" -o /tmp/response.txt "$url" 2>/dev/null)
    http_code="${response: -3}"
    
    if [ "$http_code" = "$expected_status" ]; then
        print_success "✓ $description - HTTP $http_code"
        if [ -s /tmp/response.txt ]; then
            echo "Response:"
            cat /tmp/response.txt | head -10
            echo ""
        fi
    else
        print_error "✗ $description - HTTP $http_code (expected $expected_status)"
        if [ -s /tmp/response.txt ]; then
            echo "Error response:"
            cat /tmp/response.txt
            echo ""
        fi
    fi
    echo "----------------------------------------"
}

# Get public URL from argument or auto-detect
PUBLIC_URL="$1"

if [ -z "$PUBLIC_URL" ]; then
    print_status "No URL provided, attempting to auto-detect from Ngrok..."
    
    # Check if ngrok is running
    if ! docker ps --format "{{.Names}}" | grep -q "pronunciation-ngrok"; then
        print_error "Ngrok container is not running!"
        print_error "Start it with: ./start-ngrok.sh"
        exit 1
    fi
    
    # Auto-detect public URL
    PUBLIC_URL=$(curl -s http://localhost:4041/api/tunnels 2>/dev/null | grep -o '"public_url":"https://[^"]*"' | head -1 | cut -d'"' -f4)
    
    if [ -z "$PUBLIC_URL" ]; then
        print_error "Could not auto-detect public URL"
        print_error "Usage: $0 <public_url>"
        print_error "Example: $0 https://abc123.ngrok-free.app"
        exit 1
    fi
    
    print_success "Auto-detected URL: $PUBLIC_URL"
fi

# Remove trailing slash
PUBLIC_URL=${PUBLIC_URL%/}

print_status "=========================================="
print_status "Testing Ngrok Public URL for Pronunciation Assessment Service"
print_status "Public URL: $PUBLIC_URL"
print_status "=========================================="

# Test 1: Health Check
test_endpoint "$PUBLIC_URL/health" "Health Check Endpoint"

# Test 2: Root Endpoint  
test_endpoint "$PUBLIC_URL/" "Root Endpoint"

# Test 3: Service Info
test_endpoint "$PUBLIC_URL/api/info" "Service Info Endpoint"

# Test 4: Pronunciation Assessment Endpoint (GET - should return method not allowed)
test_endpoint "$PUBLIC_URL/api/pronunciation-assessment" "Pronunciation Assessment Endpoint (GET)" "405"

# Test 5: Check if service accepts POST (with empty body)
print_status "Testing: Pronunciation Assessment POST endpoint"
print_status "URL: $PUBLIC_URL/api/pronunciation-assessment"

post_response=$(curl -s -w "%{http_code}" -o /tmp/post_response.txt -X POST "$PUBLIC_URL/api/pronunciation-assessment" 2>/dev/null)
post_code="${post_response: -3}"

if [ "$post_code" = "400" ] || [ "$post_code" = "422" ]; then
    print_success "✓ POST endpoint accessible - HTTP $post_code (validation error expected)"
elif [ "$post_code" = "200" ]; then
    print_success "✓ POST endpoint working - HTTP $post_code"
else
    print_warning "⚠ POST endpoint - HTTP $post_code (check if service is fully loaded)"
fi

if [ -s /tmp/post_response.txt ]; then
    echo "POST Response:"
    cat /tmp/post_response.txt
    echo ""
fi
echo "----------------------------------------"

# Test 6: Performance test
print_status "Testing: Response time"
start_time=$(date +%s%N)
curl -s "$PUBLIC_URL/health" > /dev/null
end_time=$(date +%s%N)
response_time=$(( (end_time - start_time) / 1000000 ))

if [ $response_time -lt 1000 ]; then
    print_success "✓ Response time: ${response_time}ms (Good)"
elif [ $response_time -lt 3000 ]; then
    print_warning "⚠ Response time: ${response_time}ms (Acceptable)"
else
    print_warning "⚠ Response time: ${response_time}ms (Slow - may be due to Ngrok latency)"
fi

# Summary
print_status "=========================================="
print_success "Ngrok Public URL Testing Complete"
print_status "=========================================="
print_status "Public URL: $PUBLIC_URL"
print_status "Available endpoints:"
echo "• Health: $PUBLIC_URL/health"
echo "• Info: $PUBLIC_URL/api/info"
echo "• Assessment: $PUBLIC_URL/api/pronunciation-assessment (POST)"
echo "• Web Interface: http://localhost:4041"

print_status "Example usage from external systems:"
echo "curl $PUBLIC_URL/health"
echo "curl $PUBLIC_URL/api/info"

print_status "For pronunciation assessment, send POST with audio file:"
echo 'curl -X POST -F "audio=@audio.wav" -F "transcript=hello world" '$PUBLIC_URL'/api/pronunciation-assessment'

# Cleanup temp files
rm -f /tmp/response.txt /tmp/post_response.txt