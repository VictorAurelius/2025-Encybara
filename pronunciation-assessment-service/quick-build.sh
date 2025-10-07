#!/bin/bash

echo "Building WhisperX Pronunciation Assessment Service (Optimized)"
echo "============================================================"

# Stop any running containers
docker compose down --volumes --remove-orphans 2>/dev/null || true

# Remove old images
docker images | grep "^pronunciation-assessment-service " | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

# Copy optimized Dockerfile
cp Dockerfile.optimized Dockerfile.temp

# Build with optimized Dockerfile
echo "Building with optimized Dockerfile (should take 3-5 minutes)..."
timeout 3600 docker build --no-cache -f Dockerfile.temp -t pronunciation-assessment-service . || {
    echo "Build failed!"
    rm -f Dockerfile.temp
    exit 1
}

# Remove temp Dockerfile
rm -f Dockerfile.temp

echo "Build completed successfully!"
echo "Starting service..."

# Start the service
docker compose up -d

echo "Service started. Checking health..."
sleep 10

# Wait for health check
for i in {1..30}; do
    if curl -s http://localhost:5000/health > /dev/null 2>&1; then
        echo "✓ Service is healthy and ready!"
        break
    else
        echo "Waiting for service... ($i/30)"
        sleep 2
    fi
done

echo ""
echo "WhisperX service is ready!"
echo "• Health: http://localhost:5000/health"
echo "• Test: python3 test_whisperx.py"
echo "• Logs: docker logs pronunciation-assessment-service-whisperx"