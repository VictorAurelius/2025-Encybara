#!/bin/bash

# Enhanced Docker Build Script with Download Monitoring
# For Content Scoring Service with Process Tracking for Large Downloads (>50MB)

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
DEFAULT_IMAGE_NAME="content-scoring-service"
DEFAULT_TAG="latest"
SIZE_THRESHOLD_MB=50
BUILD_LOG_FILE="/tmp/docker-build-$(date +%Y%m%d-%H%M%S).log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
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

print_header() {
    echo -e "${PURPLE}================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}================================${NC}"
}

# Function to monitor Docker build progress and extract download information
monitor_build_progress() {
    local dockerfile="$1"
    local image_name="$2"
    local tag="$3"
    local context="$4"
    
    print_header "BUILDING DOCKER IMAGE WITH DOWNLOAD MONITORING"
    print_status "Image: ${image_name}:${tag}"
    print_status "Dockerfile: ${dockerfile}"
    print_status "Context: ${context}"
    print_status "Size threshold: ${SIZE_THRESHOLD_MB}MB"
    print_status "Build log: ${BUILD_LOG_FILE}"
    echo ""
    
    # Start time tracking
    local start_time=$(date +%s)
    
    # Build the Docker image with progress monitoring
    print_status "Starting Docker build process..."
    
    {
        echo "=== DOCKER BUILD LOG ===" 
        echo "Started at: $(date)"
        echo "Image: ${image_name}:${tag}"
        echo "Dockerfile: ${dockerfile}"
        echo "Threshold: ${SIZE_THRESHOLD_MB}MB"
        echo ""
    } > "$BUILD_LOG_FILE"
    
    # Use docker build with progress tracking
    docker build \
        --progress=plain \
        --no-cache \
        -f "$dockerfile" \
        -t "${image_name}:${tag}" \
        "$context" 2>&1 | tee -a "$BUILD_LOG_FILE" | while IFS= read -r line; do
        
        # Real-time output with progress indication
        echo "$line"
        
        # Track large download patterns
        if [[ "$line" =~ "LARGE DOWNLOAD" ]] || [[ "$line" =~ "Downloading" ]] && [[ "$line" =~ "MB" ]]; then
            echo -e "${CYAN}🔍 [MONITOR] Large download detected: $line${NC}"
        fi
        
        # Track pip install progress
        if [[ "$line" =~ "pip install" ]] || [[ "$line" =~ "Collecting" ]]; then
            echo -e "${YELLOW}📦 [MONITOR] Package installation: $line${NC}"
        fi
        
        # Track spaCy model downloads
        if [[ "$line" =~ "spacy download" ]] || [[ "$line" =~ "spaCy model" ]]; then
            echo -e "${PURPLE}🧠 [MONITOR] spaCy model operation: $line${NC}"
        fi
        
        # Track layer caching
        if [[ "$line" =~ "CACHED" ]]; then
            echo -e "${GREEN}⚡ [MONITOR] Using cached layer${NC}"
        fi
    done
    
    # Check if build was successful
    local build_exit_code=${PIPESTATUS[0]}
    local end_time=$(date +%s)
    local build_duration=$((end_time - start_time))
    
    echo "" >> "$BUILD_LOG_FILE"
    echo "Build completed at: $(date)" >> "$BUILD_LOG_FILE"
    echo "Build duration: ${build_duration}s" >> "$BUILD_LOG_FILE"
    echo "Exit code: $build_exit_code" >> "$BUILD_LOG_FILE"
    
    if [ $build_exit_code -eq 0 ]; then
        print_success "Docker build completed successfully!"
        print_status "Build duration: ${build_duration}s"
        
        # Get image size
        local image_size=$(docker images "${image_name}:${tag}" --format "{{.Size}}")
        print_status "Final image size: $image_size"
        
        return 0
    else
        print_error "Docker build failed with exit code: $build_exit_code"
        return $build_exit_code
    fi
}

# Function to analyze build log for large downloads
analyze_build_log() {
    local log_file="$1"
    
    print_header "DOWNLOAD ANALYSIS REPORT"
    
    if [ ! -f "$log_file" ]; then
        print_warning "Build log file not found: $log_file"
        return 1
    fi
    
    echo ""
    print_status "Analyzing build log for large downloads (>${SIZE_THRESHOLD_MB}MB)..."
    echo ""
    
    # Extract download tracker reports
    if grep -q "LARGE DOWNLOAD SUMMARY REPORT" "$log_file"; then
        echo -e "${CYAN}📊 Download Tracker Summary:${NC}"
        sed -n '/LARGE DOWNLOAD SUMMARY REPORT/,/^$/p' "$log_file" | head -50
        echo ""
    fi
    
    # Look for pip package downloads with size info
    echo -e "${YELLOW}📦 Package Download Activity:${NC}"
    grep -i "downloading\|collecting\|installing" "$log_file" | head -20 || echo "No package download activity found"
    echo ""
    
    # Look for spaCy model downloads
    echo -e "${PURPLE}🧠 spaCy Model Activity:${NC}"
    grep -i "spacy\|model" "$log_file" | head -10 || echo "No spaCy model activity found"
    echo ""
    
    # Calculate total build time from log
    local start_line=$(grep "Started at:" "$log_file" | head -1)
    local end_line=$(grep "Build completed at:" "$log_file" | head -1)
    
    if [ -n "$start_line" ] && [ -n "$end_line" ]; then
        echo -e "${GREEN}⏱️  Build Timing:${NC}"
        echo "  $start_line"
        echo "  $end_line"
        
        local duration_line=$(grep "Build duration:" "$log_file" | head -1)
        if [ -n "$duration_line" ]; then
            echo "  $duration_line"
        fi
        echo ""
    fi
    
    # Show any warnings or errors related to downloads
    echo -e "${RED}⚠️  Download Warnings/Errors:${NC}"
    grep -i "warning\|error\|failed" "$log_file" | grep -i "download\|install\|model" | head -10 || echo "No download-related warnings found"
    echo ""
    
    print_status "Full build log saved to: $log_file"
    echo ""
}

