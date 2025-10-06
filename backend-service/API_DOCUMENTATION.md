# API Documentation

## Pronunciation Assessment API

### POST /api/v1/pronunciation/assess

Sends an audio file to the pronunciation assessment service for evaluation.

**Request:**
- Method: POST
- Content-Type: multipart/form-data
- Parameter: `file` (audio file)

**Response:**
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Pronunciation assessment completed successfully",
  "data": {
    "pronunciation_score": number,
    "transcript": string
  }
}
```

**Error Response:**
```json
{
  "statusCode": 400,
  "error": "Assessment failed",
  "message": "Error assessing pronunciation: [error details]",
  "data": null
}
```

**Notes:**
- The service accepts audio files and forwards them to a local pronunciation assessment service running on port 5000
- The service is accessed through ngrok for local development
- No file format conversion is needed - files are sent directly to the assessment service