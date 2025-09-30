#!/bin/bash

# Quick server start script without tests
set -e

echo "🚀 Starting Content Scoring Service Server"

# Change to the project root directory
cd "$(dirname "$0")/.."

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "❌ Virtual environment not found. Please run ./run-dev.sh first to set up the environment."
    exit 1
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    source venv/Scripts/activate
else
    source venv/bin/activate
fi

# Check if core dependencies are installed
if ! python -c "import fastapi, uvicorn" 2>/dev/null; then
    echo "❌ Core dependencies not found. Please run ./run-dev.sh first to install dependencies."
    exit 1
fi

# Start the development server
echo "🌟 Starting development server..."
echo "API Documentation: http://localhost:5001/docs"
echo "Health Check: http://localhost:5001/health"
echo "Metrics: http://localhost:5001/metrics"
echo ""
echo "Press Ctrl+C to stop the server"

uvicorn app.main:app --host 0.0.0.0 --port 5001 --reload