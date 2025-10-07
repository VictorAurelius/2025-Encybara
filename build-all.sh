#!/bin/bash

# Build All Services Script
# Builds content-scoring-service, pronunciation-assessment-service, and backend-service with one command

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[BUILD ALL]${NC} $1"
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
    echo "Build All Services Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --clean          Clean build (remove old containers and images)"
    echo "  --no-cache       Build without cache"
    echo "  --tunnel         Include Ngrok tunnel for content-scoring-service"
    echo "  --help           Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                      # Build all services normally"
    echo "  $0 --clean --no-cache   # Clean rebuild everything"
    echo "  $0 --tunnel             # Build with Ngrok tunnel"
}

# Parse command line arguments
CLEAN=false
NO_CACHE=false
TUNNEL=false

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
        --tunnel)
            TUNNEL=true
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

print_status "======================================"
print_status "Building All Encybara Services"
print_status "======================================"
print_status "Services to build:"
print_status "  1. CMS Service (React Frontend)"
print_status "  2. Content Scoring Service"
print_status "  3. Pronunciation Assessment Service"
print_status "  4. Backend Service"
print_status "======================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Build CMS Service (React Frontend) first
print_status "Building CMS Service (React Frontend)..."
cd cms-service

# Check if CMS already built and skip if not clean build
if [ "$CLEAN" != true ] && [ -d "../deployment/cms/build/react-build" ] && [ -f "../deployment/cms/build/react-build/index.html" ] && [ -d "../deployment/cms/build/react-build/static" ]; then
    print_status "CMS already built (found complete React build), skipping..."
    cd ..
else
    print_status "Building CMS from source..."
    
    # Check if node_modules exists and package.json has changed
    if [ "$CLEAN" = true ] || [ ! -d "node_modules" ]; then
        print_status "Installing CMS dependencies..."
        if command -v npm > /dev/null; then
            npm install --legacy-peer-deps
        elif command -v yarn > /dev/null; then
            yarn install
        else
            print_error "Neither npm nor yarn found. Please install Node.js first."
            exit 1
        fi
    fi

    # Build React app
    print_status "Building React application..."
    if command -v npm > /dev/null; then
        npm run build
    elif command -v yarn > /dev/null; then
        yarn build
    fi

    # Copy build artifacts to deployment folder
    if [ -d "build" ]; then
        print_status "Copying React build to deployment folder..."
        mkdir -p ../deployment/cms/build
        rm -rf ../deployment/cms/build/react-build
        cp -r build ../deployment/cms/build/react-build
        print_success "CMS build artifacts copied to deployment folder"
    else
        print_error "React build failed - no build directory found"
        exit 1
    fi

    cd ..
fi

# Build content-scoring-service
print_status "Building content-scoring-service..."
cd content-scoring-service

BUILD_ARGS=""
if [ "$CLEAN" = true ]; then
    BUILD_ARGS="$BUILD_ARGS --clean"
fi

if [ "$NO_CACHE" = true ]; then
    BUILD_ARGS="$BUILD_ARGS --no-cache"
fi

if [ "$TUNNEL" = true ]; then
    BUILD_ARGS="$BUILD_ARGS --tunnel"
fi

# Check if content-scoring-service already built
if [ "$CLEAN" != true ] && docker images | grep -q "^content-scoring-service.*latest"; then
    print_status "Content-scoring-service image already exists, skipping build..."
else
    print_status "Building content-scoring-service with standard build script..."
    
    if [ -f "./build.sh" ]; then
        print_status "Using standard build.sh for content-scoring-service..."
        ./build.sh $BUILD_ARGS
        print_success "Content-scoring-service built successfully with standard configuration!"
    elif [ -f "./quick-fix.sh" ]; then
        print_status "Using quick fix for content-scoring-service..."
        ./quick-fix.sh
    else
        print_error "No build.sh script found in content-scoring-service directory!"
        exit 1
    fi
fi

cd ..

# Build pronunciation-assessment-service
print_status "Building pronunciation-assessment-service..."
cd pronunciation-assessment-service

# Check if pronunciation-assessment-service already built
if [ "$CLEAN" != true ] && docker images | grep -q "^pronunciation-assessment-service.*latest"; then
    print_status "Pronunciation-assessment-service image already exists, skipping build..."
