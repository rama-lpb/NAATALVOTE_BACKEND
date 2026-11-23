#!/usr/bin/env bash
# Rejoue les cas d'évaluation. --dry-run : analyse heuristique (aucun appel au modèle).
# Sans --dry-run et avec ANTHROPIC_API_KEY : exécution réelle de l'agent IA (3 répétitions par cas).
set -euo pipefail
cd "$(dirname "$0")/../.."
OUT=${EVAL_OUT:-/tmp/agent-evals}; mkdir -p "$OUT"
MODE=$([ "${1:-}" = "--dry-run" ] && echo heuristique || echo modele)
for f in agents/evals/*.diff; do
  name=$(basename "$f" .diff)
  for run in 1 2 3; do
    start=$(date +%s.%N)
    v=$(python3 agents/review_agent.py --diff-file "$f" --out "$OUT/$name-$run.md" "$@" | sed 's/verdict=//')
    end=$(date +%s.%N)
    printf '%s;%s;run%s;%s;%.2fs\n' "$MODE" "$name" "$run" "$v" "$(echo "$end - $start" | bc)"
  done
done
