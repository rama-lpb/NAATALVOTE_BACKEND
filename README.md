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
