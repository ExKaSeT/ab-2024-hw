FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY .. /app/.
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/image-editor-api/target/image-editor-api-0.0.1-SNAPSHOT.jar /app/api.jar
COPY --from=builder /app/image-editor-processors/target/image-editor-processors-0.0.1-SNAPSHOT.jar /app/processors.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/api.jar"]