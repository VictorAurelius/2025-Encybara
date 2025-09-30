# Pronunciation Assessment API Documentation

## Overview

The Pronunciation Assessment Microservice provides RESTful APIs for evaluating speech pronunciation using Montreal Forced Aligner (MFA) and Goodness of Pronunciation (GOP) algorithms.

## Base URL

```
http://localhost:5000
```

## Authentication

Currently, no authentication is required. For production deployment, consider implementing API key authentication or OAuth.

## Content Types

- **Request**: `multipart/form-data` (for file uploads)
- **Response**: `application/json`

## Error Handling

All endpoints return JSON responses with the following structure:

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error description",
  "code": 400
}
```

## Endpoints

### 1. Health Check

Check service availability and resource usage.

**Endpoint:** `GET /health`

**Response Example:**
```json
{
  "status": "healthy",
  "memory_usage_mb": 1250.5,
  "service": "pronunciation-assessment"
}
```

**Status Codes:**
- `200`: Service is healthy
- `500`: Service is unhealthy

---

### 2. Service Information

Get detailed information about the service capabilities.

**Endpoint:** `GET /api/info`

**Response Example:**
```json
{
  "service": "Pronunciation Assessment Microservice",
  "version": "1.0.0",
  "description": "RESTful API for pronunciation assessment using Montreal Forced Aligner and GOP algorithm",
  "endpoints": {
    "health": "/health",
    "assessment": "/api/pronunciation-assessment",
    "info": "/api/info"
  },
  "supported_audio_formats": ["wav", "mp3", "flac", "m4a"],
  "max_file_size_mb": 6,
  "features": [
    "Montreal Forced Aligner integration",
    "GOP (Goodness of Pronunciation) scoring",
    "Phoneme-level assessment",
    "Memory optimization (≤3GB)",
    "Security validation"
  ]
}
```

---

### 3. Root Endpoint

Basic service information and navigation.

**Endpoint:** `GET /`

**Response Example:**
```json
{
  "message": "Pronunciation Assessment Microservice",
  "status": "running",
  "health_check": "/health",
  "api_info": "/api/info",
  "assessment_endpoint": "/api/pronunciation-assessment"
}
```

---

### 4. Pronunciation Assessment

Main endpoint for pronunciation evaluation.

**Endpoint:** `POST /api/pronunciation-assessment`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `audio` | File | Yes | Audio file (WAV, MP3, FLAC, M4A) |
| `transcript` | String | Yes | Text transcript of expected speech |

**Request Constraints:**
- **File Size**: Maximum 6MB
- **Audio Format**: 16kHz mono recommended (automatically converted)
- **Transcript**: 1-1000 characters, plain text
- **Supported MIME Types**: `audio/wav`, `audio/mpeg`, `audio/flac`, `audio/mp4`

**cURL Example:**
```bash
curl -X POST \
  -F "audio=@sample.wav" \
  -F "transcript=hello world this is a test" \
  http://localhost:5000/api/pronunciation-assessment
```

**Response Example:**
```json
{
  "success": true,
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
        "end_time": 0.090
      },
      {
        "phoneme": "eh",
        "gop_score": 82.5,
        "quality": "good",
        "start_time": 0.090,
        "end_time": 0.180
      }
    ],
    "total_phonemes": 12,
    "average_duration": 0.130
  }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `overall_score` | Number | Overall pronunciation score (0-100) |
| `fluency_score` | Number | Fluency assessment score (0-100) |
| `phoneme_scores` | Array | Detailed phoneme-level assessments |
| `total_phonemes` | Number | Total number of phonemes analyzed |
| `average_duration` | Number | Average phoneme duration in seconds |

**Phoneme Score Object:**

| Field | Type | Description |
|-------|------|-------------|
| `phoneme` | String | IPA phoneme symbol |
| `gop_score` | Number | GOP score for this phoneme (0-100) |
| `quality` | String | Quality label: "excellent", "good", "fair", "poor" |
| `start_time` | Number | Phoneme start time in seconds |
| `end_time` | Number | Phoneme end time in seconds |

**Error Responses:**

