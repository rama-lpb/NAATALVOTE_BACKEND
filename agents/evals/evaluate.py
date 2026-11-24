"""Campagne reproductible, verdicts attendus et plafond global en crédits existants."""
import argparse
import csv
import json
import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[2]
ap = argparse.ArgumentParser()
ap.add_argument('--dry-run', action='store_true')
ap.add_argument('--budget-usd', type=float, default=0.50)
args = ap.parse_args()
if not 0 <= args.budget_usd <= 0.50: ap.error('plafond global compris entre 0 et 0,50 USD')
mode = 'heuristique' if args.dry_run else 'modele'
out = Path(os.environ.get('EVAL_OUT', '/tmp/naatalvote-agent-evals')).resolve()
out.mkdir(parents=True, exist_ok=True)
expected = json.loads((root/'agents/evals/expected.json').read_text())
rows = []
spent = 0.0
for diff in sorted((root/'agents/evals').glob('*.diff')):
    for repeat in range(1, 4):
        report = out/f'{diff.stem}-{repeat}.md'
        env = dict(os.environ, AGENT_BUDGET_USD=str(max(0,args.budget_usd-spent)))
        process = subprocess.run([sys.executable,str(root/'agents/review_agent.py'),'--mode',mode,'--diff-file',str(diff),'--out',str(report)],env=env,cwd=root,capture_output=True,text=True)
        data = json.loads(report.with_suffix('.json').read_text())
        trace = data['trace']; actual = data['resultat']['verdict']; cost = trace.get('cout_usd',0)
        spent += cost
        rows.append({'mode':trace['mode'],'cas':diff.stem,'repetition':repeat,'attendu':expected[diff.stem]['attendu'],'obtenu':actual,'conforme':actual==expected[diff.stem]['attendu'] and process.returncode==0,'duree_s':trace['duree_s'],'cout_usd':cost,'erreur':trace.get('erreur',''),'rapport':report.name})
with (out/'resultats.csv').open('w',newline='') as f:
    writer=csv.DictWriter(f,fieldnames=rows[0].keys());writer.writeheader();writer.writerows(rows)
summary={'mode':mode,'executions':len(rows),'conformes':sum(r['conforme'] for r in rows),'cout_total_usd':round(spent,6),'plafond_global_usd':args.budget_usd,'evaluation_independante':False}
(out/'synthese.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2))
print(json.dumps(summary,ensure_ascii=False))
sys.exit(0 if all(r['conforme'] for r in rows) else 1)
