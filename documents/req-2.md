# Plan Task req-2: Chuyển đổi Format API Document & Testcase sang Excel-friendly

## 📥 Context & Input Files

### Output Files từ req-1 (Cần sửa):
1. `documents/API_Document_AssessPronunciation.md` - Format hiện tại (Markdown)
2. `documents/Testcase_API_AssessPronunciation.md` - Format hiện tại (Markdown)

### New Sample Files (Format mẫu mới):
3. `documents/API Document - SAMPLE.pdf` - Format Excel-friendly với 3 API examples:
   - CreateVoucher (POST /api/Voucher)
   - CreateKhachHang (POST /api/KhachHang)
   - UpdateSanPham (PUT /api/SanPham/UpdateSanPham)
4. `documents/Testcase API - SAMPLE.pdf` - Format testcase Excel-friendly (AddQuestionsToLesson API)

### Original Input Files (Reference):
5. `backend-service/src/main/java/utc/englishlearning/Encybara/controller/PronunciationAssessmentController.java`
6. `documents/API Document.pdf` - Template gốc
7. `documents/Testcase API.pdf` - Template gốc

---

## 🎯 Objective

Chuyển đổi **2 file output từ req-1** sang **format mới Excel-friendly** dựa trên SAMPLE files:
1. **API Document** - Format dạng bảng, dễ copy-paste vào Excel
2. **Testcase API** - Format dạng bảng chi tiết theo từng test case, dễ tracking và paste vào Excel

**Key Requirement**: Format phải dễ dàng paste vào Excel với structure rõ ràng, columns alignment tốt.

---

## 📊 Format Analysis từ SAMPLE Files

### 📄 API Document - SAMPLE Format:

**Structure:**
```
Page 1: Summary Table
┌──────┬────────────────┬─────────────────────────────────┬────────┬─────────┐
│ No.  │ Sheet name     │ API                             │ Status │ Remarks │
├──────┼────────────────┼─────────────────────────────────┼────────┼─────────┤
│ 1    │ CreateVoucher  │ POST/api/Voucher                │ Done   │         │
│ 2    │ CreateKhachHang│ POST/api/KhachHang              │ Done   │         │
│ 3    │ UpdateSanPham  │ PUT/api/SanPham/UpdateSanPham   │ Done   │         │
└──────┴────────────────┴─────────────────────────────────┴────────┴─────────┘

Page 2+: Chi tiết từng API
┌─────────────────┬──────────────────────────┐
│ API name        │ CreateVoucher            │
│ Endpoint        │ /api/Voucher             │
└─────────────────┴──────────────────────────┘

Request
┌────────┬───────┐
│ Method │ Post  │
│ Format │ Json  │
└────────┴───────┘

Header Table:
┌────┬────────┬───────────┬───────────┬──────┐
│ No │ Header │ Data Type │ Mandatory │ Note │
└────┴────────┴───────────┴───────────┴──────┘

Params Table:
┌────┬────────┬───────────┬───────────┬──────┐
│ No │ Params │ Data Type │ Mandatory │ Note │
└────┴────────┴───────────┴───────────┴──────┘

Body Table:
┌────┬─────────────┬───────────┬───────────┬────────────────────────────┐
│ No │ Body Data   │ Data Type │ Mandatory │ Note                       │
├────┼─────────────┼───────────┼───────────┼────────────────────────────┤
│ 1  │ ten         │ nvarchar  │ ◯         │ Tối đa 40 ký tự...         │
│ 2  │ hinhThuc... │ int       │ ◯         │ có 2 giá trị: 1: ..., 0:...│
└────┴─────────────┴───────────┴───────────┴────────────────────────────┘

Request Sample:
{ "ten": "...", "hinhThucGiamGia": 0, ... }

Response Table:
┌────┬─────┬───────────┬───────────┬──────┐
│ No │ Key │ Data Type │ Mandatory │ Note │
└────┴─────┴───────────┴───────────┴──────┘

Response Example:
...

Error code Table:
┌────┬────────────┬──────────────────────────┐
│ No │ Error code │ Note                     │
├────┼────────────┼──────────────────────────┤
│ 1  │ 200        │ Success                  │
│ 2  │ 400        │ Bad request - MS001      │
└────┴────────────┴──────────────────────────┘
```

