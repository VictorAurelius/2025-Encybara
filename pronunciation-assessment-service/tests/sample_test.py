#!/usr/bin/env python3
"""
Sample test file for Pronunciation Assessment Service
This file demonstrates how to test the service with sample data
"""

import os
import sys
import requests
import json
import time
import tempfile
import wave
import numpy as np

# Add parent directory to path to import modules
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def create_sample_audio(filename="test_audio.wav", duration=2.0, sample_rate=16000):
    """
    Create a sample audio file for testing.
    
    Args:
        filename: Output filename
        duration: Duration in seconds
        sample_rate: Sample rate in Hz
    
    Returns:
        Path to created audio file
    """
    # Generate a simple sine wave
    t = np.linspace(0, duration, int(sample_rate * duration), False)
    
    # Mix multiple frequencies to simulate speech-like audio
    frequency1 = 440  # A4 note
    frequency2 = 880  # A5 note
    frequency3 = 220  # A3 note
    
    audio_data = (
        0.3 * np.sin(2 * np.pi * frequency1 * t) +
        0.2 * np.sin(2 * np.pi * frequency2 * t) +
        0.1 * np.sin(2 * np.pi * frequency3 * t)
    )
    
    # Add some noise to make it more realistic
    noise = np.random.normal(0, 0.05, audio_data.shape)
    audio_data = audio_data + noise
    
    # Normalize to prevent clipping
    audio_data = audio_data / np.max(np.abs(audio_data))
    
    # Convert to 16-bit integers
    audio_data = (audio_data * 32767).astype(np.int16)
    
    # Write WAV file
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)  # Mono
        wav_file.setsampwidth(2)  # 16-bit
        wav_file.setframerate(sample_rate)
        wav_file.writeframes(audio_data.tobytes())
    
    print(f"Created sample audio file: {filename}")
    return filename

