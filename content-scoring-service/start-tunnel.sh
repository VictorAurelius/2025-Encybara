#!/bin/bash

# =============================================================================
# start-tunnel.sh - Start Ngrok Tunnel for Content Scoring Service
# =============================================================================
# Script đơn giản để start ngrok tunnel sau khi service đã chạy
# Tránh vấn đề Docker Compose profiles không support
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
NGROK_CONTAINER="content-scoring-ngrok"
NGROK_CONFIG_FILE="./ngrok/ngrok.yml"

# Function to show help
show_help() {
    echo "start-tunnel.sh - Start Ngrok Tunnel for Content Scoring Service"
    echo ""
    echo "Usage: ./start-tunnel.sh [AUTHTOKEN]"
    echo ""
    echo "Arguments:"
    echo "  AUTHTOKEN   Optional. Your Ngrok authtoken."
    echo ""
    echo "Examples:"
    echo "  ./start-tunnel.sh                           # Use existing config"
    echo "  ./start-tunnel.sh 2abc123def456...          # Update authtoken and start"
    echo ""
    echo "Options:"
    echo "  --help       Show this help message"
    echo "  --stop       Stop tunnel"
    echo "  --status     Show tunnel status"
    echo ""
    exit 0
}

# Parse arguments
case "$1" in
    --help|-h)
        show_help
        ;;
    --stop)
        echo -e "${BLUE}[INFO] Stopping Ngrok tunnel...${NC}"
        docker stop "$NGROK_CONTAINER" >/dev/null 2>&1 || true
        docker rm "$NGROK_CONTAINER" >/dev/null 2>&1 || true
        echo -e "${GREEN}[SUCCESS] Ngrok tunnel stopped${NC}"
        exit 0
        ;;
    --status)
        echo -e "${BLUE}[INFO] Checking tunnel status...${NC}"
        if docker ps --filter "name=$NGROK_CONTAINER" --format "table {{.Names}}" | grep -q "$NGROK_CONTAINER"; then
            echo -e "${GREEN}[SUCCESS] Ngrok container is running${NC}"
            
            # Try to get URL
            if [[ -f "./get-ngrok-url-direct.sh" ]]; then
                echo -e "${BLUE}[INFO] Getting public URL...${NC}"
                ./get-ngrok-url-direct.sh
            elif [[ -f "./get-tunnel-url.sh" ]]; then
                echo -e "${BLUE}[INFO] Getting public URL...${NC}"
                ./get-tunnel-url.sh
            else
                echo -e "${YELLOW}[INFO] Check manually: http://localhost:4040${NC}"
            fi
        else
            echo -e "${RED}[ERROR] Ngrok container is not running${NC}"
            echo -e "${YELLOW}[HINT] Start tunnel: ./start-tunnel.sh${NC}"
        fi
        exit 0
        ;;
esac

# Function to check if main service is running
check_main_service() {
    echo -e "${BLUE}[INFO] Checking content-scoring-service...${NC}"
    
    if ! docker ps --filter "name=$SERVICE_NAME" --format "table {{.Names}}" | grep -q "$SERVICE_NAME"; then
        echo -e "${RED}[ERROR] Content-scoring-service is not running${NC}"
        echo -e "${YELLOW}[HINT] Start service first:${NC}"
        echo -e "${BLUE}  ./build.sh${NC}"
        echo -e "${BLUE}  # hoặc${NC}"
        echo -e "${BLUE}  ./quick-fix.sh${NC}"
        return 1
    fi
    
    # Check if service is responding
    if curl -s http://localhost:$SERVICE_PORT/health > /dev/null 2>&1; then
        echo -e "${GREEN}[SUCCESS] Service is healthy at port $SERVICE_PORT${NC}"
        return 0
    else
        echo -e "${YELLOW}[WARNING] Service container running but not responding${NC}"
        echo -e "${BLUE}[INFO] Waiting for service to be ready...${NC}"
        
        for i in {1..10}; do
            if curl -s http://localhost:$SERVICE_PORT/health > /dev/null 2>&1; then
                echo -e "${GREEN}[SUCCESS] Service is now ready!${NC}"
                return 0
            fi
            echo -n "."
            sleep 2
        done
        
        echo -e "\n${RED}[ERROR] Service not responding after 20s${NC}"
        echo -e "${YELLOW}[HINT] Check logs: docker logs $SERVICE_NAME${NC}"
        return 1
    fi
}

# Function to update authtoken in config
update_authtoken() {
    local authtoken="$1"
    
    if [[ -n "$authtoken" ]]; then
        echo -e "${BLUE}[INFO] Updating authtoken in config...${NC}"
        
        # Create config directory if not exists
        mkdir -p "./ngrok"
        
        # Create/update ngrok config with authtoken
        cat > "$NGROK_CONFIG_FILE" << EOF
version: 2
authtoken: $authtoken
tunnels:
  content-scoring:
    addr: host.docker.internal:$SERVICE_PORT
    proto: http
    inspect: true
    bind_tls: true
EOF
        
        echo -e "${GREEN}[SUCCESS] Updated authtoken in config${NC}"
    else
        # Check if authtoken exists in config
        if [[ -f "$NGROK_CONFIG_FILE" ]] && grep -q "authtoken:" "$NGROK_CONFIG_FILE" && ! grep -q "your_ngrok_auth_token_here" "$NGROK_CONFIG_FILE"; then
            echo -e "${GREEN}[INFO] Using existing authtoken from config${NC}"
        else
            echo -e "${YELLOW}[WARNING] No valid authtoken found${NC}"
            echo -e "${YELLOW}[HINT] Provide authtoken:${NC}"
            echo -e "${BLUE}  ./start-tunnel.sh YOUR_AUTHTOKEN${NC}"
            echo -e "${BLUE}  # hoặc update manually in: $NGROK_CONFIG_FILE${NC}"
            return 1
        fi
    fi
}

