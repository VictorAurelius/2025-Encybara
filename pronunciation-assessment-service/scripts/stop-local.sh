#!/bin/bash
# Pronunciation Assessment Service - Stop Local Service Script
# This script stops the locally running Flask service

set -e

echo "=== Pronunciation Assessment Service - Stop Local Service ==="

# Find and kill the Flask process
FLASK_PID=$(ps aux | grep "python.*app.py" | grep -v grep | awk '{print $2}' | head -1)

if [ -n "$FLASK_PID" ]; then
    echo "Found Flask service running with PID: $FLASK_PID"
    echo "Stopping service..."
    
    # Try graceful shutdown first
    kill -TERM $FLASK_PID 2>/dev/null || true
    
    # Wait a bit for graceful shutdown
    sleep 2
    
    # Check if still running
    if ps -p $FLASK_PID > /dev/null 2>&1; then
        echo "Service didn't stop gracefully, forcing shutdown..."
        kill -KILL $FLASK_PID 2>/dev/null || true
    fi
    
    # Verify it's stopped
    sleep 1
    if ps -p $FLASK_PID > /dev/null 2>&1; then
        echo "❌ Failed to stop service (PID: $FLASK_PID)"
        exit 1
    else
        echo "✅ Service stopped successfully"
    fi
else
    echo "ℹ️  No Flask service found running"
fi

# Also check for any other Python processes that might be the service
OTHER_PIDS=$(ps aux | grep -E "python.*(flask|pronunciation|assessment)" | grep -v grep | grep -v "stop-local" | awk '{print $2}')

if [ -n "$OTHER_PIDS" ]; then
    echo "Found other related processes: $OTHER_PIDS"
    echo "Stopping them as well..."
    echo "$OTHER_PIDS" | xargs kill -TERM 2>/dev/null || true
    sleep 2
    echo "$OTHER_PIDS" | xargs kill -KILL 2>/dev/null || true
fi

# Check if port 5000 is still in use
if command -v lsof > /dev/null 2>&1; then
    PORT_USAGE=$(lsof -ti:5000 2>/dev/null || true)
    if [ -n "$PORT_USAGE" ]; then
        echo "Port 5000 is still in use by process(es): $PORT_USAGE"
        echo "Killing processes using port 5000..."
        echo "$PORT_USAGE" | xargs kill -TERM 2>/dev/null || true
        sleep 2
        echo "$PORT_USAGE" | xargs kill -KILL 2>/dev/null || true
    fi
fi

echo ""
echo "=== Service Status ==="
if curl -s -f http://localhost:5000 > /dev/null 2>&1; then
    echo "❌ Service is still responding (may take a moment to fully stop)"
else
    echo "✅ Service is not responding - successfully stopped"
fi

echo ""
echo "To start the service again, run:"
echo "  ./scripts/run-local.sh"