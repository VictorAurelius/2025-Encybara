# Plan Task cho Claude Code - Tạo tài liệu Test API

## 📥 Available Input Files

### Template Files:
1. `documents/API_Document.pdf` - Template for API documentation
2. `documents/Testcase_API.pdf` - Template for API test cases

### User-Provided Inputs:
3. **Controller file path** : backend-service/.../PronunciationAssessmentController.java
4. **Endpoint name** : assessPronunciation

---

## 🎯 Objective

Tạo **2 file Excel** tài liệu test API hoàn chỉnh dựa trên template, bao gồm:
1. **API Documentation** - Mô tả chi tiết API endpoint
2. **Test Cases** - Danh sách các test case để thực thi manual/automation

---

## 📋 Task Plan

### **Task 1: Analyze Templates**
**Duration:** ~10 minutes

**Actions:**
- [ ] Đọc `API_Document.pdf` để hiểu cấu trúc template:
  - API name, Endpoint, Method
  - Request structure (Header, Params, Body)
  - Response structure (Success & Error)
  - Error codes
  
- [ ] Đọc `Testcase_API.pdf` để hiểu format test case:
  - Project info (Name, API Name, Owner)
  - Test case structure (ID, Name, Precondition, Test data, Expected result)
  - Status tracking columns (PENDING, NOT RUN, PASSED, FAILED)
  - Request/Response format
  - Database validation
  - Logic & Error code sections

- [ ] Tạo mental model về relationship giữa 2 documents:
  - API Document = Specification
  - Test Cases = Validation scenarios dựa trên spec

**Output:** Understanding of template structure

---

### **Task 2: Parse Controller & Extract API Information**
**Duration:** ~15 minutes

**Actions:**
- [ ] Đọc file controller được user chỉ định (ví dụ: `UserController.java`, `user.controller.ts`)

- [ ] Tìm endpoint được chỉ định và extract thông tin:
  ```
  - HTTP Method (GET/POST/PUT/DELETE/PATCH)
  - Endpoint URL
  - Request Headers (Authorization, Content-Type, etc.)
  - Request Parameters (Query params, Path params)
  - Request Body (JSON schema, fields, types, mandatory)
  - Response format (Success response schema)
  - Error responses (Error codes, messages)
  - Business logic (từ code hoặc comments)
  ```

- [ ] Parse annotations/decorators để lấy validation rules:
  - Required fields
  - Data types
  - Max/min length
  - Regex patterns
  - Custom validators

- [ ] Identify dependencies:
  - Database tables affected
  - External API calls
  - Services/repositories used

**Output:** Complete API specification extracted from code

---

### **Task 3: Generate API Documentation**
**Duration:** ~20 minutes

**Actions:**
- [ ] Tạo file Excel `/mnt/user-data/outputs/API_Document_[EndpointName].xlsx` theo template

- [ ] Fill in sections:

  **1. API Overview:**
  ```
  - API name: [Extracted from endpoint]
  - Endpoint: [Full URL path]
  - Method: [HTTP method]
  - Format data: JSON
  ```

  **2. Request Section:**
  
  **Headers Table:**
  | No | Header | Data Type | Mandatory | Note |
  |----|--------|-----------|-----------|------|
  | 1 | Authorization | String | Yes | Bearer token |
  | 2 | Content-Type | String | Yes | application/json |
  | ... | ... | ... | ... | ... |

  **Params Table:**
  | No | Params | Data Type | Mandatory | Note |
  |----|--------|-----------|-----------|------|
  | 1 | userId | String | Yes | User identifier |
  | ... | ... | ... | ... | ... |

  **Body Table:**
  | No | Body Data | Data Type | Mandatory | Note |
  |----|-----------|-----------|-----------|------|
  | 1 | username | String | Yes | Max 50 chars |
  | 2 | email | String | Yes | Valid email format |
  | ... | ... | ... | ... | ... |

  **Request Sample:**
  ```json
  {
    "username": "john_doe",
    "email": "john@example.com",
    ...
  }
  ```

  **3. Response Section:**
  
  **Success Response Table:**
  | No | Key | Data Type | Mandatory | Note |
  |----|-----|-----------|-----------|------|
  | 1 | status | String | Yes | Success/Error |
  | 2 | data | Object | Yes | Response payload |
  | ... | ... | ... | ... | ... |

  **Response Example:**
  ```json
  {
    "status": "success",
    "data": { ... },
    "message": "Created successfully"
  }
  ```

  **4. Error Codes Table:**
  | No | Error code | Note |
  |----|------------|------|
  | 1 | 400 | Bad Request - Invalid input |
  | 2 | 401 | Unauthorized - Invalid token |
  | 3 | 404 | Not Found - Resource not exists |
  | 4 | 500 | Internal Server Error |
  | ... | ... | ... |

**Output:** Completed API_Document Excel file

---

### **Task 4: Generate Test Cases**
**Duration:** ~30 minutes

**Actions:**
- [ ] Tạo file Excel `/mnt/user-data/outputs/Testcase_API_[EndpointName].xlsx` theo template

- [ ] Fill in Project Information:
  ```
  - Project Name: [From context or ask user]
  - API Name: [Endpoint name]
  - Owner: [Leave blank for user to fill]
  - Number of Testcase: [Auto-calculate from generated cases]
  ```

