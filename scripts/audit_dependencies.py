#!/usr/bin/env python3
"""Contrôle bloquant des versions Maven résolues via l'API publique OSV.

Le SBOM est produit par CycloneDX. Seules les coordonnées publiques des
bibliothèques sont transmises. Une erreur réseau ou un résultat incomplet
échoue ; aucun résultat vide ne peut être présenté comme un audit réussi.
"""
import argparse
import datetime
import json
from pathlib import Path
import subprocess
import sys
import urllib.request


def components(bom):
    result = []
    for c in bom.get('components', []):
        if c.get('purl', '').startswith('pkg:maven/') and c.get('group') != 'com.naatalvote':
            if not all(isinstance(c.get(k), str) and c[k] for k in ('group', 'name', 'version')):
                raise ValueError('Composant Maven incomplet')
            result.append(c)
    if not result:
        raise ValueError('Aucune dépendance Maven résolue dans le SBOM')
    return result


def audit(cs):
    findings = []
    for start in range(0, len(cs), 100):
        batch = cs[start:start + 100]
        queries = [{'package': {'ecosystem': 'Maven', 'name': c['group'] + ':' + c['name']},
                    'version': c['version']} for c in batch]
        request = urllib.request.Request('https://api.osv.dev/v1/querybatch',
            data=json.dumps({'queries': queries}).encode(), headers={'Content-Type': 'application/json'})
        with urllib.request.urlopen(request, timeout=30) as response:
            data = json.load(response)
        results = data.get('results')
        if not isinstance(results, list) or len(results) != len(batch):
            raise ValueError('Réponse OSV incomplète')
        for c, result in zip(batch, results):
            if not isinstance(result, dict) or result.get('next_page_token'):
                raise ValueError('Réponse OSV incomplète ou paginée : revue nécessaire')
            vulnerabilities = result.get('vulns', [])
            if not isinstance(vulnerabilities, list) or any(not v.get('id') for v in vulnerabilities):
                raise ValueError('Avis OSV invalide')
            if vulnerabilities:
                findings.append({'package': c['group'] + ':' + c['name'], 'version': c['version'],
                                 'advisories': [v['id'] for v in vulnerabilities]})
    return findings


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--bom', default='target/bom.json')
    parser.add_argument('--out', default='target/dependency-audit.json')
    args = parser.parse_args()
    report = {'date_utc': datetime.datetime.now(datetime.timezone.utc).isoformat(),
              'source': 'https://api.osv.dev/v1/querybatch', 'etat': 'erreur',
              'portee': 'Dépendances Maven résolues hors tests ; aucun avis connu ne signifie pas absence de risque'}
    code = 2
    try:
        report['commit'] = subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip()
        cs = components(json.loads(Path(args.bom).read_text()))
        report['composants'] = len(cs)
        report['constats'] = audit(cs)
        report['etat'] = 'vulnerabilites' if report['constats'] else 'conforme'
        code = 1 if report['constats'] else 0
    except Exception as exc:
        report['erreur'] = type(exc).__name__
    finally:
        path = Path(args.out)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(report, ensure_ascii=False, indent=2))
    print(json.dumps(report, ensure_ascii=False))
    return code


if __name__ == '__main__':
    sys.exit(main())
