# Base image for all stages
FROM openjdk:21-jdk-slim AS base
# RUN apt-get update -y && apt-get install maven -y
RUN apt-get install maven -y
WORKDIR /app
COPY . .

# Development stage
FROM base AS dev
RUN mvn clean install -DskipTests
CMD ["mvn", "spring-boot:run"]

# Testing stage
FROM base AS test
RUN echo "== Run test without executing the app =="
CMD ["mvn", "clean test"]

# Production stage
FROM base AS prod
RUN mvn build
COPY --from=base /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
