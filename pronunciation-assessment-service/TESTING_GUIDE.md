# Testing Guide - Pronunciation Assessment Service

## Overview

This guide provides comprehensive instructions for testing the Pronunciation Assessment Service, from basic functionality checks to advanced integration testing.

## Quick Test Commands

### 1. Basic Service Test
```bash
# Test all endpoints
./scripts/test.sh

# Test specific endpoint
./scripts/test.sh health
./scripts/test.sh assessment
```

### 2. Python Test Suite
```bash
# Run comprehensive test
python tests/sample_test.py

# Test specific functionality
python tests/sample_test.py --test health
python tests/sample_test.py --test assessment
```

### 3. Manual cURL Testing
```bash
# Health check
curl http://localhost:5000/health

# Service info
curl http://localhost:5000/api/info

# Pronunciation assessment
curl -X POST \
  -F "audio=@your_audio.wav" \
  -F "transcript=hello world" \
  http://localhost:5000/api/pronunciation-assessment
```

## Detailed Testing Procedures

### Service Startup Testing

#### 1. Container Health Verification
```bash
# Check container status
docker ps | grep pronunciation-assessment

# Verify service is responding
curl -f http://localhost:5000/health || echo "Service not ready"

# Check memory usage
curl -s http://localhost:5000/health | jq '.memory_usage_mb'
```

#### 2. Log Analysis
```bash
# View startup logs
docker logs pronunciation-assessment-container

# Follow live logs
docker logs -f pronunciation-assessment-container

# Check for errors
docker logs pronunciation-assessment-container 2>&1 | grep -i error
```

### Audio File Testing

#### 1. Create Test Audio Files
```bash
# Generate test audio with different characteristics
sox -n -r 16000 -c 1 test_mono_16k.wav synth 2.0 sine 440
sox -n -r 44100 -c 2 test_stereo_44k.wav synth 2.0 sine 440
sox -n -r 8000 -c 1 test_mono_8k.wav synth 2.0 sine 440

# Test different formats
ffmpeg -i test_mono_16k.wav test.mp3
ffmpeg -i test_mono_16k.wav test.flac
ffmpeg -i test_mono_16k.wav test.m4a
```

#### 2. Audio Format Testing
```bash
# Test each supported format
for format in wav mp3 flac m4a; do
    echo "Testing $format format..."
    curl -X POST \
      -F "audio=@test.$format" \
      -F "transcript=hello world test" \
      http://localhost:5000/api/pronunciation-assessment
    echo
done
```

#### 3. Audio Quality Testing
```bash
# Test various sample rates and channels
test_audio_quality() {
    local filename=$1
    local description=$2
    
    echo "Testing: $description"
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
        -X POST \
        -F "audio=@$filename" \
        -F "transcript=test audio quality" \
        http://localhost:5000/api/pronunciation-assessment)
    
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    
    if [ "$http_code" -eq 200 ]; then
        echo "✓ Success: $description"
        echo "$body" | jq -r '.data.overall_score // "No score"'
    else
        echo "✗ Failed: $description ($http_code)"
        echo "$body" | jq -r '.error // "Unknown error"'
    fi
    echo
}

# Run tests
test_audio_quality "test_mono_16k.wav" "16kHz Mono (Optimal)"
test_audio_quality "test_stereo_44k.wav" "44.1kHz Stereo (High Quality)"
test_audio_quality "test_mono_8k.wav" "8kHz Mono (Low Quality)"
```

### Transcript Testing

#### 1. Various Transcript Scenarios
```bash
# Test different transcript lengths and content
test_transcript() {
    local transcript="$1"
    local description="$2"
    
    echo "Testing transcript: $description"
    curl -s -X POST \
        -F "audio=@test_mono_16k.wav" \
        -F "transcript=$transcript" \
        http://localhost:5000/api/pronunciation-assessment | \
        jq -r '.success, .error // "No error"'
    echo
}

# Run transcript tests
test_transcript "hello" "Single word"
test_transcript "hello world" "Two words"
test_transcript "The quick brown fox jumps over the lazy dog" "Long sentence"
test_transcript "I'm going to the store" "Contractions"
test_transcript "123 numbers test" "Mixed content"
test_transcript "" "Empty transcript"
test_transcript "$(printf 'a%.0s' {1..1001})" "Too long transcript"
```

#### 2. Special Characters Testing
```bash
# Test various special characters
special_tests=(
    "hello, world!"
    "what's happening?"
    "test-hyphen-words"
    "numbers 1 2 3"
    "UPPERCASE words"
    "mixed CaSe WoRdS"
)

for transcript in "${special_tests[@]}"; do
    echo "Testing: '$transcript'"
    curl -s -X POST \
        -F "audio=@test_mono_16k.wav" \
        -F "transcript=$transcript" \
        http://localhost:5000/api/pronunciation-assessment | \
        jq -r '.success // false'
done
```

### Error Handling Testing

