# Plan Task req-3: Tạo Consolidated Output Document trong thư mục output

## 📥 Context & Input Files

### Investigation Results:
1. ✅ **pronunciation-assessment-service** đã được điều tra chi tiết:
   - Service: Python Flask API
   - Endpoint: `POST /api/pronunciation-assessment`
   - Response structure thực tế đã xác định
   - Pipeline: SimpleAligner + GOP Scorer

### Input Files:
2. `documents/API Document - SAMPLE.pdf` - Format mẫu tổng hợp (3 APIs)
3. `documents/Testcase API - SAMPLE.pdf` - Format testcase mẫu
4. `backend-service/.../PronunciationAssessmentController.java` - Controller code
5. `pronunciation-assessment-service/app/main.py` - External service code
6. `pronunciation-assessment-service/README.md` - Service documentation

### Previous Work:
7. `documents/req-1.md` - Plan task 1 (original)
8. `documents/req-2.md` - Plan task 2 (Excel-friendly format)
9. `documents/API_Document_AssessPronunciation_v2.md` - API Doc v2
10. `documents/Testcase_API_AssessPronunciation_v2.md` - Testcase v2

---

## 🎯 Objective

Tạo **bảng tổng hợp** (consolidated output) trong thư mục **`output/`** theo đúng format SAMPLE:

1. **API Document tổng hợp** - 1 file duy nhất chứa:
   - Page 1: Summary table (danh sách tất cả APIs)
   - Page 2+: Chi tiết từng API (mỗi API 1 page)

2. **Testcase tổng hợp** - Format theo SAMPLE testcase:
   - Test cases lần lượt như sample
   - Table format dễ paste Excel
   - Response structure CHÍNH XÁC từ pronunciation-assessment-service

**Key Requirements:**
- ✅ Output trong thư mục `output/`
- ✅ Format giống hệt SAMPLE (consolidated, không phải separate files)
- ✅ Response structure dựa trên điều tra thực tế pronunciation-assessment-service
- ✅ Sẵn sàng paste vào Excel

---

## 🔍 Investigation Results: pronunciation-assessment-service Response Structure

### Response từ pronunciation-assessment-service (External Service):

```json
{
  "success": true,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "overall_score": 84.2,        // Điểm tổng thể (0-100)
    "fluency_score": 92.0,        // Điểm trôi chảy (0-100)
    "phoneme_scores": [           // Array chi tiết từng phoneme
      {
        "phoneme": "h",           // Ký hiệu âm vị IPA
        "gop_score": 88.9,        // Điểm GOP (Goodness of Pronunciation)
        "quality": "excellent",   // Chất lượng: excellent/good/fair/poor
        "start_time": 0.010,      // Thời điểm bắt đầu (giây)
        "end_time": 0.090,        // Thời điểm kết thúc (giây)
        "character": "h",         // Ký tự tương ứng trong text
        "word_index": 0,          // Index của từ trong câu
        "phoneme_index": 0        // Index của phoneme trong từ
      },
      {
        "phoneme": "ɛ",
        "gop_score": 82.5,
        "quality": "good",
        "start_time": 0.090,
        "end_time": 0.180,
        "character": "e",
        "word_index": 0,
        "phoneme_index": 1
      }
      // ... more phonemes
    ],
    "total_phonemes": 12,         // Tổng số phoneme đã phân tích
    "average_duration": 0.130     // Thời gian trung bình mỗi phoneme (giây)
  }
}
```

### Response từ Backend Controller (Encybara):

