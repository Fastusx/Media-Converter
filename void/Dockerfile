FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache ffmpeg

WORKDIR /app

RUN mkdir -p /app/arquivos-no-docker/uploads /app/arquivos-no-docker/convertidos \
    && chmod -R 777 /app/arquivos-no-docker

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
