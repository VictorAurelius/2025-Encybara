#!/bin/bash

# =============================================================================
# test-ngrok-public.sh - Test Ngrok Public URL for Content Scoring Service
# =============================================================================
# Script để test comprehensive public URL của Ngrok tunnel cho content-scoring-service
# Sử dụng: ./test-ngrok-public.sh [public_url]
#
# Nếu không cung cấp URL, script sẽ tự động detect từ Ngrok API
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
SERVICE_NAME="content-scoring-service"
SERVICE_PORT="5001"
NGROK_API_URL="http://localhost:4040/api/tunnels"

# Test configuration
TIMEOUT=10
MAX_RESPONSE_TIME=2000  # 2 seconds in milliseconds

# Function to show help
show_help() {
    echo "test-ngrok-public.sh - Test Ngrok Public URL for Content Scoring Service"
    echo ""
    echo "Usage: ./test-ngrok-public.sh [PUBLIC_URL]"
    echo ""
    echo "Arguments:"
    echo "  PUBLIC_URL   Optional. Ngrok public URL to test."
    echo "               If not provided, will auto-detect from Ngrok API."
    echo ""
    echo "Examples:"
    echo "  ./test-ngrok-public.sh                                    # Auto-detect URL"
    echo "  ./test-ngrok-public.sh https://abc123.ngrok-free.app      # Test specific URL"
    echo ""
    echo "Tests performed:"
    echo "  ✓ Health Check endpoint (/health)"
    echo "  ✓ Metrics endpoint (/metrics)"
    echo "  ✓ API Documentation (/docs)"
    echo "  ✓ Content Scoring endpoint (/api/content-scoring)"
    echo "  ✓ HTTP methods validation"
    echo "  ✓ Response time measurement"
    echo ""
    exit 0
}

# Check for help flag
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    show_help
fi

# Function to auto-detect public URL
auto_detect_url() {
    echo -e "${BLUE}[TEST] No URL provided, attempting to auto-detect from Ngrok...${NC}"
    
    # Check if get-tunnel-url.sh exists
    if [[ ! -f "./get-tunnel-url.sh" ]]; then
        echo -e "${RED}[ERROR] get-tunnel-url.sh not found${NC}"
        echo -e "${YELLOW}[HINT] Make sure you're in the content-scoring-service directory${NC}"
        exit 1
    fi
    
    # Make script executable
    chmod +x ./get-tunnel-url.sh
    
    # Get URL from script
    local detected_url
    detected_url=$(./get-tunnel-url.sh 2>/dev/null | tail -1)
    
    if [[ -z "$detected_url" || "$detected_url" == *"ERROR"* ]]; then
        echo -e "${RED}[ERROR] Không thể auto-detect public URL${NC}"
        echo -e "${YELLOW}[HINT] Kiểm tra Ngrok có đang chạy:${NC}"
        echo -e "${BLUE}  ./start-ngrok.sh${NC}"
        echo -e "${BLUE}  # hoặc${NC}"
        echo -e "${BLUE}  ngrok http $SERVICE_PORT${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}[SUCCESS] Auto-detected URL: $detected_url${NC}"
    echo "$detected_url"
}

