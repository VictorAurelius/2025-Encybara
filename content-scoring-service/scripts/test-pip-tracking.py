#!/usr/bin/env python3
"""
Simple test script to verify pip download tracking works correctly
"""

import sys
import os
import tempfile

# Add current directory to path to import download_tracker
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from download_tracker import DownloadTracker

def create_test_requirements():
    """Create a test requirements file with known large packages"""
    test_content = """
# Test requirements with known large packages
torch>=2.0.0
scipy>=1.10.0
scikit-learn>=1.3.0
numpy>=1.24.0
"""
    
    # Create temporary requirements file
    with tempfile.NamedTemporaryFile(mode='w', suffix='.txt', delete=False) as f:
        f.write(test_content.strip())
        return f.name

def test_direct_pip_tracking():
    """Test pip tracking with a simple package"""
    print("🧪 TESTING DIRECT PIP TRACKING")
    print("=" * 50)
    
    # Create test requirements
    requirements_file = create_test_requirements()
    
    try:
        print(f"📝 Created test requirements: {requirements_file}")
        
        # Initialize tracker
        tracker = DownloadTracker(size_threshold_mb=10)  # Lower threshold for testing
        
        print("\n🚀 Starting pip install tracking...")
        print("📊 Tracking downloads larger than 10MB")
        print("-" * 50)
        
        # Track the installation
        result = tracker.track_pip_install(requirements_file)
        
        print("\n" + "=" * 50)
        print("📊 TRACKING RESULTS:")
        print(tracker.generate_report())
        
        if result == 0:
            print("✅ Test completed successfully!")
        else:
            print("❌ Test failed with errors")
            
    except Exception as e:
        print(f"❌ Test failed with exception: {e}")
        import traceback
        traceback.print_exc()
        
    finally:
        # Clean up
        try:
            os.unlink(requirements_file)
            print(f"🧹 Cleaned up test file: {requirements_file}")
        except:
            pass

def test_parser_functions():
    """Test just the parser functions without actual downloads"""
    print("\n🧪 TESTING PARSER FUNCTIONS")
    print("=" * 50)
    
    tracker = DownloadTracker()
    
    # Test download line parsing
    test_lines = [
        "Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)",
        "Downloading scipy-1.15.3-cp310-cp310-manylinux_2_17_x86_64.manylinux2014_x86_64.whl (37.7 MB)",
        "  50%|██████     | 450MB/900MB [00:30<00:30, 15.0MB/s]",
        "━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 356.8/888.0 MB 2.3 MB/s eta 0:03:52",
    ]
    
    print("📝 Testing line parsing:")
    for line in test_lines:
        print(f"\nInput: {line}")
        
        # Test download line parsing
        download_result = tracker._parse_pip_download_line(line)
        if download_result:
            print(f"   📦 Download: {download_result['package']} - {tracker._format_size(download_result['size_bytes'])}")
        
        # Test progress line parsing
        dummy_info = {'size_bytes': 888 * 1024 * 1024}
        progress_result = tracker._parse_pip_progress_line(line, dummy_info)
        if progress_result:
            print(f"   📊 Progress: {progress_result['progress_percent']:.1f}% @ {progress_result['speed_mbps']:.1f} MB/s")
        
        if not download_result and not progress_result:
            print("   ❌ No match found")

def main():
    """Main test function"""
    print("🚀 PIP DOWNLOAD TRACKING TEST SUITE")
    print("=" * 60)
    
    # Test parsing functions first (safer)
    test_parser_functions()
    
    # Ask user if they want to test actual pip install
    print("\n" + "=" * 60)
    response = input("Do you want to test actual pip install tracking? (y/N): ").strip().lower()
    
    if response in ['y', 'yes']:
        test_direct_pip_tracking()
    else:
        print("✅ Skipping actual pip install test")
    
    print("\n🎉 All tests completed!")

if __name__ == "__main__":
    main()