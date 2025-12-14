#!/bin/bash

# Test script for Google Translate API
# This script tests the translation endpoint to verify the API key is working correctly

echo "=================================="
echo "Google Translate API Test"
echo "=================================="
echo ""

# Configuration
BASE_URL="http://localhost:8080"
ENDPOINT="/api/v1/dictionary/translate"

# Test cases
echo "Test 1: Translate 'Hello' to Vietnamese (vi)"
echo "Request: GET ${BASE_URL}${ENDPOINT}/vi/Hello"
echo ""
curl -X GET "${BASE_URL}${ENDPOINT}/vi/Hello" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || curl -X GET "${BASE_URL}${ENDPOINT}/vi/Hello" -H "Content-Type: application/json" -w "\n\nHTTP Status: %{http_code}\n" -s

echo ""
echo "=================================="
echo ""

echo "Test 2: Translate 'Good morning' to Vietnamese (vi)"
echo "Request: GET ${BASE_URL}${ENDPOINT}/vi/Good%20morning"
echo ""
curl -X GET "${BASE_URL}${ENDPOINT}/vi/Good%20morning" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || curl -X GET "${BASE_URL}${ENDPOINT}/vi/Good%20morning" -H "Content-Type: application/json" -w "\n\nHTTP Status: %{http_code}\n" -s

echo ""
echo "=================================="
echo ""

echo "Test 3: Translate 'Thank you' to Spanish (es)"
echo "Request: GET ${BASE_URL}${ENDPOINT}/es/Thank%20you"
echo ""
curl -X GET "${BASE_URL}${ENDPOINT}/es/Thank%20you" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || curl -X GET "${BASE_URL}${ENDPOINT}/es/Thank%20you" -H "Content-Type: application/json" -w "\n\nHTTP Status: %{http_code}\n" -s

echo ""
echo "=================================="
echo ""

echo "Test 4: Translate 'Computer' to French (fr)"
echo "Request: GET ${BASE_URL}${ENDPOINT}/fr/Computer"
echo ""
curl -X GET "${BASE_URL}${ENDPOINT}/fr/Computer" \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  -s | jq '.' 2>/dev/null || curl -X GET "${BASE_URL}${ENDPOINT}/fr/Computer" -H "Content-Type: application/json" -w "\n\nHTTP Status: %{http_code}\n" -s

echo ""
echo "=================================="
echo "Test completed!"
echo "=================================="
echo ""
echo "Expected results:"
echo "- HTTP Status: 200"
echo "- statusCode: 200"
echo "- message: 'Translation successful'"
echo "- data: Translated text in target language"
echo ""
echo "If you see errors, check:"
echo "1. Backend service is running on port 8080"
echo "2. Google Translate API key is valid"
echo "3. API key has Translation API enabled"
echo "=================================="