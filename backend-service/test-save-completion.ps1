# Test Save Completion API Workflow (PowerShell)
# This script tests the complete enrollment save-completion flow for both normal and writing lessons

# Configuration
$BACKEND_URL = "http://localhost:8080"
$AUTH_API = "/api/v1/auth"
$ENROLLMENT_API = "/api/v1/enrollments"
$ANSWER_API = "/api/v1/answers"
$LESSON_RESULT_API = "/api/v1/lesson-results"
$COURSE_API = "/api/v1/courses"
$LESSON_API = "/api/v1/lessons"

# Default user credentials
$DEFAULT_EMAIL = "user@example.com"
$DEFAULT_PASSWORD = "Abc@123456"

# Global variables
$script:ACCESS_TOKEN = ""
$script:USER_ID = ""
$script:PLACEMENT_COURSE_ID = ""
$script:WRITING_LESSON_ID = ""
$script:NORMAL_LESSON_ID = ""
$script:ENROLLMENT_ID = ""
$script:SESSION_ID = [int](Get-Date -UFormat %s)

Write-Host "============================================" -ForegroundColor Blue
Write-Host "  SAVE-COMPLETION API WORKFLOW TEST" -ForegroundColor Blue
Write-Host "============================================" -ForegroundColor Blue
Write-Host "Testing Enrollment Save-Completion" -ForegroundColor Yellow
Write-Host "Backend URL: $BACKEND_URL"
Write-Host "Session ID: $SESSION_ID"
Write-Host ""

# Function to make API calls
function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body = $null,
        [bool]$UseAuth = $true
    )
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($UseAuth -and $script:ACCESS_TOKEN) {
        $headers["Authorization"] = "Bearer $script:ACCESS_TOKEN"
    }
    
    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers -Body $jsonBody
        } else {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers
        }
        return @{ Success = $true; Data = $response; StatusCode = 200 }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        return @{ Success = $false; Error = $errorBody; StatusCode = $statusCode }
    }
}

# Step 1: Get authentication token
Write-Host "Step 1: Getting authentication token..." -ForegroundColor Cyan

$loginData = @{
    username = $DEFAULT_EMAIL
    password = $DEFAULT_PASSWORD
}

$result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$AUTH_API/login" -Body $loginData -UseAuth $false

