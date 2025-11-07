# API Document V3

**Document Date:** 2025-11-07
**Status:** Corrected based on actual backend code analysis
**Changes from V2:**
- Fixed CreateQuestion endpoint URL (was /api/CreateQuestion, now /api/v1/questions)
- Added detailed validation rules from service layer
- Added complete ENUM values
- Added response structure details

---

## 1. CreateQuestion API

### Endpoint
```
POST /api/v1/questions
```

**IMPORTANT:** V2 documentation incorrectly listed this as `/api/CreateQuestion`

### Authentication
Required: Bearer Token in Authorization header

### Request Body
```json
{
  "quesContent": "string",
  "keyword": "string",
  "quesType": "ENUM",
  "skillType": "ENUM",
  "point": "int",
  "questionChoices": [
    {
      "choiceContent": "string",
      "choiceKey": "boolean"
    }
  ]
}
```

### Request Fields

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| quesContent | String | Yes | Question content/text | Cannot be empty or whitespace only |
| keyword | String | Yes | Question keyword for search | Auto-set to "writing task" or "speaking task" if empty for WRITING/SPEAKING types |
| quesType | QuestionTypeEnum | Yes | Type of question | CHOICE, MULTIPLE, TEXT, LISTENING, WRITING, SPEAKING |
| skillType | SkillTypeEnum | Yes | Skill being tested | LISTENING, READING, WRITING, SPEAKING, ALLSKILLS |
| point | int | Yes | Points for the question | Must be positive (> 0) |
| questionChoices | List<Question_Choice> | Conditional | Answer choices | Required for CHOICE/MULTIPLE types, NOT allowed for WRITING/SPEAKING types |

### Question_Choice Structure
```json
{
  "choiceContent": "string",  // The choice text
  "choiceKey": "boolean"      // true if this is the correct answer
}
```

### Validation Rules

1. **Content Validation:**
   - `quesContent` cannot be null, empty, or whitespace only
   - `point` must be positive (> 0)

2. **Type-Skill Matching:**
   - WRITING questions MUST have skillType = WRITING
   - SPEAKING questions MUST have skillType = SPEAKING
   - Error: `IllegalArgumentException` if mismatch

3. **Choices Validation:**
   - WRITING and SPEAKING questions CANNOT have questionChoices
   - If questionChoices provided for WRITING/SPEAKING: `IllegalArgumentException: "[type] questions cannot have choices"`

4. **Keyword Auto-fill:**
   - If keyword is null/empty for WRITING question → auto-set to "writing task"
   - If keyword is null/empty for SPEAKING question → auto-set to "speaking task"

### Response (Success - 200 OK)
```json
{
  "statusCode": 200,
  "message": "Question created successfully",
  "data": {
    "id": 123,
    "quesContent": "string",
    "keyword": "string",
    "quesType": "CHOICE",
    "skillType": "READING",
    "point": 10,
    "questionChoices": [
      {
        "id": 1,
        "choiceContent": "string",
        "choiceKey": true
      }
    ]
  }
}
```

### Error Responses

| HTTP Status | Error Type | Example Message |
|-------------|------------|-----------------|
| 400 | Bad Request | "Question content cannot be empty" |
| 400 | Bad Request | "Question point must be positive" |
| 400 | Bad Request | "WRITING questions must have WRITING skill type" |
| 400 | Bad Request | "SPEAKING questions must have SPEAKING skill type" |
| 400 | Bad Request | "WRITING questions cannot have choices" |
| 400 | Bad Request | "SPEAKING questions cannot have choices" |
| 401 | Unauthorized | Invalid or missing token |
| 403 | Forbidden | User lacks permission to create questions |
| 500 | Internal Server Error | Server-side error |

---

## 2. AddQuestionsToLesson API

### Endpoint
```
POST /api/v1/lessons/{lessonId}/questions
```

### Authentication
Required: Bearer Token in Authorization header

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| lessonId | Long | Yes | ID of the lesson to add questions to |

