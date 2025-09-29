#!/usr/bin/env python3
"""
Download Progress Tracker for Content Scoring Service
Monitors and tracks downloads/pulls larger than 50MB during container build
"""

import os
import sys
import time
import subprocess
import json
from datetime import datetime
from typing import Dict, List, Optional
import urllib.request
import urllib.parse

class DownloadTracker:
    def __init__(self, size_threshold_mb: int = 50):
        self.size_threshold = size_threshold_mb * 1024 * 1024  # Convert to bytes
        self.downloads: List[Dict] = []
        self.start_time = datetime.now()
        
    def log_download_start(self, name: str, url: str = "", estimated_size: int = 0):
        """Log the start of a download"""
        download_info = {
            "name": name,
            "url": url,
            "estimated_size": estimated_size,
            "start_time": datetime.now().isoformat(),
            "status": "starting",
            "progress": 0
        }
        self.downloads.append(download_info)
        
        if estimated_size > self.size_threshold:
            print(f"🔄 [LARGE DOWNLOAD] Starting: {name}")
            print(f"   📦 Estimated size: {self._format_size(estimated_size)}")
            print(f"   🔗 URL: {url}")
            print(f"   ⏰ Started at: {download_info['start_time']}")
            
    def log_download_progress(self, name: str, current: int, total: int):
        """Log download progress"""
        for download in self.downloads:
            if download["name"] == name:
                download["progress"] = (current / total) * 100 if total > 0 else 0
                download["current_size"] = current
                download["total_size"] = total
                
                if total > self.size_threshold:
                    progress_bar = self._create_progress_bar(current, total)
                    print(f"\r🔄 {name}: {progress_bar} {self._format_size(current)}/{self._format_size(total)}", end="", flush=True)
                break
                
    def log_download_complete(self, name: str, final_size: int, success: bool = True):
        """Log download completion"""
        for download in self.downloads:
            if download["name"] == name:
                download["status"] = "completed" if success else "failed"
                download["final_size"] = final_size
                download["end_time"] = datetime.now().isoformat()
                download["duration"] = (datetime.now() - datetime.fromisoformat(download["start_time"])).total_seconds()
                
                if final_size > self.size_threshold:
                    print(f"\n✅ [LARGE DOWNLOAD] Completed: {name}")
                    print(f"   📊 Final size: {self._format_size(final_size)}")
                    print(f"   ⏱️  Duration: {download['duration']:.2f}s")
                    print(f"   🚀 Speed: {self._format_size(final_size / download['duration'])}/s")
                break
    
    def track_pip_install(self, requirements_file: str):
        """Track pip install progress with size monitoring"""
        print(f"🔍 Analyzing packages in {requirements_file}...")
        
        # Pre-analyze packages to estimate sizes
        with open(requirements_file, 'r') as f:
            packages = [line.strip() for line in f if line.strip() and not line.startswith('#')]
        
        large_packages = []
        for package in packages:
            package_name = package.split('>=')[0].split('==')[0].split('[')[0]
            estimated_size = self._estimate_package_size(package_name)
            if estimated_size > self.size_threshold:
                large_packages.append((package_name, estimated_size))
        
        if large_packages:
            print(f"📦 Found {len(large_packages)} potentially large packages:")
            for pkg, size in large_packages:
                print(f"   • {pkg}: ~{self._format_size(size)}")
            print()
        
        # Run pip install with progress tracking
        cmd = [
            sys.executable, "-m", "pip", "install", 
            "--upgrade", "--progress-bar", "on",
            "-r", requirements_file
        ]
        
        print(f"🚀 Starting pip install for {requirements_file}...")
        self.log_download_start(f"pip install {requirements_file}", "", sum(size for _, size in large_packages))
        
        try:
            process = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                universal_newlines=True,
                bufsize=1
            )
            
            current_package = None
            for line in process.stdout:
                line = line.strip()
                print(f"  {line}")
                
                # Track individual package downloads
                if "Downloading" in line and any(pkg in line for pkg, _ in large_packages):
                    for pkg, _ in large_packages:
                        if pkg in line:
                            current_package = pkg
                            self.log_download_start(f"Package: {pkg}", line)
                            break
                
                # Track progress if available
                if current_package and ("%" in line or "MB" in line or "KB" in line):
                    self._parse_pip_progress(current_package, line)
            
            process.wait()
            if process.returncode == 0:
                self.log_download_complete(f"pip install {requirements_file}", sum(size for _, size in large_packages), True)
                print("✅ pip install completed successfully")
            else:
                self.log_download_complete(f"pip install {requirements_file}", 0, False)
                print("❌ pip install failed")
                
        except Exception as e:
            print(f"❌ Error during pip install: {e}")
            self.log_download_complete(f"pip install {requirements_file}", 0, False)
    
    def track_spacy_download(self, model_name: str):
        """Track spaCy model download"""
        print(f"🔍 Checking spaCy model: {model_name}")
        
        # Estimate spaCy model size (en_core_web_sm is typically ~50MB)
        model_sizes = {
            "en_core_web_sm": 50 * 1024 * 1024,  # ~50MB
            "en_core_web_md": 50 * 1024 * 1024,  # ~50MB  
            "en_core_web_lg": 750 * 1024 * 1024,  # ~750MB
        }
        
        estimated_size = model_sizes.get(model_name, 50 * 1024 * 1024)
        
        if estimated_size > self.size_threshold:
            self.log_download_start(f"spaCy model: {model_name}", f"https://github.com/explosion/spacy-models/releases/download/{model_name}", estimated_size)
            
            try:
                cmd = [sys.executable, "-m", "spacy", "download", model_name]
                print(f"🚀 Downloading spaCy model: {model_name}")
                
                process = subprocess.Popen(
                    cmd,
                    stdout=subprocess.PIPE,
                    stderr=subprocess.STDOUT,
                    universal_newlines=True
                )
                
                for line in process.stdout:
                    line = line.strip()
                    print(f"  {line}")
                    
                    # Parse spaCy download progress if available
                    if "%" in line or "MB" in line:
                        self._parse_spacy_progress(model_name, line)
                
                process.wait()
                if process.returncode == 0:
                    self.log_download_complete(f"spaCy model: {model_name}", estimated_size, True)
                else:
                    self.log_download_complete(f"spaCy model: {model_name}", 0, False)
                    
            except Exception as e:
                print(f"❌ Error downloading spaCy model: {e}")
                self.log_download_complete(f"spaCy model: {model_name}", 0, False)
    
    def _estimate_package_size(self, package_name: str) -> int:
        """Estimate package size based on known patterns"""
        # Known large packages and their approximate sizes
        large_packages = {
            "sentence-transformers": 100 * 1024 * 1024,  # ~100MB
            "torch": 800 * 1024 * 1024,  # ~800MB
            "tensorflow": 500 * 1024 * 1024,  # ~500MB
            "scikit-learn": 80 * 1024 * 1024,  # ~80MB
            "numpy": 20 * 1024 * 1024,  # ~20MB
            "spacy": 60 * 1024 * 1024,  # ~60MB
            "transformers": 300 * 1024 * 1024,  # ~300MB
        }
        
        return large_packages.get(package_name.lower(), 10 * 1024 * 1024)  # Default 10MB
    
    def _parse_pip_progress(self, package: str, line: str):
        """Parse pip progress from output line"""
        try:
            # Basic progress tracking - pip output varies
            if "MB" in line or "KB" in line:
                parts = line.split()
                for i, part in enumerate(parts):
                    if "MB" in part or "KB" in part:
                        size_str = part.replace("MB", "").replace("KB", "").replace("(", "").replace(")", "")
                        try:
                            size = float(size_str)
                            if "MB" in part:
                                size *= 1024 * 1024
                            elif "KB" in part:
                                size *= 1024
                            # Rough progress estimation
                            self.log_download_progress(f"Package: {package}", int(size), int(size * 1.1))
                        except:
                            pass
        except:
            pass
    
    def _parse_spacy_progress(self, model: str, line: str):
        """Parse spaCy download progress from output line"""
        try:
            # spaCy output parsing - varies by version
            if "%" in line:
                # Look for percentage patterns
                import re
                percent_match = re.search(r'(\d+)%', line)
                if percent_match:
                    percent = int(percent_match.group(1))
                    estimated_total = 50 * 1024 * 1024  # 50MB default
                    current = int((percent / 100) * estimated_total)
                    self.log_download_progress(f"spaCy model: {model}", current, estimated_total)
        except:
            pass
    
    def _create_progress_bar(self, current: int, total: int, width: int = 30) -> str:
        """Create a visual progress bar"""
        if total <= 0:
            return "[" + "?" * width + "]"
        
        percent = current / total
        filled = int(width * percent)
        bar = "█" * filled + "░" * (width - filled)
        return f"[{bar}] {percent:.1%}"
    
    def _format_size(self, size: int) -> str:
        """Format size in human readable format"""
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024:
                return f"{size:.1f}{unit}"
            size /= 1024
        return f"{size:.1f}TB"
    
    def generate_report(self) -> str:
        """Generate a summary report of all downloads"""
        large_downloads = [d for d in self.downloads if d.get('final_size', 0) > self.size_threshold]
        
        if not large_downloads:
            return "📊 No large downloads (>50MB) detected during build."
        
        report = [
            "📊 LARGE DOWNLOAD SUMMARY REPORT",
            "=" * 50,
            f"🕐 Build started: {self.start_time.isoformat()}",
            f"📦 Large downloads detected: {len(large_downloads)}",
            f"💾 Total size threshold: {self._format_size(self.size_threshold)}",
            ""
        ]
        
        total_size = 0
        total_time = 0
        
        for i, download in enumerate(large_downloads, 1):
            size = download.get('final_size', 0)
            duration = download.get('duration', 0)
            total_size += size
            total_time += duration
            
            report.extend([
                f"{i}. {download['name']}",
                f"   📊 Size: {self._format_size(size)}",
                f"   ⏱️  Duration: {duration:.2f}s",
                f"   🚀 Speed: {self._format_size(size / duration) if duration > 0 else 'N/A'}/s",
                f"   ✅ Status: {download['status']}",
                ""
            ])
        
        report.extend([
            "📈 TOTALS:",
            f"   💾 Total downloaded: {self._format_size(total_size)}",
            f"   ⏱️  Total time: {total_time:.2f}s",
            f"   🚀 Average speed: {self._format_size(total_size / total_time) if total_time > 0 else 'N/A'}/s",
            ""
        ])
        
        return "\n".join(report)

def main():
    """Main function for command line usage"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Track large downloads during container build')
    parser.add_argument('--requirements', '-r', help='Requirements file to track pip install')
    parser.add_argument('--spacy-model', '-s', help='spaCy model to track download')
    parser.add_argument('--threshold', '-t', type=int, default=50, help='Size threshold in MB (default: 50)')
    
    args = parser.parse_args()
    
    tracker = DownloadTracker(args.threshold)
    
    if args.requirements:
        tracker.track_pip_install(args.requirements)
    
    if args.spacy_model:
        tracker.track_spacy_download(args.spacy_model)
    
    # Generate and display report
    print("\n" + tracker.generate_report())

if __name__ == "__main__":
    main()