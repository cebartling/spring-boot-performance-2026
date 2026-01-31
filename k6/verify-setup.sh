#!/bin/bash
# Verification script for extract-metrics.sh setup

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Verifying extract-metrics.sh Setup ==="
echo ""

# Check if virtual environment exists
echo "[1/4] Checking virtual environment..."
if [ ! -d ".venv" ]; then
    echo "❌ Virtual environment not found"
    echo "Run: python3 -m venv .venv && .venv/bin/pip install requests"
    exit 1
fi
echo "✓ Virtual environment exists"
echo ""

# Check if requests library is installed
echo "[2/4] Checking Python dependencies..."
if ! .venv/bin/python -c "import requests" 2>/dev/null; then
    echo "❌ requests library not installed"
    echo "Run: .venv/bin/pip install requests"
    exit 1
fi
echo "✓ requests library installed"
echo ""

# Check if extract-metrics.sh is executable
echo "[3/4] Checking extract-metrics.sh permissions..."
if [ ! -x "extract-metrics.sh" ]; then
    echo "❌ extract-metrics.sh is not executable"
    echo "Run: chmod +x extract-metrics.sh"
    exit 1
fi
echo "✓ extract-metrics.sh is executable"
echo ""

# Test script help output
echo "[4/4] Testing script execution..."
if ! ./extract-metrics.sh --help >/dev/null 2>&1; then
    echo "❌ Failed to run extract-metrics.sh"
    exit 1
fi
echo "✓ extract-metrics.sh runs successfully"
echo ""

echo "=== Setup Verification Complete ==="
echo ""
echo "Next steps:"
echo "1. Start the monitoring stack: docker-compose up -d"
echo "2. Run a test: ./run-single-test.sh webflux baseline"
echo "3. Extract metrics: ./extract-metrics.sh --app webflux --duration 6m --format markdown"
echo ""
echo "For more information, see: METRICS_EXTRACTION.md"
