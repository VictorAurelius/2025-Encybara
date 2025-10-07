#!/usr/bin/env python3
"""Test script for WhisperX pronunciation assessment service."""

import os
import sys
import logging
import tempfile
import requests
import json
from pathlib import Path

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def test_whisperx_pipeline():
    """Test WhisperX pipeline directly."""
    try:
        # Add app to path
        app_path = Path(__file__).parent / "app"
        sys.path.insert(0, str(app_path))
        
        from services.whisperx_aligner import WhisperXAligner
        from services.gop_scorer import GOPScorer
        
        logger.info("Testing WhisperX aligner...")
        aligner = WhisperXAligner()
        logger.info("✓ WhisperX aligner initialized successfully")
        
        logger.info("Testing GOP scorer...")
        gop_scorer = GOPScorer()
        logger.info("✓ GOP scorer initialized successfully")
        
        # Test with a dummy alignment result
        dummy_alignment = {
            'phonemes': [
                {'phoneme': 'h', 'start_time': 0.0, 'end_time': 0.1, 'confidence': 0.9},
                {'phoneme': 'ɛ', 'start_time': 0.1, 'end_time': 0.2, 'confidence': 0.8},
                {'phoneme': 'l', 'start_time': 0.2, 'end_time': 0.3, 'confidence': 0.9},
                {'phoneme': 'oʊ', 'start_time': 0.3, 'end_time': 0.5, 'confidence': 0.7},
            ],
            'words': [
                {'word': 'hello', 'start_time': 0.0, 'end_time': 0.5, 'confidence': 0.85}
            ],
            'recognized_text': 'hello',
            'alignment_quality': 0.85
        }
        
        phoneme_data = gop_scorer.parse_whisperx_alignment(dummy_alignment)
        logger.info(f"✓ Parsed {len(phoneme_data)} phonemes from test data")
        
        # Test GOP scoring
        for phoneme_info in phoneme_data[:2]:  # Test first 2 phonemes
            audio_features = {'spectral_centroid_mean': 1500, 'energy': 0.2}
            gop_score = gop_scorer.calculate_phoneme_gop_score(phoneme_info, audio_features)
            quality = gop_scorer.determine_quality_label(gop_score)
            logger.info(f"✓ Phoneme '{phoneme_info['phoneme']}': GOP={gop_score:.2f}, Quality={quality}")
        
        logger.info("✅ WhisperX pipeline test completed successfully!")
        return True
        
    except Exception as e:
        logger.error(f"❌ WhisperX pipeline test failed: {str(e)}")
        return False

def test_service_endpoint(base_url: str = "http://localhost:5000"):
    """Test the service endpoint."""
    try:
        # Test health endpoint
        logger.info(f"Testing health endpoint at {base_url}/health...")
        response = requests.get(f"{base_url}/health", timeout=10)
        if response.status_code == 200:
            logger.info("✓ Health endpoint is responding")
            logger.info(f"Health response: {response.json()}")
        else:
            logger.warning(f"Health endpoint returned status: {response.status_code}")
        
        # Test info endpoint
        logger.info(f"Testing info endpoint at {base_url}/api/info...")
        response = requests.get(f"{base_url}/api/info", timeout=10)
        if response.status_code == 200:
            info_data = response.json()
            logger.info("✓ Info endpoint is responding")
            logger.info(f"Service description: {info_data.get('description', 'N/A')}")
            logger.info(f"Features: {info_data.get('features', [])}")
        else:
            logger.warning(f"Info endpoint returned status: {response.status_code}")
        
        return True
        
    except requests.exceptions.ConnectionError:
        logger.warning(f"❌ Cannot connect to service at {base_url}. Is the service running?")
        return False
    except Exception as e:
        logger.error(f"❌ Service endpoint test failed: {str(e)}")
        return False

def create_test_audio():
    """Create a simple test audio file."""
    try:
        import numpy as np
        import soundfile as sf
        
        # Generate a simple sine wave (440 Hz for 1 second)
        sample_rate = 16000
        duration = 1.0
        frequency = 440
        
        t = np.linspace(0, duration, int(sample_rate * duration))
        audio_data = 0.3 * np.sin(2 * np.pi * frequency * t)
        
        # Save to temporary file
        temp_audio = tempfile.NamedTemporaryFile(suffix='.wav', delete=False)
        sf.write(temp_audio.name, audio_data, sample_rate)
        
        logger.info(f"✓ Created test audio file: {temp_audio.name}")
        return temp_audio.name
        
    except ImportError:
        logger.warning("soundfile not available, skipping audio file test")
        return None
    except Exception as e:
        logger.error(f"Failed to create test audio: {str(e)}")
        return None

def test_full_assessment(base_url: str = "http://localhost:5000"):
    """Test full pronunciation assessment."""
    try:
        # Create test audio
        audio_file = create_test_audio()
        if not audio_file:
            logger.warning("Skipping full assessment test (no audio file)")
            return False
        
        # Test pronunciation assessment endpoint
        logger.info("Testing pronunciation assessment endpoint...")
        
        with open(audio_file, 'rb') as f:
            files = {'audio': f}
            data = {'transcript': 'hello world'}
            
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
            logger.info(f"Result: {json.dumps(result, indent=2)}")
            return True
        else:
            logger.error(f"Assessment failed with status {response.status_code}: {response.text}")
            return False
            
    except Exception as e:
        logger.error(f"❌ Full assessment test failed: {str(e)}")
        return False

def main():
    """Run all tests."""
    logger.info("🧪 Starting WhisperX Pronunciation Assessment Service Tests")
    logger.info("=" * 60)
    
    results = []
    
    # Test 1: Pipeline components
    logger.info("1. Testing WhisperX pipeline components...")
    results.append(test_whisperx_pipeline())
    
    # Test 2: Service endpoints
    logger.info("\n2. Testing service endpoints...")
    results.append(test_service_endpoint())
    
    # Test 3: Full assessment (if service is running)
    logger.info("\n3. Testing full pronunciation assessment...")
    results.append(test_full_assessment())
    
    # Summary
    logger.info("\n" + "=" * 60)
    logger.info("📊 Test Results Summary:")
    test_names = ["Pipeline Components", "Service Endpoints", "Full Assessment"]
    
    for i, (name, result) in enumerate(zip(test_names, results)):
        status = "✅ PASS" if result else "❌ FAIL"
        logger.info(f"{i+1}. {name}: {status}")
    
    passed = sum(results)
    total = len(results)
    logger.info(f"\nOverall: {passed}/{total} tests passed")
    
    if passed == total:
        logger.info("🎉 All tests passed! WhisperX service is ready.")
        return 0
    else:
        logger.warning("⚠️  Some tests failed. Check the logs above.")
        return 1

if __name__ == "__main__":
    exit(main())