Backend **wrap lại** response từ external service:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "overall_score": 84.2,
    "fluency_score": 92.0,
    "phoneme_scores": [
      {
        "phoneme": "h",
        "gop_score": 88.9,
        "quality": "excellent",
        "start_time": 0.010,
        "end_time": 0.090,
        "character": "h",
        "word_index": 0,
        "phoneme_index": 0
      }
    ],
    "total_phonemes": 12,
    "average_duration": 0.130
  }
}
```

**Key Insights:**
- `data` object chứa **toàn bộ response** từ external service
- Không parse/transform, trả nguyên bản Map<String, Object>
- Fields: overall_score, fluency_score, phoneme_scores, total_phonemes, average_duration

---

## 📊 SAMPLE Format Analysis

### API Document - SAMPLE Structure:

**Page 1: Summary Table**
```
┌────┬────────────────┬─────────────────────────────────┬────────┬─────────┐
│ No │ Sheet name     │ API                             │ Status │ Remarks │
├────┼────────────────┼─────────────────────────────────┼────────┼─────────┤
│ 1  │ CreateVoucher  │ POST/api/Voucher                │ Done   │         │
│ 2  │ CreateKhachHang│ POST/api/KhachHang              │ Done   │         │
│ 3  │ UpdateSanPham  │ PUT/api/SanPham/UpdateSanPham   │ Done   │         │
└────┴────────────────┴─────────────────────────────────┴────────┴─────────┘
```

**Page 2+: Chi tiết từng API (mỗi API 1 trang)**

Example: CreateVoucher (Page 2)
```
API name        CreateVoucher
Endpoint        /api/Voucher

Request
Method          Post
Format data     Json

Header Table:
┌────┬────────┬───────────┬───────────┬──────┐
│ No │ Header │ Data Type │ Mandatory │ Note │
└────┴────────┴───────────┴───────────┴──────┘

Params Table:
┌────┬────────┬───────────┬───────────┬──────┐
│ No │ Params │ Data Type │ Mandatory │ Note │
└────┴────────┴───────────┴───────────┴──────┘

Body Table:
┌────┬───────────┬───────────┬───────────┬──────────────────┐
│ No │ Body Data │ Data Type │ Mandatory │ Note             │
├────┼───────────┼───────────┼───────────┼──────────────────┤
│ 1  │ ten       │ nvarchar  │ ◯         │ Tối đa 40 ký tự  │
└────┴───────────┴───────────┴───────────┴──────────────────┘

Request Sample:
{ "ten": "...", ... }

Response Table:
┌────┬─────┬───────────┬───────────┬──────┐
│ No │ Key │ Data Type │ Mandatory │ Note │
└────┴─────┴───────────┴───────────┴──────┘

Response Example:
...

Error code Table:
┌────┬────────────┬──────────────────┐
│ No │ Error code │ Note             │
├────┼────────────┼──────────────────┤
│ 1  │ 200        │ Success          │
│ 2  │ 400        │ Bad request...   │
└────┴────────────┴──────────────────┘
```

**Format Characteristics:**
- **Consolidated**: Tất cả APIs trong 1 file
- **Structured**: Page 1 = Summary, Page 2+ = Details
- **Table-driven**: Mỗi section là 1 table rõ ràng
- **Excel-ready**: Dễ copy-paste

### Testcase - SAMPLE Structure:

**Page 1: TEST REPORT Summary**
```
┌────┬────────────┬──────────┬───────┬─────────────────────────┬──────────────────┐
│ No │ Sheet name │ API name │ Total │ Result (P/F/B/N)        │ Execution (A/M)  │
└────┴────────────┴──────────┴───────┴─────────────────────────┴──────────────────┘
```

**Page 2+: Chi tiết Test Cases**
```
Project Name    ...
API Name        ...
Owner           ...

Statistics:
PASSED      24
FAILED      5
...