| Status Code | Error | Description |
|-------------|-------|-------------|
| `400` | Missing 'audio' file | Audio file not provided |
| `400` | Missing 'transcript' field | Transcript text not provided |
| `400` | Invalid file type | Unsupported audio format |
| `400` | File size exceeds limit | File larger than 6MB |
| `400` | Invalid transcript | Empty or too long transcript |
| `413` | Request Entity Too Large | File upload limit exceeded |
| `500` | Assessment failed | Internal processing error |
| `503` | Service unavailable | High memory usage |

## Quality Scoring Scale

| Score Range | Quality Label | Description |
|-------------|---------------|-------------|
| 85-100 | Excellent | Native-like pronunciation |
| 70-84 | Good | Clear and understandable |
| 55-69 | Fair | Some pronunciation issues |
| 0-54 | Poor | Significant pronunciation problems |

## Rate Limits

Currently, no rate limits are implemented. For production:
- Consider implementing rate limiting (e.g., 10 requests/minute per IP)
- Add authentication with user-specific quotas
- Monitor resource usage and implement circuit breakers

## Best Practices

### Audio Quality
- Use 16kHz mono WAV format for best results
- Ensure clear recording with minimal background noise
- Speak at normal conversational pace
- Avoid clipping or distortion

### Transcript Guidelines
- Use exact words as spoken in audio
- Include common contractions (e.g., "I'm", "don't")
- Use standard spelling and punctuation
- Match the language and accent of the audio

### Error Handling
- Always check the `success` field in responses
- Implement retry logic with exponential backoff
- Handle specific error codes appropriately
- Log errors for debugging purposes

## SDK Examples

### Python Example
```python
import requests

def assess_pronunciation(audio_file_path, transcript):
    url = "http://localhost:5000/api/pronunciation-assessment"
    
    with open(audio_file_path, 'rb') as audio_file:
        files = {'audio': audio_file}
        data = {'transcript': transcript}
        
        response = requests.post(url, files=files, data=data)
        
        if response.status_code == 200:
            result = response.json()
            if result['success']:
                return result['data']
            else:
                raise Exception(f"Assessment failed: {result['error']}")
        else:
            raise Exception(f"HTTP {response.status_code}: {response.text}")

# Usage
try:
    result = assess_pronunciation("sample.wav", "hello world")
    print(f"Overall Score: {result['overall_score']}")
    print(f"Fluency Score: {result['fluency_score']}")
    print(f"Total Phonemes: {result['total_phonemes']}")
except Exception as e:
    print(f"Error: {e}")
```

### JavaScript Example
```javascript
async function assessPronunciation(audioFile, transcript) {
    const formData = new FormData();
    formData.append('audio', audioFile);
    formData.append('transcript', transcript);
    
    try {
        const response = await fetch('http://localhost:5000/api/pronunciation-assessment', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            return result.data;
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        console.error('Assessment error:', error);
        throw error;
    }
}

// Usage
const fileInput = document.getElementById('audioFile');
const transcript = "hello world";

assessPronunciation(fileInput.files[0], transcript)
    .then(result => {
        console.log('Overall Score:', result.overall_score);
        console.log('Phoneme Details:', result.phoneme_scores);
    })
    .catch(error => {
        console.error('Error:', error);
    });
```

## Troubleshooting

### Common Issues

**1. "Assessment failed" Error**
- Check audio file format and quality
- Ensure transcript matches spoken content
- Verify file isn't corrupted

**2. "File size exceeds limit" Error**
- Compress audio file or reduce duration
- Convert to more efficient format (WAV → MP3)
- Check file size is under 6MB

**3. "Service unavailable" Error**
- Wait and retry request
- Check service memory usage with `/health`
- Restart service if necessary

**4. Alignment Issues**
- Ensure transcript exactly matches spoken words
- Check for background noise or audio quality issues
- Try shorter audio segments

### Performance Optimization

- Use compressed audio formats for faster upload
- Process shorter audio segments (< 30 seconds)
- Implement client-side audio preprocessing
- Cache common assessment results

---

*For additional support, please refer to the [Development Setup](DEVELOPMENT.md) and [Deployment Guide](DEPLOYMENT.md).*