else
    if [ -f "./build.sh" ]; then
        BUILD_ARGS_PRON=""
        if [ "$CLEAN" = true ]; then
            BUILD_ARGS_PRON="$BUILD_ARGS_PRON --clean"
        fi
        if [ "$NO_CACHE" = true ]; then
            BUILD_ARGS_PRON="$BUILD_ARGS_PRON --no-cache"
        fi

        print_status "Building pronunciation-assessment-service with build script..."
        ./build.sh $BUILD_ARGS_PRON
    else
        print_warning "pronunciation-assessment-service build script not found, using docker-compose..."
        if [ "$NO_CACHE" = true ]; then
            docker-compose build --no-cache
        else
            docker-compose build
        fi
    fi
fi

cd ..

# Build backend-service
print_status "Building backend-service..."
cd backend-service

# Check if backend service already built - look for proper image name
if [ "$CLEAN" != true ] && (docker images | grep -q "^encybara-backend.*latest" || docker images | grep -q "^deployment.*backend.*latest"); then
    print_status "Backend service image already exists, skipping build..."
else
    print_status "Building backend-service (needs environment config)..."
    if [ -f "./build.sh" ]; then
        print_status "Building backend-service with build script..."
        ./build.sh
    else
        print_error "backend-service build script not found!"
        exit 1
    fi
fi

cd ..

print_success "======================================"
print_success "Build process completed!"
print_success "======================================"
print_status "Build Summary:"
# Check each service individually
CMS_STATUS="Unknown"
if [ -d "deployment/cms/build/react-build" ] && [ -f "deployment/cms/build/react-build/index.html" ]; then
    CMS_STATUS="✅ Built"
else
    CMS_STATUS="❌ Missing"
fi

CONTENT_STATUS="Unknown"  
if docker images | grep -q "^content-scoring-service.*latest"; then
    CONTENT_STATUS="✅ Available"
else
    CONTENT_STATUS="❌ Missing"
fi

PRONUNCIATION_STATUS="Unknown"
if docker images | grep -q "^pronunciation-assessment-service.*latest"; then
    PRONUNCIATION_STATUS="✅ Available"  
else
    PRONUNCIATION_STATUS="❌ Missing"
fi

BACKEND_STATUS="Unknown"
if docker images | grep -q "^encybara-backend.*latest\|^deployment.*backend.*latest"; then
    BACKEND_STATUS="✅ Available"
else
    BACKEND_STATUS="❌ Missing"  
fi

print_status "• CMS Service: $CMS_STATUS"
print_status "• Content Scoring: $CONTENT_STATUS"
print_status "• Pronunciation: $PRONUNCIATION_STATUS"  
print_status "• Backend Service: $BACKEND_STATUS"
print_success "======================================"
print_status "Service URLs (when running):"
print_status "• CMS Frontend: http://localhost:3000"
print_status "• Content Scoring Service: http://localhost:5001"
print_status "• Pronunciation Assessment Service: http://localhost:5000"
print_status "• Backend API: http://localhost:8080"
print_status "• API Documentation: http://localhost:8080/swagger-ui.html"
print_status "• Nginx Gateway: http://localhost:80 (with --profile gateway)"

if [ "$TUNNEL" = true ]; then
    print_status "• Ngrok Tunnel Interface: http://localhost:4040"
fi

print_status ""
print_status "To start all services:"
print_status "• docker-compose -f docker-compose.all.yml up -d"
print_status ""
print_status "Or start individually:"
print_status "• Content Scoring: docker-compose -f content-scoring-service/docker-compose.yml up -d"
print_status "• Pronunciation: docker-compose -f pronunciation-assessment-service/docker-compose.yml up -d"
print_status "• Backend + DB: docker-compose -f deployment/docker-compose.yml up -d"
print_status "• CMS + Nginx: docker-compose -f deployment/docker-compose.yml up -d nginx"

print_status ""
print_status "To test services:"
print_status "• Content Scoring: cd content-scoring-service && python -m pytest tests/ -v"
print_status "• Pronunciation: cd pronunciation-assessment-service && python3 test_simple.py"
print_status "• Backend Integration: cd backend-service && ./test-content-scoring.sh"

print_status ""
print_status "To view logs:"
print_status "• Content Scoring: docker-compose -f content-scoring-service/docker-compose.yml logs -f"
print_status "• Pronunciation: docker-compose -f pronunciation-assessment-service/docker-compose.yml logs -f"
print_status "• Backend: docker-compose -f deployment/docker-compose/docker-compose.yml logs -f backend"

print_success "======================================"
print_success "Note: Services are configured to use localhost URLs by default"
print_success "Check application.properties for service URL configuration"
print_success "======================================"