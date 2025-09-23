# Stage 1: Build the application
FROM gradle:8.7-jdk17 AS build
WORKDIR /backend-app/source

# Copy các file cấu hình Gradle
COPY build.gradle.kts settings.gradle.kts ./

# Copy toàn bộ source code
COPY . .

# Build app
RUN gradle clean build -x test --no-daemon

# Stage 2: Run the application
FROM openjdk:17-slim
WORKDIR /backend-app
EXPOSE 8080

# Install required packages
RUN apt-get update && \
    apt-get install -y \
    ffmpeg \
    curl && \
    rm -rf /var/lib/apt/lists/*

# Tạo sẵn cấu trúc thư mục uploads
RUN mkdir -p /app/uploads/questions /app/uploads/lessons

# Đặt quyền truy cập phù hợp cho thư mục
RUN chmod -R 777 /app/uploads

# Thêm non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Thay đổi quyền sở hữu cho thư mục uploads
RUN chown -R spring:spring /app/uploads

USER spring:spring

COPY --from=build /backend-app/source/build/libs/*.jar backend-app.jar

# Thêm health check
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "backend-app.jar"]