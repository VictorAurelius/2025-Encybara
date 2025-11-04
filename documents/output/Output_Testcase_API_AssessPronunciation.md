ADMIN@VANKIET MINGW64 /f/code/a-hoctap/project-1/2025-Encybara (feature/documents-for-testcase)       
$ ./backend-service/test-pronunciation-testcase.sh
============================================
  PRONUNCIATION API TEST SUITE
============================================

Based on: documents/output/Testcase_API_AssessPronunciation.md
Total Test Cases: 30 (20 AUTOMATION + 10 MANUAL)

Configuration:
  Backend URL: http://18.136.223.96:8080
  Audio file: documents/input/audio_sample.mp3
  Reference text: Most of my peers go crazy about Vietnamese rap music 'cause it's in vogue, you k... 

✓ Audio file found

Getting authentication token...
✓ Authentication successful
Access token: eyJhbGciOiJIUzUxMiJ9...
----------------------------------------
========================================
  VALIDATE CATEGORY (10 TCs)
========================================

[ID-001] Testing: Gọi API với method POST
  ✓ PASSED - HTTP 200

[ID-002] Testing: Gọi API với method GET
  ✗ FAILED - Expected 405, got 500
  Response: {"statusCode":500,"message":"Request method 'GET' is not supported"}...

[ID-003] Testing: Gọi API với method PUT
  ✗ FAILED - Expected 405, got 500
  Response: {"statusCode":500,"message":"Request method 'PUT' is not supported"}...

[ID-004] Testing: Gọi API với method DELETE
  ✗ FAILED - Expected 405, got 500
  Response: {"statusCode":500,"message":"Request method 'DELETE' is not supported"}...

[ID-005] Testing: Valid request với đầy đủ params
  ✓ PASSED - HTTP 200

[ID-006] Testing: Missing audio file
  ✗ FAILED - Expected 400, got 500
  Response:
000{"statusCode":500,"message":"Required part 'file' is not present."}...

[ID-007] Creating empty audio file for test...
[ID-007] Testing: Empty audio file
  ✓ PASSED - HTTP 400

[ID-008] Testing: Missing text parameter
  ✗ FAILED - Expected 400, got 500
  Response: {"statusCode":500,"message":"Required request parameter 'text' for method parameter type String is not present"}...

[ID-009] Testing: Empty text parameter (whitespace only)
  ✗ FAILED - Expected 400, got 503
  Response:
000{"statusCode":503,"error":"Pronunciation assessment failed","message":"Lỗi kết nối tới pronunciation-assessment-service: 500 INTERNAL SERVER ERROR: \"{\"code\":500,\"error\":\"Pronunciation assess...  

[ID-010] Creating invalid file for test...
[ID-010] Testing: Invalid audio file format
  → Manual test - skipping automation

========================================
  LOGIC CATEGORY (5 TCs)
========================================

[ID-011] Testing: Text mismatch với audio content
  → Manual test - skipping automation

[ID-012] Testing: Long text assessment (paragraph-length)
  → Manual test - skipping automation

[ID-013] Testing: Multiple punctuation in text
  ✓ PASSED - HTTP 200

[ID-014] Testing: Special characters and numbers in text
  ✓ PASSED - HTTP 200

[ID-015] Testing: Very short audio test
  → Manual test - skipping automation

========================================
  ERROR CODE CATEGORY (10 TCs)
========================================

[ID-016] Testing: Request without authentication token
  ✓ PASSED - HTTP 401

[ID-017] Testing: Request with expired token
  ✓ PASSED - HTTP 401

[ID-018] Testing: Request with invalid token
  ✓ PASSED - HTTP 401

[ID-019] Service not configured - MANUAL TEST

[ID-020] Service timeout - MANUAL TEST

[ID-021] Service returns 404 - MANUAL TEST

[ID-022] Service returns 500 - MANUAL TEST

[ID-023] Service returns invalid response - MANUAL TEST

[ID-024] Large file exceeds size limit - MANUAL TEST

[ID-025] Concurrent requests handling - MANUAL TEST

========================================
  FORMAT RESPONSE CATEGORY (5 TCs)
========================================

[ID-026] Testing: Success response structure
  ✓ PASSED - All required fields present

[ID-027] Testing: Error response structure
  ✗ FAILED - Invalid error response structure

[ID-028] Testing: overall_score field validation
  ✓ PASSED - overall_score exists: 68.16

[ID-029] Testing: fluency_score field validation
  ✓ PASSED - fluency_score exists: 93.9

[ID-030] Testing: phoneme_scores array structure
  ✓ PASSED - phoneme_scores array structure valid


========================================
  TEST EXECUTION SUMMARY
========================================

Total Test Cases: 30
Passed: 12
Failed: 7
Pending/Manual: 11

Success Rate (Automated): 63.2%

Detailed Results:
----------------------------------------
✓ ID-001: Gọi API với method POST
✗ ID-002: Gọi API với method GET (Expected 405, got 500)
✗ ID-003: Gọi API với method PUT (Expected 405, got 500)
✗ ID-004: Gọi API với method DELETE (Expected 405, got 500)
✓ ID-005: Valid request với đầy đủ params
✗ ID-006: Missing audio file (Expected 400, got 500)
✓ ID-007: Empty audio file
✗ ID-008: Missing text parameter (Expected 400, got 500)
✗ ID-009: Empty text parameter (whitespace only) (Expected 400, got 503)
⊙ ID-010: Invalid audio file format (Manual)
⊙ ID-011: Text mismatch với audio content (Manual)
⊙ ID-012: Long text assessment (paragraph-length) (Manual)
✓ ID-013: Multiple punctuation in text
✓ ID-014: Special characters and numbers in text
⊙ ID-015: Very short audio test (Manual)
✓ ID-016: Request without authentication token
✓ ID-017: Request with expired token
✓ ID-018: Request with invalid token
⊙ ID-019: Pronunciation service not configured (Manual)
⊙ ID-020: Pronunciation service timeout (Manual)
⊙ ID-021: Pronunciation service returns 404 (Manual)
⊙ ID-022: Pronunciation service returns 500 (Manual)
⊙ ID-023: Service returns invalid response (Manual)
⊙ ID-024: Large file exceeds size limit (Manual)
⊙ ID-025: Concurrent requests handling (Manual)
✓ ID-026: Success response structure verified
✗ ID-027: Error response structure validation failed
✓ ID-028: overall_score field validated
✓ ID-029: fluency_score field validated
✓ ID-030: phoneme_scores array structure validated
----------------------------------------

Report saved to: test-report-20251104-221045.txt

Cleaning up temporary files...
✓ Cleanup completed

============================================
  TEST SUITE COMPLETED
============================================

Some tests failed. Please review the report.
