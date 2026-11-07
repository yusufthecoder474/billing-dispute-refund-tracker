FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY reporting-service/pom.xml ./pom.xml
RUN mvn -B -ntp dependency:go-offline
COPY reporting-service/src ./src
RUN mvn -B -ntp -DskipTests package
FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build /workspace/target/reporting-service-1.0.0.jar app.jar
USER app
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=5s --start-period=50s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8081/api/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","app.jar"]
