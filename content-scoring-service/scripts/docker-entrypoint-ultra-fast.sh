#!/bin/bash

# Ultra-fast Docker entrypoint script - NO ML dependencies
set -e

echo "⚡ Starting Content Scoring Service (Ultra-Fast Build - NO ML)"

# Basic dependency check (skip ML libraries)
echo "🔍 Verifying core dependencies..."
python -c "
try:
    import fastapi
    import uvicorn
    import pydantic
    import nltk
    import numpy
    from app.main import app
    print('✅ Core dependencies verified (ML libraries disabled)')
except ImportError as e:
    print(f'❌ Missing dependency: {e}')
    exit(1)
except Exception as e:
    print(f'⚠️  Warning: {e}')
"

# Download basic NLTK data if needed
echo "📦 Setting up basic NLP data..."
python -c "
import nltk
try:
    nltk.download('punkt', quiet=True)
    nltk.download('stopwords', quiet=True)
    print('✅ Basic NLP data ready')
except:
    print('⚠️  NLP data download failed, continuing...')
"

# Start the application
echo "🌟 Starting FastAPI server..."
echo "🔗 API Documentation: http://localhost:5001/docs"
echo "💓 Health Check: http://localhost:5001/health"
echo "⚠️  Note: ML features will return mock responses"

exec uvicorn app.main:app --host 0.0.0.0 --port 5001