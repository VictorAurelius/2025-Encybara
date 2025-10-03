#!/bin/bash

# Get tunnel URLs for both services from ngrok API
echo "Getting tunnel URLs..."

# Check if ngrok API is accessible
if ! curl -s http://localhost:4040/api/tunnels > /dev/null 2>&1; then
    echo "❌ Cannot connect to ngrok API at http://localhost:4040"
    echo "💡 Make sure ngrok container is running:"
    echo "   docker-compose ps"
    echo "   ./start-container.sh"
    exit 1
fi

# Get ngrok tunnel information
TUNNELS=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null)

if [ -z "$TUNNELS" ]; then
    echo "❌ No tunnel data received"
    exit 1
fi

echo "🌐 Active Tunnels:"
echo "────────────────────────────────────────────────────────────────"

# Parse JSON without jq (basic parsing)
if command -v jq >/dev/null 2>&1; then
    # Use jq if available
    echo "$TUNNELS" | jq -r '.tunnels[] | "  🔗 \(.name): \(.public_url)"'
else
    # Parse without jq
    echo "$TUNNELS" | grep -o '"name":"[^"]*"' | sed 's/"name":"//g' | sed 's/"//g' | while read name; do
        # Get URL for this tunnel
        url=$(echo "$TUNNELS" | grep -A10 "\"name\":\"$name\"" | grep -o '"public_url":"[^"]*"' | head -1 | sed 's/"public_url":"//g' | sed 's/"//g')
        if [ -n "$url" ]; then
            echo "  🔗 $name: $url"
        fi
    done
fi

echo ""
echo "📊 Web Interface: http://localhost:4040"
echo "📋 API Endpoint: http://localhost:4040/api/tunnels"