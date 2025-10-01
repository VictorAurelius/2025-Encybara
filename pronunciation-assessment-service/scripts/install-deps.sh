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

# Check if Python 3 is installed (try multiple command variations)
PYTHON_CMD=""
if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
elif command -v python &> /dev/null; then
    # Check if it's Python 3
    PYTHON_VERSION=$(python --version 2>&1)
    if [[ $PYTHON_VERSION == Python\ 3.* ]]; then
        PYTHON_CMD="python"
    fi
fi

if [ -z "$PYTHON_CMD" ]; then
    echo -e "${RED}Error: Python 3 is not installed${NC}"
    echo -e "${YELLOW}Please install Python 3.8+ first${NC}"
    echo "Available Python versions:"
    command -v python && python --version || echo "  python: not found"
    command -v python3 && python3 --version || echo "  python3: not found"
    exit 1
fi

PYTHON_VERSION=$($PYTHON_CMD --version)
echo -e "${GREEN}✓ Found: ${PYTHON_VERSION}${NC}"

# Check if pip is installed (try multiple variations)
PIP_CMD=""
if command -v pip3 &> /dev/null; then
    PIP_CMD="pip3"
elif command -v pip &> /dev/null; then
    # Check if pip is for Python 3
    PIP_VERSION=$(pip --version 2>&1)
    if [[ $PIP_VERSION == *"python 3"* ]]; then
        PIP_CMD="pip"
    fi
fi

if [ -z "$PIP_CMD" ]; then
    echo -e "${YELLOW}Warning: pip not found, will install using Python module${NC}"
else
    echo -e "${GREEN}✓ Found: $PIP_CMD${NC}"
fi

# Create virtual environment if it doesn't exist
VENV_DIR="venv"
if [ ! -d "$VENV_DIR" ]; then
    echo -e "${YELLOW}Creating virtual environment...${NC}"
    $PYTHON_CMD -m venv $VENV_DIR
    echo -e "${GREEN}✓ Virtual environment created${NC}"
else
    echo -e "${GREEN}✓ Virtual environment already exists${NC}"
fi

# Activate virtual environment
echo -e "${YELLOW}Activating virtual environment...${NC}"

# Detect the correct activation script path (Windows vs Linux/macOS)
if [ -f "$VENV_DIR/Scripts/activate" ]; then
    # Windows path
    source $VENV_DIR/Scripts/activate
elif [ -f "$VENV_DIR/bin/activate" ]; then
    # Linux/macOS path
    source $VENV_DIR/bin/activate
else
    echo -e "${RED}Error: Virtual environment activation script not found${NC}"
    echo "Expected locations:"
    echo "  Windows: $VENV_DIR/Scripts/activate"
    echo "  Linux/macOS: $VENV_DIR/bin/activate"
    exit 1
fi

# Upgrade pip and install essential build tools
echo -e "${YELLOW}Upgrading pip and installing build tools...${NC}"
pip install --upgrade pip setuptools wheel

# Install requirements
echo -e "${YELLOW}Installing dependencies...${NC}"

# Determine Python version and select appropriate requirements file
PYTHON_MAJOR_MINOR=$($PYTHON_CMD --version | grep -oE '[0-9]+\.[0-9]+')
echo "Detected Python version: $PYTHON_MAJOR_MINOR"

REQUIREMENTS_FILE=""
if [ -f "../requirements-py313.txt" ] && [[ "$PYTHON_MAJOR_MINOR" == "3.13" ]]; then
    REQUIREMENTS_FILE="../requirements-py313.txt"
    echo -e "${YELLOW}Using Python 3.13 compatible requirements${NC}"
elif [ -f "../requirements.txt" ]; then
    REQUIREMENTS_FILE="../requirements.txt"
    echo -e "${YELLOW}Using standard requirements${NC}"
elif [ -f "requirements-py313.txt" ] && [[ "$PYTHON_MAJOR_MINOR" == "3.13" ]]; then
    # Fallback: if running from project root
    REQUIREMENTS_FILE="requirements-py313.txt"
    echo -e "${YELLOW}Using Python 3.13 compatible requirements${NC}"
elif [ -f "requirements.txt" ]; then
    # Fallback: if running from project root
    REQUIREMENTS_FILE="requirements.txt"
    echo -e "${YELLOW}Using standard requirements${NC}"
else
    echo -e "${RED}Error: No requirements.txt found${NC}"
    echo "Expected locations:"
    echo "  ../requirements.txt or ../requirements-py313.txt (when running from scripts/)"
    echo "  requirements.txt or requirements-py313.txt (when running from project root)"
    echo ""
    echo "Current working directory: $(pwd)"
    echo "Please make sure you're in the pronunciation-assessment-service directory"
    exit 1
fi

# Install dependencies
echo -e "${YELLOW}Installing dependencies from $REQUIREMENTS_FILE...${NC}"
pip install -r "$REQUIREMENTS_FILE"
echo -e "${GREEN}✓ Dependencies installed successfully${NC}"

# Check if all important packages are installed
echo -e "${YELLOW}Verifying installation...${NC}"
$PYTHON_CMD -c "
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