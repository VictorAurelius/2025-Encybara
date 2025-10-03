#!/bin/bash

# =============================================================================
# debug-scripts.sh - Debug Scripts in Content Scoring Service
# =============================================================================
# Script để debug tại sao các scripts không chạy được
# =============================================================================

echo "=== SCRIPT DEBUG INFORMATION ==="
echo ""

echo "1. Current Directory:"
pwd
echo ""

echo "2. Files in current directory:"
ls -la *.sh 2>/dev/null || echo "No .sh files found in current directory"
echo ""

echo "3. Script permissions:"
if [[ -f "test-ngrok-public.sh" ]]; then
    echo "test-ngrok-public.sh: EXISTS"
    ls -la test-ngrok-public.sh 2>/dev/null || echo "Cannot check permissions"
else
    echo "test-ngrok-public.sh: NOT FOUND in current directory"
fi
echo ""

if [[ -f "tunnel.sh" ]]; then
    echo "tunnel.sh: EXISTS"
    ls -la tunnel.sh 2>/dev/null || echo "Cannot check permissions"
else
    echo "tunnel.sh: NOT FOUND in current directory"
fi
echo ""

echo "4. Bash version and location:"
bash --version | head -1 2>/dev/null || echo "Bash not found"
which bash 2>/dev/null || echo "Cannot locate bash"
echo ""

echo "5. Test simple script execution:"
echo 'echo "Hello from inline script"' > temp_test.sh
if bash temp_test.sh 2>/dev/null; then
    echo "✓ Bash execution works"
else
    echo "✗ Bash execution failed"
fi
rm -f temp_test.sh 2>/dev/null
echo ""

echo "6. Environment check:"
echo "OS Type: $OSTYPE"
echo "Shell: $SHELL"
echo "PATH: $PATH" | head -c 200
echo ""

echo "7. Try running test script with explicit bash:"
if [[ -f "test-ngrok-public.sh" ]]; then
    echo "Running: bash test-ngrok-public.sh --help"
    bash test-ngrok-public.sh --help 2>&1 | head -5
else
    echo "test-ngrok-public.sh not found in current directory"
fi
echo ""

echo "=== END DEBUG INFO ==="