#### 1. Invalid Input Testing
```bash
# Test missing audio file
echo "Testing missing audio file..."
curl -X POST -F "transcript=test" http://localhost:5000/api/pronunciation-assessment

# Test missing transcript
echo "Testing missing transcript..."
curl -X POST -F "audio=@test_mono_16k.wav" http://localhost:5000/api/pronunciation-assessment

# Test invalid file type
echo "hello world" > test.txt
curl -X POST -F "audio=@test.txt" -F "transcript=test" http://localhost:5000/api/pronunciation-assessment
rm test.txt
```

#### 2. File Size Testing
```bash
# Create oversized file (>6MB)
sox -n -r 16000 -c 1 large_file.wav synth 240.0 sine 440  # 4 minutes

echo "Testing large file (should fail)..."
curl -X POST \
    -F "audio=@large_file.wav" \
    -F "transcript=this is a very long audio file test" \
    http://localhost:5000/api/pronunciation-assessment

rm large_file.wav
```

#### 3. Malformed File Testing
```bash
# Create corrupted WAV file
echo "Not a real WAV file" > corrupted.wav
curl -X POST \
    -F "audio=@corrupted.wav" \
    -F "transcript=test" \
    http://localhost:5000/api/pronunciation-assessment
rm corrupted.wav
```

### Performance Testing

#### 1. Response Time Testing
```bash
# Measure response times
test_performance() {
    echo "Performance Test - Response Times"
    echo "================================="
    
    for i in {1..5}; do
        echo "Request $i:"
        time curl -s -X POST \
            -F "audio=@test_mono_16k.wav" \
            -F "transcript=performance test number $i" \
            http://localhost:5000/api/pronunciation-assessment > /dev/null
    done
}

test_performance
```

#### 2. Memory Usage Monitoring
```bash
# Monitor memory usage during processing
monitor_memory() {
    echo "Memory Usage Monitoring"
    echo "======================"
    
    # Get initial memory
    initial_memory=$(curl -s http://localhost:5000/health | jq -r '.memory_usage_mb')
    echo "Initial memory usage: ${initial_memory} MB"
    
    # Send request and monitor
    curl -s -X POST \
        -F "audio=@test_mono_16k.wav" \
        -F "transcript=memory monitoring test" \
        http://localhost:5000/api/pronunciation-assessment > /dev/null &
    
    request_pid=$!
    
    while kill -0 $request_pid 2>/dev/null; do
        current_memory=$(curl -s http://localhost:5000/health | jq -r '.memory_usage_mb')
        echo "Current memory usage: ${current_memory} MB"
        sleep 2
    done
    
    final_memory=$(curl -s http://localhost:5000/health | jq -r '.memory_usage_mb')
    echo "Final memory usage: ${final_memory} MB"
}

monitor_memory
```

#### 3. Concurrent Request Testing
```bash
# Test concurrent requests (be careful not to overload)
concurrent_test() {
    echo "Concurrent Request Test (3 requests)"
    echo "==================================="
    
    # Send 3 concurrent requests
    for i in {1..3}; do
        (
            echo "Starting request $i..."
            time curl -s -X POST \
                -F "audio=@test_mono_16k.wav" \
                -F "transcript=concurrent test $i" \
                http://localhost:5000/api/pronunciation-assessment | \
                jq -r ".data.overall_score // \"Failed\""
        ) &
    done
    
    wait
    echo "All concurrent requests completed"
}

concurrent_test
```

### Integration Testing

#### 1. End-to-End Workflow Testing
```bash
# Test complete workflow
workflow_test() {
    echo "End-to-End Workflow Test"
    echo "========================"
    
    # Step 1: Check service health
    echo "1. Checking service health..."
    health_response=$(curl -s http://localhost:5000/health)
    echo "   Memory usage: $(echo $health_response | jq -r '.memory_usage_mb') MB"
    
    # Step 2: Get service info
    echo "2. Getting service info..."
    info_response=$(curl -s http://localhost:5000/api/info)
    echo "   Max file size: $(echo $info_response | jq -r '.max_file_size_mb') MB"
    
    # Step 3: Process audio
    echo "3. Processing audio file..."
    assessment_response=$(curl -s -X POST \
        -F "audio=@test_mono_16k.wav" \
        -F "transcript=complete workflow integration test" \
        http://localhost:5000/api/pronunciation-assessment)
    
    success=$(echo $assessment_response | jq -r '.success')
    if [ "$success" = "true" ]; then
        overall_score=$(echo $assessment_response | jq -r '.data.overall_score')
        fluency_score=$(echo $assessment_response | jq -r '.data.fluency_score')
        total_phonemes=$(echo $assessment_response | jq -r '.data.total_phonemes')
        
        echo "   ✓ Assessment successful!"
        echo "   Overall Score: $overall_score"
        echo "   Fluency Score: $fluency_score"
        echo "   Total Phonemes: $total_phonemes"
    else
        echo "   ✗ Assessment failed!"
        echo "   Error: $(echo $assessment_response | jq -r '.error')"
    fi
    
    # Step 4: Check final memory usage
    echo "4. Checking final memory usage..."
    final_health=$(curl -s http://localhost:5000/health)
    echo "   Final memory usage: $(echo $final_health | jq -r '.memory_usage_mb') MB"
}

workflow_test
```

