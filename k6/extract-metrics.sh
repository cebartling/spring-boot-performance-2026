#!/bin/bash
# Wrapper script to run extract-metrics.py with virtual environment

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"
PYTHON_SCRIPT="$SCRIPT_DIR/extract-metrics.py"

# Check if venv exists
if [ ! -d "$VENV_DIR" ]; then
    echo "Error: Virtual environment not found at $VENV_DIR" >&2
    echo "Run: cd $SCRIPT_DIR && python3 -m venv .venv && .venv/bin/pip install requests" >&2
    exit 1
fi

# Run the Python script with venv
exec "$VENV_DIR/bin/python" "$PYTHON_SCRIPT" "$@"
