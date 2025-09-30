#!/usr/bin/env python3
"""
Test script to demonstrate download tracking functionality
Simulates Docker build output with large downloads
"""

import sys
import time
import subprocess
from download_tracker import DownloadTracker

def simulate_docker_output():
    """Simulate Docker build output with large downloads"""
    
    print("🧪 Testing Download Tracker with Simulated Docker Output")
    print("=" * 60)
    
    # Sample Docker build lines from your example
    sample_lines = [
        "#10 87.53   Downloading scipy-1.15.3-cp310-cp310-manylinux_2_17_x86_64.manylinux2014_x86_64.whl (37.7 MB)",
        "#10 89.1    ━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 15.2/37.7 MB 2.0 MB/s eta 0:00:11",
        "#10 91.2    ━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 22.1/37.7 MB 2.0 MB/s eta 0:00:08",
        "#10 93.4    ━━━━━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 30.5/37.7 MB 2.0 MB/s eta 0:00:04",
        "#10 95.1    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 37.7/37.7 MB 2.0 MB/s eta 0:00:00",
        "#10 106.5   Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)",
        "#10 110.2   ━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 45.2/888.0 MB 1.5 MB/s eta 0:09:22",
        "#10 115.8   ━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 125.7/888.0 MB 1.8 MB/s eta 0:07:05",
        "#10 122.1   ━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 245.3/888.0 MB 2.1 MB/s eta 0:05:08",
        "#10 128.9   ━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 356.8/888.0 MB 2.3 MB/s eta 0:03:52",
        "#10 135.2   ━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 467.2/888.0 MB 2.4 MB/s eta 0:02:55",
        "#10 142.5   ━━━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 587.9/888.0 MB 2.5 MB/s eta 0:02:00",
        "#10 149.1   ━━━━━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 698.4/888.0 MB 2.6 MB/s eta 0:01:13",
        "#10 155.8   ━━━━━━━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░ 789.1/888.0 MB 2.7 MB/s eta 0:00:37",
        "#10 160.2   ━━━━━━━━━━━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░ 845.6/888.0 MB 2.8 MB/s eta 0:00:15",
        "#10 163.9   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 888.0/888.0 MB 2.8 MB/s eta 0:00:00",
        "#10 164.1   Installing collected packages: torch, scipy",
        "#10 168.5   Successfully installed torch-2.8.0 scipy-1.15.3",
    ]
    
    tracker = DownloadTracker(size_threshold_mb=50)
    
    print("\n🚀 Starting simulated Docker build process...")
    print("📊 Tracking downloads larger than 50MB\n")
    
    current_package = None
    current_download_info = {}
    
    for line in sample_lines:
        print(f"Docker: {line}")
        
        # Parse Docker build output format
        docker_line = line
        if line.startswith('#'):
            parts = line.split(' ', 2)
            if len(parts) >= 3:
                docker_line = parts[2]
        
        # Track individual package downloads with real-time progress
        if "Downloading" in docker_line:
            download_info = tracker._parse_docker_download_line(docker_line)
            if download_info and download_info['size_mb'] >= 50:
                package_name = download_info['package']
                current_package = package_name
                current_download_info = download_info
                
                print(f"\n🔄 [LARGE DOWNLOAD] Starting: {package_name}")
                print(f"   📦 Size: {tracker._format_size(download_info['size_bytes'])}")
                print(f"   🔗 File: {download_info['filename']}")
                tracker.log_download_start(f"Package: {package_name}", download_info['filename'], download_info['size_bytes'])
        
        # Track real-time download progress
        elif current_package and ("━━━" in docker_line or "█" in docker_line) and ("MB" in docker_line):
            progress_info = tracker._parse_docker_progress_line(docker_line, current_download_info)
            if progress_info:
                tracker._display_realtime_progress(current_package, progress_info)
                tracker.log_download_progress(
                    f"Package: {current_package}", 
                    progress_info['current_bytes'], 
                    progress_info['total_bytes']
                )
        
        # Check if download completed
        if current_package and ("Installing collected packages" in docker_line or "Successfully installed" in docker_line):
            if current_download_info:
                print(f"\n✅ [LARGE DOWNLOAD] Completed: {current_package}")
                tracker.log_download_complete(f"Package: {current_package}", current_download_info['size_bytes'], True)
            current_package = None
            current_download_info = {}
        
        # Simulate time delay
        time.sleep(0.5)
    
    print("\n" + "=" * 60)
    print(tracker.generate_report())

