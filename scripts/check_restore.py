#!/usr/bin/env python3
"""Sauvegarde/restauration exclusivement sur le conteneur local fictif ESP221."""
import argparse
import datetime
import hashlib
import json
from pathlib import Path
import subprocess

ap=argparse.ArgumentParser();ap.add_argument('--out',required=True);args=ap.parse_args()
out=Path(args.out).resolve();out.mkdir(parents=True,exist_ok=True)
container='naatalvote-esp221-postgres'
def run(*cmd,**kwargs): return subprocess.run(list(cmd),check=True,capture_output=True,**kwargs)
def sql(database,query):
    return run('docker','exec',container,'psql','-U','naatalvote','-d',database,'-At','-c',query,text=True).stdout.strip()
backup=run('docker','exec',container,'pg_dump','-U','naatalvote','-d','naatalvote','-Fc').stdout
(out/'naatalvote-local.dump').write_bytes(backup)
target='nv_restore_'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%d%H%M%S')
run('docker','exec',container,'createdb','-U','naatalvote',target)
run('docker','exec','-i',container,'pg_restore','-U','naatalvote','--no-owner','--exit-on-error','-d',target,input=backup)
checks=[]
for table in ['utilisateur','utilisateur_role','utilisateur_telephone','election','candidat','vote','trace_vote','fraude','suspension','action_log','flyway_schema_history']:
    order='installed_rank' if table=='flyway_schema_history' else ('utilisateur_id,role' if table=='utilisateur_role' else ('utilisateur_id,telephone' if table=='utilisateur_telephone' else 'id'))
    query=f'SELECT row_to_json(t) FROM (SELECT * FROM {table} ORDER BY {order}) t'
    source=sql('naatalvote',query);restored=sql(target,query)
    checks.append({'table':table,'rows':len(source.splitlines()),'sha256':hashlib.sha256(source.encode()).hexdigest(),'identique':source==restored})
result={'date_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'environnement':'conteneur local avec données fictives','source':'naatalvote','destination':target,'dump_sha256':hashlib.sha256(backup).hexdigest(),'tables':checks,'reussi':all(c['identique'] for c in checks)}
(out/'restauration.json').write_text(json.dumps(result,ensure_ascii=False,indent=2));print(json.dumps(result,ensure_ascii=False,indent=2))
assert result['reussi']
