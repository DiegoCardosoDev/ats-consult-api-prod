# syntax=docker/dockerfile:1

FROM node:20-alpine AS frontend
WORKDIR /frontend
COPY ats-frontend/package.json ats-frontend/package-lock.json ./
RUN npm ci
COPY ats-frontend/ ./
RUN npm run build -- --configuration production

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /build
COPY consult.api/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY consult.api/src ./src
COPY --from=frontend /frontend/dist/ats-frontend/browser ./src/main/resources/static
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
RUN addgroup -S app && adduser -S app -G app
COPY --from=backend /build/target/*.war /app/app.war
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.war"]
