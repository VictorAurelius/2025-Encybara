#!/bin/bash

# Get tunnel URLs for both services from ngrok API
echo "Getting tunnel URLs..."

# Get ngrok tunnel information
curl -s http://localhost:4040/api/tunnels | jq -r '.tunnels[] | "\(.name): \(.public_url)"'

echo "Available tunnels listed above."