# Construye el frontend, luego el jar, y deja solo el jar en la imagen final.
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-25 AS backend
WORKDIR /app
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
COPY --from=frontend /app/frontend/dist ./src/main/resources/static
RUN mvn -q package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/cyber-deck-backend.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=cloud
ENTRYPOINT ["java", "-jar", "app.jar"]
