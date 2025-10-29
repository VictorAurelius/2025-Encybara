# API DOCUMENT

**Format:** Consolidated Excel-friendly format
**Last Updated:** 2025-10-29
**Project:** Encybara - English Learning Platform

---

## Summary Table

| No. | Sheet name              | API                                   | Status | Remarks |
|-----|-------------------------|---------------------------------------|--------|---------|
| 1   | GradeAnswer             | PUT /api/v1/answers/grade/{answerId}  | Done   |         |

---

## API Detail: GradeAnswer

| API name        | GradeAnswer                            |
|-----------------|----------------------------------------|
| Endpoint        | /api/v1/answers/grade/{answerId}       |

---

### Request

| Method          | PUT                                    |
|-----------------|----------------------------------------|
| Format data     | application/json                       |

---

### Header

| No  | Header        | Data Type | Mandatory | Note                                    |
|-----|---------------|-----------|-----------|-----------------------------------------|
| 1   | Authorization | String    | ◯         | Bearer token (JWT) - Required for authenticated users |
| 2   | Content-Type  | String    | ◯         | application/json                        |

---

### Path Parameters

| No  | Parameter | Data Type | Mandatory | Note                                                |
|-----|-----------|-----------|-----------|-----------------------------------------------------|
| 1   | answerId  | Long      | ◯         | The ID of the answer to be graded                   |

---

### Body

| No  | Body Data | Data Type | Mandatory | Note                                    |
|-----|-----------|-----------|-----------|------------------------------------------|
|     |           |           |           | N/A (no request body required)           |

---

### Request Sample

**Endpoint:**
```
PUT /api/v1/answers/grade/123
```

**cURL Example:**
```bash
curl -X PUT "http://localhost:8080/api/v1/answers/grade/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

### Response

| No  | Key              | Data Type | Mandatory | Note                                           |
|-----|------------------|-----------|-----------|------------------------------------------------|
| 1   | statusCode       | Integer   | ◯         | HTTP status code (200 for success)             |
| 2   | error            | String    | ◯         | Error message (null on success)                |
| 3   | message          | String    | ◯         | Success/error message                          |
| 4   | data             | Object    | ◯         | Graded answer data (ResAnswerDTO)              |
| 4.1 | data.id          | Long      | ◯         | Answer ID                                      |
| 4.2 | data.questionId  | Long      | ◯         | Associated question ID                         |
| 4.3 | data.answerContent | String  | ◯         | User's answer content                          |
| 4.4 | data.pointAchieved | Integer | ◯         | Points awarded after grading                   |
| 4.5 | data.sessionId   | Long      | ◯         | Session identifier for this attempt            |
| 4.6 | data.improvement | String    |           | Improvement notes/feedback (optional)          |
| 4.7 | data.enrollmentId| Long      |           | Associated enrollment ID (optional)            |

---

### Response Example

**Success Response - CHOICE Question (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Answer graded successfully",
  "data": {
    "id": 123,
    "questionId": 45,
    "answerContent": "Paris",
    "pointAchieved": 10,
    "sessionId": 1,
    "improvement": null,
    "enrollmentId": 78
  }
}
```

**Success Response - MULTIPLE Question with Full Points (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Answer graded successfully",
  "data": {
    "id": 124,
    "questionId": 46,
    "answerContent": "A, C, D",
    "pointAchieved": 10,
    "sessionId": 2,
    "improvement": null,
    "enrollmentId": 78
  }
}
```

**Success Response - MULTIPLE Question with Partial Points (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Answer graded successfully",
  "data": {
    "id": 125,
    "questionId": 47,
    "answerContent": "A, C",
    "pointAchieved": 7,
    "sessionId": 1,
    "improvement": "Partial credit: 2 of 3 correct answers selected",
    "enrollmentId": 78
  }
}
```

