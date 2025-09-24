# 🎮 Kịch Bản Test Game System V2

## 📋 Tổng Quan
Game system mới đã được thiết kế với mối quan hệ:
- **Game** → **Course** → **Question**
- **GameSession** quản lý session của user
- **GameAnswer** lưu trữ câu trả lời và reference đến Question từ Course
`x
## 🧪 Test Scenarios

### 1. Test Chuẩn Bị Dữ Liệu

#### 1.1 Kiểm tra Course có Questions
```http
GET /api/course/getAllActiveForStudent
Authorization: Bearer {token}
```
**Expected:** Trả về danh sách courses có questions > 0

#### 1.2 Kiểm tra Questions trong Course
```http
GET /api/course/{courseId}/questions
Authorization: Bearer {token}
```
**Expected:** Trả về danh sách questions của course

---

### 2. Test Game Management

#### 2.1 Tạo Game Mới
```http
POST /api/v1/game/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "courseId": 1,
  "name": "Review Game - Basic English",
  "description": "Ôn tập từ vựng cơ bản",
  "gameType": "REVIEW",
  "maxQuestions": 10,
  "timeLimit": 300
}
```
**Expected:** 
- Status: 200
- Trả về Game với ID mới
- Game được liên kết với Course

#### 2.2 Lấy Danh Sách Games theo Course
```http
GET /api/v1/game/course/{courseId}
Authorization: Bearer {token}
```
**Expected:** Trả về danh sách games của course

#### 2.3 Lấy Chi Tiết Game
```http
GET /api/v2/game/{gameId}
Authorization: Bearer {token}
```
**Expected:** 
- Game details với course information
- maxQuestions, timeLimit, gameType

---

### 3. Test Game Session

#### 3.1 Bắt Đầu Game Session
```http
POST /api/v2/game/start
Authorization: Bearer {token}
Content-Type: application/json

{
  "gameId": 1
}
```
**Expected:**
- Status: 200
- Trả về GameSession với ID
- startTime được set
- Danh sách questions random từ Course
- totalQuestions = maxQuestions của Game

#### 3.2 Lấy Question Hiện Tại trong Session
```http
GET /api/v2/game/session/{sessionId}/current-question
Authorization: Bearer {token}
```
**Expected:**
- Question tiếp theo chưa trả lời
- Thông tin session progress
- Time remaining

#### 3.3 Submit Answer
```http
POST /api/v2/game/session/{sessionId}/answer
Authorization: Bearer {token}
Content-Type: application/json

