# =============================================================================
# Mekong Mobile Smart CRM - Module L5 (Spare-part Inventory)
# Multi-stage Dockerfile - Component 4 (reproducible package)
#
# Stage 1 builds the executable JAR with the Maven Wrapper.
# Stage 2 runs it as a non-root user in a minimal JRE image.
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: build
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Normalise CRLF line endings inherited from Windows checkouts
RUN apk add --no-cache curl

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Warm the dependency cache (missing artifacts are fetched again in the build)
RUN ./mvnw -B -DskipTests dependency:go-offline || true

COPY src/ src/
COPY seeds/ seeds/

# Tests run separately with `mvnw test` / `mvnw.cmd test` (Component 3)
RUN ./mvnw -B clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# curl is required by the Compose healthcheck
RUN apk add --no-cache curl \
    && addgroup -S mekonggroup \
    && adduser -S mekonguser -G mekonggroup

COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/seeds/ ./seeds/

USER mekonguser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=dev
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
