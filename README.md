# NAATALVOTE Secure — Backend (DDD + Hexagonale)

Ce dossier contient le squelette du backend Spring Boot (Java 17) structuré en **4 modules Maven** pour respecter la séparation des responsabilités :

- `naatalvote-domain` : entités métier + value objects + règles (aucune dépendance Spring/JPA)
- `naatalvote-application` : cas d’usage + ports (interfaces) appelées par l’infrastructure
- `naatalvote-infrastructure` : adaptateurs techniques (persistance, sécurité, scheduler, gateways externes)
- `naatalvote-api` : API REST (controllers) + bootstrap Spring Boot

## Démarrage (dev)

Depuis `Bacend/` :

```bash
mvn -q -pl naatalvote-api -am package -DskipTests
java -jar naatalvote-api/target/naatalvote-api-1.0.0-SNAPSHOT.jar
```

Swagger (profile `inmemory` par défaut) :
- `http://localhost:8080/api/v1/swagger`
- OpenAPI JSON : `http://localhost:8080/api/v1/docs`

## Démarrage Docker (PostgreSQL + Redis)

À la racine du repo :

```bash
docker compose up --build
```

Swagger :
- `http://localhost:8080/api/v1/swagger`

Variables env attendues (profile `postgres`) :
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
 - `SPRING_DATA_REDIS_HOST`
 - `SPRING_DATA_REDIS_PORT`

## Notes d’architecture

- Le **Domain** ne contient aucune annotation JPA/Spring : la persistance se fait via des modèles `*Entity` en infrastructure + mapping.
- Les **ports** sont définis en `naatalvote-application`, et implémentés en `naatalvote-infrastructure`.
- Les **controllers** ne manipulent pas directement la persistance : ils appellent des services applicatifs.