# Function to validate URL format
validate_url() {
    local url="$1"
    
    if [[ ! "$url" =~ ^https?:// ]]; then
        echo -e "${RED}[ERROR] Invalid URL format: $url${NC}"
        echo -e "${YELLOW}[HINT] URL should start with http:// or https://${NC}"
        exit 1
    fi
    
    # Remove trailing slash
    url=$(echo "$url" | sed 's|/$||')
    echo "$url"
}

# Function to measure response time
measure_response_time() {
    local url="$1"
    local start_time end_time response_time
    
    start_time=$(date +%s%3N)
    curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "$url" >/dev/null 2>&1
    end_time=$(date +%s%3N)
    
    response_time=$((end_time - start_time))
    echo "$response_time"
}

# Function to make HTTP request with error handling
make_request() {
    local method="$1"
    local url="$2"
    local expected_status="$3"
    local data="$4"
    
    local response status_code
    
    if [[ "$method" == "POST" && -n "$data" ]]; then
        response=$(curl -s -w "\n%{http_code}" --max-time $TIMEOUT \
                       -X POST \
                       -H "Content-Type: application/json" \
                       -d "$data" \
                       "$url" 2>/dev/null)
    else
        response=$(curl -s -w "\n%{http_code}" --max-time $TIMEOUT \
                       -X "$method" \
                       "$url" 2>/dev/null)
    fi
    
    if [[ $? -ne 0 ]]; then
        echo "TIMEOUT"
        return 1
    fi
    
    status_code=$(echo "$response" | tail -1)
    local body=$(echo "$response" | head -n -1)
    
    echo "$status_code|$body"
    return 0
}

# Function to test endpoint
test_endpoint() {
    local method="$1"
    local endpoint="$2"
    local expected_status="$3"
    local description="$4"
    local data="$5"
    
    local url="$PUBLIC_URL$endpoint"
    
    echo -e "${BLUE}[TEST] Testing: $description${NC}"
    echo -e "${BLUE}[TEST] URL: $url${NC}"
    
    local response
    response=$(make_request "$method" "$url" "$expected_status" "$data")
    
    if [[ "$response" == "TIMEOUT" ]]; then
        echo -e "${RED}[ERROR] ✗ $description - Request timeout${NC}"
        echo ""
        return 1
    fi
    
    local status_code body
    status_code=$(echo "$response" | cut -d'|' -f1)
    body=$(echo "$response" | cut -d'|' -f2-)
    
    # Check status code
    if [[ "$status_code" == "$expected_status" ]]; then
        echo -e "${GREEN}[SUCCESS] ✓ $description - HTTP $status_code${NC}"
        
        # Show response body if it's JSON and not too long
        if [[ -n "$body" && ${#body} -lt 1000 ]]; then
            if echo "$body" | python3 -m json.tool >/dev/null 2>&1; then
                echo "Response:"
                echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
            else
                echo "Response: $body"
            fi
        elif [[ -n "$body" ]]; then
            echo "Response: $(echo "$body" | head -c 200)..."
        fi
    else
        echo -e "${RED}[ERROR] ✗ $description - HTTP $status_code (expected $expected_status)${NC}"
        
        if [[ -n "$body" ]]; then
            echo "Error response:"
            echo "$body" | head -c 500
        fi
    fi
    
    echo ""
    echo "----------------------------------------"
    return 0
}

# Function to test response time
test_response_time() {
    echo -e "${BLUE}[TEST] Testing: Response time${NC}"
    
    local response_time
    response_time=$(measure_response_time "$PUBLIC_URL/health")
    
    if [[ "$response_time" -lt "$MAX_RESPONSE_TIME" ]]; then
        echo -e "${GREEN}[SUCCESS] ✓ Response time: ${response_time}ms (Good)${NC}"
    else
        echo -e "${YELLOW}[WARNING] ⚠ Response time: ${response_time}ms (Slow)${NC}"
    fi
    
    echo ""
}

# Function to run all tests
run_tests() {
    echo -e "${CYAN}[TEST] Testing Ngrok Public URL for Content Scoring Service${NC}"
    echo -e "${CYAN}[TEST] Public URL: $PUBLIC_URL${NC}"
    echo -e "${CYAN}[TEST] ==========================================${NC}"
    
    # Test health endpoint
    test_endpoint "GET" "/health" "200" "Health Check Endpoint"
    
    # Test metrics endpoint
    test_endpoint "GET" "/metrics" "200" "Metrics Endpoint"
    
    # Test docs endpoint
    test_endpoint "GET" "/docs" "200" "API Documentation Endpoint"
    
    # Test content scoring endpoint with GET (should fail)
    test_endpoint "GET" "/api/content-scoring" "405" "Content Scoring Endpoint (GET - should fail)" ""
    
    # Test content scoring endpoint with POST (should return validation error)
    local test_data='{"question": "What is AI?", "answer": "AI is artificial intelligence"}'
    test_endpoint "POST" "/api/content-scoring" "200" "Content Scoring POST endpoint" "$test_data"
    
    # Test response time
    test_response_time
    
    # Final summary
    echo -e "${CYAN}[TEST] ==========================================${NC}"
    echo -e "${GREEN}[SUCCESS] Ngrok Public URL Testing Complete${NC}"
    echo -e "${CYAN}[TEST] ==========================================${NC}"
    echo -e "${CYAN}[TEST] Public URL: $PUBLIC_URL${NC}"
    echo -e "${CYAN}[TEST] Available endpoints:${NC}"
    echo -e "${CYAN}• Health: $PUBLIC_URL/health${NC}"
    echo -e "${CYAN}• Metrics: $PUBLIC_URL/metrics${NC}"
    echo -e "${CYAN}• Docs: $PUBLIC_URL/docs${NC}"
    echo -e "${CYAN}• API: $PUBLIC_URL/api/content-scoring (POST)${NC}"
    echo -e "${CYAN}• Web Interface: http://localhost:4040${NC}"
    
    echo -e "${CYAN}[TEST] Example usage from external systems:${NC}"
    echo -e "${CYAN}curl $PUBLIC_URL/health${NC}"
    echo -e "${CYAN}curl $PUBLIC_URL/metrics${NC}"
    echo -e "${CYAN}[TEST] For content scoring, send POST with JSON:${NC}"
    echo -e "${CYAN}curl -X POST -H \"Content-Type: application/json\" \\${NC}"
    echo -e "${CYAN}  -d '{\"question\":\"What is AI?\",\"answer\":\"AI is artificial intelligence\"}' \\${NC}"
    echo -e "${CYAN}  $PUBLIC_URL/api/content-scoring${NC}"
}

# Main execution
main() {
    # Get public URL (from argument or auto-detect)
    local public_url="$1"
    
    if [[ -z "$public_url" ]]; then
        public_url=$(auto_detect_url)
    fi
    
    # Validate URL
    PUBLIC_URL=$(validate_url "$public_url")
    
    # Run tests
    run_tests
}

# Execute main function
main "$@"