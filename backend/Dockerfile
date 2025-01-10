# make an updated base image
# `docker buildx build --tag updated_base-openjdk:21-jdk-slim --target update .`
FROM openjdk:21-jdk-slim AS update
RUN apt-get update -y && apt-get install maven -y

# Base image for all stages
# Started by docker-compose
# Ensure you have the environment you want in `.env`.
FROM updated_base-openjdk:21-jdk-slim AS base
COPY --from=update / /
WORKDIR /app
COPY . .

# Development stage
FROM base AS dev
RUN echo "== Dev environment =="
RUN mvn clean install -DskipTests
CMD ["mvn", "spring-boot:run", "-DskipTests"]

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