**Key Characteristics:**
- Compact, table-driven format
- Mỗi field trong table có số thứ tự
- Note column chứa tất cả validation rules
- Request/Response samples ngắn gọn
- Error codes có mã lỗi cụ thể (MS001, MSAPI00101, etc.)

---

### 📋 Testcase API - SAMPLE Format:

**Structure:**
```
Page 1: TEST REPORT Summary
┌────┬────────────┬──────────┬───────┬─────────┬────────────┬──────────┐
│ No │ Sheet name │ API name │ Total │ Result  │ Execution  │ Progress │
│    │            │          │ TC    │ P/F/B/N │ Auto/Man   │ %        │
└────┴────────────┴──────────┴───────┴─────────┴────────────┴──────────┘

Page 2+: Chi tiết Test Cases
┌─────────────────┬────────────────────────────────┐
│ Project Name    │ Encybara - English Learning... │
│ API Name        │ AddQuestionsToLesson           │
│ Owner           │                                │
└─────────────────┴────────────────────────────────┘

┌─────────┬───┐
│ PASSED  │ 24│
│ FAILED  │ 5 │
│ PENDING │ 0 │
│ NOT RUN │ 0 │
│ AUTO    │ 0 │
│ MANUAL  │ 29│
└─────────┴───┘

Test Cases Table:
┌─────┬──────┬──────────────┬──────────────┬───────────┬─────────┬──────┬─────────────────┬─────────────────┬──────────┬────────┬───────────┬──────┐
│ ID  │ Item │ Test Name    │ Precondition │ Test Case │ params  │ body │ Status code     │ Body            │ Expected │ Result │ Execution │ Note │
│     │      │              │              │           │         │      │                 │                 │ Database │        │           │      │
├─────┼──────┼──────────────┼──────────────┼───────────┼─────────┼──────┼─────────────────┼─────────────────┼──────────┼────────┼───────────┼──────┤
│ID-001│Method│Gọi API với...│1. Đăng nhập  │           │         │      │200 OK           │                 │          │Passed  │Manual     │      │
│ID-002│Validate│questionIds │1. Đăng nhập  │Kiểm thử..│lessonId=2│{...} │200 OK           │"Questions added"│          │Passed  │Manual     │      │
└─────┴──────┴──────────────┴──────────────┴───────────┴─────────┴──────┴─────────────────┴─────────────────┴──────────┴────────┴───────────┴──────┘

Grouped by Item:
- Validate (ID-001 ~ ID-010)
- Logic (ID-011 ~ ID-020)
- Error code (ID-021 ~ ID-030)
- Format response (ID-031+)
```

**Key Characteristics:**
- Mỗi test case là 1 row trong table lớn
- Columns: ID, Item, Testcase Name, Precondition, Test Case, params, body, Status code, Body, Expected Database, Result, Execution, Note
- Test data được viết trực tiếp trong cell (ngắn gọn)
- Grouped by "Item" category (Validate, Logic, Error code, Format response, Other)
- Summary statistics ở đầu file

---

## 📋 Task Plan

### **Task 1: Analyze Format Differences**
**Duration:** ~15 minutes

**Actions:**
- [x] So sánh format hiện tại (Markdown) vs format mới (Table-based)
- [x] Identify key differences:
  - Current: Markdown với nhiều sections, mỗi test case là 1 section riêng
  - New: Table-driven, compact, Excel-ready
  - Current: Verbose explanations
  - New: Concise, cell-based data
- [x] List ra các thay đổi cần thiết:
  - **API Document**: Chuyển từ Markdown tables → Plain text tables với alignment tốt hơn
  - **Testcase**: Chuyển từ multiple sections → Single large table với multiple rows

**Output:** Clear understanding of transformation needed

---

### **Task 2: Read Original Controller for Complete Context**
**Duration:** ~10 minutes

