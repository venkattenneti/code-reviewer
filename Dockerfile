# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

# WHY WORKDIR: sets /app as working directory inside the container
# so all subsequent commands run relative to /app, not /
WORKDIR /app

# WHY copy pom.xml first, THEN source:
# Docker builds in layers. If pom.xml hasn't changed, Docker reuses
# the cached dependency-download layer — skipping mvn dependency:go-offline
# on every build. Only changed when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source — this layer only rebuilds when your Java code changes
COPY src ./src

# Package the app — skip tests (tests run in CI, not in Docker build)
RUN mvn package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy ONLY the JAR from Stage 1 — everything else is discarded
COPY --from=build /app/target/*.jar app.jar

# WHY EXPOSE: documents which port the app listens on
# doesn't actually publish the port — that's docker-compose's job
EXPOSE 8080

# WHY -Duser.timezone: same fix as your IntelliJ VM option —
# the JVM inside the container also defaults to Asia/Calcutta
# without this, you'd hit the same PostgreSQL timezone error
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]