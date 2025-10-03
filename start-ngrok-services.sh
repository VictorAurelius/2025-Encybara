#!/bin/bash

# Start unified ngrok service as Docker container
echo "🐳 Starting unified ngrok service as Docker container..."

# Navigate to ngrok-service directory
cd ngrok-service

# Check if container startup script exists
if [ -f "start-container.sh" ]; then
    echo "📦 Using containerized ngrok service..."
    chmod +x start-container.sh
    ./start-container.sh
else
    echo "❌ Container startup script not found!"
    echo "Please run from the correct directory or check if start-container.sh exists"
    exit 1
fi