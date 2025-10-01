#!/bin/bash

# Pronunciation Assessment Service - Run Local Script
# This script runs the Flask app directly without Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_NAME="Pronunciation Assessment Service"
HOST="0.0.0.0"
PORT="5000"
VENV_DIR="venv"

echo -e "${BLUE}=== $SERVICE_NAME - Local Run ===${NC}"
echo -e "${YELLOW}Host:${NC} $HOST"
echo -e "${YELLOW}Port:${NC} $PORT"
echo -e "${YELLOW}Service URL:${NC} http://localhost:$PORT"
echo ""

# Check if virtual environment exists
if [ ! -d "$VENV_DIR" ]; then
    echo -e "${RED}Error: Virtual environment not found${NC}"
    echo -e "${YELLOW}Please run: ${BLUE}./scripts/install-deps.sh${NC} first"
    exit 1
fi

# Check if port is available
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo -e "${YELLOW}Warning: Port $PORT is already in use${NC}"
    echo -e "${YELLOW}Finding process using port $PORT...${NC}"
    lsof -Pi :$PORT -sTCP:LISTEN
    echo ""
    read -p "Do you want to kill the process and continue? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Killing process on port $PORT...${NC}"
        lsof -ti:$PORT | xargs kill -9 2>/dev/null || true
        sleep 2
    else
        echo -e "${RED}Aborted${NC}"
        exit 1
    fi
fi

# Check if required files exist
if [ ! -f "app.py" ]; then
    echo -e "${RED}Error: app.py not found${NC}"
    echo -e "${YELLOW}Please make sure you're in the pronunciation-assessment-service directory${NC}"
    exit 1
fi

if [ ! -f "gop_scorer.py" ]; then
    echo -e "${RED}Error: gop_scorer.py not found${NC}"
    exit 1
fi

if [ ! -f "utils.py" ]; then
    echo -e "${RED}Error: utils.py not found${NC}"
    exit 1
fi

# Activate virtual environment
echo -e "${YELLOW}Activating virtual environment...${NC}"
source $VENV_DIR/bin/activate

# Check if Flask is installed
if ! python -c "import flask" 2>/dev/null; then
    echo -e "${RED}Error: Flask is not installed${NC}"
    echo -e "${YELLOW}Please run: ${BLUE}./scripts/install-deps.sh${NC} first"
    exit 1
fi

# Set environment variables
export FLASK_APP=app.py
export FLASK_ENV=development
export SECRET_KEY="pronunciation-local-$(date +%s)"

echo -e "${GREEN}✓ Environment configured${NC}"
echo -e "${YELLOW}Starting Flask development server...${NC}"
echo ""

# Create temp directory if it doesn't exist
mkdir -p temp

# Start the application
echo -e "${BLUE}Starting $SERVICE_NAME...${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop the service${NC}"
echo ""

# Run with python directly
python app.py || {
    echo -e "${RED}Error: Failed to start the service${NC}"
    echo -e "${YELLOW}Check the error messages above${NC}"
    exit 1
}