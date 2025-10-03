#!/bin/bash

# Start unified ngrok service for both pronunciation-assessment and content-scoring services
echo "Starting unified ngrok service for both pronunciation-assessment and content-scoring services..."

# Navigate to ngrok-service directory
cd "$(dirname "$0")"

# Start ngrok with the unified configuration
ngrok start --all --config=ngrok.yml --log=stdout

echo "Unified ngrok service started with pooling-enabled for both services."