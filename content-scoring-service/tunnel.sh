#!/bin/bash

# =============================================================================
# tunnel.sh - Simple Tunnel Wrapper for Content Scoring Service
# =============================================================================
# Script wrapper đơn giản để start/stop/check ngrok tunnel
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to show help
show_help() {
    echo "tunnel.sh - Simple Tunnel Management for Content Scoring Service"
    echo ""
    echo "Usage: ./tunnel.sh [COMMAND] [AUTHTOKEN]"
    echo ""
    echo "Commands:"
    echo "  start [AUTHTOKEN]   Start tunnel with optional authtoken"
    echo "  stop                Stop tunnel"
    echo "  status              Check tunnel status and get URL"
    echo "  url                 Get public URL only"
    echo "  test                Test public URL endpoints"
    echo ""
    echo "Examples:"
    echo "  ./tunnel.sh start                           # Start with existing config"
    echo "  ./tunnel.sh start 2abc123def456...          # Start with new authtoken"
    echo "  ./tunnel.sh status                          # Check status and get URL"
    echo "  ./tunnel.sh stop                           # Stop tunnel"
    echo "  ./tunnel.sh test                           # Test all endpoints"
    echo ""
    echo "Quick Setup:"
    echo "  1. Build service: ./build.sh (or ./quick-fix.sh)"
    echo "  2. Start tunnel: ./tunnel.sh start YOUR_AUTHTOKEN"
    echo "  3. Get URL: ./tunnel.sh url"
    echo "  4. Test: ./tunnel.sh test"
    echo ""
    exit 0
}

# Parse command
COMMAND="$1"
AUTHTOKEN="$2"

case "$COMMAND" in
    start)
        echo -e "${CYAN}[TUNNEL] Starting Ngrok tunnel...${NC}"
        if [[ -f "./start-tunnel.sh" ]]; then
            chmod +x ./start-tunnel.sh
            ./start-tunnel.sh "$AUTHTOKEN"
        else
            echo -e "${RED}[ERROR] start-tunnel.sh not found${NC}"
            exit 1
        fi
        ;;
    stop)
        echo -e "${CYAN}[TUNNEL] Stopping Ngrok tunnel...${NC}"
        if [[ -f "./start-tunnel.sh" ]]; then
            chmod +x ./start-tunnel.sh
            ./start-tunnel.sh --stop
        else
            echo -e "${RED}[ERROR] start-tunnel.sh not found${NC}"
            exit 1
        fi
        ;;
    status)
        echo -e "${CYAN}[TUNNEL] Checking tunnel status...${NC}"
        if [[ -f "./start-tunnel.sh" ]]; then
            chmod +x ./start-tunnel.sh
            ./start-tunnel.sh --status
        else
            echo -e "${RED}[ERROR] start-tunnel.sh not found${NC}"
            exit 1
        fi
        ;;
    url)
        echo -e "${CYAN}[TUNNEL] Getting public URL...${NC}"
        if [[ -f "./get-ngrok-url-direct.sh" ]]; then
            chmod +x ./get-ngrok-url-direct.sh
            ./get-ngrok-url-direct.sh | tail -1
        elif [[ -f "./get-tunnel-url.sh" ]]; then
            chmod +x ./get-tunnel-url.sh
            ./get-tunnel-url.sh | tail -1
        else
            echo -e "${RED}[ERROR] URL getter script not found${NC}"
            exit 1
        fi
        ;;
    test)
        echo -e "${CYAN}[TUNNEL] Testing public URL...${NC}"
        
        # Get URL first
        local public_url=""
        if [[ -f "./get-ngrok-url-direct.sh" ]]; then
            chmod +x ./get-ngrok-url-direct.sh
            public_url=$(./get-ngrok-url-direct.sh 2>/dev/null | tail -1)
        elif [[ -f "./get-tunnel-url.sh" ]]; then
            chmod +x ./get-tunnel-url.sh
            public_url=$(./get-tunnel-url.sh 2>/dev/null | tail -1)
        fi
        
        if [[ -n "$public_url" && "$public_url" != *"ERROR"* ]]; then
            if [[ -f "./test-ngrok-public.sh" ]]; then
                chmod +x ./test-ngrok-public.sh
                ./test-ngrok-public.sh "$public_url"
            else
                echo -e "${YELLOW}[WARNING] test-ngrok-public.sh not found${NC}"
                echo -e "${BLUE}[INFO] Testing manually...${NC}"
                echo -e "${BLUE}curl $public_url/health${NC}"
                curl -s "$public_url/health" | head -c 200
            fi
        else
            echo -e "${RED}[ERROR] Could not get public URL${NC}"
            echo -e "${YELLOW}[HINT] Check if tunnel is running: ./tunnel.sh status${NC}"
            exit 1
        fi
        ;;
    ""|--help|-h|help)
        show_help
        ;;
    *)
        echo -e "${RED}[ERROR] Unknown command: $COMMAND${NC}"
        echo -e "${YELLOW}[HINT] Use --help for usage information${NC}"
        exit 1
        ;;
esac