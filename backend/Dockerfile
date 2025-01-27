# make an updated base image
FROM openjdk:21-jdk-slim AS base
WORKDIR /app
COPY . .
RUN apt-get update -y && apt-get install maven -y

# Development stage
FROM base AS dev
RUN echo -e "\033[0;32m====< Dev environment >====\033[0m"
RUN mvn clean install -DskipTests
CMD ["mvn", "spring-boot:run", "-DskipTests"]

# Testing stage
FROM base AS test
RUN echo -e "\033[0;32m====< Run test without executing the app >====\033[0m"
CMD ["mvn", "clean test"]

# Production stage
FROM base AS prod
RUN mvn build
COPY --from=base /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
