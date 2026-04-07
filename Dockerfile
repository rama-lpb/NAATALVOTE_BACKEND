FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY naatalvote-domain/pom.xml /workspace/naatalvote-domain/pom.xml
COPY naatalvote-application/pom.xml /workspace/naatalvote-application/pom.xml
COPY naatalvote-infrastructure/pom.xml /workspace/naatalvote-infrastructure/pom.xml
COPY naatalvote-api/pom.xml /workspace/naatalvote-api/pom.xml

RUN mvn -q -pl naatalvote-api -am -DskipTests package || true

COPY naatalvote-domain/src /workspace/naatalvote-domain/src
COPY naatalvote-application/src /workspace/naatalvote-application/src
COPY naatalvote-infrastructure/src /workspace/naatalvote-infrastructure/src
COPY naatalvote-api/src /workspace/naatalvote-api/src

RUN mvn -q -pl naatalvote-api -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/naatalvote-api/target/naatalvote-api-1.0.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

