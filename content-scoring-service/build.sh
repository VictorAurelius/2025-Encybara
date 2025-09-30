#!/bin/bash

# Content Scoring Service - Build Script
# Script để build tất cả container với một câu lệnh

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[BUILD]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Content Scoring Service - Build Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --clean          Xóa tất cả images và containers cũ trước khi build"
    echo "  --no-cache       Build không sử dụng cache"
    echo "  --monitoring     Build kèm theo monitoring stack (Prometheus + Grafana)"
    echo "  --caching        Build kèm theo Redis caching"
    echo "  --proxy          Build kèm theo NGINX reverse proxy"
    echo "  --all            Build tất cả services (monitoring + caching + proxy)"
    echo "  --help           Hiển thị help này"
    echo ""
    echo "Examples:"
    echo "  $0                          # Build service cơ bản"
    echo "  $0 --clean --no-cache       # Clean build từ đầu"
    echo "  $0 --monitoring             # Build với monitoring"
    echo "  $0 --all                    # Build tất cả services"
}

# Parse command line arguments
CLEAN=false
NO_CACHE=false
MONITORING=false
CACHING=false
PROXY=false
PROFILES=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN=true
            shift
            ;;
        --no-cache)
            NO_CACHE=true
            shift
            ;;
        --monitoring)
            MONITORING=true
            shift
            ;;
        --caching)
            CACHING=true
            shift
            ;;
        --proxy)
            PROXY=true
            shift
            ;;
        --all)
            MONITORING=true
            CACHING=true
            PROXY=true
            shift
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Build profiles based on options
if [ "$MONITORING" = true ]; then
    PROFILES="$PROFILES,monitoring"
fi

if [ "$CACHING" = true ]; then
    PROFILES="$PROFILES,caching"
fi

if [ "$PROXY" = true ]; then
    PROFILES="$PROFILES,proxy"
fi

# Remove leading comma
PROFILES=${PROFILES#,}

print_status "======================================"
print_status "Content Scoring Service - Build Script"
print_status "======================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose > /dev/null 2>&1 && ! docker compose version > /dev/null 2>&1; then
    print_error "Docker Compose is not installed or not available."
    exit 1
fi

# Use docker compose if available, otherwise use docker-compose
COMPOSE_CMD="docker-compose"
if docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
fi

print_status "Using compose command: $COMPOSE_CMD"

# Clean up if requested
if [ "$CLEAN" = true ]; then
    print_status "Cleaning up old containers and images..."
    
    # Stop and remove containers
    $COMPOSE_CMD down --volumes --remove-orphans || true
    
    # Remove images
    docker images | grep content-scoring | awk '{print $3}' | xargs -r docker rmi -f || true
    
    # Prune unused resources
    docker system prune -f
    
    print_success "Cleanup completed"
fi

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p logs
mkdir -p nginx
mkdir -p monitoring

# Create nginx config if it doesn't exist
if [ "$PROXY" = true ] && [ ! -f "nginx/nginx.conf" ]; then
    print_status "Creating NGINX configuration..."
    cat > nginx/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream content_scoring {
        server content-scoring-service:5001;
    }

    server {
        listen 80;
        server_name localhost;

        location / {
            proxy_pass http://content_scoring;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /health {
            proxy_pass http://content_scoring/health;
            access_log off;
        }
    }
}
EOF
fi

# Build the Docker image
print_status "Building Docker image..."

BUILD_ARGS=""
if [ "$NO_CACHE" = true ]; then
    BUILD_ARGS="--no-cache"
fi

# Build the main service
docker build $BUILD_ARGS -t content-scoring-service .

print_success "Docker image built successfully"

# Start services with docker-compose
print_status "Starting services with Docker Compose..."

# Handle profiles properly for different docker-compose versions
COMPOSE_ARGS=""
if [ -n "$PROFILES" ]; then
    # Replace commas with spaces for profiles
    PROFILES_FORMATTED=$(echo "$PROFILES" | tr ',' ' ')
    for profile in $PROFILES_FORMATTED; do
        COMPOSE_ARGS="$COMPOSE_ARGS --profile $profile"
    done
fi

print_status "Starting containers with profiles: ${PROFILES:-none}"

# Try with profiles first, fallback to basic compose if profiles not supported
if [ -n "$COMPOSE_ARGS" ]; then
    print_status "Attempting to start with profiles..."
    if ! $COMPOSE_CMD up -d $COMPOSE_ARGS 2>/dev/null; then
        print_warning "Profiles not supported in this docker-compose version"
        print_status "Starting basic service only..."
        $COMPOSE_CMD up -d content-scoring-service
    fi
else
    $COMPOSE_CMD up -d
fi

print_success "All services started successfully"

# Wait for health check
print_status "Waiting for service health check..."
sleep 10

# Check if the main service is healthy
for i in {1..30}; do
    if curl -s http://localhost:5001/health > /dev/null 2>&1; then
        print_success "Service is healthy and ready!"
        break
    else
        print_status "Waiting for service to be ready... ($i/30)"
        sleep 2
    fi
    
    if [ $i -eq 30 ]; then
        print_warning "Service health check timeout. Please check logs manually."
    fi
done

# Show service status
print_status "Service Status:"
$COMPOSE_CMD ps

# Show useful URLs
print_status "======================================"
print_success "Build completed successfully!"
print_status "======================================"
print_status "Available endpoints:"
print_status "• Main Service: http://localhost:5001"
print_status "• API Documentation: http://localhost:5001/docs"
print_status "• Health Check: http://localhost:5001/health"
print_status "• Metrics: http://localhost:5001/metrics"

if [ "$MONITORING" = true ]; then
    print_status "• Prometheus: http://localhost:9090"
    print_status "• Grafana: http://localhost:3100 (admin/admin123)"
fi

if [ "$CACHING" = true ]; then
    print_status "• Redis: localhost:6379"
fi

if [ "$PROXY" = true ]; then
    print_status "• NGINX Proxy: http://localhost:80"
fi

print_status "======================================"
print_status "To view logs: $COMPOSE_CMD logs -f content-scoring-service"
print_status "To stop services: $COMPOSE_CMD down"
print_status "======================================"