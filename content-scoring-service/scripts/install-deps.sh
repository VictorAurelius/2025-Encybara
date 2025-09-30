#!/bin/bash

# Simple dependency installer script
set -e

echo "🚀 Installing Python dependencies step by step"

# Change to the project root directory
cd "$(dirname "$0")/.."

# Activate virtual environment
echo "🔧 Activating virtual environment..."
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    source venv/Scripts/activate
else
    source venv/bin/activate
fi

# Upgrade pip first
echo "📦 Upgrading pip..."
pip install --upgrade pip

# Install core dependencies first
echo "📥 Installing core dependencies..."
pip install "fastapi>=0.104.1"
pip install "uvicorn[standard]>=0.24.0"
pip install "pydantic>=2.5.0"

# Install utilities
echo "📥 Installing utilities..."
pip install "python-multipart>=0.0.6"
pip install "python-jose[cryptography]>=3.3.0"
pip install "passlib[bcrypt]>=1.7.4"

# Install monitoring
echo "📥 Installing monitoring tools..."
pip install "prometheus-client>=0.19.0"

# Install testing dependencies
echo "📥 Installing testing dependencies..."
pip install "pytest>=7.4.3"
pip install "pytest-asyncio>=0.21.1"
pip install "httpx>=0.25.2"
pip install "pytest-cov>=4.1.0"

# Install ML dependencies with wheels
echo "📥 Installing ML dependencies (this may take a while)..."
pip install numpy
pip install scikit-learn
pip install sentence-transformers
pip install spacy

echo "✅ All dependencies installed successfully!"