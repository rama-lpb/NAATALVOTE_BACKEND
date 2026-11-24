# Agent de revue NAATALVOTE

Agent unique en lecture seule : diff entrant, fichiers de la base de confiance, proposition de revue et de tests. Aucune fusion ni livraison automatique.

## Modes et reproduction

```bash
python3 -m venv .venv
.venv/bin/pip install -r agents/requirements.txt
.venv/bin/python -m unittest discover -s agents/tests -v
.venv/bin/python agents/review_agent.py --mode heuristique --diff-file agents/evals/cas_nominal.diff
# Clé uniquement dans ANTHROPIC_API_KEY, jamais dans une ligne de commande ou un fichier versionné.
.venv/bin/python agents/review_agent.py --mode modele --diff-file agents/evals/cas_nominal.diff
```

Le défaut est **heuristique**. En mode modèle, une clé absente produit `indetermine`, aucune substitution silencieuse. Les tests scriptés indiquent `client_simule`. Le code retour 2 indique une analyse indisponible ou interrompue.

Claude Sonnet 5.5 et ses tarifs de 2 USD/M jetons entrants et 10 USD/M sortants ont été vérifiés dans https://platform.claude.com/docs/en/models/overview le 23 novembre 2026. La disponibilité effective est contrôlée auprès de l’API avant une analyse réelle. Ces montants servent à l’estimation, pas à garantir le solde du compte. Désactiver toute recharge automatique.

## Limites et traces

8 tours, 15 outils, 300 secondes et 0,50 USD estimé par exécution ; `AGENT_BUDGET_USD` peut uniquement abaisser le plafond. Réservation conservatrice avant appel ; timeout réseau sans réessais automatiques. Markdown et JSON contiennent le mode, modèle, commit, durée, jetons, coût et erreurs. Les métriques acquises sont conservées après interruption.

Les fichiers cachés, non versionnés, trop volumineux, clés et liens symboliques sont refusés. Les contenus sont des données, jamais des instructions. Les contrôles ne constituent pas une preuve d’immunité à l’injection.

## GitHub Actions

`pull_request_target` exécute exclusivement le code de la base de confiance et acquiert le diff par l’API GitHub. L’analyse automatique est heuristique et ne reçoit pas de clé du modèle. Une exécution réelle se demande manuellement avec `workflow_dispatch` et un numéro de PR. Le job de publication reçoit seulement le rapport et les permissions de commentaire, sans clé Claude.

Le workflow doit d’abord exister sur la branche principale pour être utilisable automatiquement. Ne jamais remplacer cette séparation par l’exécution des scripts de la PR avec un secret.

## Évaluation

Huit cas construits et trois répétitions constituent une référence heuristique, pas une mesure indépendante du modèle. Compléter avec les cas de sécurité de l’orchestrateur, un diff réel et une grille humaine. Ne pas qualifier une analyse non exécutée de réussie.
