#!/bin/bash

# Fast Docker entrypoint script for Content Scoring Service
set -e

echo "🚀 Starting Content Scoring Service (Fast Build)"

# Basic dependency check
echo "🔍 Verifying core dependencies..."
python -c "
try:
    import fastapi
    import uvicorn
    import pydantic
    from app.main import app
    print('✅ Core dependencies verified')
except ImportError as e:
    print(f'❌ Missing dependency: {e}')
    exit(1)
except Exception as e:
    print(f'⚠️  Warning: {e}')
"

# Start the application
echo "🌟 Starting FastAPI server..."
echo "🔗 API Documentation: http://localhost:5001/docs"
echo "💓 Health Check: http://localhost:5001/health"

exec uvicorn app.main:app --host 0.0.0.0 --port 5001