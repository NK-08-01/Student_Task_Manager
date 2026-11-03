# ==============================================================================
# Multi-stage Dockerfile for Student Task Manager
# Ideal for deploying to Render, Railway, Fly.io, AWS, or DigitalOcean
# ==============================================================================

# Stage 1: Build the application using Maven and OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package application into a standalone fat JAR
RUN mvn clean package -DskipTests

# Stage 2: Create lightweight production runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the executable jar from builder stage
COPY --from=builder /app/target/task-manager-*.jar app.jar

# Expose server port
EXPOSE 8080

# Environment variables (can be overridden in production)
ENV PORT=8080

# Launch Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
