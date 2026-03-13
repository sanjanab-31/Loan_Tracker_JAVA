# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Use the port from application.properties
EXPOSE 8081
# Ensure the application listens on the port Render provides
ENV SERVER_PORT=8081
ENTRYPOINT ["java", "-jar", "app.jar"]
