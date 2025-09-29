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
        """Track pip install progress with real-time monitoring"""
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
            current_download_info = {}
            
            for line in process.stdout:
                line = line.strip()
                
                # Parse Docker build output format (#step timestamp output)
                docker_line = line
                if line.startswith('#'):
                    parts = line.split(' ', 2)
                    if len(parts) >= 3:
                        docker_line = parts[2]  # Extract actual content after step number and timestamp
                
                # Track individual package downloads with real-time progress
                if "Downloading" in docker_line:
                    download_info = self._parse_docker_download_line(docker_line)
                    if download_info and download_info['size_mb'] >= self.size_threshold / (1024 * 1024):
                        package_name = download_info['package']
                        current_package = package_name
                        current_download_info = download_info
                        
                        print(f"\n🔄 [LARGE DOWNLOAD] Starting: {package_name}")
                        print(f"   📦 Size: {self._format_size(download_info['size_bytes'])}")
                        print(f"   🔗 File: {download_info['filename']}")
                        self.log_download_start(f"Package: {package_name}", download_info['filename'], download_info['size_bytes'])
                
                # Track real-time download progress (progress bar lines)
                elif current_package and ("━━━" in docker_line or "█" in docker_line) and ("MB" in docker_line or "%" in docker_line):
                    progress_info = self._parse_docker_progress_line(docker_line, current_download_info)
                    if progress_info:
                        self._display_realtime_progress(current_package, progress_info)
                        self.log_download_progress(
                            f"Package: {current_package}",
                            progress_info['current_bytes'],
                            progress_info['total_bytes']
                        )
                
                # Print all lines for debugging
                print(f"  {line}")
                
                # Check if download completed
                if current_package and ("Installing collected packages" in docker_line or "Successfully installed" in docker_line):
                    if current_download_info:
                        print(f"\n✅ [LARGE DOWNLOAD] Completed: {current_package}")
                        self.log_download_complete(f"Package: {current_package}", current_download_info['size_bytes'], True)
                    current_package = None
                    current_download_info = {}
            
            process.wait()
            if process.returncode == 0:
                self.log_download_complete(f"pip install {requirements_file}", sum(size for _, size in large_packages), True)
                print("\n✅ pip install completed successfully")
            else:
                self.log_download_complete(f"pip install {requirements_file}", 0, False)
                print("\n❌ pip install failed")
                
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
        # Known large packages and their approximate sizes (updated with realistic values)
        large_packages = {
            "sentence-transformers": 100 * 1024 * 1024,  # ~100MB
            "torch": 900 * 1024 * 1024,  # ~900MB (like in your example: 888MB)
            "tensorflow": 500 * 1024 * 1024,  # ~500MB
            "scikit-learn": 80 * 1024 * 1024,  # ~80MB
            "scipy": 40 * 1024 * 1024,  # ~40MB (like in your example: 37.7MB)
            "numpy": 20 * 1024 * 1024,  # ~20MB
            "spacy": 60 * 1024 * 1024,  # ~60MB
            "transformers": 300 * 1024 * 1024,  # ~300MB
            "pandas": 30 * 1024 * 1024,  # ~30MB
            "matplotlib": 40 * 1024 * 1024,  # ~40MB
            "pillow": 30 * 1024 * 1024,  # ~30MB
            "opencv-python": 90 * 1024 * 1024,  # ~90MB
        }
        
        return large_packages.get(package_name.lower(), 10 * 1024 * 1024)  # Default 10MB
    
    def _parse_docker_download_line(self, line: str) -> Optional[Dict]:
        """Parse Docker/pip download line to extract package info and size"""
        try:
            import re
            
            # Parse pattern: "Downloading package-version-platform.whl (XXX.X MB)"
            download_pattern = r'Downloading\s+([^\s]+)\s+\(([0-9]+\.?[0-9]*)\s*(MB|KB|GB)\)'
            match = re.search(download_pattern, line)
            
            if match:
                filename = match.group(1)
                size_value = float(match.group(2))
                size_unit = match.group(3)
                
                # Convert to bytes
                size_bytes = size_value
                if size_unit == "KB":
                    size_bytes *= 1024
                elif size_unit == "MB":
                    size_bytes *= 1024 * 1024
                elif size_unit == "GB":
                    size_bytes *= 1024 * 1024 * 1024
                
                # Extract package name from filename
                package_name = filename.split('-')[0] if '-' in filename else filename.split('.')[0]
                
                return {
                    'package': package_name,
                    'filename': filename,
                    'size_bytes': int(size_bytes),
                    'size_mb': size_bytes / (1024 * 1024),
                    'size_unit': size_unit,
                    'size_value': size_value
                }
                
        except Exception as e:
            pass
        
        return None
    
    def _parse_docker_progress_line(self, line: str, download_info: Dict) -> Optional[Dict]:
        """Parse Docker/pip progress bar line to extract current progress"""
        try:
            import re
            
            # Parse pattern: "━━━━━━━━━━━━ XX.X/YYY.Y MB speed eta"
            # Also handle: "████████████░░░ XX.X/YYY.Y MB"
            progress_pattern = r'[━█░▓▒]+\s+([0-9]+\.?[0-9]*)/([0-9]+\.?[0-9]*)\s*(MB|KB|GB)'
            match = re.search(progress_pattern, line)
            
            if match:
                current_value = float(match.group(1))
                total_value = float(match.group(2))
                unit = match.group(3)
                
                # Convert to bytes
                multiplier = 1
                if unit == "KB":
                    multiplier = 1024
                elif unit == "MB":
                    multiplier = 1024 * 1024
                elif unit == "GB":
                    multiplier = 1024 * 1024 * 1024
                
                current_bytes = int(current_value * multiplier)
                total_bytes = int(total_value * multiplier)
                
                # Extract speed if available
                speed_pattern = r'([0-9]+\.?[0-9]*)\s*(MB|KB|GB)/s'
                speed_match = re.search(speed_pattern, line)
                speed_mbps = 0
                if speed_match:
                    speed_value = float(speed_match.group(1))
                    speed_unit = speed_match.group(2)
                    if speed_unit == "KB":
                        speed_mbps = speed_value / 1024
                    elif speed_unit == "MB":
                        speed_mbps = speed_value
                    elif speed_unit == "GB":
                        speed_mbps = speed_value * 1024
                
                return {
                    'current_bytes': current_bytes,
                    'total_bytes': total_bytes,
                    'current_mb': current_value if unit == "MB" else current_value / 1024 if unit == "KB" else current_value * 1024,
                    'total_mb': total_value if unit == "MB" else total_value / 1024 if unit == "KB" else total_value * 1024,
                    'progress_percent': (current_bytes / total_bytes * 100) if total_bytes > 0 else 0,
                    'speed_mbps': speed_mbps,
                    'unit': unit
                }
                
        except Exception as e:
            pass
        
        return None
    
    def _display_realtime_progress(self, package_name: str, progress_info: Dict):
        """Display real-time progress for large downloads"""
        if progress_info['total_bytes'] > self.size_threshold:
            # Create visual progress bar
            progress_bar = self._create_progress_bar(
                progress_info['current_bytes'],
                progress_info['total_bytes'],
                width=40
            )
            
            # Calculate ETA if speed is available
            eta_str = ""
            if progress_info['speed_mbps'] > 0:
                remaining_mb = progress_info['total_mb'] - progress_info['current_mb']
                eta_seconds = remaining_mb / progress_info['speed_mbps']
                eta_str = f" ETA: {int(eta_seconds)}s"
            
            # Format speed
            speed_str = ""
            if progress_info['speed_mbps'] > 0:
                speed_str = f" @ {progress_info['speed_mbps']:.1f}MB/s"
            
            # Print progress line (overwrite previous line)
            progress_line = (
                f"\r🔄 {package_name}: {progress_bar} "
                f"{progress_info['progress_percent']:.1f}% "
                f"({self._format_size(progress_info['current_bytes'])}"
                f"/{self._format_size(progress_info['total_bytes'])})"
                f"{speed_str}{eta_str}"
            )
            
            print(progress_line, end="", flush=True)
    
    def _parse_pip_progress(self, package: str, line: str):
        """Parse pip progress from output line (legacy method)"""
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