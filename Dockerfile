# Development Stage
FROM eclipse-temurin:23-jdk-alpine AS development

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle

# Add execute permission to gradlew
RUN chmod +x ./gradlew

# Copy application source (real-time sync in Dev)
COPY src /app/src

# Install dependencies
RUN ./gradlew clean assemble --no-daemon -x test

# Expose application port
EXPOSE 8080

# Run the application in Dev mode
CMD ["./gradlew", "bootRun", "--no-daemon"]

# Builder Stage
FROM eclipse-temurin:23-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle

RUN chmod +x ./gradlew

# Copy application source
COPY src /app/src

# Build the application
RUN ./gradlew clean build --no-daemon -x test

# Production Stage
FROM eclipse-temurin:23-jre-alpine AS production

WORKDIR /app

# Copy only the built artifact from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application in Prod mode
ENTRYPOINT ["java", "-jar", "app.jar"]
