FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache ffmpeg
WORKDIR /app

RUN mkdir -p /app/arquivos-no-docker/uploads /app/arquivos-no-docker/convertidos \
    && chmod -R 777 /app/arquivos-no-docker

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]