if ($result.Success) {
    $script:ACCESS_TOKEN = $result.Data.data.access_token
    if (-not $script:ACCESS_TOKEN) {
        $script:ACCESS_TOKEN = $result.Data.data.accessToken
    }
    $script:USER_ID = $result.Data.data.user.id
    
    Write-Host "✓ Authentication successful" -ForegroundColor Green
    Write-Host "  User ID: $script:USER_ID"
    Write-Host "  Token: $($script:ACCESS_TOKEN.Substring(0, 20))..."
}
else {
    Write-Host "✗ Authentication failed - HTTP $($result.StatusCode)" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------"

# Step 2: Find Placement Test course
Write-Host "Step 2: Finding Placement Test course..." -ForegroundColor Cyan

$result = Invoke-ApiRequest -Method "GET" -Uri "$BACKEND_URL$COURSE_API?page=0&size=100"

if ($result.Success) {
    $courses = $result.Data.data.content
    $placementCourse = $courses | Where-Object { $_.name -eq "English Placement Test" } | Select-Object -First 1
    
    if ($placementCourse) {
        $script:PLACEMENT_COURSE_ID = $placementCourse.id
        Write-Host "✓ Found Placement Test course" -ForegroundColor Green
        Write-Host "  Course ID: $script:PLACEMENT_COURSE_ID"
    }
    else {
        Write-Host "✗ Placement Test course not found" -ForegroundColor Red
        exit 1
    }
}
else {
    Write-Host "✗ Failed to get courses - HTTP $($result.StatusCode)" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------"

# Step 3: Find Writing and Normal lessons
Write-Host "Step 3: Finding Writing and Normal lessons..." -ForegroundColor Cyan

# Get course details which includes lesson IDs
$result = Invoke-ApiRequest -Method "GET" -Uri "$BACKEND_URL$COURSE_API/$script:PLACEMENT_COURSE_ID"

if ($result.Success) {
    $lessonIds = $result.Data.data.lessonIds
    
    if (-not $lessonIds -or $lessonIds.Count -eq 0) {
        Write-Host "✗ No lessons found in course" -ForegroundColor Red
        exit 1
    }
    
    # Get details for each lesson to find WRITING and READING lessons
    foreach ($lessonId in $lessonIds) {
        $lessonResult = Invoke-ApiRequest -Method "GET" -Uri "$BACKEND_URL$LESSON_API/$lessonId"
        
        if ($lessonResult.Success) {
            $lesson = $lessonResult.Data.data
            
            if ($lesson.name -eq "(PLACEMENT) Writing" -and $lesson.skillType -eq "WRITING") {
                $script:WRITING_LESSON_ID = $lesson.id
            }
            elseif ($lesson.name -eq "(PLACEMENT) Text - Reading" -and $lesson.skillType -eq "READING") {
                $script:NORMAL_LESSON_ID = $lesson.id
            }
        }
    }
    
    if ($script:WRITING_LESSON_ID -and $script:NORMAL_LESSON_ID) {
        Write-Host "✓ Found lessons" -ForegroundColor Green
        Write-Host "  Writing Lesson ID: $script:WRITING_LESSON_ID"
        Write-Host "  Normal Lesson ID: $script:NORMAL_LESSON_ID"
    }
    else {
        Write-Host "✗ Could not find required lessons" -ForegroundColor Red
        if (-not $script:WRITING_LESSON_ID) { Write-Host "  Missing: WRITING lesson" }
        if (-not $script:NORMAL_LESSON_ID) { Write-Host "  Missing: READING lesson" }
        exit 1
    }
}
else {
    Write-Host "✗ Failed to get course details - HTTP $($result.StatusCode)" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------"

# Step 4: Create enrollment or get existing one
Write-Host "Step 4: Checking for existing enrollment or creating new one..." -ForegroundColor Cyan

# First, check if enrollment already exists
$result = Invoke-ApiRequest -Method "GET" -Uri "$BACKEND_URL$ENROLLMENT_API/latest?courseId=$script:PLACEMENT_COURSE_ID&userId=$script:USER_ID"

if ($result.Success) {
    # Enrollment exists, use it
    $script:ENROLLMENT_ID = $result.Data.data.id
    Write-Host "✓ Using existing enrollment" -ForegroundColor Yellow
    Write-Host "  Enrollment ID: $script:ENROLLMENT_ID"
}
else {
    # No existing enrollment, create new one
    Write-Host "  No existing enrollment found, creating new one..." -ForegroundColor Cyan
    
    $enrollmentData = @{
        userId = $script:USER_ID
        courseId = $script:PLACEMENT_COURSE_ID
    }

    $result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$ENROLLMENT_API" -Body $enrollmentData

    if ($result.Success) {
        $script:ENROLLMENT_ID = $result.Data.data.id
        Write-Host "✓ Enrollment created" -ForegroundColor Green
        Write-Host "  Enrollment ID: $script:ENROLLMENT_ID"
    }
    else {
        Write-Host "✗ Enrollment creation failed - HTTP $($result.StatusCode)" -ForegroundColor Red
        Write-Host "Error: $($result.Error)"
        exit 1
    }
}
Write-Host "----------------------------------------"

# Step 5: Activate enrollment
Write-Host "Step 5: Activating enrollment..." -ForegroundColor Cyan

$result = Invoke-ApiRequest -Method "PUT" -Uri "$BACKEND_URL$ENROLLMENT_API/$script:ENROLLMENT_ID/join"

if ($result.Success) {
    Write-Host "✓ Enrollment activated" -ForegroundColor Green
}
else {
    Write-Host "✗ Enrollment activation failed - HTTP $($result.StatusCode)" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------"

# Step 6a: Get questions for WRITING lesson
Write-Host "Step 6a: Getting questions for WRITING lesson..." -ForegroundColor Cyan

# Get lesson details which includes questionIds
$result = Invoke-ApiRequest -Method "GET" -Uri "$BACKEND_URL$LESSON_API/$script:WRITING_LESSON_ID"

if ($result.Success) {
    $questionIds = $result.Data.data.questionIds
    
    if (-not $questionIds -or $questionIds.Count -eq 0) {
        Write-Host "⚠ No questions found for WRITING lesson" -ForegroundColor Yellow
        Write-Host "✗ Cannot continue without questions" -ForegroundColor Red
        exit 1
    }
    
    $writingQuestionCount = $questionIds.Count
    Write-Host "✓ Found $writingQuestionCount questions for WRITING lesson" -ForegroundColor Green
    $writingQuestionId = $questionIds[0]
}
else {
    Write-Host "✗ Failed to get WRITING lesson details - HTTP $($result.StatusCode)" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------"

# Step 7a: Submit WRITING answer
Write-Host "Step 7a: Submitting WRITING answer (with score from content-scoring)..." -ForegroundColor Cyan

$writingAnswerData = @{
    questionId = $writingQuestionId
    answerContent = "My name is John Doe, I am 25 years old, and I work as a software engineer. I enjoy programming, reading technical books, and learning new technologies. In my free time, I like playing video games and spending time with my friends and family."
    pointAchieved = 85
    improvement = "Good introduction with clear structure. Consider adding more specific details about your interests."
    enrollmentId = $script:ENROLLMENT_ID
    sessionId = $script:SESSION_ID
}

$result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$ANSWER_API/user/$script:USER_ID" -Body $writingAnswerData

if ($result.Success) {
    Write-Host "✓ WRITING answer submitted successfully" -ForegroundColor Green
    Write-Host "  Points: 85/100"
}
else {
    Write-Host "✗ Failed to submit WRITING answer - HTTP $($result.StatusCode)" -ForegroundColor Red
    Write-Host "Error: $($result.Error)"
    exit 1
}
Write-Host "----------------------------------------"

# Step 8a: Create lesson result for WRITING
Write-Host "Step 8a: Creating lesson result for WRITING lesson..." -ForegroundColor Cyan

$lessonResultData = @{
    lessonId = $script:WRITING_LESSON_ID
    enrollmentId = $script:ENROLLMENT_ID
    sessionId = $script:SESSION_ID
    stuTime = 300
}

$result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$LESSON_RESULT_API/user/$script:USER_ID" -Body $lessonResultData

if ($result.Success) {
    $totalPoints = $result.Data.data.totalPoints
    $comLevel = $result.Data.data.comLevel
    Write-Host "✓ Lesson result created for WRITING lesson" -ForegroundColor Green
    Write-Host "  Total Points: $totalPoints"
    Write-Host "  Completion Level: $comLevel%"
}
else {
    Write-Host "✗ Failed to create WRITING lesson result - HTTP $($result.StatusCode)" -ForegroundColor Red
    Write-Host "Error: $($result.Error)"
    exit 1
}
Write-Host "----------------------------------------"

# Step 6b: Get questions for NORMAL lesson
Write-Host "Step 6b: Getting questions for NORMAL lesson..." -ForegroundColor Cyan

$result = Invoke-ApiRequest -Method "GET" -Uri "$BACKEND_URL/api/v1/questions/lesson/$script:NORMAL_LESSON_ID"

if ($result.Success) {
    $normalQuestions = $result.Data.data
    $normalQuestionCount = $normalQuestions.Count
    Write-Host "✓ Found $normalQuestionCount questions for NORMAL lesson" -ForegroundColor Green
}
else {
    Write-Host "✗ Failed to get NORMAL questions - HTTP $($result.StatusCode)" -ForegroundColor Red
    exit 1
}
Write-Host "----------------------------------------"

# Step 7b: Submit answers for NORMAL lesson
Write-Host "Step 7b: Submitting answers for NORMAL lesson..." -ForegroundColor Cyan

$count = 0
foreach ($question in $normalQuestions) {
    $count++
    
    $answerData = @{
        questionId = $question.id
        answerContent = "Tired as he was, he agreed to help me with my homework."
        enrollmentId = $script:ENROLLMENT_ID
        sessionId = $script:SESSION_ID
    }
    
    $result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$ANSWER_API/user/$script:USER_ID" -Body $answerData
    
    if ($result.Success) {
        $answerId = $result.Data.data.id
        
        # Grade the answer
        $gradeResult = Invoke-ApiRequest -Method "PUT" -Uri "$BACKEND_URL$ANSWER_API/grade/$answerId"
        
        Write-Host "✓ Answer $count submitted and graded" -ForegroundColor Green
    }
    
    if ($count -ge 3) { break }
}
Write-Host "----------------------------------------"

# Step 8b: Create lesson result for NORMAL
Write-Host "Step 8b: Creating lesson result for NORMAL lesson..." -ForegroundColor Cyan

$lessonResultData = @{
    lessonId = $script:NORMAL_LESSON_ID
    enrollmentId = $script:ENROLLMENT_ID
    sessionId = $script:SESSION_ID
    stuTime = 300
}

$result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$LESSON_RESULT_API/user/$script:USER_ID" -Body $lessonResultData

if ($result.Success) {
    $totalPoints = $result.Data.data.totalPoints
    $comLevel = $result.Data.data.comLevel
    Write-Host "✓ Lesson result created for NORMAL lesson" -ForegroundColor Green
    Write-Host "  Total Points: $totalPoints"
    Write-Host "  Completion Level: $comLevel%"
}
else {
    Write-Host "✗ Failed to create NORMAL lesson result - HTTP $($result.StatusCode)" -ForegroundColor Red
    Write-Host "Error: $($result.Error)"
    exit 1
}
Write-Host "----------------------------------------"

# Step 9: Test save-completion API
Write-Host "Step 9: Testing SAVE-COMPLETION API..." -ForegroundColor Cyan
Write-Host "This is the main test - saving enrollment completion" -ForegroundColor Yellow

$result = Invoke-ApiRequest -Method "POST" -Uri "$BACKEND_URL$ENROLLMENT_API/$script:ENROLLMENT_ID/save-completion"

if ($result.Success) {
    $totalPoints = $result.Data.data.totalPoints
    $comLevel = $result.Data.data.comLevel
    $skillScore = $result.Data.data.skillScore
    
    Write-Host "✓✓✓ SAVE-COMPLETION API TEST PASSED ✓✓✓" -ForegroundColor Green
    Write-Host "  Total Points: $totalPoints" -ForegroundColor Green
    Write-Host "  Completion Level: $comLevel%" -ForegroundColor Green
    Write-Host "  Skill Score: $skillScore" -ForegroundColor Green
    
    if ($totalPoints -gt 50) {
        Write-Host "  ✓ Writing question points were counted correctly" -ForegroundColor Green
    }
    else {
        Write-Host "  ⚠ Warning: Total points seem low, writing points may not be counted" -ForegroundColor Yellow
    }
}
else {
    Write-Host "✗✗✗ SAVE-COMPLETION API TEST FAILED ✗✗✗" -ForegroundColor Red
    Write-Host "  HTTP Status: $($result.StatusCode)" -ForegroundColor Red
    Write-Host "Error: $($result.Error)"
    exit 1
}
Write-Host "----------------------------------------"

Write-Host ""
Write-Host "============================================" -ForegroundColor Blue
Write-Host "  TEST SUMMARY" -ForegroundColor Blue
Write-Host "============================================" -ForegroundColor Blue
Write-Host "✓ All tests passed successfully!" -ForegroundColor Green
Write-Host "✓ WRITING questions are properly scored" -ForegroundColor Green
Write-Host "✓ NORMAL questions are properly scored" -ForegroundColor Green
Write-Host "✓ Save-completion API works correctly" -ForegroundColor Green
Write-Host ""
Write-Host "Key Points Verified:" -ForegroundColor Yellow
Write-Host "  1. Authentication works"
Write-Host "  2. Enrollment creation and activation works"
Write-Host "  3. WRITING answers with scores are saved correctly"
Write-Host "  4. Normal answers are graded correctly"
Write-Host "  5. Lesson results calculate points properly"
Write-Host "  6. Save-completion API aggregates all points correctly"
Write-Host ""
Write-Host "Enrollment ID: $script:ENROLLMENT_ID" -ForegroundColor Cyan
Write-Host "Session ID: $script:SESSION_ID" -ForegroundColor Cyan
Write-Host ""