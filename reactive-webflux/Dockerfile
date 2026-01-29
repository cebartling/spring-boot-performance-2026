# Build stage
FROM gradle:9.0.0-jdk24 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src
RUN gradle clean bootJar --no-daemon

# Runtime stage
FROM amazoncorretto:24
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
