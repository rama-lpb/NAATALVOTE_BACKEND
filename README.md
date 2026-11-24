# NAATALVOTE Secure — Backend (DDD + Hexagonale)

Ce dossier contient le squelette du backend Spring Boot (Java 17) structuré en **4 modules Maven** pour respecter la séparation des responsabilités :

- `naatalvote-domain` : entités métier + value objects + règles (aucune dépendance Spring/JPA)
- `naatalvote-application` : cas d’usage + ports (interfaces) appelées par l’infrastructure
- `naatalvote-infrastructure` : adaptateurs techniques (persistance, sécurité, scheduler, gateways externes)
- `naatalvote-api` : API REST (controllers) + bootstrap Spring Boot

## Démarrage (dev)

Depuis la racine du repo backend (`BACKEND_NAATALVOTE/`) :

```bash
mvn -q -pl naatalvote-api -am package -DskipTests
java -jar naatalvote-api/target/naatalvote-api-1.0.0-SNAPSHOT.jar
```

Swagger (profile `inmemory` par défaut) :
- `http://localhost:8080/api/v1/swagger`
- OpenAPI JSON : `http://localhost:8080/api/v1/docs`

## Démarrage Docker (PostgreSQL + Redis)

À la racine du repo backend :

```bash
docker compose up --build
```

Notes :
- PostgreSQL est exposé sur `localhost:5433` (pour éviter les conflits si `5432` est déjà utilisé sur ta machine).
- Un service **AppDAFF simulation** est lancé sur `localhost:9090` (fixtures CNI -> user).

Swagger :
- `http://localhost:8080/api/v1/swagger`

Variables env attendues (profile `postgres`) :
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
 - `SPRING_DATA_REDIS_HOST`
 - `SPRING_DATA_REDIS_PORT`
 - `NAATALVOTE_CORS_ALLOWED_ORIGINS` (ex: URL du frontend Render)
 - `APPDAFF_BASE_URL` (ex: `http://localhost:9090` ou `http://appdaff:9090` en Docker)

## Notes d’architecture

- Le **Domain** ne contient aucune annotation JPA/Spring : la persistance se fait via des modèles `*Entity` en infrastructure + mapping.
- Les **ports** sont définis en `naatalvote-application`, et implémentés en `naatalvote-infrastructure`.
- Les **controllers** ne manipulent pas directement la persistance : ils appellent des services applicatifs.

## Version et validation ESP 221

Le backend utilise Spring Boot **4.1.3**, Java 17 et springdoc **3.2.1**. La migration conserve les règles métier et les quatre modules ; elle adapte Jackson 3, les modules Web MVC/Flyway et les noms d’auto-configuration du profil mémoire. La branche 3.5 n’est plus maintenue gratuitement et la mise à jour 3.5.16 ne supprimait pas tous les avis de sécurité.

Validation complète (Docker indispensable pour l’intégration) :

```bash
mvn -B -ntp verify -Dapi.version=1.44
mvn -B -ntp org.cyclonedx:cyclonedx-maven-plugin:2.9.1:makeAggregateBom -DoutputFormat=json -DincludeTestScope=false
python3 -m unittest discover -s scripts -p 'test_*.py' -v
python3 scripts/audit_dependencies.py
```

Le dernier contrôle local du 24 novembre comprend 31 tests Java, dont six sur PostgreSQL/Redis réels, sans test ignoré. L’audit OSV des 137 composants Maven résolus hors tests ne signale aucun avis connu à cette date. Une erreur réseau, un inventaire vide ou une réponse incomplète font échouer le contrôle ; aucun avis n’est masqué. Le rapport et le SBOM sont conservés par la CI. Ce résultat n’est pas une certification de sécurité et ne couvre pas le système d’exploitation de l’image.

`docker compose up --build` est une démonstration locale avec le profil `postgres,demo`, OTP simulé et clés éphémères ; ses ports sont liés à la boucle locale. Ne l’utiliser qu’avec des données fictives. Hors démonstration, le profil `postgres` exige `NAATALVOTE_JWT_PRIVATE_KEY` et `NAATALVOTE_JWT_PUBLIC_KEY` au format PEM et ne renvoie pas l’OTP.

La livraison Render est manuelle : workflow_dispatch sur main avec deploy=true, après revue humaine. L’image doit porter le SHA du commit, et `/api/v1/health` doit confirmer cette même version. Les comptes cloud et secrets ne sont pas créés par le simple ajout des YAML.
