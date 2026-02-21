# ============================================================
# Stage 1: BUILD — compile the Spring Boot app into a JAR
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first (so Maven dependencies are cached if code hasn't changed)
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source code
COPY src ./src

# Build the JAR (skip tests for faster build)
RUN mvn clean package -DskipTests -B --no-transfer-progress && \
    cp target/*.jar target/app.jar

# ============================================================
# Stage 2: RUN — use a lightweight JRE to run the JAR
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the renamed JAR from the build stage
COPY --from=builder /app/target/app.jar app.jar

# Railway injects PORT dynamically — Spring reads it via ${PORT:8080}
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