**Success Response - TEXT Question (200 OK):**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Answer graded successfully",
  "data": {
    "id": 126,
    "questionId": 48,
    "answerContent": "The capital of France is Paris.",
    "pointAchieved": 5,
    "sessionId": 1,
    "improvement": null,
    "enrollmentId": 78
  }
}
```

**Error Response - Answer Not Found (404):**
```json
{
  "statusCode": 404,
  "error": "Resource Not Found",
  "message": "Answer not found",
  "data": null
}
```

**Error Response - Unauthorized (401):**
```json
{
  "statusCode": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "data": null
}
```

---

### Error code

| No  | Status Code | Error Message                  | Description                                      |
|-----|-------------|--------------------------------|--------------------------------------------------|
| 1   | 200         | Answer graded successfully     | Answer graded and points calculated successfully |
| 2   | 400         | Invalid path variable format   | answerId is not a valid Long format              |
| 3   | 401         | Unauthorized                   | No authentication token provided                 |
| 4   | 401         | Invalid or expired token       | JWT token is invalid or has expired              |
| 5   | 404         | Answer not found               | Answer with given ID does not exist              |
| 6   | 500         | Internal server error          | Database connection error                        |
| 7   | 500         | Internal server error          | Unexpected error during grading process          |

---

### Grading Logic

The grading system supports three question types:

#### 1. CHOICE (Single Choice)
- **Logic:** Compare user's answer with correct choice
- **Scoring:**
  - Full points if answer matches correct choice
  - Zero points if answer is incorrect
- **Comparison:** Case-insensitive, normalized (trim, lowercase, remove extra spaces and trailing punctuation)

#### 2. MULTIPLE (Multiple Choice)
- **Logic:** Compare user's selected choices with all correct choices
- **Scoring:**
  - Full points if ALL correct choices selected AND NO incorrect choices
  - Partial points based on ratio: `(correctCount / totalCorrect) * questionPoints`
  - Result is rounded to nearest integer
- **Example:**
  - Question has 4 correct answers (A, B, C, D), worth 10 points
  - User selects A, B, C (3 correct)
  - Partial = (3 / 4) * 10 = 7.5 → **8 points** (rounded)

#### 3. TEXT (Text Input)
- **Logic:** Compare user's text with correct answer text
- **Scoring:**
  - Full points if normalized text matches correct answer
  - Zero points if text doesn't match
- **Normalization:**
  - Trim leading/trailing whitespace
  - Convert to lowercase
  - Replace multiple spaces with single space
  - Remove trailing punctuation (. ! ?)

---

### Additional Information

#### Question Type Support
| Question Type | Code      | Grading Method        |
|---------------|-----------|------------------------|
| Single Choice | CHOICE    | Binary (full or zero) |
| Multiple Choice | MULTIPLE | Partial credit allowed |
| Text Input    | TEXT      | Binary (full or zero) |
| Listening     | LISTENING | Not auto-graded       |
| Writing       | WRITING   | Not auto-graded       |
| Speaking      | SPEAKING  | Not auto-graded       |

**Note:** Only CHOICE, MULTIPLE, and TEXT question types support automatic grading.

#### Normalization Rules
The system normalizes both user answers and correct answers before comparison to ensure fair grading:
1. Trim leading/trailing whitespace
2. Convert to lowercase
3. Replace multiple consecutive spaces with single space
4. Remove trailing punctuation marks (. ! ?)

**Examples:**
- `"  Paris  "` → `"paris"`
- `"The Answer."` → `"the answer"`
- `"CORRECT   ANSWER"` → `"correct answer"`

#### Validation Requirements
- **answerId** must be a valid Long integer
- **Answer** must exist in database
- **Question** associated with answer must be loaded
- **Question choices** must be available for comparison
- **User authentication** required via JWT Bearer token

#### Database Updates
After successful grading:
- The `point_achieved` field is updated with calculated score
- Changes are persisted to the database
- If any error occurs during grading, the transaction is rolled back

---

### Performance Notes

| Metric                  | Value/Note                              |
|-------------------------|-----------------------------------------|
| Average response time   | 50-100ms                                |
| Maximum response time   | 500ms                                   |
| Database operations     | 1 read, 1 update per request            |
| Concurrent requests     | Supported (transactional)               |
| Rate limiting           | Subject to global API rate limits       |

---

### Security Considerations

1. **Authentication Required:** All requests must include valid JWT Bearer token
2. **Authorization:** Users can only grade answers they have permission to access
3. **Input Validation:** Path parameter `answerId` is validated as Long type
4. **SQL Injection Protection:** JPA repository methods prevent SQL injection
5. **Transaction Safety:** Database updates are wrapped in transactions

---

**END OF API DOCUMENT**
