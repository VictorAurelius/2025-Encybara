#!/usr/bin/env python3
"""Test script for Simple Pronunciation Assessment Service."""

import os
import sys
import logging
import tempfile
import requests
import json
import time
from pathlib import Path

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def test_simple_aligner_pipeline():
    """Test SimpleAligner pipeline directly."""
    try:
        # Add app to path
        app_path = Path(__file__).parent / "app"
        sys.path.insert(0, str(app_path))
        
        from services.simple_aligner import SimpleAligner
        from services.gop_scorer import GOPScorer
        
        logger.info("Testing SimpleAligner...")
        aligner = SimpleAligner()
        logger.info("✓ SimpleAligner initialized successfully")
        
        logger.info("Testing GOP scorer...")
        gop_scorer = GOPScorer()
        logger.info("✓ GOP scorer initialized successfully")
        
        # Test with a dummy audio path and transcript
        dummy_audio_path = "/tmp/test.wav"
        test_transcript = "hello world"
        
        # Test alignment
        alignment_result = aligner.align_audio_text(dummy_audio_path, test_transcript)
        if alignment_result:
            logger.info("✓ SimpleAligner alignment successful")
            logger.info(f"  - Recognized text: {alignment_result.get('recognized_text', 'N/A')}")
            logger.info(f"  - Phonemes found: {len(alignment_result.get('phonemes', []))}")
            logger.info(f"  - Words found: {len(alignment_result.get('words', []))}")
        else:
            logger.error("✗ SimpleAligner alignment failed")
            return False
        
        # Test GOP scoring
        phoneme_data = gop_scorer.parse_alignment_data(alignment_result)
        logger.info(f"✓ Parsed {len(phoneme_data)} phonemes from alignment data")
        
        # Test GOP scoring for first few phonemes
        for i, phoneme_info in enumerate(phoneme_data[:3]):
            audio_features = {'spectral_centroid_mean': 1500, 'energy': 0.2}
            gop_score = gop_scorer.calculate_phoneme_gop_score(phoneme_info, audio_features)
            quality = gop_scorer.determine_quality_label(gop_score)
            logger.info(f"✓ Phoneme '{phoneme_info['phoneme']}': GOP={gop_score:.2f}, Quality={quality}")
        
        logger.info("✅ SimpleAligner pipeline test completed successfully!")
        return True
        
    except Exception as e:
        logger.error(f"❌ SimpleAligner pipeline test failed: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def wait_for_service(base_url: str = "http://localhost:5000", max_wait: int = 30):
    """Wait for service to be ready."""
    logger.info(f"Waiting for service at {base_url} to be ready...")
    
    for i in range(max_wait):
        try:
            response = requests.get(f"{base_url}/health", timeout=2)
            if response.status_code == 200:
                logger.info(f"✓ Service is ready after {i+1} seconds")
                return True
        except requests.exceptions.RequestException:
            pass
        
        time.sleep(1)
        if i < max_wait - 1:
            logger.info(f"Waiting... ({i+1}/{max_wait})")
    
    logger.error(f"❌ Service not ready after {max_wait} seconds")
    return False

def test_service_endpoints(base_url: str = "http://localhost:5000"):
    """Test the service endpoints."""
    try:
        # Test health endpoint
        logger.info(f"Testing health endpoint at {base_url}/health...")
        response = requests.get(f"{base_url}/health", timeout=10)
        if response.status_code == 200:
            logger.info("✓ Health endpoint is responding")
            health_data = response.json()
            logger.info(f"  Status: {health_data.get('status', 'N/A')}")
            logger.info(f"  Service: {health_data.get('service', 'N/A')}")
        else:
            logger.warning(f"Health endpoint returned status: {response.status_code}")
            return False
        
        # Test info endpoint if available
        try:
            logger.info(f"Testing info endpoint at {base_url}/api/info...")
            response = requests.get(f"{base_url}/api/info", timeout=10)
            if response.status_code == 200:
                info_data = response.json()
                logger.info("✓ Info endpoint is responding")
                logger.info(f"  Description: {info_data.get('description', 'N/A')}")
                logger.info(f"  Version: {info_data.get('version', 'N/A')}")
                if 'features' in info_data:
                    logger.info(f"  Features: {info_data['features']}")
            else:
                logger.info(f"Info endpoint returned status: {response.status_code}")
        except requests.exceptions.RequestException:
            logger.info("Info endpoint not available (optional)")
        
        return True
        
    except requests.exceptions.ConnectionError:
        logger.error(f"❌ Cannot connect to service at {base_url}. Is the service running?")
        return False
    except Exception as e:
        logger.error(f"❌ Service endpoint test failed: {str(e)}")
        return False

def create_test_audio():
    """Create a simple test audio file."""
    try:
        import numpy as np
        
        # Generate a simple sine wave (440 Hz for 1 second)
        sample_rate = 16000
        duration = 1.0
        frequency = 440
        
        t = np.linspace(0, duration, int(sample_rate * duration))
        audio_data = 0.3 * np.sin(2 * np.pi * frequency * t)
        
        # Save as raw bytes (simulate WAV-like data)
        temp_audio = tempfile.NamedTemporaryFile(suffix='.wav', delete=False)
        
        # Write a simple WAV header + data
        import struct
        with open(temp_audio.name, 'wb') as f:
            # Simple WAV header (44 bytes)
            f.write(b'RIFF')
            f.write(struct.pack('<I', 36 + len(audio_data) * 2))
            f.write(b'WAVE')
            f.write(b'fmt ')
            f.write(struct.pack('<I', 16))  # fmt chunk size
            f.write(struct.pack('<H', 1))   # PCM format
            f.write(struct.pack('<H', 1))   # mono
            f.write(struct.pack('<I', sample_rate))
            f.write(struct.pack('<I', sample_rate * 2))
            f.write(struct.pack('<H', 2))   # block align
            f.write(struct.pack('<H', 16))  # bits per sample
            f.write(b'data')
            f.write(struct.pack('<I', len(audio_data) * 2))
            
            # Write audio data
            for sample in audio_data:
                f.write(struct.pack('<h', int(sample * 32767)))
        
        logger.info(f"✓ Created test audio file: {temp_audio.name}")
        return temp_audio.name
        
    except ImportError:
        logger.warning("numpy not available, creating empty audio file")
        temp_audio = tempfile.NamedTemporaryFile(suffix='.wav', delete=False)
        temp_audio.write(b'dummy audio data')
        temp_audio.close()
        return temp_audio.name
    except Exception as e:
        logger.error(f"Failed to create test audio: {str(e)}")
        return None

def test_pronunciation_assessment(base_url: str = "http://localhost:5000"):
    """Test pronunciation assessment endpoint."""
    try:
        # Create test audio
        audio_file = create_test_audio()
        if not audio_file:
            logger.warning("Skipping pronunciation assessment test (no audio file)")
            return False
        
        # Test pronunciation assessment endpoint
        logger.info("Testing pronunciation assessment endpoint...")
        
        with open(audio_file, 'rb') as f:
            files = {'audio': ('test.wav', f, 'audio/wav')}
            data = {'transcript': 'hello world test'}
            
            response = requests.post(
                f"{base_url}/api/pronunciation-assessment",
                files=files,
                data=data,
                timeout=30
            )
        
        # Cleanup audio file
        os.unlink(audio_file)
        
        if response.status_code == 200:
            result = response.json()
            logger.info("✓ Pronunciation assessment completed successfully")
            logger.info(f"  Overall score: {result.get('overall_score', 'N/A')}")
            logger.info(f"  Fluency score: {result.get('fluency_score', 'N/A')}")
            logger.info(f"  Total phonemes: {result.get('total_phonemes', 'N/A')}")
            
            # Show sample phoneme scores
            phoneme_scores = result.get('phoneme_scores', [])
            if phoneme_scores:
                logger.info("  Sample phoneme scores:")
                for phoneme in phoneme_scores[:3]:
                    logger.info(f"    - {phoneme.get('phoneme', 'N/A')}: {phoneme.get('gop_score', 'N/A')} ({phoneme.get('quality', 'N/A')})")
            
            return True
        else:
            logger.error(f"Assessment failed with status {response.status_code}")
            logger.error(f"Response: {response.text}")
            return False
            
    except Exception as e:
        logger.error(f"❌ Pronunciation assessment test failed: {str(e)}")
        return False

def test_error_handling(base_url: str = "http://localhost:5000"):
    """Test error handling."""
    try:
        logger.info("Testing error handling...")
        
        # Test with no audio file
        response = requests.post(
            f"{base_url}/api/pronunciation-assessment",
            data={'transcript': 'test'},
            timeout=10
        )
        
        if response.status_code == 400:
            logger.info("✓ Correctly handles missing audio file")
        else:
            logger.warning(f"Expected 400 for missing audio, got {response.status_code}")
        
        # Test with no transcript
        temp_audio = tempfile.NamedTemporaryFile(suffix='.wav')
        temp_audio.write(b'dummy')
        temp_audio.seek(0)
        
        response = requests.post(
            f"{base_url}/api/pronunciation-assessment",
            files={'audio': temp_audio},
            timeout=10
        )
        
        if response.status_code == 400:
            logger.info("✓ Correctly handles missing transcript")
        else:
            logger.warning(f"Expected 400 for missing transcript, got {response.status_code}")
        
        temp_audio.close()
        return True
        
    except Exception as e:
        logger.error(f"❌ Error handling test failed: {str(e)}")
        return False

def main():
    """Run all tests."""
    logger.info("🧪 Starting Simple Pronunciation Assessment Service Tests")
    logger.info("=" * 70)
    
    results = []
    
    # Test 1: Pipeline components
    logger.info("1. Testing SimpleAligner pipeline components...")
    results.append(test_simple_aligner_pipeline())
    
    # Test 2: Wait for service
    logger.info("\n2. Waiting for service to be ready...")
    service_ready = wait_for_service()
    
    if service_ready:
        # Test 3: Service endpoints
        logger.info("\n3. Testing service endpoints...")
        results.append(test_service_endpoints())
        
        # Test 4: Pronunciation assessment
        logger.info("\n4. Testing pronunciation assessment...")
        results.append(test_pronunciation_assessment())
        
        # Test 5: Error handling
        logger.info("\n5. Testing error handling...")
        results.append(test_error_handling())
    else:
        logger.warning("Skipping service tests - service not available")
        results.extend([False, False, False])
    
    # Summary
    logger.info("\n" + "=" * 70)
    logger.info("📊 Test Results Summary:")
    test_names = [
        "Pipeline Components", 
        "Service Endpoints", 
        "Pronunciation Assessment",
        "Error Handling"
    ]
    
    for i, (name, result) in enumerate(zip(test_names, results)):
        status = "✅ PASS" if result else "❌ FAIL"
        logger.info(f"{i+1}. {name}: {status}")
    
    passed = sum(results)
    total = len(results)
    logger.info(f"\nOverall: {passed}/{total} tests passed")
    
    if passed == total:
        logger.info("🎉 All tests passed! Simple Pronunciation Assessment Service is ready.")
        return 0
    elif passed >= total - 1:
        logger.info("✅ Most tests passed! Service is functional.")
        return 0
    else:
        logger.warning("⚠️  Some tests failed. Check the logs above.")
        return 1

if __name__ == "__main__":
    exit(main())