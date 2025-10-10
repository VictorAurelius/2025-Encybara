#!/bin/bash

# Quick test to verify audioLink data

BACKEND_URL="http://localhost:8080"

# Get token
echo "Getting token..."
TOKEN_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{"username":"user@example.com","password":"Abc@123456"}' \
    "${BACKEND_URL}/api/v1/auth/login")

echo "Token response: $TOKEN_RESPONSE"

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
if [[ -z "$ACCESS_TOKEN" ]]; then
    ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
fi

echo "Access token: ${ACCESS_TOKEN:0:20}..."

# Test basic API call
echo -e "\n=== Testing basic API call ==="
echo "URL: ${BACKEND_URL}/api/v1/speaking-sample-answers/question/550"

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    "${BACKEND_URL}/api/v1/speaking-sample-answers/question/550")

echo "Response: $RESPONSE"

# Extract just the response body
RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')
HTTP_STATUS=$(echo "$RESPONSE" | tail -n1 | sed 's/HTTP_STATUS://')

echo -e "\nHTTP Status: $HTTP_STATUS"
echo -e "\nResponse Body: $RESPONSE_BODY"

# Check for audioLink in response
if [[ "$RESPONSE_BODY" == *"audioLink"* ]]; then
    echo -e "\n✓ Found audioLink in response!"
    echo "$RESPONSE_BODY" | grep -o '"audioLink":"[^"]*"' || echo "Found audioLink field but value might be null"
else
    echo -e "\n✗ No audioLink found in response"
fi