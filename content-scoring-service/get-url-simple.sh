#!/bin/bash

# =============================================================================
# get-url-simple.sh - Simple URL Getter for Ngrok
# =============================================================================
# Script đơn giản để lấy public URL từ ngrok container
# =============================================================================

echo "[INFO] Getting Ngrok public URL..."

# Method 1: Try localhost API
echo "[INFO] Trying localhost API..."
URL=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null | grep -o '"public_url":"https://[^"]*"' | sed 's/"public_url":"//g' | sed 's/"//g' | head -1)

if [[ -n "$URL" ]]; then
    echo "[SUCCESS] Found URL via localhost API: $URL"
    echo "$URL"
    exit 0
fi

# Method 2: Try from container logs
echo "[INFO] Trying container logs..."
URL=$(docker logs content-scoring-ngrok 2>&1 | grep -o 'https://[a-zA-Z0-9-]*\.ngrok[a-zA-Z0-9.-]*' | head -1)

if [[ -n "$URL" ]]; then
    echo "[SUCCESS] Found URL via logs: $URL"
    echo "$URL"
    exit 0
fi

# Method 3: Try container command
echo "[INFO] Trying container command..."
URL=$(docker exec content-scoring-ngrok ps aux 2>/dev/null | grep -o 'https://[a-zA-Z0-9-]*\.ngrok[a-zA-Z0-9.-]*' | head -1)

if [[ -n "$URL" ]]; then
    echo "[SUCCESS] Found URL via container: $URL"
    echo "$URL"
    exit 0
fi

echo "[ERROR] Could not find public URL"
echo "[HINT] Check ngrok web interface: http://localhost:4040"
exit 1