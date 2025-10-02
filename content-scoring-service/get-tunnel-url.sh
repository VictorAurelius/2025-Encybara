#!/bin/bash

# =============================================================================
# get-tunnel-url.sh - Get Ngrok Public URL for Content Scoring Service
# =============================================================================
# Script để lấy public URL từ Ngrok tunnel cho content-scoring-service
# Sử dụng: ./get-tunnel-url.sh [--json] [--host-only] [--with-port]
#
# Options:
#   --json       : Output raw JSON response
#   --host-only  : Output only hostname (without https://)
#   --with-port  : Include port in output
#   --help       : Show help message
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NGROK_API_URL="http://localhost:4040/api/tunnels"
SERVICE_NAME="content-scoring-service"
SERVICE_PORT="5001"

# Default options
OUTPUT_JSON=false
HOST_ONLY=false
WITH_PORT=false
SHOW_HELP=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --json)
            OUTPUT_JSON=true
            shift
            ;;
        --host-only)
            HOST_ONLY=true
            shift
            ;;
        --with-port)
            WITH_PORT=true
            shift
            ;;
        --help)
            SHOW_HELP=true
            shift
            ;;
        *)
            echo -e "${RED}[ERROR] Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# Show help
if [ "$SHOW_HELP" = true ]; then
    echo "get-tunnel-url.sh - Get Ngrok Public URL for Content Scoring Service"
    echo ""
    echo "Usage: ./get-tunnel-url.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --json       Output raw JSON response from Ngrok API"
    echo "  --host-only  Output only hostname (without https://)"
    echo "  --with-port  Include port in output"
    echo "  --help       Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./get-tunnel-url.sh                    # Get full public URL"
    echo "  ./get-tunnel-url.sh --host-only        # Get hostname only"
    echo "  ./get-tunnel-url.sh --json             # Get raw JSON"
    echo "  ./get-tunnel-url.sh --with-port        # Include port info"
    echo ""
    exit 0
fi

# Function to check if Ngrok is running
check_ngrok_running() {
    if ! curl -s "$NGROK_API_URL" > /dev/null 2>&1; then
        echo -e "${RED}[ERROR] Ngrok không chạy hoặc không accessible tại $NGROK_API_URL${NC}" >&2
        echo -e "${YELLOW}[HINT] Chạy Ngrok trước:${NC}" >&2
        echo -e "${BLUE}  ./start-ngrok.sh${NC}" >&2
        echo -e "${BLUE}  # hoặc${NC}" >&2
        echo -e "${BLUE}  ngrok http $SERVICE_PORT${NC}" >&2
        return 1
    fi
    return 0
}

# Function to get tunnels data
get_tunnels_data() {
    local response
    response=$(curl -s "$NGROK_API_URL" 2>/dev/null)
    
    if [ $? -ne 0 ] || [ -z "$response" ]; then
        echo -e "${RED}[ERROR] Không thể lấy dữ liệu từ Ngrok API${NC}" >&2
        return 1
    fi
    
    echo "$response"
}

# Function to extract public URL
extract_public_url() {
    local tunnels_data="$1"
    local public_url
    
    # Try to find tunnel for our service port
    if command -v jq >/dev/null 2>&1; then
        # Use jq if available
        public_url=$(echo "$tunnels_data" | jq -r ".tunnels[] | select(.config.addr == \"http://localhost:$SERVICE_PORT\" or .config.addr == \"localhost:$SERVICE_PORT\") | .public_url" | grep "https://" | head -1)
        
        # Fallback: find any HTTPS tunnel
        if [ -z "$public_url" ] || [ "$public_url" = "null" ]; then
            public_url=$(echo "$tunnels_data" | jq -r '.tunnels[] | select(.proto == "https") | .public_url' | head -1)
        fi
    else
        # Parse without jq (basic parsing)
        public_url=$(echo "$tunnels_data" | grep -o '"public_url":"https://[^"]*"' | sed 's/"public_url":"//g' | sed 's/"//g' | head -1)
    fi
    
    echo "$public_url"
}

# Function to format output based on options
format_output() {
    local url="$1"
    
    if [ "$HOST_ONLY" = true ]; then
        # Extract hostname only
        hostname=$(echo "$url" | sed 's|https://||g' | sed 's|http://||g' | cut -d'/' -f1)
        echo "$hostname"
    elif [ "$WITH_PORT" = true ]; then
        # Add port info
        echo "$url (port: $SERVICE_PORT)"
    else
        # Standard full URL
        echo "$url"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}[INFO] Đang lấy Ngrok public URL cho $SERVICE_NAME...${NC}" >&2
    
    # Check if Ngrok is running
    if ! check_ngrok_running; then
        exit 1
    fi
    
    # Get tunnels data
    local tunnels_data
    tunnels_data=$(get_tunnels_data)
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    # Output raw JSON if requested
    if [ "$OUTPUT_JSON" = true ]; then
        echo "$tunnels_data"
        exit 0
    fi
    
    # Extract public URL
    local public_url
    public_url=$(extract_public_url "$tunnels_data")
    
    if [ -z "$public_url" ] || [ "$public_url" = "null" ]; then
        echo -e "${RED}[ERROR] Không tìm thấy Ngrok tunnel cho $SERVICE_NAME${NC}" >&2
        echo -e "${YELLOW}[HINT] Kiểm tra xem tunnel có chạy đúng port $SERVICE_PORT không:${NC}" >&2
        echo -e "${BLUE}  curl $NGROK_API_URL${NC}" >&2
        exit 1
    fi
    
    # Format and output result
    local formatted_url
    formatted_url=$(format_output "$public_url")
    
    echo -e "${GREEN}[SUCCESS] Public URL found:${NC}" >&2
    echo "$formatted_url"
    
    # Show additional info
    echo -e "${BLUE}[INFO] Service endpoints:${NC}" >&2
    echo -e "${BLUE}  Health: $public_url/health${NC}" >&2
    echo -e "${BLUE}  Metrics: $public_url/metrics${NC}" >&2
    echo -e "${BLUE}  API: $public_url/api/content-scoring${NC}" >&2
    echo -e "${BLUE}  Docs: $public_url/docs${NC}" >&2
}

# Execute main function
main "$@"