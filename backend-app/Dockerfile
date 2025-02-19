# Stage 1: Build the application
FROM gradle:8.7-jdk17 AS build
COPY --chown=gradle:gradle . /backend-app/source
WORKDIR /backend-app/source

# Copy các file cấu hình Gradle
COPY build.gradle.kts settings.gradle.kts ./

# Build app
RUN gradle clean build -x test --no-daemon

# Stage 2: Run the application
FROM openjdk:17-slim
WORKDIR /backend-app
EXPOSE 8080

# Thêm non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=build /backend-app/source/build/libs/*.jar backend-app.jar

# Thêm health check
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "backend-app.jar"]


# # Stage 1: Build the application
# FROM gradle:8.7-jdk17 AS build
# COPY --chown=gradle:gradle . /hoidanit/jobhunter
# WORKDIR /hoidanit/jobhunter

# #skip task: test
# RUN gradle clean build -x test --no-daemon

# # Stage 2: Run the application
# FROM openjdk:17-slim
# EXPOSE 8080
# COPY --from=build /hoidanit/jobhunter/build/libs/*.jar /hoidanit/spring-boot-job-hunter.jar
# ENTRYPOINT ["java", "-jar", "/hoidanit/spring-boot-job-hunter.jar"]