**Actions:**
- [ ] Đọc `PronunciationAssessmentController.java` để extract:
  - Endpoint details
  - Request parameters (file, text)
  - Validation logic
  - Error handling scenarios
  - Response structure
- [ ] Cross-reference với current output để verify accuracy
- [ ] Note down any missing information trong current output

**Output:** Complete API specification reference

---

### **Task 3: Transform API Document to Excel-friendly Format**
**Duration:** ~30 minutes

**Actions:**
- [ ] Tạo file mới `documents/API_Document_AssessPronunciation_v2.md`

- [ ] **Page 1: Summary Table**
  ```
  ┌────┬──────────────────────────┬────────────────────────────────────┬────────┬─────────┐
  │ No │ Sheet name               │ API                                │ Status │ Remarks │
  ├────┼──────────────────────────┼────────────────────────────────────┼────────┼─────────┤
  │ 1  │ AssessPronunciation      │ POST /api/v1/pronunciation/assess  │ Done   │         │
  └────┴──────────────────────────┴────────────────────────────────────┴────────┴─────────┘
  ```

- [ ] **Page 2: API Detail**

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
  ┌────┬───────────────┬───────────┬───────────┬──────────────────────────────┐
  │ No │ Header        │ Data Type │ Mandatory │ Note                         │
  ├────┼───────────────┼───────────┼───────────┼──────────────────────────────┤
  │ 1  │ Authorization │ String    │ ◯         │ Bearer token (JWT)           │
  │ 2  │ Content-Type  │ String    │ ◯         │ multipart/form-data          │
  └────┴───────────────┴───────────┴───────────┴──────────────────────────────┘
  ```

  **Params Table:**
  ```
  ┌────┬────────┬─────────────────┬───────────┬────────────────────────────────────┐
  │ No │ Params │ Data Type       │ Mandatory │ Note                               │
  ├────┼────────┼─────────────────┼───────────┼────────────────────────────────────┤
  │ 1  │ file   │ MultipartFile   │ ◯         │ Audio file (WAV, MP3, etc.)        │
  │ 2  │ text   │ String          │ ◯         │ Reference text for pronunciation   │
  └────┴────────┴─────────────────┴───────────┴────────────────────────────────────┘
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

  **Response Table:**
  ```
  ┌────┬────────────┬───────────┬───────────┬──────────────────────────┐
  │ No │ Key        │ Data Type │ Mandatory │ Note                     │
  ├────┼────────────┼───────────┼───────────┼──────────────────────────┤
  │ 1  │ statusCode │ Integer   │ ◯         │ HTTP status code         │
  │ 2  │ error      │ String    │ ◯         │ Error message (null OK)  │
  │ 3  │ message    │ String    │ ◯         │ Response message         │
  │ 4  │ data       │ Object    │ ◯         │ Assessment result data   │
  └────┴────────────┴───────────┴───────────┴──────────────────────────┘
  ```

  **Response Example:**
  ```json
  {
    "statusCode": 200,
    "error": null,
    "message": "Pronunciation assessment completed successfully",
    "data": {
      "accuracyScore": 85.5,
      "pronunciationScore": 82.3,
      "fluencyScore": 88.7,
      "completenessScore": 90.0,
      "prosodyScore": 84.2,
      "words": [...]
    }
  }
  ```

  **Error code Table:**
  ```
  ┌────┬────────────┬────────────────────────────────────────────────────────┐
  │ No │ Error code │ Note                                                   │
  ├────┼────────────┼────────────────────────────────────────────────────────┤
  │ 1  │ 200        │ Success                                                │
  │ 2  │ 400        │ Bad Request - Missing audio file                       │
  │ 3  │ 400        │ Bad Request - Missing text                             │
  │ 4  │ 401        │ Unauthorized - Invalid/missing JWT token               │
  │ 5  │ 404        │ Not Found - Service endpoint not found                 │
  │ 6  │ 503        │ Service Unavailable - Service not configured           │
  │ 7  │ 503        │ Service Unavailable - Connection timeout               │
  │ 8  │ 500        │ Internal Server Error                                  │
  └────┴────────────┴────────────────────────────────────────────────────────┘
  ```

