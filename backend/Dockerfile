FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
COPY gradlew gradlew.bat build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S survey && adduser -S survey -G survey && mkdir -p /app/uploads && chown -R survey:survey /app
COPY --from=build /workspace/build/libs/questionnaire-api.jar app.jar
USER survey
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