Test Cases Table (lần lượt từng dòng):
┌──────┬──────┬────────────┬─────────────┬───────────┬────────┬──────┬────────┬──────┬──────────┬────────┬───────────┬──────┐
│ ID   │ Item │ Test Name  │ Precondition│ Test Case │ params │ body │ Status │ Body │ Expected │ Result │ Execution │ Note │
│      │      │            │             │           │        │      │ code   │      │ Database │        │           │      │
├──────┼──────┼────────────┼─────────────┼───────────┼────────┼──────┼────────┼──────┼──────────┼────────┼───────────┼──────┤
│ID-001│Method│Gọi POST... │1. Đăng nhập │...        │...     │...   │200 OK  │...   │...       │Passed  │Manual     │      │
└──────┴──────┴────────────┴─────────────┴───────────┴────────┴──────┴────────┴──────┴──────────┴────────┴───────────┴──────┘
```

---

## 📋 Task Plan

### **Task 1: Create Output Directory Structure**
**Duration:** ~2 minutes

**Actions:**
- [ ] Tạo thư mục `output/` nếu chưa có
- [ ] Structure:
  ```
  output/
  ├── API_Document_AssessPronunciation.md    # Consolidated API doc
  └── Testcase_API_AssessPronunciation.md    # Consolidated testcase
  ```

**Output:** Clean output directory ready

---

### **Task 2: Generate Consolidated API Document**
**Duration:** ~25 minutes

**Actions:**
- [ ] Tạo file `output/API_Document_AssessPronunciation.md`

- [ ] **Page 1: Summary Table**
  ```
  # API DOCUMENT

  ## Summary Table

  | No. | Sheet name              | API                                   | Status | Remarks |
  |-----|-------------------------|---------------------------------------|--------|---------|
  | 1   | AssessPronunciation     | POST /api/v1/pronunciation/assess     | Done   |         |
  ```

- [ ] **Page 2: AssessPronunciation Details**

  **API Overview:**
  ```
  API name        AssessPronunciation
  Endpoint        /api/v1/pronunciation/assess
  ```

  **Request:**
  ```
  Method          POST
  Format data     multipart/form-data
  ```

  **Header Table:**
  ```
  | No  | Header        | Data Type | Mandatory | Note                                    |
  |-----|---------------|-----------|-----------|-----------------------------------------|
  | 1   | Authorization | String    | ◯         | Bearer token (JWT)                      |
  | 2   | Content-Type  | String    | ◯         | multipart/form-data                     |
  ```

  **Params Table:**
  ```
  | No  | Params | Data Type       | Mandatory | Note                                                |
  |-----|--------|-----------------|-----------|-----------------------------------------------------|
  | 1   | file   | MultipartFile   | ◯         | Audio file (WAV, MP3, FLAC, M4A), max 6MB          |
  | 2   | text   | String          | ◯         | Reference text for pronunciation assessment         |
  ```

  **Body:**
  ```
  N/A (uses multipart/form-data params)
  ```

  **Request Sample:**
  ```
  file: audio_sample.wav (binary)
  text: "Hello, how are you today?"
  ```

  **Response Table:** (Dựa trên investigation)
  ```
  | No  | Key              | Data Type       | Mandatory | Note                                           |
  |-----|------------------|-----------------|-----------|------------------------------------------------|
  | 1   | statusCode       | Integer         | ◯         | HTTP status code (200 for success)             |
  | 2   | error            | String          | ◯         | Error message (null on success)                |
  | 3   | message          | String          | ◯         | Success/error message                          |
  | 4   | data             | Object          | ◯         | Assessment result object                       |
  | 4.1 | overall_score    | Double          | ◯         | Overall pronunciation score (0-100)            |
  | 4.2 | fluency_score    | Double          | ◯         | Fluency score (0-100)                          |
  | 4.3 | phoneme_scores   | Array[Object]   | ◯         | Array of phoneme-level scores                  |
  | 4.4 | total_phonemes   | Integer         | ◯         | Total number of phonemes analyzed              |
  | 4.5 | average_duration | Double          | ◯         | Average duration per phoneme (seconds)         |
  ```

  **Phoneme Score Object Structure:**
  ```
  | No  | Key            | Data Type | Note                                           |
  |-----|----------------|-----------|------------------------------------------------|
  | 1   | phoneme        | String    | IPA phoneme symbol (e.g., "h", "ɛ")            |
  | 2   | gop_score      | Double    | GOP score for this phoneme (0-100)             |
  | 3   | quality        | String    | Quality: excellent/good/fair/poor              |
  | 4   | start_time     | Double    | Start time in seconds                          |
  | 5   | end_time       | Double    | End time in seconds                            |
  | 6   | character      | String    | Corresponding character in text                |
  | 7   | word_index     | Integer   | Index of word in sentence                      |
  | 8   | phoneme_index  | Integer   | Index of phoneme in word                       |
  ```

  **Response Example:**
  ```json
  {
    "statusCode": 200,
    "error": null,
    "message": "Pronunciation assessment completed successfully",
    "data": {
      "overall_score": 84.2,
      "fluency_score": 92.0,
      "phoneme_scores": [
        {
          "phoneme": "h",
          "gop_score": 88.9,
          "quality": "excellent",
          "start_time": 0.010,
          "end_time": 0.090,
          "character": "h",
          "word_index": 0,
          "phoneme_index": 0
        },
        {
          "phoneme": "ɛ",
          "gop_score": 82.5,
          "quality": "good",
          "start_time": 0.090,
          "end_time": 0.180,
          "character": "e",
          "word_index": 0,
          "phoneme_index": 1
        }
      ],
      "total_phonemes": 12,
      "average_duration": 0.130
    }
  }
  ```

  **Error code Table:**
  ```
  | No  | Error code | Note                                                                        |
  |-----|------------|-----------------------------------------------------------------------------|
  | 1   | 200        | Success                                                                     |
  | 2   | 400        | Bad Request - Missing or empty audio file                                   |
  | 3   | 400        | Bad Request - Missing or empty text transcript                              |
  | 4   | 401        | Unauthorized - Invalid or missing JWT token                                 |
  | 5   | 404        | Not Found - Pronunciation assessment service endpoint not found             |
  | 6   | 503        | Service Unavailable - Pronunciation service not configured                  |
  | 7   | 503        | Service Unavailable - Cannot connect to pronunciation service               |
  | 8   | 500        | Internal Server Error - Unexpected server error                             |
  ```

**Output:** Consolidated API Document với accurate response structure

---

### **Task 3: Generate Consolidated Testcase Document**
**Duration:** ~35 minutes

**Actions:**
- [ ] Tạo file `output/Testcase_API_AssessPronunciation.md`

- [ ] **Page 1: TEST REPORT Summary**
  ```
  # TEST REPORT

  | No. | Sheet name          | API name                              | Total Testcase | Result |        |         |         | Execution |        |     |
  |-----|---------------------|---------------------------------------|----------------|--------|--------|---------|---------|-----------|--------|-----|
  |     |                     |                                       |                | Passed | Failed | Blocked | Not run | Automation| Manual | N/A |
  | 1   | AssessPronunciation | POST /api/v1/pronunciation/assess     | 30             | 0      | 0      | 0       | 30      | 20        | 10     | 0   |
  ```

- [ ] **Page 2: Project Information**
  ```
  Project Name    Encybara - English Learning Platform
  API Name        POST /api/v1/pronunciation/assess
  Owner

  PASSED          0
  FAILED          0
  PENDING         30
  NOT RUN         0
  AUTOMATION      20
  MANUAL          10
  Number of TC    30
  ```

- [ ] **Page 3+: Test Cases Table (Lần lượt từng dòng như SAMPLE)**

  **Format:** 1 bảng lớn với tất cả test cases

  **Columns:**
  - ID
  - Item
  - Testcase Name
  - Precondition
  - Test Case
  - params
  - body
  - Status code
  - Body
  - Expected Database Result
  - Result
  - Execution
  - Note

  **Test Cases Groups:**

  **A. Validate (ID-001 ~ ID-010) - 10 TCs:**
  - ID-001: Method POST (valid)
  - ID-002: Method GET (invalid)
  - ID-003: Method PUT (invalid)
  - ID-004: Method DELETE (invalid)
  - ID-005: Valid request với đầy đủ params
  - ID-006: Missing audio file
  - ID-007: Empty audio file
  - ID-008: Missing text parameter
  - ID-009: Empty text parameter
  - ID-010: Invalid audio file format

  **B. Logic (ID-011 ~ ID-015) - 5 TCs:**
  - ID-011: Text mismatch với audio content
  - ID-012: Long text assessment (paragraph)
  - ID-013: Multiple punctuation in text
  - ID-014: Special characters in text
  - ID-015: Very short audio (< 1 second)

  **C. Error code (ID-016 ~ ID-025) - 10 TCs:**
  - ID-016: Request without authentication token
  - ID-017: Request with expired token
  - ID-018: Request with invalid token
  - ID-019: Service not configured
  - ID-020: Service timeout
  - ID-021: Service returns 404
  - ID-022: Service returns 500
  - ID-023: Service returns invalid response
  - ID-024: Large file exceeds limit
  - ID-025: Concurrent requests

  **D. Format response (ID-026 ~ ID-030) - 5 TCs:**
  - ID-026: Verify response structure success case
  - ID-027: Verify response structure error case
  - ID-028: Verify overall_score field exists và valid
  - ID-029: Verify fluency_score field exists và valid
  - ID-030: Verify phoneme_scores array structure

  **Example row format:**
  ```
  | ID-001 | Validate | Gọi API với method POST | 1. Đăng nhập thành công | Send POST request | file=audio.wav<br>text=Hello world | N/A | 200 | {"statusCode":200,"error":null,"data":{...}} | N/A | PENDING | AUTOMATION | Happy path |
  ```

- [ ] Fill all 30 test cases với:
  - Concise precondition
  - Clear test steps
  - Realistic test data
  - Expected response với CHÍNH XÁC structure từ investigation
  - Proper Result/Execution assignment

**Output:** Consolidated Testcase với 30 test cases format như SAMPLE

---

### **Task 4: Verify Response Structure Accuracy**
**Duration:** ~10 minutes

**Actions:**
- [ ] Cross-check với pronunciation-assessment-service code:
  - ResponseFormatter.format_assessment_result() returns correct structure
  - Fields match: overall_score, fluency_score, phoneme_scores, total_phonemes, average_duration
  - Phoneme object fields: phoneme, gop_score, quality, start_time, end_time, character, word_index, phoneme_index

- [ ] Cross-check với controller code:
  - Controller wraps external service response in RestResponse
  - Fields: statusCode, error, message, data
  - data contains entire external service response

- [ ] Update test cases Body column với accurate response examples

**Output:** Verified accurate response structure

---

### **Task 5: Ensure SAMPLE Format Compliance**
**Duration:** ~10 minutes

**Actions:**
- [ ] **API Document:**
  - [ ] Page 1 có summary table
  - [ ] Page 2+ có chi tiết từng API
  - [ ] Format giống CreateVoucher example
  - [ ] Tables properly aligned
  - [ ] All sections complete

- [ ] **Testcase:**
  - [ ] Page 1 có TEST REPORT summary
  - [ ] Page 2 có project info và statistics
  - [ ] Page 3+ có test cases table
  - [ ] Format giống AddQuestionsToLesson example
  - [ ] All 30 TCs trong 1 table lớn
  - [ ] Columns match SAMPLE

- [ ] **Excel Compatibility:**
  - [ ] Tables dễ copy-paste
  - [ ] No line breaks in single cell (use <br> cho multi-line)
  - [ ] Proper alignment

**Output:** SAMPLE-compliant documents

---

### **Task 6: Final Quality Check**
**Duration:** ~10 minutes

**Actions:**
- [ ] **API Document Checklist:**
  - [ ] Summary table accurate
  - [ ] Response structure matches investigation
  - [ ] phoneme_scores array documented correctly
  - [ ] All fields from external service included
  - [ ] Error codes comprehensive
  - [ ] Examples realistic

- [ ] **Testcase Checklist:**
  - [ ] 30 test cases total
  - [ ] Grouped correctly (Validate 10, Logic 5, Error 10, Format 5)
  - [ ] Response examples use accurate structure
  - [ ] Test data realistic
  - [ ] All columns filled
  - [ ] Execution types assigned (20 AUTO, 10 MANUAL)

- [ ] **Verification:**
  - [ ] Count test cases: should be 30
  - [ ] Check response field names match pronunciation-assessment-service
  - [ ] Verify external service endpoint documented (/api/pronunciation-assessment)
  - [ ] Ensure no information missing vs investigation

**Output:** Production-ready consolidated documents

---

## ✅ Quality Checklist

Before delivery:
- [ ] Output directory created: `output/`
- [ ] API Document follows SAMPLE format exactly (Page 1 summary + Page 2+ details)
- [ ] Testcase follows SAMPLE format exactly (TEST REPORT + detailed table)
- [ ] Response structure accurate based on pronunciation-assessment-service investigation
- [ ] phoneme_scores array structure documented
- [ ] All 30 test cases present and properly formatted
- [ ] Test cases grouped correctly (Validate, Logic, Error, Format)
- [ ] Tables Excel-compatible
- [ ] No information loss vs investigation
- [ ] External service details documented

---

## 📦 Final Deliverables

**Two consolidated files in output directory:**
```
output/
├── API_Document_AssessPronunciation.md      # Consolidated API doc
└── Testcase_API_AssessPronunciation.md      # Consolidated testcase (30 TCs)
```

**Characteristics:**
- ✅ Consolidated format (not separate files)
- ✅ Follows SAMPLE structure exactly
- ✅ Response structure based on actual pronunciation-assessment-service code
- ✅ 30 test cases covering all scenarios
- ✅ Excel-ready tables
- ✅ Complete documentation

---

## 🔍 Key Investigation Findings

### pronunciation-assessment-service Response Structure:

**External Service Response Fields:**
```
data: {
  overall_score: Double (0-100)
  fluency_score: Double (0-100)
  phoneme_scores: Array[
    {
      phoneme: String (IPA symbol)
      gop_score: Double (0-100)
      quality: String (excellent/good/fair/poor)
      start_time: Double (seconds)
      end_time: Double (seconds)
      character: String
      word_index: Integer
      phoneme_index: Integer
    }
  ]
  total_phonemes: Integer
  average_duration: Double (seconds)
}
```

**Backend Controller Wrapper:**
```
{
  statusCode: Integer
  error: String (null on success)
  message: String
  data: <entire external service response>
}
```

**Service Details:**
- Endpoint: `POST /api/pronunciation-assessment`
- Service URL: Configured via `pronunciation-assessment.service.url`
- Timeout: 30s (configurable)
- Max file size: 6MB
- Supported formats: WAV, MP3, FLAC, M4A

---

## 💡 Critical Notes

### Response Structure:
1. **Accurate Fields**: Dựa trên actual code investigation
2. **phoneme_scores**: Array of objects, mỗi object có 8 fields
3. **Nested Structure**: data.phoneme_scores[].phoneme, gop_score, quality, etc.
4. **Quality Values**: "excellent", "good", "fair", "poor"

### Test Cases:
1. **30 Total**: Balanced coverage (10 validate, 5 logic, 10 error, 5 format)
2. **Response Examples**: Must use accurate field names
3. **Execution Split**: 20 AUTOMATION, 10 MANUAL
4. **Format**: Lần lượt từng dòng như SAMPLE

### SAMPLE Compliance:
1. **Consolidated**: 1 file API doc, 1 file testcase
2. **Structure**: Summary first, details after
3. **Tables**: Excel-ready format
4. **Content**: Concise but complete

---

## 🎯 Success Criteria

Documents are successful if:
- ✅ Output directory `output/` tồn tại
- ✅ API Document follows SAMPLE format (consolidated)
- ✅ Testcase follows SAMPLE format (30 TCs in 1 table)
- ✅ Response structure matches pronunciation-assessment-service exactly
- ✅ phoneme_scores array properly documented
- ✅ All test cases có accurate expected responses
- ✅ Tables dễ paste vào Excel
- ✅ No missing information from investigation
- ✅ Professional, production-ready quality

---

## 📝 Execution Order

1. Task 1: Create output directory (2 min)
2. Task 2: Generate consolidated API Document (25 min)
3. Task 3: Generate consolidated Testcase (35 min)
4. Task 4: Verify response structure accuracy (10 min)
5. Task 5: Ensure SAMPLE format compliance (10 min)
6. Task 6: Final quality check (10 min)

**Total estimated time:** ~1 hour 42 minutes

---

## 🚀 Ready to Execute

Tất cả thông tin cần thiết đã được thu thập:
- ✅ pronunciation-assessment-service structure investigated
- ✅ SAMPLE format analyzed
- ✅ Controller code reviewed
- ✅ Response fields documented
- ✅ Plan detailed and ready

**Next step:** Execute plan để tạo consolidated output documents!
