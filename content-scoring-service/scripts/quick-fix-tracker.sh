#!/bin/bash

# Quick Fix Script for Download Tracker Issues
# Ensures proper permissions and validates functionality

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "🔧 QUICK FIX FOR DOWNLOAD TRACKER"
echo "================================="

# 1. Fix permissions
echo "📋 Step 1: Fixing permissions..."
chmod +x "$SCRIPT_DIR/download-tracker.py" 2>/dev/null || echo "  ✅ download-tracker.py already executable"
chmod +x "$SCRIPT_DIR/build-docker-with-monitoring.sh" 2>/dev/null || echo "  ✅ build-docker-with-monitoring.sh already executable"
chmod +x "$SCRIPT_DIR/test-pip-tracking.py" 2>/dev/null || echo "  ✅ test-pip-tracking.py already executable"

# 2. Validate Python script syntax
echo ""
echo "📋 Step 2: Validating Python scripts..."
python -m py_compile "$SCRIPT_DIR/download-tracker.py" && echo "  ✅ download-tracker.py syntax OK" || echo "  ❌ download-tracker.py has syntax errors"
python -m py_compile "$SCRIPT_DIR/test-pip-tracking.py" && echo "  ✅ test-pip-tracking.py syntax OK" || echo "  ❌ test-pip-tracking.py has syntax errors"

# 3. Test parser functions
echo ""
echo "📋 Step 3: Testing parser functions..."
cd "$PROJECT_DIR"

python3 -c "
import sys
sys.path.insert(0, 'scripts')
from download_tracker import DownloadTracker

tracker = DownloadTracker()

# Test download line parsing
test_line = 'Downloading torch-2.8.0-cp310-cp310-manylinux_2_28_x86_64.whl (888.0 MB)'
result = tracker._parse_pip_download_line(test_line)

if result:
    print(f'  ✅ Parser working: {result[\"package\"]} - {tracker._format_size(result[\"size_bytes\"])}')
else:
    print('  ❌ Parser failed')

# Test progress line parsing  
progress_line = '━━━━━░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 356.8/888.0 MB 2.3 MB/s eta 0:03:52'
progress_result = tracker._parse_pip_progress_line(progress_line, {'size_bytes': 888 * 1024 * 1024})

if progress_result:
    print(f'  ✅ Progress parser working: {progress_result[\"progress_percent\"]:.1f}%')
else:
    print('  ❌ Progress parser failed')
" || echo "  ❌ Parser test failed"

# 4. Check requirements file
echo ""
echo "📋 Step 4: Checking requirements files..."
if [ -f "$PROJECT_DIR/requirements-prod.txt" ]; then
    echo "  ✅ requirements-prod.txt found"
    echo "  📦 Large packages detected:"
    grep -E "(torch|scipy|scikit-learn|sentence-transformers|spacy)" "$PROJECT_DIR/requirements-prod.txt" | head -5 | while read line; do
        echo "     • $line"
    done
else
    echo "  ❌ requirements-prod.txt not found"
fi

# 5. Test basic functionality
echo ""
echo "📋 Step 5: Testing basic functionality..."
python3 -c "
import sys
sys.path.insert(0, 'scripts')
from download_tracker import DownloadTracker

tracker = DownloadTracker(size_threshold_mb=50)
print(f'  ✅ Tracker initialized with threshold: {tracker._format_size(tracker.size_threshold)}')

# Test size estimation
torch_size = tracker._estimate_package_size('torch')
print(f'  📦 Estimated torch size: {tracker._format_size(torch_size)}')

if torch_size > tracker.size_threshold:
    print('  ✅ torch will be tracked (size > threshold)')
else:
    print('  ⚠️  torch will NOT be tracked (size < threshold)')
" || echo "  ❌ Basic functionality test failed"

echo ""
echo "🎯 RECOMMENDED NEXT STEPS:"
echo "========================="
echo "1. Test parsing functions:"
echo "   python scripts/test-pip-tracking.py"
echo ""
echo "2. Test with actual Docker build:"
echo "   ./scripts/build-docker-with-monitoring.sh"
echo ""
echo "3. If still having issues, check build logs:"
echo "   tail -f /tmp/docker-build-*.log"
echo ""
echo "✅ Quick fix completed!"