#!/bin/bash

# Simple Windows-compatible build script for WhisperX
echo "==================================================="
echo "WhisperX Pronunciation Assessment Service - Build"
echo "==================================================="

# Clean up old containers and images
echo "Cleaning up old containers..."
docker compose down --volumes --remove-orphans 2>/dev/null || true
docker images | grep "pronunciation-assessment-service" | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

# Build with CPU-only Dockerfile
echo "Building CPU-only WhisperX service (no CUDA downloads)..."
echo "This should take 3-5 minutes..."

docker build --no-cache -f Dockerfile.cpu-only -t pronunciation-assessment-service . || {
    echo "ERROR: Build failed!"
    exit 1
}

echo "Build completed successfully!"
echo ""

# Start the service
echo "Starting WhisperX service..."
docker compose up -d

echo ""
echo "Waiting for service to be ready..."
sleep 15

# Check health
for i in {1..20}; do
    if curl -s http://localhost:5000/health > /dev/null 2>&1; then
        echo "✓ Service is healthy and ready!"
        break
    else
        echo "Waiting... ($i/20)"
        sleep 3
    fi
    
    if [ $i -eq 20 ]; then
        echo "⚠ Service might still be starting. Check logs manually."
    fi
done

echo ""
echo "🎉 WhisperX Pronunciation Assessment Service is ready!"
echo ""
echo "Available endpoints:"
echo "• Health: http://localhost:5000/health"
echo "• Info: http://localhost:5000/api/info"  
echo "• Assessment: http://localhost:5000/api/pronunciation-assessment"
echo ""
echo "Test command:"
echo "python3 test_whisperx.py"
echo ""
echo "View logs:"
echo "docker logs pronunciation-assessment-service-whisperx"