### Request Body
```json
{
  "questionIds": [1, 2, 3]
}
```

### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| questionIds | Array<Long> | Yes | List of question IDs to add to the lesson |

### Validation Rules

1. **Lesson Validation:**
   - Lesson with `lessonId` must exist
   - Error: `ResourceNotFoundException: "Lesson not found"` (404)

2. **Questions Validation:**
   - ALL question IDs must exist in database
   - If any question ID is missing: `ResourceNotFoundException: "One or more questions not found"` (404)

3. **Duplicate Prevention:**
   - Questions cannot already exist in the lesson
   - If duplicate found: `ResourceAlreadyExistsException: "Question with ID [id] already exists in the lesson."` (409)

4. **Transaction Handling:**
   - Operation is transactional - either all questions are added or none

### Response (Success - 200 OK)
```json
{
  "statusCode": 200,
  "message": "Questions added to lesson successfully",
  "data": null
}
```

### Error Responses

| HTTP Status | Error Type | Example Message |
|-------------|------------|-----------------|
| 400 | Bad Request | Invalid request body format |
| 401 | Unauthorized | Invalid or missing token |
| 403 | Forbidden | User lacks permission to modify lessons |
| 404 | Not Found | "Lesson not found" |
| 404 | Not Found | "One or more questions not found" |
| 409 | Conflict | "Question with ID [id] already exists in the lesson." |
| 500 | Internal Server Error | Server-side error |

---

## 3. Notification API

### Endpoint
```
POST /api/v1/notifications
```

### Authentication
Required: Bearer Token in Authorization header

### Request Body
```json
{
  "message": "string",
  "userId": 123,
  "entityId": 456,
  "entityType": "string",
  "img": "ENUM"
}
```

### Request Fields

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| message | String | Yes | Notification message content | - |
| userId | Long | Yes | ID of user to receive notification | User must exist |
| entityId | Long | No | ID of related entity (optional) | - |
| entityType | String | No | Type of related entity (optional) | e.g., "REVIEW", "DISCUSSION" |
| img | ImageNotiEnum | Yes | Notification icon type | STUDY, FLASHCARD, SCHEDULE, ACCOUNT |

### ImageNotiEnum Values
- `STUDY`: Study-related notifications
- `FLASHCARD`: Flashcard-related notifications
- `SCHEDULE`: Schedule-related notifications
- `ACCOUNT`: Account-related notifications

### Validation Rules

1. **User Validation:**
   - User with `userId` should exist (validated by repository, may cause 404 if FK constraint fails)

2. **Default Values:**
   - `isRead` is automatically set to `false`
   - `createdAt` is automatically set to current timestamp (Instant.now())

