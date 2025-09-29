# Use Python 3.10 slim image as base
FROM python:3.10-slim

# Set working directory
WORKDIR /app

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy production requirements first for better caching
COPY requirements-prod.txt .

# Install Python dependencies with optimization for container build
RUN pip install --upgrade pip && \
    pip install --prefer-binary --only-binary=all -r requirements-prod.txt || \
    pip install -r requirements-prod.txt

# Download spaCy English model (with error handling)
RUN python -m spacy download en_core_web_sm || echo "Warning: spaCy model download failed, will retry at runtime"

# Copy startup script
COPY scripts/docker-entrypoint.sh ./scripts/
RUN chmod +x ./scripts/docker-entrypoint.sh

# Copy application code
COPY app/ ./app/

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 5001

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:5001/health || exit 1

# Run the application with entrypoint script
CMD ["./scripts/docker-entrypoint.sh"]