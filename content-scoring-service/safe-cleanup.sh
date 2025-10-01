#!/bin/bash

# Safe cleanup script for content-scoring-service
# This script only removes containers and images related to THIS project
# Usage: ./safe-cleanup.sh [--force]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[CLEANUP]${NC} $1"
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

# Parse arguments
FORCE=false
if [ "$1" = "--force" ]; then
    FORCE=true
fi

print_status "=========================================="
print_status "Safe Cleanup: Content Scoring Service"
print_status "=========================================="

# Warning
if [ "$FORCE" = false ]; then
    print_warning "⚠️  This will remove content-scoring-service containers and images"
    print_warning "⚠️  Other Docker projects will NOT be affected"
    echo -n "Continue? (y/N): "
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        print_status "Cleanup cancelled by user"
        exit 0
    fi
fi

# Use docker compose if available, otherwise use docker-compose
COMPOSE_CMD="docker-compose"
if docker compose version > /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
fi

print_status "Using compose command: $COMPOSE_CMD"

# Stop and remove containers managed by docker-compose
print_status "Stopping containers managed by docker-compose..."
$COMPOSE_CMD down --volumes --remove-orphans || true

# Remove specific containers by name (fallback)
print_status "Removing content-scoring containers by name..."
CONTAINERS=$(docker ps -a --filter "name=content-scoring" --format "{{.Names}}" | grep -E '^content-scoring' || true)
if [ -n "$CONTAINERS" ]; then
    echo "$CONTAINERS" | while read -r container; do
        print_status "Removing container: $container"
        docker rm -f "$container" || true
    done
else
    print_status "No content-scoring containers found"
fi

# Remove specific images
print_status "Removing content-scoring-service images..."
IMAGES=$(docker images --filter "reference=content-scoring-service*" --format "{{.ID}}" || true)
if [ -n "$IMAGES" ]; then
    echo "$IMAGES" | while read -r image; do
        print_status "Removing image: $image"
        docker rmi -f "$image" || true
    done
else
    print_status "No content-scoring-service images found"
fi

# Clean up orphaned volumes specific to this project
print_status "Cleaning up project-specific volumes..."
VOLUMES=$(docker volume ls --filter "name=content" --format "{{.Name}}" || true)
if [ -n "$VOLUMES" ]; then
    echo "$VOLUMES" | while read -r volume; do
        print_status "Removing volume: $volume"
        docker volume rm "$volume" || true
    done
else
    print_status "No content-related volumes found"
fi

# Show what's preserved
print_success "=========================================="
print_success "Safe cleanup completed!"
print_success "=========================================="
print_status "Removed: content-scoring-service containers and images"
print_success "Preserved: All other Docker projects and system resources"
print_warning "⚠️  Did NOT run 'docker system prune' to protect other projects"

# Show remaining Docker resources summary
print_status "Current Docker resource summary:"
echo "Containers: $(docker ps -a --format "{{.Names}}" | wc -l) total"
echo "Images: $(docker images --format "{{.Repository}}" | wc -l) total" 
echo "Volumes: $(docker volume ls --format "{{.Name}}" | wc -l) total"

print_status "To rebuild: ./build.sh --no-cache"