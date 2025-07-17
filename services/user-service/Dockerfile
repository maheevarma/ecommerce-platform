FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/user-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=dev

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
