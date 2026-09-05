# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source so code edits don't re-download deps.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090

# No fallback values here - DB_URL, DB_USERNAME, DB_PASSWORD, CLOUDINARY_*,
# and CORS_ALLOWED_ORIGINS must be supplied by the container runtime
# (docker run -e / --env-file, docker-compose, or the hosting platform's
# env var settings). The app will fail to start otherwise - by design.
ENTRYPOINT ["java", "-jar", "app.jar"]