- [ ] Generate comprehensive test cases covering:

  **A. Happy Path (Positive Test Cases):**
  ```
  TC001: Create user with valid data
  TC002: Create user with minimum required fields
  TC003: Create user with all optional fields
  ```

  **B. Validation Test Cases (Negative):**
  ```
  TC004: Missing required field [field_name]
  TC005: Invalid data type for [field_name]
  TC006: Exceed max length for [field_name]
  TC007: Invalid format for [field_name] (email, phone, etc.)
  TC008: Duplicate [unique_field]
  ```

  **C. Authentication/Authorization:**
  ```
  TC009: Request without token
  TC010: Request with invalid token
  TC011: Request with expired token
  TC012: Request with insufficient permissions
  ```

  **D. Business Logic:**
  ```
  TC013: [Specific business rule validation]
  TC014: [Edge case handling]
  ```

  **E. Error Handling:**
  ```
  TC015: Handle database connection error
  TC016: Handle external service timeout
  ```

- [ ] For each test case, fill in columns:

  | Column | Content |
  |--------|---------|
  | **ID** | TC001, TC002, ... |
  | **Item** | Validate/Logic/Error code/Format response |
  | **Testcase Name** | Descriptive name |
  | **Precondition** | Setup requirements (user exists, DB state, etc.) |
  | **Test Case** | Detailed steps to execute |
  | **Test data - Request** | JSON request with specific test values |
  | **params** | URL/Query parameters |
  | **body** | Request body JSON |
  | **Expected Response - Status code** | 200, 400, 401, etc. |
  | **Expected Response - Body** | Expected JSON response |
  | **Validate** | What to validate (response fields, DB changes) |
  | **Expected Database Result** | DB state after API call |
  | **Execution** | AUTOMATION / MANUAL |
  | **Note** | Additional context |
  | **Status** | PENDING (default) |

- [ ] Group test cases by categories:
  - Sheet 1: "All Test Cases" (tổng hợp)
  - Sheet 2: "Validate" (validation tests)
  - Sheet 3: "Logic" (business logic tests)
  - Sheet 4: "Error code" (error handling tests)
  - Sheet 5: "Other" (edge cases)

**Output:** Completed Testcase_API Excel file with comprehensive test scenarios

---

### **Task 5: Quality Check & Finalization**
**Duration:** ~10 minutes

**Actions:**
- [ ] Review API_Document:
  - All mandatory fields documented
  - Request/Response samples are valid JSON
  - Error codes are complete
  - Data types are accurate

- [ ] Review Testcase_API:
  - Test cases cover all validation rules from code
  - Each test case has clear expected result
  - Test data is realistic and valid
  - No duplicate test scenarios
  - Proper categorization (Validate/Logic/Error/etc.)

- [ ] Ensure consistency:
  - Field names match between documentation and test cases
  - Data types are consistent
  - Error codes in doc match test cases

- [ ] Add summary section in test case file:
  - Total test cases: [count]
  - By category: Validate (X), Logic (Y), Error (Z)
  - By execution: Manual (A), Automation (B)

**Output:** Finalized, review-ready documents

---

## ✅ Quality Checklist

Before delivering, verify:
- [ ] API Document has all sections filled (no [TBD])
- [ ] Request/Response samples are valid and realistic
- [ ] Error codes are comprehensive (4xx, 5xx scenarios)
- [ ] Test cases cover positive, negative, and edge cases
- [ ] Each test case has clear precondition and expected result
- [ ] Test data in test cases is specific (not generic placeholders)
- [ ] Database validation columns specify table and expected changes
- [ ] Excel formatting is clean and readable
- [ ] Column widths are adjusted for readability

---

## 📦 Final Deliverables

**Two Excel files:**
```
/mnt/user-data/outputs/API_Document_[EndpointName].xlsx
/mnt/user-data/outputs/Testcase_API_[EndpointName].xlsx
```

These files must be:
- ✅ Ready for tester to execute without additional clarification
- ✅ Complete with all validation scenarios
- ✅ Properly formatted in Excel (tables, headers, colors)
- ✅ Include realistic test data samples
- ✅ Have clear pass/fail criteria for each test case

---

## 💡 Critical Instructions for Claude Code

### When analyzing controller code:
1. **Focus on the specified endpoint only** - don't document other endpoints
2. **Extract validation rules carefully** - from annotations, validators, and code logic
3. **Identify all possible error scenarios** - from try-catch blocks, if conditions, validation logic
4. **Note database operations** - CREATE, UPDATE, DELETE for "Expected Database Result"

### When generating test cases:
1. **Be specific with test data** - use real-looking values, not "test123"
2. **Cover boundary conditions** - min/max lengths, empty strings, null values
3. **Include authentication scenarios** - token-based tests if API is protected
4. **Consider cascading effects** - if API creates records, test duplicate scenarios
5. **Minimum 15-20 test cases** - comprehensive coverage expected

### Excel formatting:
1. **Use XLSX format with the xlsx skill** - read `/mnt/skills/public/xlsx/SKILL.md` first
2. **Freeze header rows** - for easy scrolling
3. **Auto-fit column widths** - for readability
4. **Use borders and colors** - for visual separation
5. **Bold headers** - for clarity

### Example interaction:
```
User: Generate test docs for POST /api/users/register in UserController.java
Claude: 
1. [Reads controller file]
2. [Extracts endpoint details]
3. [Generates API_Document_UserRegister.xlsx]
4. [Generates Testcase_API_UserRegister.xlsx]
5. [Provides download links]
```

---

## 🎯 Success Criteria

Documents are successful if:
- Tester can execute all test cases without asking for clarification
- API document can be used by frontend developer to integrate
- Test coverage is comprehensive (positive, negative, edge cases)
- All validation rules from code are covered in test cases
- Excel files are professionally formatted and easy to read