- [ ] Format tables với proper alignment cho Excel paste
- [ ] Keep content concise trong Note columns
- [ ] Ensure all mandatory fields marked with ◯

**Output:** Excel-friendly API Document

---

### **Task 4: Transform Testcase to Excel-friendly Format**
**Duration:** ~45 minutes

**Actions:**
- [ ] Tạo file mới `documents/Testcase_API_AssessPronunciation_v2.md`

- [ ] **Page 1: TEST REPORT Summary**
  ```
  TEST REPORT

  ┌────┬──────────────────────────┬──────────────────────────────────┬───────┬────────┬────────┬─────────┬─────────┬──────┬────────┬─────────┬──────────┬──────────┐
  │ No │ Sheet name               │ API name                         │ Total │ Result                          │ Execution          │ Status   │ Progress │ Remarks  │
  │    │                          │                                  │ TC    │ Passed │ Failed │ Blocked │ Not run │ Auto │ Manual │ N/A      │ Testing  │ %        │          │
  ├────┼──────────────────────────┼──────────────────────────────────┼───────┼────────┼────────┼─────────┼─────────┼──────┼────────┼─────────┼──────────┼──────────┤
  │ 1  │ AssessPronunciation      │ POST /api/v1/pronunciation/assess│ 20    │ 0      │ 0      │ 0       │ 20      │ 15   │ 5      │ 0       │          │          │
  └────┴──────────────────────────┴──────────────────────────────────┴───────┴────────┴────────┴─────────┴─────────┴──────┴────────┴─────────┴──────────┴──────────┘
  ```

- [ ] **Page 2: Project Info & Statistics**
  ```
  Project Name    Encybara - English Learning Platform
  API Name        POST /api/v1/pronunciation/assess
  Owner

  ┌─────────────┬────┐
  │ PASSED      │ 0  │
  │ FAILED      │ 0  │
  │ PENDING     │ 20 │
  │ NOT RUN     │ 0  │
  │ AUTOMATION  │ 15 │
  │ MANUAL      │ 5  │
  │ Total TC    │ 20 │
  └─────────────┴────┘
  ```

- [ ] **Page 3+: Test Cases Table**

  Convert all 20 test cases từ current format → Single table format:

  ```
  ┌────────┬──────────────────┬──────────────────────────┬──────────────────────┬─────────────────────┬─────────────┬──────────────────┬──────────────┬──────────────────────┬──────────────────┬────────┬───────────┬──────┐
  │ ID     │ Item             │ Testcase Name            │ Precondition         │ Test Case           │ params      │ body             │ Status code  │ Body                 │ Expected         │ Result │ Execution │ Note │
  │        │                  │                          │                      │                     │             │                  │              │                      │ Database         │        │           │      │
  ├────────┼──────────────────┼──────────────────────────┼──────────────────────┼─────────────────────┼─────────────┼──────────────────┼──────────────┼──────────────────────┼──────────────────┼────────┼───────────┼──────┤
  │ Validate                                                                                                                                                                                                                  │
  ├────────┼──────────────────┼──────────────────────────┼──────────────────────┼─────────────────────┼─────────────┼──────────────────┼──────────────┼──────────────────────┼──────────────────┼────────┼───────────┼──────┤
  │ TC-001 │ Validate         │ Valid pronunciation...   │ User authenticated   │ Send POST with...   │ file=audio  │ text="Hello..."  │ 200          │ {statusCode:200,...} │ N/A              │ PENDING│ AUTOMATION│      │
  │        │                  │                          │ Service running      │                     │             │                  │              │                      │                  │        │           │      │
  ├────────┼──────────────────┼──────────────────────────┼──────────────────────┼─────────────────────┼─────────────┼──────────────────┼──────────────┼──────────────────────┼──────────────────┼────────┼───────────┼──────┤
  │ TC-002 │ Validate         │ Missing audio file       │ User authenticated   │ Send POST without...│             │ text="Hello"     │ 400          │ {error:"Missing...} │ N/A              │ PENDING│ AUTOMATION│      │
  ├────────┼──────────────────┼──────────────────────────┼──────────────────────┼─────────────────────┼─────────────┼──────────────────┼──────────────┼──────────────────────┼──────────────────┼────────┼───────────┼──────┤
  │ ...    │ ...              │ ...                      │ ...                  │ ...                 │ ...         │ ...              │ ...          │ ...                  │ ...              │ ...    │ ...       │ ...  │
  └────────┴──────────────────┴──────────────────────────┴──────────────────────┴─────────────────────┴─────────────┴──────────────────┴──────────────┴──────────────────────┴──────────────────┴────────┴───────────┴──────┘
  ```

