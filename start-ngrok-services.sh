#!/bin/bash

# Start unified ngrok service for both pronunciation-assessment-service and content-scoring-service
echo "Starting unified ngrok service for both services..."

# Navigate to ngrok-service directory
cd ngrok-service

# Start ngrok with the unified configuration
ngrok start --all --config=ngrok.yml --log=stdout

echo "Unified ngrok service started with pooling-enabled for both pronunciation-assessment-service and content-scoring-service."
echo "Web interface available at: http://localhost:4040"
echo "Tunnels:"
echo "  - pronunciation-assessment: port 5000"
echo "  - content-scoring: port 5001"