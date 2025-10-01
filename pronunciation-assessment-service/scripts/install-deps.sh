#!/bin/bash

# Pronunciation Assessment Service - Install Dependencies Script
# This script installs Python dependencies for running the service locally

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Pronunciation Assessment Service - Install Dependencies ===${NC}"
echo ""

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Error: Python 3 is not installed${NC}"
    echo -e "${YELLOW}Please install Python 3.8+ first${NC}"
    exit 1
fi

PYTHON_VERSION=$(python3 --version)
echo -e "${GREEN}✓ Found: ${PYTHON_VERSION}${NC}"

# Check if pip is installed
if ! command -v pip3 &> /dev/null; then
    echo -e "${RED}Error: pip3 is not installed${NC}"
    echo -e "${YELLOW}Please install pip3 first${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found: pip3${NC}"

# Create virtual environment if it doesn't exist
VENV_DIR="venv"
if [ ! -d "$VENV_DIR" ]; then
    echo -e "${YELLOW}Creating virtual environment...${NC}"
    python3 -m venv $VENV_DIR
    echo -e "${GREEN}✓ Virtual environment created${NC}"
else
    echo -e "${GREEN}✓ Virtual environment already exists${NC}"
fi

# Activate virtual environment
echo -e "${YELLOW}Activating virtual environment...${NC}"
source $VENV_DIR/bin/activate

# Upgrade pip
echo -e "${YELLOW}Upgrading pip...${NC}"
pip install --upgrade pip

# Install requirements
echo -e "${YELLOW}Installing dependencies from requirements.txt...${NC}"
if [ -f "requirements.txt" ]; then
    pip install -r requirements.txt
    echo -e "${GREEN}✓ Dependencies installed successfully${NC}"
else
    echo -e "${RED}Error: requirements.txt not found${NC}"
    exit 1
fi

# Check if all important packages are installed
echo -e "${YELLOW}Verifying installation...${NC}"
python3 -c "
import flask
import librosa
import numpy
import scipy
print('✓ All core packages imported successfully')
"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Installation verification passed${NC}"
else
    echo -e "${RED}Error: Some packages failed to import${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}=== Installation Complete ===${NC}"
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Activate virtual environment: ${BLUE}source venv/bin/activate${NC}"
echo -e "  2. Run the service: ${BLUE}./scripts/run-local.sh${NC}"
echo -e "  3. Test the service: ${BLUE}./scripts/test-local.sh${NC}"
echo ""
echo -e "${YELLOW}To deactivate virtual environment: ${BLUE}deactivate${NC}"