#!/bin/bash

# Fast Docker build script for Content Scoring Service
set -e

echo "⚡ Fast Docker Build for Content Scoring Service"

# Default configuration
DOCKERFILE="Dockerfile.ultra-fast"
IMAGE_NAME="content-scoring-service"
TAG="ultra-fast"
BUILD_TYPE="ultra-fast"
NO_CACHE=""
PARALLEL_JOBS=4
PROGRESS="--progress=plain"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --ultra-fast)
            DOCKERFILE="Dockerfile.ultra-fast"
            TAG="ultra-fast"
            BUILD_TYPE="ultra-fast"
            echo "⚡ Using ultra-fast build (NO ML - under 1 minute)"
            shift
            ;;
        --minimal)
            DOCKERFILE="Dockerfile.fast"
            TAG="minimal"
            BUILD_TYPE="minimal"
            echo "📦 Using minimal build (basic ML)"
            shift
            ;;
        --cache)
            DOCKERFILE="Dockerfile.cache"
            TAG="cached"
            BUILD_TYPE="cached"
            echo "🔄 Using cache-optimized build"
            shift
            ;;
        --full)
            DOCKERFILE="Dockerfile.optimized"
            TAG="full"
            BUILD_TYPE="full"
            echo "🏗️ Using full build (slowest but complete)"
            shift
            ;;
        --tag)
            TAG="$2"
            shift 2
            ;;
        --no-cache)
            NO_CACHE="--no-cache"
            echo "🚫 Building without cache"
            shift
            ;;
        --parallel)
            PARALLEL_JOBS="$2"
            shift 2
            ;;
        --quiet)
            PROGRESS="--progress=quiet"
            echo "🔇 Quiet build mode"
            shift
            ;;
        --verbose)
            PROGRESS="--progress=plain"
            echo "📢 Verbose build mode"
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --ultra-fast  Use ultra-fast Dockerfile (NO ML - default, <1 min)"
            echo "  --minimal     Use minimal Dockerfile (basic ML, ~2-3 min)"
            echo "  --cache       Use cache-optimized Dockerfile (~3-5 min)"
            echo "  --full        Use full production Dockerfile (~10-15 min)"
            echo "  --tag TAG     Set custom image tag"
            echo "  --no-cache    Build without using cache"
            echo "  --parallel N  Set parallel jobs (default: 4)"
            echo "  --quiet       Quiet build output"
            echo "  --verbose     Verbose build output (default)"
            echo "  --help, -h    Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                    # Ultra-fast build (NO ML)"
            echo "  $0 --minimal          # Minimal build (basic ML)"
            echo "  $0 --cache            # Cache-optimized build"
            echo "  $0 --full --no-cache  # Full clean build"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Change to project root
cd "$(dirname "$0")/.."

# Verify required files
echo "🔍 Checking required files..."
if [ ! -f "$DOCKERFILE" ]; then
    echo "❌ $DOCKERFILE not found"
    exit 1
fi

if [ ! -f "requirements-minimal.txt" ]; then
    echo "❌ requirements-minimal.txt not found"
    exit 1
fi

# Show build information
echo ""
echo "🏗️ Build Configuration:"
echo "   Dockerfile: $DOCKERFILE"
echo "   Image:      $IMAGE_NAME:$TAG"
echo "   Build Type: $BUILD_TYPE"
echo "   Cache:      $([ -n "$NO_CACHE" ] && echo "Disabled" || echo "Enabled")"
echo "   Jobs:       $PARALLEL_JOBS"
echo ""

# Start build with timing
START_TIME=$(date +%s)

echo "🚀 Starting Docker build..."
echo "📊 Progress tracking enabled..."

# Enable BuildKit for better progress display
export DOCKER_BUILDKIT=1

docker build \
    $NO_CACHE \
    $PROGRESS \
    --build-arg BUILD_TYPE=$BUILD_TYPE \
    --build-arg BUILDKIT_INLINE_CACHE=1 \
    -f $DOCKERFILE \
    -t $IMAGE_NAME:$TAG \
    .

# Calculate build time
END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))

echo ""
echo "✅ Build completed successfully!"
echo "⏱️  Build time: ${BUILD_TIME}s"
echo ""

# Show image info
echo "📊 Image Information:"
docker images $IMAGE_NAME:$TAG --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

echo ""
echo "🚀 Quick start commands:"
echo "  # Run container:"
echo "  docker run -p 5001:5001 $IMAGE_NAME:$TAG"
echo ""
echo "  # Run with docker-compose:"
echo "  docker-compose up"
echo ""
echo "  # Test container:"
echo "  ./scripts/test-container.sh --tag $TAG"