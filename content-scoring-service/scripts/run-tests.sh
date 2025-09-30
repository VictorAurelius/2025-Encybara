#!/bin/bash

# Test runner script with better error handling
set -e

echo "🧪 Running Content Scoring Service Tests"

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

# Check if dependencies are installed
echo "🔍 Checking dependencies..."
if ! python -c "import fastapi, uvicorn, pydantic, pytest" 2>/dev/null; then
    echo "❌ Required dependencies not found. Please run ./run-dev.sh first to install dependencies."
    exit 1
fi

# Check if app can be imported
echo "🔍 Checking if app can be imported..."
if ! python -c "from app.main import app" 2>/dev/null; then
    echo "❌ Cannot import app. There may be missing dependencies or syntax errors."
    echo "💡 Try running: python -c 'from app.main import app' to see the specific error"
    exit 1
fi

# Run tests with various options
echo "🧪 Running tests with timeout (60 seconds)..."
echo "📝 Test options: verbose, short traceback, stop on first failure"
echo ""

# Try different test running strategies
if command -v timeout >/dev/null 2>&1; then
    # Unix/Linux/Git Bash with timeout command
    timeout 60s pytest tests/ -v --tb=short -x --disable-warnings
else
    # Windows without timeout - use pytest-timeout if available
    if python -c "import pytest_timeout" 2>/dev/null; then
        pytest tests/ -v --tb=short -x --disable-warnings --timeout=60
    else
        echo "⚠️  No timeout available, running tests without timeout..."
        pytest tests/ -v --tb=short -x --disable-warnings
    fi
fi

echo ""
echo "✅ Tests completed successfully!"