- [ ] Transform all 20 test cases:
  - **Validate (TC-001 ~ TC-007)**: Input validation tests
  - **Logic (TC-008 ~ TC-010)**: Business logic tests
  - **Error Code (TC-011 ~ TC-017)**: Error handling tests
  - **Format Response (TC-018 ~ TC-019)**: Response structure tests
  - **Other (TC-020)**: Concurrent requests test

- [ ] For each test case:
  - Shorten Precondition to key points only
  - Condense Test Case steps to concise description
  - params column: Key parameters only (e.g., "file=audio.wav")
  - body column: Simplified JSON or "N/A"
  - Status code: Just the code (200, 400, 401, etc.)
  - Body: Shortened response (key fields only)
  - Expected Database: "N/A" hoặc brief description
  - Result: PENDING/PASSED/FAILED
  - Execution: AUTOMATION/MANUAL

**Output:** Excel-friendly Testcase document

---

### **Task 5: Ensure Excel Paste Compatibility**
**Duration:** ~15 minutes

**Actions:**
- [ ] Review table formatting:
  - Use consistent column separators (│, ├, ┼, ┤)
  - Align columns properly
  - Ensure no line breaks within cells (use comma for multiple items)
- [ ] Test paste into Excel:
  - Tables should maintain structure
  - Columns should be clearly separated
  - No formatting issues
- [ ] Optimize for readability:
  - Keep cell content concise
  - Use abbreviations where appropriate
  - Ensure all data is visible without scrolling

**Output:** Verified Excel-compatible tables

---

### **Task 6: Quality Check & Finalization**
**Duration:** ~15 minutes

**Actions:**
- [ ] **API Document v2 Checklist:**
  - [ ] Summary table có đầy đủ API info
  - [ ] Header, Params, Body tables complete
  - [ ] Request sample realistic
  - [ ] Response table accurate
  - [ ] Error codes comprehensive
  - [ ] All tables aligned properly
  - [ ] Content concise, không verbose

- [ ] **Testcase v2 Checklist:**
  - [ ] TEST REPORT summary accurate
  - [ ] Project info complete
  - [ ] Statistics correct (20 test cases)
  - [ ] All test cases converted to table rows
  - [ ] Grouped by Item correctly
  - [ ] Test data concise nhưng sufficient
  - [ ] Expected results clear
  - [ ] Execution type assigned

- [ ] **Cross-verification:**
  - [ ] API Document v2 matches controller code
  - [ ] Testcase v2 covers all scenarios from API doc
  - [ ] No missing information vs current version
  - [ ] Format consistent với SAMPLE files

- [ ] **Excel Compatibility:**
  - [ ] Copy một section → paste vào Excel → verify
  - [ ] Table structure maintained
  - [ ] No broken formatting

**Output:** Production-ready documents

---

## ✅ Quality Checklist

Before delivery:
- [ ] API Document v2 follows SAMPLE format exactly
- [ ] Testcase v2 follows SAMPLE format exactly
- [ ] All tables are properly formatted with alignment
- [ ] Content is concise, Excel-friendly
- [ ] No verbose explanations in table cells
- [ ] All test cases fit in single table structure
- [ ] Test data is realistic but brief
- [ ] Error codes match controller implementation
- [ ] Documents can be directly pasted into Excel
- [ ] No information loss compared to v1

