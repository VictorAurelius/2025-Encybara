#!/bin/bash

# Start All Encybara Services
# Starts all services using docker-compose.all.yml

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[START]${NC} $1"
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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

print_status "======================================"
print_status "Starting All Encybara Services"
print_status "======================================"

# Check if docker-compose.all.yml exists
if [ ! -f "docker-compose.all.yml" ]; then
    print_error "docker-compose.all.yml not found!"
    print_error "Please run this script from the project root directory."
    exit 1
fi

# Parse command line arguments
DETACH="-d"
REBUILD=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --foreground|-f)
            DETACH=""
            shift
            ;;
        --rebuild)
            REBUILD=true
            shift
            ;;
        --help|-h)
            echo "Start All Encybara Services"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -f, --foreground    Run in foreground (with logs)"
            echo "  --rebuild           Rebuild images before starting"
            echo "  -h, --help          Show this help"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Rebuild if requested
if [ "$REBUILD" = true ]; then
    print_status "Rebuilding all services..."
    docker-compose -f docker-compose.all.yml build
fi

# Start services
print_status "Starting services..."
docker-compose -f docker-compose.all.yml up $DETACH

if [ -n "$DETACH" ]; then
    print_success "======================================"
    print_success "All services started successfully!"
    print_success "======================================"

    echo ""
    print_status "Service URLs:"
    print_status "• Content Scoring Service: http://localhost:5001"
    print_status "• Pronunciation Assessment: http://localhost:5000"
    print_status "• Backend API: http://localhost:8080"
    print_status "• API Documentation: http://localhost:8080/swagger-ui.html"

    echo ""
    print_status "Check service health:"
    print_status "• curl http://localhost:5001/health"
    print_status "• curl http://localhost:5000/health"
    print_status "• curl http://localhost:8080/actuator/health"

    echo ""
    print_status "View logs:"
    print_status "• docker-compose -f docker-compose.all.yml logs -f"
    print_status "• docker-compose -f docker-compose.all.yml logs -f [service-name]"

    echo ""
    print_status "Stop services:"
    print_status "• ./stop-all.sh"
    print_status "• docker-compose -f docker-compose.all.yml down"

    print_success "======================================"
fi