# Function to stop existing tunnel
stop_existing_tunnel() {
    if docker ps -q --filter "name=$NGROK_CONTAINER" | grep -q .; then
        echo -e "${YELLOW}[INFO] Stopping existing tunnel...${NC}"
        docker stop "$NGROK_CONTAINER" >/dev/null 2>&1 || true
    fi
    
    if docker ps -aq --filter "name=$NGROK_CONTAINER" | grep -q .; then
        docker rm "$NGROK_CONTAINER" >/dev/null 2>&1 || true
    fi
}

# Function to start ngrok container
start_ngrok_container() {
    echo -e "${BLUE}[INFO] Starting Ngrok container...${NC}"
    
    # Get authtoken from config file
    local authtoken=""
    if [[ -f "$NGROK_CONFIG_FILE" ]]; then
        authtoken=$(grep "authtoken:" "$NGROK_CONFIG_FILE" | sed 's/authtoken: *//' | tr -d ' ')
    fi
    
    if [[ -z "$authtoken" || "$authtoken" == "your_ngrok_auth_token_here" ]]; then
        echo -e "${RED}[ERROR] No valid authtoken found in config${NC}"
        return 1
    fi
    
    echo -e "${BLUE}[INFO] Using environment variable method (no config file mount)${NC}"
    
    # Start container with environment variable (avoid config file mounting issues)
    if docker run -d \
        --name "$NGROK_CONTAINER" \
        --network="content-scoring-service_content-scoring-network" \
        -p "4040:4040" \
        -e NGROK_AUTHTOKEN="$authtoken" \
        --add-host="host.docker.internal:host-gateway" \
        ngrok/ngrok:latest \
        http host.docker.internal:$SERVICE_PORT \
        > /dev/null 2>&1; then
        
        echo -e "${GREEN}[SUCCESS] Ngrok container started${NC}"
        return 0
    else
        echo -e "${RED}[ERROR] Failed to start Ngrok container${NC}"
        echo -e "${YELLOW}[HINT] Check authtoken validity${NC}"
        return 1
    fi
}

# Function to wait and check tunnel
check_tunnel_status() {
    echo -e "${BLUE}[INFO] Waiting for tunnel to initialize...${NC}"
    sleep 8
    
    # Check if container is still running
    if ! docker ps --filter "name=$NGROK_CONTAINER" --format "table {{.Names}}" | grep -q "$NGROK_CONTAINER"; then
        echo -e "${RED}[ERROR] Ngrok container crashed${NC}"
        echo -e "${YELLOW}[DEBUG] Container logs:${NC}"
        docker logs "$NGROK_CONTAINER" 2>&1 | head -10
        return 1
    fi
    
    echo -e "${GREEN}[SUCCESS] Ngrok container is running${NC}"
    
    # Try to get public URL
    echo -e "${BLUE}[INFO] Getting public URL...${NC}"
    
    if [[ -f "./get-ngrok-url-direct.sh" ]]; then
        chmod +x ./get-ngrok-url-direct.sh
        ./get-ngrok-url-direct.sh
    elif [[ -f "./get-tunnel-url.sh" ]]; then
        chmod +x ./get-tunnel-url.sh
        ./get-tunnel-url.sh
    else
        echo -e "${YELLOW}[INFO] Check tunnel manually: http://localhost:4040${NC}"
    fi
}

# Main execution
main() {
    echo -e "${CYAN}[INFO] Starting Ngrok Tunnel for Content Scoring Service${NC}"
    echo -e "${CYAN}[INFO] ============================================${NC}"
    echo ""
    
    # Check if main service is running
    if ! check_main_service; then
        exit 1
    fi
    
    # Update authtoken if provided
    if ! update_authtoken "$1"; then
        exit 1
    fi
    
    # Stop existing tunnel
    stop_existing_tunnel
    
    # Start ngrok container
    if start_ngrok_container; then
        check_tunnel_status
    else
        echo -e "${RED}[ERROR] Failed to start tunnel${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${GREEN}[SUCCESS] Tunnel setup complete!${NC}"
    echo -e "${CYAN}[INFO] Commands:${NC}"
    echo -e "${BLUE}• Check status: ./start-tunnel.sh --status${NC}"
    echo -e "${BLUE}• Stop tunnel: ./start-tunnel.sh --stop${NC}"
    echo -e "${BLUE}• Web interface: http://localhost:4040${NC}"
}

# Execute main function
main "$@"