---

## 📦 Final Deliverables

**Two new Markdown files:**
```
documents/API_Document_AssessPronunciation_v2.md
documents/Testcase_API_AssessPronunciation_v2.md
```

**Format characteristics:**
- ✅ Table-driven, compact structure
- ✅ Excel-friendly formatting
- ✅ Easy to copy-paste vào Excel
- ✅ Proper alignment và separators
- ✅ Concise content trong cells
- ✅ Clear column headers
- ✅ Grouped sections (for testcases)

---

## 🔄 Comparison: Old vs New Format

### API Document:

| Aspect | Old Format (v1) | New Format (v2) |
|--------|----------------|-----------------|
| **Structure** | Markdown sections với multiple headers | Table-driven với clear sections |
| **Tables** | Markdown tables (`\| col \|`) | Plain text tables (┌─┬─┐) |
| **Content** | Verbose explanations | Concise, cell-based data |
| **Excel Paste** | Requires reformatting | Direct paste-ready |
| **Readability** | Good for reading | Optimized for Excel |

### Testcase:

| Aspect | Old Format (v1) | New Format (v2) |
|--------|----------------|-----------------|
| **Structure** | Each TC = separate section | All TCs in single table |
| **Detail Level** | Very detailed, verbose | Concise, essential info only |
| **Grouping** | Sections by category | Rows grouped by "Item" |
| **Test Data** | Full JSON blocks | Shortened key-value pairs |
| **Excel Paste** | Very difficult | Direct paste-ready |
| **Tracking** | Manual effort | Easy row-by-row tracking |

---

## 💡 Critical Notes

### When creating v2 files:

1. **Table Formatting:**
   - Use box-drawing characters (┌─┬─┐ ├─┼─┤ └─┴─┘)
   - Maintain consistent column widths
   - Align content properly

2. **Content Condensing:**
   - Shorten preconditions to key points
   - Reduce test steps to brief description
   - Use shortened JSON in cells
   - Keep notes minimal

3. **Excel Compatibility:**
   - Avoid line breaks in cells
   - Use commas for multiple items
   - Keep cell content readable at normal width
   - Test actual paste into Excel

4. **Data Preservation:**
   - Don't lose important validation rules
   - Keep all error scenarios
   - Maintain test coverage
   - Ensure no missing test cases

5. **Reference SAMPLE Files:**
   - Follow exact table structure from CreateVoucher example
   - Follow testcase format from AddQuestionsToLesson example
   - Match column headers exactly
   - Use same symbols (◯ for mandatory, etc.)

---

## 🎯 Success Criteria

Documents are successful if:
- Tester có thể paste trực tiếp vào Excel mà không cần format lại
- Table structure remains intact sau khi paste
- All columns are clearly separated
- Data is complete và sufficient for testing
- Format matches SAMPLE files exactly
- Content concise nhưng comprehensive
- Easy to track test execution status trong Excel

---

## 📌 Additional Requirements

1. **Keep original files:** Giữ lại v1 files để reference
2. **Version naming:** Clearly mark as v2 để distinguish
3. **Backward compatibility:** Ensure all info from v1 is preserved in v2
4. **Documentation:** Add brief note at top of v2 files explaining format

---

## 🚀 Execution Order

1. Task 1: Analyze formats (15 min)
2. Task 2: Read controller (10 min)
3. Task 3: Create API Doc v2 (30 min)
4. Task 4: Create Testcase v2 (45 min)
5. Task 5: Excel compatibility check (15 min)
6. Task 6: Quality check (15 min)

**Total estimated time:** ~2 hours 10 minutes

---

## 📝 Notes for Implementation

- Prioritize Excel-paste compatibility over visual beauty
- Keep cells concise - aim for < 100 chars per cell
- Use abbreviations where appropriate (TC = Test Case, etc.)
- Reference SAMPLE PDFs frequently during creation
- Test paste into Excel at least 3 times during process
- Verify all 20 test cases are present in final table
