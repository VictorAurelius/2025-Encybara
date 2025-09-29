#!/bin/bash

# Docker entrypoint script for Content Scoring Service
set -e

echo "🚀 Starting Content Scoring Service Container"

# Check if spaCy model is available, download if not
echo "🔍 Checking spaCy model..."
if ! python -c "import spacy; spacy.load('en_core_web_sm')" 2>/dev/null; then
    echo "📦 Downloading spaCy model with progress tracking..."
    python ./scripts/download-tracker.py --spacy-model en_core_web_sm --threshold 50 || {
        echo "⚠️ Download tracker failed, using standard download method"
        python -m spacy download en_core_web_sm || {
            echo "⚠️  Warning: Failed to download spaCy model, service may have limited functionality"
        }
    }
else
    echo "✅ spaCy model already available"
fi

# Verify core dependencies
echo "🔍 Verifying core dependencies..."
python -c "
try:
    import fastapi
    import uvicorn
    import pydantic
    from app.main import app
    print('✅ All core dependencies verified')
except ImportError as e:
    print(f'❌ Missing dependency: {e}')
    exit(1)
except Exception as e:
    print(f'⚠️  Warning: {e}')
"

# Start the application
echo "🌟 Starting Content Scoring Service..."
echo "🔗 API Documentation: http://localhost:5001/docs"
echo "💓 Health Check: http://localhost:5001/health"
echo "📊 Metrics: http://localhost:5001/metrics"

exec uvicorn app.main:app --host 0.0.0.0 --port 5001