{
  "questionId": 5,
  "selectedAnswer": "B",
  "timeSpent": 15
}
```
**Expected:**
- GameAnswer được tạo
- isCorrect được tính toán
- Score được cập nhật
- Progress được update

#### 3.4 Lấy Progress của Session
```http
GET /api/v2/game/session/{sessionId}/progress
Authorization: Bearer {token}
```
**Expected:**
- answeredQuestions / totalQuestions
- currentScore
- timeRemaining
- accuracy percentage

#### 3.5 Kết Thúc Game Session
```http
POST /api/v2/game/session/{sessionId}/finish
Authorization: Bearer {token}
```
**Expected:**
- endTime được set
- Final score calculation
- Session marked as completed
- Game results summary

---

### 4. Test Game History & Statistics

#### 4.1 Lấy Game History của User
```http
GET /api/v2/game/my-sessions
Authorization: Bearer {token}
```
**Expected:** Danh sách các game sessions đã chơi

#### 4.2 Lấy Chi Tiết Game Session
```http
GET /api/v2/game/session/{sessionId}/details
Authorization: Bearer {token}
```
**Expected:**
- Full session details
- All game answers
- Questions với correct answers
- Performance analysis

#### 4.3 Lấy Statistics theo Course
```http
GET /api/v2/game/stats/course/{courseId}
Authorization: Bearer {token}
```
**Expected:**
- Games played count
- Average score
- Best performance
- Time statistics

---

### 5. Test Edge Cases & Error Handling

#### 5.1 Tạo Game với Course Không Có Questions
```http
POST /api/v2/game/create
{
  "courseId": 999,
  "name": "Test Game",
  "gameType": "REVIEW",
  "maxQuestions": 10,
  "timeLimit": 300
}
```
**Expected:** Error - Course không tồn tại hoặc không có questions

#### 5.2 Start Game với maxQuestions > Available Questions
```http
POST /api/v2/game/start
{
  "gameId": 1
}
```
**Expected:** 
- Game session với số questions = min(maxQuestions, availableQuestions)
- Warning message

#### 5.3 Submit Answer cho Question Đã Trả Lời
```http
POST /api/v2/game/session/{sessionId}/answer
{
  "questionId": 5,
  "selectedAnswer": "A"
}
```
**Expected:** Error - Question already answered

#### 5.4 Submit Answer sau khi Session Đã Kết Thúc
```http
POST /api/v2/game/session/{sessionId}/answer
```
**Expected:** Error - Session already finished

#### 5.5 Access Session của User Khác
```http
GET /api/v2/game/session/{otherUserSessionId}/progress
```
**Expected:** Error - Unauthorized access

---

### 6. Test Performance & Data Integrity

#### 6.1 Test Random Questions
- Tạo nhiều sessions từ cùng một game
- Verify questions được random khác nhau
- Ensure không duplicate questions trong cùng session

#### 6.2 Test Concurrent Sessions
- User có thể chơi nhiều games cùng lúc?
- Session isolation
- Data consistency

#### 6.3 Test Score Calculation
- Verify score = correct answers / total questions * 100
- Check accuracy calculation
- Validate time-based scoring (nếu có)

---

### 7. Test Integration với Course System

#### 7.1 Verify Questions từ Course
```sql
-- Check questions được sử dụng trong GameAnswer có thuộc Course không
SELECT ga.*, q.*, c.name as course_name 
FROM game_answers ga
JOIN questions q ON ga.question_id = q.id
JOIN lessons l ON q.lesson_id = l.id  
JOIN courses c ON l.course_id = c.id
JOIN games g ON ga.game_session_id IN (
    SELECT id FROM game_sessions WHERE game_id = g.id
)
WHERE g.course_id = c.id;
```

#### 7.2 Test Course Status Impact
- Course bị disable → Games có còn hoạt động?
- Questions bị xóa → GameAnswer references?

---

### 8. Test UI Flow (Manual Testing)

#### 8.1 Complete Game Flow
1. Login user
2. Navigate to Course detail
3. Click "Play Game" button
4. Select game type (Review/Practice/Challenge)
5. Start game session
6. Answer questions one by one
7. View progress indicator
8. Finish game
9. View results
10. View game history

#### 8.2 Game Types Testing
- **REVIEW**: Questions from all lessons
- **PRACTICE**: Focus on specific topics
- **CHALLENGE**: Time-pressured, harder questions

---

## 🛠️ Test Tools & Scripts

### Database Queries để Verify Data
```sql
-- Check Game-Course relationships
SELECT g.*, c.name as course_name, c.id as course_id
FROM games g
JOIN courses c ON g.course_id = c.id;

-- Check GameSession data
SELECT gs.*, g.name as game_name, u.email as user_email
FROM game_sessions gs
JOIN games g ON gs.game_id = g.id
JOIN users u ON gs.user_id = u.id;

-- Check GameAnswer với Question references
SELECT ga.*, q.question as question_text, q.correct_answer
FROM game_answers ga
JOIN questions q ON ga.question_id = q.id;
```

### Postman Collection
Tạo Postman collection với:
- Authentication setup
- All API endpoints
- Test scripts cho expected responses
- Environment variables

### Load Testing
```bash
# Test concurrent users playing games
ab -n 100 -c 10 -H "Authorization: Bearer {token}" \
   -T "application/json" \
   -p game_start.json \
   http://localhost:8080/api/v2/game/start
```

---

## 📊 Expected Test Results

### Success Metrics
- ✅ All API endpoints return expected status codes
- ✅ Game sessions track progress correctly
- ✅ Questions sourced from correct Course
- ✅ Score calculations accurate
- ✅ No data leakage between users
- ✅ Proper error handling
- ✅ Performance within acceptable limits

### Performance Benchmarks
- Game creation: < 500ms
- Session start: < 1s
- Answer submission: < 200ms
- Session completion: < 300ms
- Stats retrieval: < 800ms

---

## 🔧 Test Automation

### Unit Tests
- GameService methods
- Score calculation logic
- Question randomization
- Session management

### Integration Tests
- API endpoint testing
- Database operations
- Course-Game relationships

### E2E Tests
- Complete user flows
- Cross-browser compatibility
- Mobile responsiveness

---

Kịch bản test này cover toàn bộ Game system mới với focus vào:
1. **Functional testing** - Tất cả features hoạt động đúng
2. **Integration testing** - Game-Course relationship work properly
3. **Security testing** - User isolation, authorization
4. **Performance testing** - Response times, concurrent users
5. **Data integrity** - Score accuracy, question sourcing
6. **Error handling** - Edge cases, invalid inputs