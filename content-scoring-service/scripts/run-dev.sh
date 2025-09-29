#!/bin/bash

# Development script for Content Scoring Service
set -e

echo "🚀 Starting Content Scoring Service Development Environment"

# Change to the project root directory (parent of scripts)
cd "$(dirname "$0")/.."

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python -m venv venv
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
# Check if we're on Windows (Git Bash/MINGW64) or Unix-like system
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    source venv/Scripts/activate
else
    source venv/bin/activate
fi

# Install dependencies
echo "📥 Installing dependencies..."
# Check if all key dependencies are installed
if ! python -c "import fastapi, uvicorn, pydantic, spacy, sklearn, sentence_transformers" 2>/dev/null; then
    echo "🔧 Running dependency installer..."
    bash scripts/install-deps.sh
else
    echo "✅ Dependencies already installed, skipping installation"
fi

# Download spaCy model
echo "🔤 Downloading spaCy model..."
if ! python -c "import spacy; spacy.load('en_core_web_sm')" 2>/dev/null; then
    python -m spacy download en_core_web_sm
else
    echo "✅ spaCy model already downloaded"
fi

# Run tests
echo "🧪 Running tests..."
echo "⏳ Test timeout: 60 seconds"
# Run tests with timeout and more verbose output
timeout 60s pytest tests/ -v --tb=short -x --disable-warnings || {
    echo "⚠️  Tests failed or timed out, but continuing to start server..."
    echo "💡 You can run tests manually later with: pytest tests/ -v"
}

# Start the development server
echo "🌟 Starting development server..."
echo "API Documentation: http://localhost:5001/docs"
echo "Health Check: http://localhost:5001/health"
echo "Metrics: http://localhost:5001/metrics"
echo ""
echo "Press Ctrl+C to stop the server"

uvicorn app.main:app --host 0.0.0.0 --port 5001 --reload