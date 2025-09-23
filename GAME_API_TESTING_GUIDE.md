# 🎮 Game API Testing Guide

## Overview
This guide provides step-by-step instructions for testing the new Game API endpoints that have been added to the Encybara application.

## 🚀 Quick Start

### 1. Start the Application
```bash
cd backend-app
./gradlew bootRun
```

Wait for the application to start and the data seeding to complete. You should see logs like:
```
🎮 Starting Game Data Seeding...
👥 Seeding demo users...
   ✓ Created user: alice@example.com
   ✓ Created user: bob@example.com
   ...
🎯 Seeding completed game sessions for leaderboard...
✅ Game Data Seeding completed successfully!
```

### 2. Authentication Required

All game endpoints require JWT authentication. First, obtain a JWT token by logging in:

**Login Request:**
```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "alice@example.com",
  "password": "alice123"
}
```

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "alice@example.com",
    "name": "Alice Johnson"
  }
}
```

Use the `access_token` in all subsequent requests as: `Authorization: Bearer <access_token>`

## 🎯 Game API Endpoints

### 1. Start a New Game Session

**Endpoint:** `POST /api/game/start`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/game/start \
  -H "Authorization: Bearer <your_jwt_token>" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "sessionId": 123,
  "message": "Game session started successfully",
  "timeLimit": 45,
  "totalQuestions": 10
}
```

### 2. Get Current Question

**Endpoint:** `GET /api/game/{sessionId}/question`

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/game/123/question \
  -H "Authorization: Bearer <your_jwt_token>"
```

**Expected Response:**
```json
{
  "questionId": 456,
  "questionText": "What does 'abundant' mean?",
  "choices": [
    "Scarce",
    "Plentiful", 
    "Expensive",
    "Difficult"
  ],
  "questionNumber": 1,
  "timeRemaining": 43
}
```

### 3. Submit Answer

**Endpoint:** `POST /api/game/{sessionId}/answer`

**Request Body:**
```json
{
  "questionId": 456,
  "answer": "Plentiful"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/game/123/answer \
  -H "Authorization: Bearer <your_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": 456,
    "answer": "Plentiful"
  }'
```

**Expected Response:**
```json
{
  "correct": true,
  "correctAnswer": "Plentiful",
  "score": 10,
  "gameCompleted": false,
  "nextQuestionAvailable": true
}
```

### 4. Get Game Results

**Endpoint:** `GET /api/game/{sessionId}/results`

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/game/123/results \
  -H "Authorization: Bearer <your_jwt_token>"
```

**Expected Response:**
```json
{
  "sessionId": 123,
  "totalQuestions": 10,
  "questionsAnswered": 10,
  "score": 85,
  "completed": true,
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T10:00:45",
  "duration": 45
}
```

### 5. Get Leaderboard

**Endpoint:** `GET /api/game/leaderboard`

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/game/leaderboard \
  -H "Authorization: Bearer <your_jwt_token>"
```

**Expected Response:**
```json
{
  "leaderboard": [
    {
      "rank": 1,
      "playerName": "Carol Davis",
      "score": 120,
      "questionsAnswered": 10,
      "completionTime": "2024-01-15T09:00:45"
    },
    {
      "rank": 2,
      "playerName": "Eve Brown",
      "score": 110,
      "questionsAnswered": 9,
      "completionTime": "2024-01-15T08:30:45"
    }
  ]
}
```

## 🧪 Complete Testing Scenario

### Scenario: Complete a Full Game Session

1. **Login** to get JWT token
2. **Start Game** - Note the sessionId
3. **Get Question** - Retrieve first question
4. **Submit Answer** - Answer the question
5. **Repeat steps 3-4** for remaining questions
6. **Get Results** - View final score
7. **Check Leaderboard** - See ranking

### Sample Testing Script (Bash)

```bash
#!/bin/bash

# Configuration
BASE_URL="http://localhost:8080"
EMAIL="alice@example.com"
PASSWORD="alice123"

echo "🎮 Testing Game API..."

# 1. Login
echo "1. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}")

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.access_token')
echo "   ✓ JWT Token obtained"

# 2. Start Game
echo "2. Starting game..."
START_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/game/start" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json")

SESSION_ID=$(echo $START_RESPONSE | jq -r '.sessionId')
echo "   ✓ Game started with session ID: ${SESSION_ID}"

# 3. Get First Question
echo "3. Getting first question..."
QUESTION_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/game/${SESSION_ID}/question" \
  -H "Authorization: Bearer ${TOKEN}")

QUESTION_ID=$(echo $QUESTION_RESPONSE | jq -r '.questionId')
QUESTION_TEXT=$(echo $QUESTION_RESPONSE | jq -r '.questionText')
echo "   ✓ Question: ${QUESTION_TEXT}"

# 4. Submit Answer
echo "4. Submitting answer..."
ANSWER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/game/${SESSION_ID}/answer" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"questionId\":${QUESTION_ID},\"answer\":\"Plentiful\"}")

CORRECT=$(echo $ANSWER_RESPONSE | jq -r '.correct')
echo "   ✓ Answer submitted. Correct: ${CORRECT}"

# 5. Check Leaderboard
echo "5. Checking leaderboard..."
LEADERBOARD_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/game/leaderboard" \
  -H "Authorization: Bearer ${TOKEN}")

echo "   ✓ Leaderboard retrieved"
echo $LEADERBOARD_RESPONSE | jq '.leaderboard[:3]'

echo "🎉 Game API testing completed!"
```

## 🔍 Test FFmpeg Integration

The application also includes FFmpeg test endpoints:

```bash
# Check FFmpeg status
curl -X GET http://localhost:8080/api/test/ffmpeg-status

# Get system info
curl -X GET http://localhost:8080/api/test/system-info

# Test audio conversion (requires audio file)
curl -X POST http://localhost:8080/api/test/convert-audio \
  -F "file=@/path/to/audio.wav" \
  -F "targetFormat=mp3"
```

## 🐛 Troubleshooting

### Common Issues:

1. **401 Unauthorized**: Check JWT token validity and format
2. **404 Session Not Found**: Verify sessionId exists and belongs to authenticated user
3. **400 Bad Request**: Check request body format and required fields
4. **500 Internal Server Error**: Check application logs for database/seeding issues

### Debug Steps:

1. **Check Application Logs**: Look for seeding completion messages
2. **Verify Database**: Ensure demo users and sessions were created
3. **Test Authentication**: Confirm login works with demo credentials
4. **Network Issues**: Verify application is running on correct port (8080)

## 📊 Sample Data

The application seeds the following demo accounts:

| Email | Password | Name | Field | Level |
|-------|----------|------|-------|-------|
| alice@example.com | alice123 | Alice Johnson | IT | Intermediate |
| bob@example.com | bob123 | Bob Smith | ECONOMIC | Beginner |
| carol@example.com | carol123 | Carol Davis | EVERYONE | Advanced |
| david@example.com | david123 | David Wilson | CONSTRUCTION | Intermediate |
| eve@example.com | eve123 | Eve Brown | ELECTRICITY | Beginner |

Each user will have completed game sessions with varying scores for leaderboard testing.

## 🚀 Next Steps

1. **Integration Testing**: Test with frontend application
2. **Performance Testing**: Load test with multiple concurrent users  
3. **Security Testing**: Verify JWT validation and authorization
4. **Mobile Testing**: Test API with mobile applications

---

*Happy Testing! 🎮*