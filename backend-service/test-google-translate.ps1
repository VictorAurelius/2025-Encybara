# Test script for Google Translate API
# This script tests the translation endpoint to verify the API key is working correctly

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Google Translate API Test" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$BASE_URL = "http://localhost:8080"
$ENDPOINT = "/api/v1/dictionary/translate"
$AUTH_ENDPOINT = "/api/v1/auth/login"

# Default user credentials
$DEFAULT_EMAIL = "user@example.com"
$DEFAULT_PASSWORD = "Abc@123456"

# Get authentication token
Write-Host "Getting authentication token..." -ForegroundColor Yellow
$loginData = @{
    username = $DEFAULT_EMAIL
    password = $DEFAULT_PASSWORD
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "${BASE_URL}${AUTH_ENDPOINT}" -Method Post -Body $loginData -ContentType "application/json"
    $ACCESS_TOKEN = $authResponse.data.access_token
    if (-not $ACCESS_TOKEN) {
        $ACCESS_TOKEN = $authResponse.data.accessToken
    }
    if ($ACCESS_TOKEN) {
        Write-Host "Authentication successful" -ForegroundColor Green
        Write-Host "Access token: $($ACCESS_TOKEN.Substring(0, [Math]::Min(20, $ACCESS_TOKEN.Length)))..." -ForegroundColor Gray
    } else {
        Write-Host "Failed to extract access token" -ForegroundColor Red
        Write-Host "Response: $($authResponse | ConvertTo-Json -Depth 10)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Authentication failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------" -ForegroundColor Gray
Write-Host ""

# Prepare headers
$headers = @{
    "Authorization" = "Bearer $ACCESS_TOKEN"
    "Content-Type" = "application/json"
}

# Test 1
Write-Host "Test 1: Translate 'Hello' to Vietnamese (vi)" -ForegroundColor Yellow
Write-Host "Request: GET ${BASE_URL}${ENDPOINT}/vi/Hello" -ForegroundColor Gray
Write-Host ""

try {
    $response1 = Invoke-RestMethod -Uri "${BASE_URL}${ENDPOINT}/vi/Hello" -Method Get -Headers $headers
    Write-Host "Response:" -ForegroundColor Green
    $response1 | ConvertTo-Json -Depth 10
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Test 2
Write-Host "Test 2: Translate 'Good morning' to Vietnamese (vi)" -ForegroundColor Yellow
Write-Host "Request: GET ${BASE_URL}${ENDPOINT}/vi/Good morning" -ForegroundColor Gray
Write-Host ""

try {
    $response2 = Invoke-RestMethod -Uri "${BASE_URL}${ENDPOINT}/vi/Good morning" -Method Get -Headers $headers
    Write-Host "Response:" -ForegroundColor Green
    $response2 | ConvertTo-Json -Depth 10
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Test 3
Write-Host "Test 3: Translate 'Thank you' to Spanish (es)" -ForegroundColor Yellow
Write-Host "Request: GET ${BASE_URL}${ENDPOINT}/es/Thank you" -ForegroundColor Gray
Write-Host ""

try {
    $response3 = Invoke-RestMethod -Uri "${BASE_URL}${ENDPOINT}/es/Thank you" -Method Get -Headers $headers
    Write-Host "Response:" -ForegroundColor Green
    $response3 | ConvertTo-Json -Depth 10
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Test 4
Write-Host "Test 4: Translate 'Computer' to French (fr)" -ForegroundColor Yellow
Write-Host "Request: GET ${BASE_URL}${ENDPOINT}/fr/Computer" -ForegroundColor Gray
Write-Host ""

try {
    $response4 = Invoke-RestMethod -Uri "${BASE_URL}${ENDPOINT}/fr/Computer" -Method Get -Headers $headers
    Write-Host "Response:" -ForegroundColor Green
    $response4 | ConvertTo-Json -Depth 10
    Write-Host ""
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Test completed!" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Expected results:" -ForegroundColor White
Write-Host "- statusCode: 200" -ForegroundColor White
Write-Host "- message: 'Translation successful'" -ForegroundColor White
Write-Host "- data: Translated text in target language" -ForegroundColor White
Write-Host ""
Write-Host "If you see errors, check:" -ForegroundColor Yellow
Write-Host "1. Backend service is running on port 8080" -ForegroundColor Yellow
Write-Host "2. Authentication credentials are correct" -ForegroundColor Yellow
Write-Host "3. Google Translate API key is valid (in application.properties)" -ForegroundColor Yellow
Write-Host "4. API key has Translation API enabled in Google Cloud Console" -ForegroundColor Yellow
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "To get a new token manually:" -ForegroundColor Yellow
Write-Host "curl -X POST -H 'Content-Type: application/json' -d '{\"username\":\"$DEFAULT_EMAIL\",\"password\":\"$DEFAULT_PASSWORD\"}' ${BASE_URL}${AUTH_ENDPOINT}" -ForegroundColor Gray