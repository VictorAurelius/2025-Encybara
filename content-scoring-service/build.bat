@echo off
REM Content Scoring Service - Build Script for Windows
REM Script để build tất cả container với một câu lệnh trên Windows

setlocal enabledelayedexpansion

REM Colors for output (Windows)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Function to show usage
:show_usage
echo Content Scoring Service - Build Script for Windows
echo.
echo Usage: %~0 [OPTIONS]
echo.
echo Options:
echo   --clean          Xóa tất cả images và containers cũ trước khi build
echo   --no-cache       Build không sử dụng cache
echo   --monitoring     Build kèm theo monitoring stack (Prometheus + Grafana)
echo   --caching        Build kèm theo Redis caching
echo   --proxy          Build kèm theo NGINX reverse proxy
echo   --all            Build tất cả services (monitoring + caching + proxy)
echo   --help           Hiển thị help này
echo.
echo Examples:
echo   %~0                          # Build service cơ bản
echo   %~0 --clean --no-cache       # Clean build từ đầu
echo   %~0 --monitoring             # Build với monitoring
echo   %~0 --all                    # Build tất cả services
goto :eof

REM Parse command line arguments
set CLEAN=false
set NO_CACHE=false
set MONITORING=false
set CACHING=false
set PROXY=false
set PROFILES=

:parse_args
if "%~1"=="" goto :args_done
if "%~1"=="--clean" (
    set CLEAN=true
    shift
    goto :parse_args
)
if "%~1"=="--no-cache" (
    set NO_CACHE=true
    shift
    goto :parse_args
)
if "%~1"=="--monitoring" (
    set MONITORING=true
    shift
    goto :parse_args
)
if "%~1"=="--caching" (
    set CACHING=true
    shift
    goto :parse_args
)
if "%~1"=="--proxy" (
    set PROXY=true
    shift
    goto :parse_args
)
if "%~1"=="--all" (
    set MONITORING=true
    set CACHING=true
    set PROXY=true
    shift
    goto :parse_args
)
if "%~1"=="--help" (
    call :show_usage
    exit /b 0
)
echo %RED%[ERROR]%NC% Unknown option: %~1
call :show_usage
exit /b 1

:args_done

REM Build profiles based on options
if "%MONITORING%"=="true" (
    if "%PROFILES%"=="" (
        set PROFILES=monitoring
    ) else (
        set PROFILES=%PROFILES%,monitoring
    )
)

if "%CACHING%"=="true" (
    if "%PROFILES%"=="" (
        set PROFILES=caching
    ) else (
        set PROFILES=%PROFILES%,caching
    )
)

if "%PROXY%"=="true" (
    if "%PROFILES%"=="" (
        set PROFILES=proxy
    ) else (
        set PROFILES=%PROFILES%,proxy
    )
)

echo %BLUE%[BUILD]%NC% ======================================
echo %BLUE%[BUILD]%NC% Content Scoring Service - Build Script
echo %BLUE%[BUILD]%NC% ======================================

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Docker is not running. Please start Docker first.
    exit /b 1
)

REM Check if docker-compose is available
docker-compose version >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo %RED%[ERROR]%NC% Docker Compose is not installed or not available.
        exit /b 1
    ) else (
        set COMPOSE_CMD=docker compose
    )
) else (
    set COMPOSE_CMD=docker-compose
)

echo %BLUE%[BUILD]%NC% Using compose command: !COMPOSE_CMD!

REM Clean up if requested
if "%CLEAN%"=="true" (
    echo %BLUE%[BUILD]%NC% Cleaning up old containers and images...
    
    REM Stop and remove containers
    !COMPOSE_CMD! down --volumes --remove-orphans
    
    REM Remove images
    for /f "tokens=3" %%i in ('docker images ^| findstr content-scoring') do (
        docker rmi -f %%i 2>nul
    )
    
    REM Prune unused resources
    docker system prune -f
    
    echo %GREEN%[SUCCESS]%NC% Cleanup completed
)

REM Create necessary directories
echo %BLUE%[BUILD]%NC% Creating necessary directories...
if not exist "logs" mkdir logs
if not exist "nginx" mkdir nginx
if not exist "monitoring" mkdir monitoring

