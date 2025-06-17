#FROM maven:3.9-eclipse-temurin-17 AS builder
#WORKDIR /build
#COPY . .
#RUN ./mvnw clean install -Pproduction -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY ui-1.3.0.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod

VOLUME ["/db"]

EXPOSE 9191
ENTRYPOINT ["java", "-jar", "app.jar"]