### Automated Test Suite

#### 1. Complete Test Script
```bash
#!/bin/bash
# comprehensive_test.sh - Complete automated test suite

set -e

SERVICE_URL="http://localhost:5000"
TEST_AUDIO="test_mono_16k.wav"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

test_counter=0
passed_tests=0

run_test() {
    local test_name="$1"
    local test_command="$2"
    
    test_counter=$((test_counter + 1))
    echo -e "\n${YELLOW}Test $test_counter: $test_name${NC}"
    
    if eval "$test_command"; then
        echo -e "${GREEN}✓ PASSED${NC}"
        passed_tests=$((passed_tests + 1))
    else
        echo -e "${RED}✗ FAILED${NC}"
    fi
}

# Generate test audio if it doesn't exist
if [ ! -f "$TEST_AUDIO" ]; then
    echo "Generating test audio file..."
    sox -n -r 16000 -c 1 "$TEST_AUDIO" synth 2.0 sine 440
fi

echo "Starting Comprehensive Test Suite"
echo "================================="

# Test 1: Service Health
run_test "Service Health Check" \
    "curl -s -f $SERVICE_URL/health > /dev/null"

# Test 2: Service Info
run_test "Service Information" \
    "curl -s $SERVICE_URL/api/info | jq -e '.service' > /dev/null"

# Test 3: Basic Assessment
run_test "Basic Pronunciation Assessment" \
    "curl -s -X POST -F 'audio=@$TEST_AUDIO' -F 'transcript=test' $SERVICE_URL/api/pronunciation-assessment | jq -e '.success' > /dev/null"

# Test 4: Missing Audio Error
run_test "Missing Audio Error Handling" \
    "curl -s -X POST -F 'transcript=test' $SERVICE_URL/api/pronunciation-assessment | jq -e '.success == false' > /dev/null"

# Test 5: Missing Transcript Error
run_test "Missing Transcript Error Handling" \
    "curl -s -X POST -F 'audio=@$TEST_AUDIO' $SERVICE_URL/api/pronunciation-assessment | jq -e '.success == false' > /dev/null"

# Summary
echo -e "\n================================="
echo -e "Test Results: ${GREEN}$passed_tests${NC}/$test_counter tests passed"

if [ $passed_tests -eq $test_counter ]; then
    echo -e "${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed.${NC}"
    exit 1
fi
```

#### 2. Continuous Testing
```bash
# watch_test.sh - Continuous monitoring
#!/bin/bash

while true; do
    echo "$(date): Running health check..."
    if curl -s -f http://localhost:5000/health > /dev/null; then
        memory=$(curl -s http://localhost:5000/health | jq -r '.memory_usage_mb')
        echo "✓ Service healthy (Memory: ${memory}MB)"
    else
        echo "✗ Service unhealthy!"
        # Could send alert here
    fi
    
    sleep 60  # Check every minute
done
```

## Troubleshooting Test Issues

### Common Test Failures

1. **Connection Refused**
   ```bash
   # Check if service is running
   docker ps | grep pronunciation-assessment
   
   # Check port binding
   netstat -tlpn | grep :5000
   
   # Restart service
   ./scripts/run.sh
   ```

2. **Timeout Errors**
   ```bash
   # Check service logs
   docker logs pronunciation-assessment-container
   
   # Monitor resource usage
   docker stats pronunciation-assessment-container
   
   # Increase timeout in test commands
   curl --max-time 300 ...
   ```

3. **Memory Issues**
   ```bash
   # Check memory usage
   curl -s http://localhost:5000/health | jq '.memory_usage_mb'
   
   # Restart container to clear memory
   docker restart pronunciation-assessment-container
   
   # Check system memory
   free -h
   ```

4. **Audio Processing Failures**
   ```bash
   # Verify audio file format
   file test_audio.wav
   ffprobe test_audio.wav
   
   # Test with different audio
   sox -n -r 16000 -c 1 simple_test.wav synth 1.0 sine 440
   ```

### Performance Benchmarks

**Expected Performance Metrics:**
- Health check: < 100ms
- Service info: < 50ms
- Short audio assessment (2s): < 30s
- Memory usage: < 3GB
- Startup time: < 2 minutes

**Performance Optimization:**
- Use 16kHz mono WAV files for best performance
- Keep audio files under 30 seconds
- Allow 2-5 minutes for first assessment (model loading)
- Monitor memory usage between requests

---

*For complete service documentation, see [README.md](README.md), [API.md](docs/API.md), [DEVELOPMENT.md](docs/DEVELOPMENT.md), and [DEPLOYMENT.md](docs/DEPLOYMENT.md).*