REM Create nginx config if it doesn't exist
if "%PROXY%"=="true" (
    if not exist "nginx\nginx.conf" (
        echo %BLUE%[BUILD]%NC% Creating NGINX configuration...
        (
        echo events {
        echo     worker_connections 1024;
        echo }
        echo.
        echo http {
        echo     upstream content_scoring {
        echo         server content-scoring-service:5001;
        echo     }
        echo.
        echo     server {
        echo         listen 80;
        echo         server_name localhost;
        echo.
        echo         location / {
        echo             proxy_pass http://content_scoring;
        echo             proxy_set_header Host $host;
        echo             proxy_set_header X-Real-IP $remote_addr;
        echo             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        echo             proxy_set_header X-Forwarded-Proto $scheme;
        echo         }
        echo.
        echo         location /health {
        echo             proxy_pass http://content_scoring/health;
        echo             access_log off;
        echo         }
        echo     }
        echo }
        ) > nginx\nginx.conf
    )
)

REM Build the Docker image
echo %BLUE%[BUILD]%NC% Building Docker image...

set BUILD_ARGS=
if "%NO_CACHE%"=="true" (
    set BUILD_ARGS=--no-cache
)

REM Build the main service
docker build %BUILD_ARGS% -t content-scoring-service .
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Docker image build failed
    exit /b 1
)

echo %GREEN%[SUCCESS]%NC% Docker image built successfully

REM Start services with docker-compose
echo %BLUE%[BUILD]%NC% Starting services with Docker Compose...

set COMPOSE_ARGS=
if not "%PROFILES%"=="" (
    for %%a in (%PROFILES:,= %) do (
        set COMPOSE_ARGS=!COMPOSE_ARGS! --profile %%a
    )
)

echo %BLUE%[BUILD]%NC% Starting containers with profiles: %PROFILES%
!COMPOSE_CMD! up -d !COMPOSE_ARGS!
if errorlevel 1 (
    echo %RED%[ERROR]%NC% Failed to start services
    exit /b 1
)

echo %GREEN%[SUCCESS]%NC% All services started successfully

REM Wait for health check
echo %BLUE%[BUILD]%NC% Waiting for service health check...
timeout /t 10 /nobreak >nul

REM Check if the main service is healthy
set /a counter=0
:health_check_loop
set /a counter+=1
curl -s http://localhost:5001/health >nul 2>&1
if errorlevel 1 (
    if !counter! lss 30 (
        echo %BLUE%[BUILD]%NC% Waiting for service to be ready... (!counter!/30)
        timeout /t 2 /nobreak >nul
        goto :health_check_loop
    ) else (
        echo %YELLOW%[WARNING]%NC% Service health check timeout. Please check logs manually.
    )
) else (
    echo %GREEN%[SUCCESS]%NC% Service is healthy and ready!
)

REM Show service status
echo %BLUE%[BUILD]%NC% Service Status:
!COMPOSE_CMD! ps

REM Show useful URLs
echo %BLUE%[BUILD]%NC% ======================================
echo %GREEN%[SUCCESS]%NC% Build completed successfully!
echo %BLUE%[BUILD]%NC% ======================================
echo %BLUE%[BUILD]%NC% Available endpoints:
echo %BLUE%[BUILD]%NC% • Main Service: http://localhost:5001
echo %BLUE%[BUILD]%NC% • API Documentation: http://localhost:5001/docs
echo %BLUE%[BUILD]%NC% • Health Check: http://localhost:5001/health
echo %BLUE%[BUILD]%NC% • Metrics: http://localhost:5001/metrics

if "%MONITORING%"=="true" (
    echo %BLUE%[BUILD]%NC% • Prometheus: http://localhost:9090
    echo %BLUE%[BUILD]%NC% • Grafana: http://localhost:3100 (admin/admin123)
)

if "%CACHING%"=="true" (
    echo %BLUE%[BUILD]%NC% • Redis: localhost:6379
)

if "%PROXY%"=="true" (
    echo %BLUE%[BUILD]%NC% • NGINX Proxy: http://localhost:80
)

echo %BLUE%[BUILD]%NC% ======================================
echo %BLUE%[BUILD]%NC% To view logs: !COMPOSE_CMD! logs -f content-scoring-service
echo %BLUE%[BUILD]%NC% To stop services: !COMPOSE_CMD! down
echo %BLUE%[BUILD]%NC% ======================================

exit /b 0