#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to clear screen and show header
show_header() {
    clear
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}                    🚀 NGROK TUNNEL MONITOR                     ${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}$(date '+%Y-%m-%d %H:%M:%S')${NC} | Press Ctrl+C to exit"
    echo ""
}

# Function to check ngrok status
check_ngrok_status() {
    if curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to get tunnel info
get_tunnel_info() {
    local tunnels_data=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null)
    
    if [ $? -ne 0 ] || [ -z "$tunnels_data" ]; then
        echo -e "${RED}❌ Unable to fetch tunnel data${NC}"
        return 1
    fi
    
    echo -e "${GREEN}🌐 Active Tunnels:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    
    if command -v jq >/dev/null 2>&1; then
        # Use jq for better parsing
        echo "$tunnels_data" | jq -r '.tunnels[] | "  🔗 \(.name | ljust(25)) | \(.public_url)"'
    else
        # Parse without jq (basic parsing)
        local count=0
        echo "$tunnels_data" | grep -o '"name":"[^"]*"' | sed 's/"name":"//g' | sed 's/"//g' | while read name; do
            local url=$(echo "$tunnels_data" | grep -A10 "\"name\":\"$name\"" | grep -o '"public_url":"https://[^"]*"' | head -1 | sed 's/"public_url":"//g' | sed 's/"//g')
            if [ -n "$url" ]; then
                printf "  🔗 %-25s | %s\n" "$name" "$url"
                count=$((count + 1))
            fi
        done
        
        if [ $count -eq 0 ]; then
            echo "  No active tunnels found"
        fi
    fi
}

# Function to show service status
show_service_status() {
    echo ""
    echo -e "${BLUE}📊 Service Status:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    
    # Check pronunciation-assessment service
    if curl -s http://localhost:5000/health > /dev/null 2>&1; then
        echo -e "  🟢 pronunciation-assessment | ${GREEN}RUNNING${NC} | Port 5000"
    else
        echo -e "  🔴 pronunciation-assessment | ${RED}DOWN${NC}    | Port 5000"
    fi
    
    # Check content-scoring service
    if curl -s http://localhost:5001/health > /dev/null 2>&1; then
        echo -e "  🟢 content-scoring          | ${GREEN}RUNNING${NC} | Port 5001"
    else
        echo -e "  🔴 content-scoring          | ${RED}DOWN${NC}    | Port 5001"
    fi
}

# Function to show connection stats
show_connection_stats() {
    echo ""
    echo -e "${BLUE}📈 Connection Stats:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    
    local stats=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null)
    if [ $? -eq 0 ] && [ -n "$stats" ]; then
        local count=$(echo "$stats" | grep -o '"name":' | wc -l)
        echo -e "  📡 Total tunnels: ${GREEN}$count${NC}"
        echo -e "  🌐 Web interface: ${BLUE}http://localhost:4040${NC}"
        echo -e "  📋 API endpoint: ${BLUE}http://localhost:4040/api/tunnels${NC}"
    else
        echo -e "  ${RED}❌ Unable to fetch stats${NC}"
    fi
}

# Function to show quick commands
show_commands() {
    echo ""
    echo -e "${BLUE}🔧 Quick Commands:${NC}"
    echo "────────────────────────────────────────────────────────────────"
    echo -e "  ${YELLOW}curl \$(./get-tunnel-urls.sh)/health${NC}  # Test tunnel"
    echo -e "  ${YELLOW}./get-tunnel-urls.sh${NC}                # Get URLs"
    echo -e "  ${YELLOW}pkill ngrok${NC}                         # Stop ngrok"
    echo -e "  ${YELLOW}./start-with-progress.sh${NC}            # Restart with progress"
}

# Main monitoring loop
main() {
    # Trap Ctrl+C to exit gracefully
    trap 'echo -e "\n${YELLOW}👋 Monitoring stopped.${NC}"; exit 0' INT
    
    while true; do
        show_header
        
        if check_ngrok_status; then
            echo -e "${GREEN}✅ Ngrok Status: ACTIVE${NC}"
            get_tunnel_info
            show_service_status
            show_connection_stats
            show_commands
        else
            echo -e "${RED}❌ Ngrok Status: OFFLINE${NC}"
            echo ""
            echo -e "${YELLOW}🚀 Start ngrok with:${NC}"
            echo -e "  ${BLUE}./start-with-progress.sh${NC}"
            echo -e "  ${BLUE}./start-ngrok.sh${NC}"
        fi
        
        echo ""
        echo -e "${YELLOW}🔄 Refreshing in 5 seconds...${NC}"
        sleep 5
    done
}

# Show help if requested
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "monitor.sh - Real-time Ngrok Tunnel Monitor"
    echo ""
    echo "Usage: ./monitor.sh"
    echo ""
    echo "Features:"
    echo "  • Real-time tunnel status"
    echo "  • Service health checks"
    echo "  • Connection statistics"
    echo "  • Auto-refresh every 5 seconds"
    echo ""
    echo "Controls:"
    echo "  • Ctrl+C to exit"
    echo ""
    exit 0
fi

# Run main function
main