### Response (Success - 200 OK)
```json
{
  "statusCode": 200,
  "message": "Notification created successfully",
  "data": {
    "id": 789,
    "message": "string",
    "isRead": false,
    "userId": 123,
    "createdAt": "2025-11-07T10:30:00Z",
    "entityId": 456,
    "entityType": "string",
    "img": "STUDY"
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated notification ID |
| message | String | Notification message |
| isRead | boolean | Read status (always false on creation) |
| userId | Long | User ID who receives the notification |
| createdAt | Instant | Timestamp when notification was created |
| entityId | Long | Related entity ID (nullable) |
| entityType | String | Related entity type (nullable) |
| img | ImageNotiEnum | Notification icon type |

### Error Responses

| HTTP Status | Error Type | Example Message |
|-------------|------------|-----------------|
| 400 | Bad Request | Invalid request body format or invalid ENUM value |
| 401 | Unauthorized | Invalid or missing token |
| 404 | Not Found | User not found (if FK constraint fails) |
| 500 | Internal Server Error | Server-side error |

---

## 4. Review API

### Endpoint
```
POST /api/v1/reviews
```

### Authentication
Required: Bearer Token in Authorization header

### Request Body
```json
{
  "userId": 123,
  "courseId": 456,
  "reContent": "string",
  "reSubject": "string",
  "numStar": 5,
  "status": "ENUM"
}
```

### Request Fields

| Field | Type | Required | Description | Constraints |
|-------|------|----------|-------------|-------------|
| userId | Long | Yes | ID of user creating the review | User must exist and be enrolled |
| courseId | Long | Yes | ID of course being reviewed | Course must exist |
| reContent | String | Yes | Review content/description | - |
| reSubject | String | Yes | Review subject/title | - |
| numStar | int | Yes | Star rating | Typically 1-5 |
| status | ReviewStatusEnum | Yes | Review status/category | CONTRIBUTING, CONTENT, MISTAKE |

### ReviewStatusEnum Values
- `CONTRIBUTING`: Review about contributing to the course
- `CONTENT`: Review about course content
- `MISTAKE`: Review reporting mistakes in the course

### Validation Rules

1. **Duplicate Prevention:**
   - User can only review a course once
   - If already reviewed: `ResourceAlreadyExistsException: "User has already reviewed this course."` (409)

2. **Enrollment Validation:**
   - User MUST be enrolled in the course
   - If not enrolled: `ResourceNotFoundException: "User is not enrolled in this course"` (404)

3. **Completion Requirement:**
   - User must complete at least 30% of the course to review
   - If completion < 30%: `IllegalStateException: "Must complete at least 30% of the course to review"` (400)

4. **Entity Validation:**
   - User must exist: `ResourceNotFoundException: "User not found"` (404)
   - Course must exist: `ResourceNotFoundException: "Course not found"` (404)

5. **Auto-generated Fields:**
   - `numLike` is automatically set to 0
   - A notification is automatically created for the user

### Response (Success - 200 OK)
```json
{
  "statusCode": 200,
  "message": "Review created successfully",
  "data": {
    "id": 789,
    "userId": 123,
    "courseId": 456,
    "reContent": "string",
    "reSubject": "string",
    "numStar": 5,
    "numLike": 0,
    "status": "CONTENT"
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated review ID |
| userId | Long | User who created the review |
| courseId | Long | Course being reviewed |
| reContent | String | Review content |
| reSubject | String | Review subject |
| numStar | int | Star rating |
| numLike | int | Number of likes (always 0 on creation) |
| status | ReviewStatusEnum | Review status/category |

### Error Responses

| HTTP Status | Error Type | Example Message |
|-------------|------------|-----------------|
| 400 | Bad Request | "Must complete at least 30% of the course to review" |
| 400 | Bad Request | Invalid request body format or invalid ENUM value |
| 401 | Unauthorized | Invalid or missing token |
| 404 | Not Found | "User not found" |
| 404 | Not Found | "Course not found" |
| 404 | Not Found | "User is not enrolled in this course" |
| 409 | Conflict | "User has already reviewed this course." |
| 500 | Internal Server Error | Server-side error |

---

## Common Response Structure

All APIs follow the same response wrapper structure:

```json
{
  "statusCode": <number>,
  "message": "<string>",
  "data": <object|null>
}
```

- `statusCode`: HTTP status code
- `message`: Human-readable message describing the result
- `data`: Response data (structure varies by endpoint, can be null)

---

## Common Error Response

```json
{
  "statusCode": <number>,
  "message": "<error message>",
  "data": null
}
```

---

## Authentication

All endpoints require Bearer token authentication:

```
Authorization: Bearer <token>
```

Obtain token via login endpoint: `POST /api/v1/auth/login`

---

## Notes

1. All timestamps use ISO-8601 format with UTC timezone
2. All IDs are Long (64-bit integer) type
3. ENUM values are case-sensitive strings
4. Transactions ensure data consistency for multi-step operations
5. Foreign key constraints are enforced at database level

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| V3 | 2025-11-07 | Fixed CreateQuestion endpoint, added validation details from code |
| V2 | [Previous] | Initial API documentation (had incorrect CreateQuestion endpoint) |
