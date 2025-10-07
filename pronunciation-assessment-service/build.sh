#!/bin/bash

echo "Building WhisperX Pronunciation Assessment Service..."

docker compose down --volumes --remove-orphans 2>/dev/null || true
docker rmi pronunciation-assessment-service 2>/dev/null || true

docker build --no-cache -t pronunciation-assessment-service .

echo "Starting service..."
docker compose up -d

echo "Waiting for service..."
sleep 15

for i in {1..20}; do
    if curl -s http://localhost:5000/health > /dev/null 2>&1; then
        echo "✓ Service ready!"
        break
    else
        echo "Waiting... ($i/20)"
        sleep 2
    fi
done

echo ""
echo "Service endpoints:"
echo "• Health: http://localhost:5000/health"  
echo "• Assessment: http://localhost:5000/api/pronunciation-assessment"
echo ""
echo "Test: python3 test_whisperx.py"