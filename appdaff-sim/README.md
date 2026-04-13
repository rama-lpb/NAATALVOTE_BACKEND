# AppDAFF (simulation)

Petit service HTTP de simulation pour **récupérer un utilisateur par CNI** (fixtures locales).

## Lancer en local (sans Docker)

```bash
cd BACKEND_NAATALVOTE/appdaff-sim
python3 server.py
```

Endpoints :
- `GET /api/v1/health`
- `GET /api/v1/citoyens` (liste fixtures)
- `GET /api/v1/citoyens/{cni}`

Notes :
- Tous les acteurs ont au minimum le rôle `CITOYEN`. Si un acteur a un rôle additionnel (`ADMIN`, `OPERATEUR`, `SUPERADMIN`), le front doit afficher un portail / changement de rôle.
- Les fixtures acceptent aussi des alias (ex: `CNI-0001`) via le champ `aliases`.

Exemple :
```bash
curl http://localhost:9090/api/v1/citoyens/CNI-0001
```
