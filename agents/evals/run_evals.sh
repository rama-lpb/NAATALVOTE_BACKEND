#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
exec "${AGENT_PYTHON:-python3}" agents/evals/evaluate.py "$@"
