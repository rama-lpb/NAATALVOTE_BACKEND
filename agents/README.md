# Pipeline agentique NAATALVOTE

Agent unique de revue de code et de proposition de tests, déclenché sur chaque Pull Request
vers `main` (workflow `.github/workflows/agent-review.yml`).

| Élément | Fichier |
|---|---|
| Orchestration (boucle agent + outils) | `review_agent.py` |
| Configuration (modèle, limites, budget) | `config.yaml` |
| Instructions de l'agent | `prompts/reviewer.md` |
| Cas d'évaluation | `evals/*.diff`, `evals/run_evals.sh` |

## Exécuter localement
```bash
pip install -r agents/requirements.txt
bash agents/evals/run_evals.sh --dry-run          # sans clé API (logique simulée)
export ANTHROPIC_API_KEY=...                      # ne jamais committer la clé
bash agents/evals/run_evals.sh                    # avec le modèle réel (3 répétitions par cas)
```

## Garde-fous
- Outils en lecture seule, confinés au dépôt ; fichiers sensibles (.env, clés, .git) refusés.
- Limites : 8 tours, 15 appels d'outils, 300 s, 0,50 USD par exécution.
- Contenus du diff traités comme données (cas `cas_injection.diff`).
- Repli : en cas d'erreur, verdict `indetermine` et revue manuelle.
- Le rapport est une proposition : la fusion exige la CI verte et une approbation humaine.
