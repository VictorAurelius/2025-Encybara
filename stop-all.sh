#!/bin/bash

# Stop All Encybara Services
# Stops all services started with docker-compose.all.yml

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[STOP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running."
    exit 1
fi

# Check if docker-compose.all.yml exists
if [ ! -f "docker-compose.all.yml" ]; then
    print_error "docker-compose.all.yml not found!"
    print_error "Please run this script from the project root directory."
    exit 1
fi

print_status "======================================"
print_status "Stopping All Encybara Services"
print_status "======================================"

# Parse command line arguments
REMOVE_VOLUMES=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --volumes|-v)
            REMOVE_VOLUMES=true
            shift
            ;;
        --help|-h)
            echo "Stop All Encybara Services"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -v, --volumes       Remove volumes (database data will be lost!)"
            echo "  -h, --help          Show this help"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Stop services
print_status "Stopping services..."

if [ "$REMOVE_VOLUMES" = true ]; then
    print_status "Removing volumes (database data will be deleted)..."
    docker-compose -f docker-compose.all.yml down -v
else
    docker-compose -f docker-compose.all.yml down
fi

print_success "======================================"
print_success "All services stopped successfully!"
print_success "======================================"

echo ""
print_status "To start services again:"
print_status "• ./start-all.sh"

if [ "$REMOVE_VOLUMES" = false ]; then
    echo ""
    print_status "To remove volumes (database data):"
    print_status "• ./stop-all.sh --volumes"
fi