def test_requirements_analysis():
    """Test requirements file analysis"""
    print("\n🧪 Testing Requirements Analysis")
    print("=" * 60)
    
    tracker = DownloadTracker(size_threshold_mb=50)
    
    # Check if requirements file exists
    try:
        with open('requirements-prod.txt', 'r') as f:
            packages = [line.strip() for line in f if line.strip() and not line.startswith('#')]
        
        print(f"📋 Found {len(packages)} packages in requirements-prod.txt")
        
        large_packages = []
        for package in packages:
            package_name = package.split('>=')[0].split('==')[0].split('[')[0]
            estimated_size = tracker._estimate_package_size(package_name)
            if estimated_size > tracker.size_threshold:
                large_packages.append((package_name, estimated_size))
        
        if large_packages:
            print(f"\n📦 Potentially large packages (>{tracker.size_threshold // (1024*1024)}MB):")
            for pkg, size in large_packages:
                print(f"   • {pkg}: ~{tracker._format_size(size)}")
        else:
            print("\n✅ No packages estimated to be larger than threshold")
            
    except FileNotFoundError:
        print("⚠️ requirements-prod.txt not found, creating test data...")
        test_packages = [
            ("torch", 900 * 1024 * 1024),
            ("scipy", 40 * 1024 * 1024),
            ("sentence-transformers", 100 * 1024 * 1024),
            ("scikit-learn", 80 * 1024 * 1024),
        ]
        
        for pkg, size in test_packages:
            if size > tracker.size_threshold:
                print(f"   • {pkg}: ~{tracker._format_size(size)}")

def test_parsing_functions():
    """Test parsing functions with sample data"""
    print("\n🧪 Testing Parsing Functions")
    print("=" * 60)
    
    tracker = DownloadTracker()
    
    # Test download line parsing
    test_download_lines = [
        "Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)",
        "Downloading scipy-1.15.3-cp310-cp310-manylinux_2_17_x86_64.manylinux2014_x86_64.whl (37.7 MB)",
        "Downloading small-package-1.0.0-py3-none-any.whl (2.1 KB)",
    ]
    
    print("📝 Testing pip download line parsing:")
    for line in test_download_lines:
        result = tracker._parse_pip_download_line(line)
        if result:
            print(f"   ✅ {result['package']}: {tracker._format_size(result['size_bytes'])}")
        else:
            print(f"   ❌ Failed to parse: {line}")
    
    # Test progress line parsing - both pip and docker formats
    test_progress_lines = [
        # Docker/unicode format
        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 888.0/888.0 MB 2.8 MB/s eta 0:00:00",
        "━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 356.8/888.0 MB 2.3 MB/s eta 0:03:52",
        "████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 15.2/37.7 MB 2.0 MB/s eta 0:00:11",
        # pip format
        "  50%|██████     | 450MB/900MB [00:30<00:30, 15.0MB/s]",
        "  75%|███████████| 666MB/888MB [01:20<00:20, 12.5MB/s]",
    ]
    
    print("\n📊 Testing progress line parsing:")
    dummy_info = {'size_bytes': 888 * 1024 * 1024}
    for line in test_progress_lines:
        result = tracker._parse_pip_progress_line(line, dummy_info)
        if result:
            print(f"   ✅ Progress: {result['progress_percent']:.1f}% ({result['current_mb']:.1f}/{result['total_mb']:.1f} MB) @ {result['speed_mbps']:.1f} MB/s")
        else:
            print(f"   ❌ Failed to parse: {line}")

def main():
    """Main test function"""
    print("🧪 DOWNLOAD TRACKER TEST SUITE")
    print("=" * 60)
    print("Testing download tracking functionality for Content Scoring Service")
    print("Threshold: Downloads larger than 50MB")
    print("=" * 60)
    
    # Run tests
    test_parsing_functions()
    test_requirements_analysis()
    simulate_docker_output()
    
    print("\n✅ All tests completed!")
    print("\nTo test with real Docker build, run:")
    print("   ./scripts/build-docker-with-monitoring.sh")

if __name__ == "__main__":
    main()