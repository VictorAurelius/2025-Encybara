#!/bin/bash

# Development script for Content Scoring Service
set -e

echo "🚀 Starting Content Scoring Service Development Environment"

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python -m venv venv
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "📥 Installing dependencies..."
pip install -r requirements.txt

# Download spaCy model
echo "🔤 Downloading spaCy model..."
python -m spacy download en_core_web_sm

# Run tests
echo "🧪 Running tests..."
pytest tests/ -v

# Start the development server
echo "🌟 Starting development server..."
echo "API Documentation: http://localhost:5001/docs"
echo "Health Check: http://localhost:5001/health"
echo "Metrics: http://localhost:5001/metrics"
echo ""
echo "Press Ctrl+C to stop the server"

uvicorn app.main:app --host 0.0.0.0 --port 5001 --reload