def test_service_health(base_url="http://localhost:5000"):
    """Test service health endpoint."""
    print("Testing service health...")
    
    try:
        response = requests.get(f"{base_url}/health", timeout=10)
        
        if response.status_code == 200:
            health_data = response.json()
            print(f"✓ Service is healthy")
            print(f"  Memory usage: {health_data.get('memory_usage_mb', 'N/A')} MB")
            return True
        else:
            print(f"✗ Health check failed with status {response.status_code}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"✗ Health check failed: {e}")
        return False

def test_service_info(base_url="http://localhost:5000"):
    """Test service info endpoint."""
    print("\nTesting service info...")
    
    try:
        response = requests.get(f"{base_url}/api/info", timeout=10)
        
        if response.status_code == 200:
            info_data = response.json()
            print("✓ Service info retrieved successfully")
            print(f"  Service: {info_data.get('service', 'N/A')}")
            print(f"  Version: {info_data.get('version', 'N/A')}")
            print(f"  Supported formats: {info_data.get('supported_audio_formats', 'N/A')}")
            print(f"  Max file size: {info_data.get('max_file_size_mb', 'N/A')} MB")
            return True
        else:
            print(f"✗ Service info failed with status {response.status_code}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"✗ Service info failed: {e}")
        return False

def test_pronunciation_assessment(base_url="http://localhost:5000", audio_file=None, transcript="hello world"):
    """Test pronunciation assessment endpoint."""
    print("\nTesting pronunciation assessment...")
    
    # Create sample audio if not provided
    if audio_file is None:
        audio_file = create_sample_audio("test_sample.wav", duration=2.0)
        cleanup_audio = True
    else:
        cleanup_audio = False
    
    try:
        # Prepare request
        with open(audio_file, 'rb') as f:
            files = {'audio': (audio_file, f, 'audio/wav')}
            data = {'transcript': transcript}
            
            print(f"  Sending request with audio file: {audio_file}")
            print(f"  Transcript: '{transcript}'")
            
            # Send request
            response = requests.post(
                f"{base_url}/api/pronunciation-assessment",
                files=files,
                data=data,
                timeout=120  # Increased timeout for processing
            )
        
        if response.status_code == 200:
            result = response.json()
            
            if result.get('success', False):
                assessment_data = result.get('data', {})
                print("✓ Pronunciation assessment completed successfully")
                print(f"  Overall Score: {assessment_data.get('overall_score', 'N/A')}")
                print(f"  Fluency Score: {assessment_data.get('fluency_score', 'N/A')}")
                print(f"  Total Phonemes: {assessment_data.get('total_phonemes', 'N/A')}")
                print(f"  Average Duration: {assessment_data.get('average_duration', 'N/A')} seconds")
                
                # Display first few phoneme scores
                phoneme_scores = assessment_data.get('phoneme_scores', [])
                if phoneme_scores:
                    print("  Sample Phoneme Scores:")
                    for i, phoneme in enumerate(phoneme_scores[:3]):  # Show first 3
                        print(f"    {phoneme['phoneme']}: {phoneme['gop_score']} ({phoneme['quality']})")
                
                return True
            else:
                print(f"✗ Assessment failed: {result.get('error', 'Unknown error')}")
                return False
        else:
            print(f"✗ Assessment request failed with status {response.status_code}")
            if response.headers.get('content-type', '').startswith('application/json'):
                error_data = response.json()
                print(f"  Error: {error_data.get('error', 'Unknown error')}")
            else:
                print(f"  Response: {response.text[:200]}...")
            return False
            
    except requests.exceptions.Timeout:
        print("✗ Assessment request timed out (this may indicate processing issues)")
        return False
    except requests.exceptions.RequestException as e:
        print(f"✗ Assessment request failed: {e}")
        return False
    except Exception as e:
        print(f"✗ Unexpected error during assessment: {e}")
        return False
    finally:
        # Cleanup sample audio file if we created it
        if cleanup_audio and os.path.exists(audio_file):
            try:
                os.remove(audio_file)
                print(f"  Cleaned up sample audio file: {audio_file}")
            except Exception as e:
                print(f"  Warning: Could not clean up audio file: {e}")

def test_error_cases(base_url="http://localhost:5000"):
    """Test error handling."""
    print("\nTesting error cases...")
    
    # Test 1: Missing audio file
    print("  Testing missing audio file...")
    try:
        response = requests.post(
            f"{base_url}/api/pronunciation-assessment",
            data={'transcript': 'test'},
            timeout=10
        )
        
        if response.status_code == 400:
            result = response.json()
            if 'audio' in result.get('error', '').lower():
                print("    ✓ Correctly rejected request without audio file")
            else:
                print("    ⚠ Unexpected error message for missing audio")
        else:
            print(f"    ✗ Unexpected status code: {response.status_code}")
    except Exception as e:
        print(f"    ✗ Error testing missing audio: {e}")
    
    # Test 2: Missing transcript
    print("  Testing missing transcript...")
    try:
        # Create a dummy file
        with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as tmp_file:
            tmp_file.write(b'dummy audio data')
            tmp_file_path = tmp_file.name
        
        try:
            with open(tmp_file_path, 'rb') as f:
                files = {'audio': ('test.wav', f, 'audio/wav')}
                response = requests.post(
                    f"{base_url}/api/pronunciation-assessment",
                    files=files,
                    timeout=10
                )
            
            if response.status_code == 400:
                result = response.json()
                if 'transcript' in result.get('error', '').lower():
                    print("    ✓ Correctly rejected request without transcript")
                else:
                    print("    ⚠ Unexpected error message for missing transcript")
            else:
                print(f"    ✗ Unexpected status code: {response.status_code}")
        finally:
            os.unlink(tmp_file_path)
            
    except Exception as e:
        print(f"    ✗ Error testing missing transcript: {e}")

def run_comprehensive_test(base_url="http://localhost:5000"):
    """Run comprehensive test suite."""
    print("=" * 60)
    print("PRONUNCIATION ASSESSMENT SERVICE - COMPREHENSIVE TEST")
    print("=" * 60)
    
    test_results = []
    
    # Test 1: Health Check
    test_results.append(test_service_health(base_url))
    
    # Test 2: Service Info
    test_results.append(test_service_info(base_url))
    
    # Test 3: Pronunciation Assessment
    test_results.append(test_pronunciation_assessment(base_url))
    
    # Test 4: Error Cases
    test_error_cases(base_url)  # This doesn't return a result
    
    # Summary
    print("\n" + "=" * 60)
    print("TEST SUMMARY")
    print("=" * 60)
    
    passed = sum(test_results)
    total = len(test_results)
    
    print(f"Tests Passed: {passed}/{total}")
    
    if passed == total:
        print("🎉 All tests passed! Service is working correctly.")
        return True
    else:
        print("⚠️ Some tests failed. Please check the service configuration.")
        return False

def main():
    """Main test function."""
    import argparse
    
    parser = argparse.ArgumentParser(description='Test Pronunciation Assessment Service')
    parser.add_argument('--url', default='http://localhost:5000', help='Service base URL')
    parser.add_argument('--audio', help='Path to audio file for testing')
    parser.add_argument('--transcript', default='hello world', help='Transcript text')
    parser.add_argument('--test', choices=['health', 'info', 'assessment', 'errors', 'all'], 
                       default='all', help='Which test to run')
    
    args = parser.parse_args()
    
    if args.test == 'health':
        return test_service_health(args.url)
    elif args.test == 'info':
        return test_service_info(args.url)
    elif args.test == 'assessment':
        return test_pronunciation_assessment(args.url, args.audio, args.transcript)
    elif args.test == 'errors':
        test_error_cases(args.url)
        return True
    else:  # 'all'
        return run_comprehensive_test(args.url)

if __name__ == "__main__":
    try:
        # Check if required packages are available
        import numpy as np
        import wave
    except ImportError as e:
        print(f"Error: Required package not available: {e}")
        print("Please install: pip install numpy")
        sys.exit(1)
    
    success = main()
    sys.exit(0 if success else 1)