# Function to test the built image
test_built_image() {
    local image_name="$1"
    local tag="$2"
    
    print_header "TESTING BUILT IMAGE"
    
    print_status "Testing image: ${image_name}:${tag}"
    
    # Test basic container startup
    print_status "Testing container startup..."
    local container_id=$(docker run -d -p 5001:5001 "${image_name}:${tag}")
    
    if [ $? -eq 0 ]; then
        print_success "Container started successfully: $container_id"
        
        # Wait a bit for startup
        sleep 10
        
        # Test health endpoint
        print_status "Testing health endpoint..."
        if curl -f http://localhost:5001/health > /dev/null 2>&1; then
            print_success "Health check passed!"
        else
            print_warning "Health check failed or endpoint not ready"
        fi
        
        # Clean up test container
        print_status "Cleaning up test container..."
        docker stop "$container_id" > /dev/null 2>&1
        docker rm "$container_id" > /dev/null 2>&1
        print_success "Test container cleaned up"
        
    else
        print_error "Failed to start test container"
        return 1
    fi
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Enhanced Docker build script with download monitoring for files >50MB"
    echo ""
    echo "Options:"
    echo "  -n, --name NAME          Docker image name (default: $DEFAULT_IMAGE_NAME)"
    echo "  -t, --tag TAG            Docker image tag (default: $DEFAULT_TAG)"
    echo "  -f, --file DOCKERFILE    Dockerfile to use (default: Dockerfile)"
    echo "  -c, --context CONTEXT    Build context (default: current directory)"
    echo "  -s, --size-threshold MB  Size threshold in MB (default: $SIZE_THRESHOLD_MB)"
    echo "  --no-test               Skip image testing after build"
    echo "  --no-analysis           Skip build log analysis"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Build with defaults"
    echo "  $0 -n myapp -t v1.0                 # Custom name and tag"
    echo "  $0 -f Dockerfile.optimized          # Use optimized Dockerfile"
    echo "  $0 -s 100                           # Track downloads >100MB"
    echo ""
}

# Main function
main() {
    local image_name="$DEFAULT_IMAGE_NAME"
    local tag="$DEFAULT_TAG"
    local dockerfile="Dockerfile"
    local context="$PROJECT_DIR"
    local size_threshold="$SIZE_THRESHOLD_MB"
    local run_tests=true
    local run_analysis=true
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -n|--name)
                image_name="$2"
                shift 2
                ;;
            -t|--tag)
                tag="$2"
                shift 2
                ;;
            -f|--file)
                dockerfile="$2"
                shift 2
                ;;
            -c|--context)
                context="$2"
                shift 2
                ;;
            -s|--size-threshold)
                size_threshold="$2"
                SIZE_THRESHOLD_MB="$2"
                shift 2
                ;;
            --no-test)
                run_tests=false
                shift
                ;;
            --no-analysis)
                run_analysis=false
                shift
                ;;
            -h|--help)
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
    
    # Convert relative paths to absolute
    if [[ ! "$dockerfile" = /* ]]; then
        dockerfile="$PROJECT_DIR/$dockerfile"
    fi
    
    if [[ ! "$context" = /* ]]; then
        context="$(cd "$context" && pwd)"
    fi
    
    # Validate inputs
    if [ ! -f "$dockerfile" ]; then
        print_error "Dockerfile not found: $dockerfile"
        exit 1
    fi
    
    if [ ! -d "$context" ]; then
        print_error "Build context directory not found: $context"
        exit 1
    fi
    
    # Ensure download tracker script exists and is executable
    local tracker_script="$PROJECT_DIR/scripts/download-tracker.py"
    if [ ! -f "$tracker_script" ]; then
        print_error "Download tracker script not found: $tracker_script"
        exit 1
    fi
    chmod +x "$tracker_script"
    
    # Start the build process
    print_header "ENHANCED DOCKER BUILD WITH DOWNLOAD MONITORING"
    echo ""
    
    # Monitor and build
    if monitor_build_progress "$dockerfile" "$image_name" "$tag" "$context"; then
        
        # Analyze build log
        if [ "$run_analysis" = true ]; then
            echo ""
            analyze_build_log "$BUILD_LOG_FILE"
        fi
        
        # Test the built image
        if [ "$run_tests" = true ]; then
            echo ""
            test_built_image "$image_name" "$tag"
        fi
        
        echo ""
        print_success "Build process completed successfully!"
        print_status "Image: ${image_name}:${tag}"
        print_status "Build log: $BUILD_LOG_FILE"
        
    else
        print_error "Build process failed!"
        echo ""
        print_status "Check the build log for details: $BUILD_LOG_FILE"
        exit 1
    fi
}

# Run main function with all arguments
main "$@"