# Content Scoring Service Dockerfile
# Multi-stage build for optimized production image with download tracking

FROM python:3.10-slim as builder

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
  PYTHONUNBUFFERED=1 \
  PIP_NO_CACHE_DIR=1 \
  PIP_DISABLE_PIP_VERSION_CHECK=1

# Install system dependencies for building
RUN apt-get update && apt-get install -y \
  build-essential \
  curl \
  wget \
  bc \
  && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy requirements first for better caching
COPY requirements.txt .

# Install Python dependencies with enhanced download tracking
RUN echo "[BUILD STAGE] Installing Python dependencies with download tracking..." && \
  pip install -r requirements.txt --progress-bar pretty -v 2>&1 | tee /tmp/install.log | while IFS= read -r line; do \
  # Track large downloads (>10MB) \
  if echo "$line" | grep -E "Downloading.*\([0-9]+\.?[0-9]*\s*(MB|GB)\)" > /dev/null; then \
  size=$(echo "$line" | grep -oE "[0-9]+\.?[0-9]*\s*(MB|GB)" | head -1); \
  if echo "$size" | grep -E "[0-9]+\.?[0-9]*\s*GB" > /dev/null || \
  echo "$size" | grep -E "(1[0-9]|[2-9][0-9]|[0-9]{3,})\.?[0-9]*\s*MB" > /dev/null; then \
  echo "[LARGE DOWNLOAD] $line"; \
  echo "[LARGE DOWNLOAD] File size: $size - This may take a while..."; \
  else \
  echo "[DOWNLOAD] $line"; \
  fi; \
  # Track download progress \
  elif echo "$line" | grep -E "[0-9]+%.*[0-9]+\.?[0-9]*/[0-9]+\.?[0-9]*\s*(MB|KB|GB)" > /dev/null; then \
  progress=$(echo "$line" | grep -oE "[0-9]+%" | head -1); \
  sizes=$(echo "$line" | grep -oE "[0-9]+\.?[0-9]*/[0-9]+\.?[0-9]*\s*(MB|KB|GB)" | head -1); \
  echo "[DOWNLOAD PROGRESS] $progress - $sizes"; \
  # Track PyTorch downloads \
  elif echo "$line" | grep "torch.*\.whl" > /dev/null; then \
  echo "[PYTORCH DOWNLOAD] $line"; \
  # Track sentence-transformers downloads \
  elif echo "$line" | grep "sentence-transformers" > /dev/null; then \
  echo "[MODEL DOWNLOAD] $line"; \
  # Track general downloads \
  elif echo "$line" | grep "Downloading" > /dev/null && echo "$line" | grep "http" > /dev/null; then \
  echo "[DOWNLOAD] $line"; \
  # Track installation progress \
  elif echo "$line" | grep "Installing collected packages" > /dev/null; then \
  echo "[INSTALL] $line"; \
  elif echo "$line" | grep "Successfully installed" > /dev/null; then \
  echo "[SUCCESS] $line"; \
  # Track errors \
  elif echo "$line" | grep -E "(ERROR|Failed)" > /dev/null; then \
  echo "[ERROR] $line"; \
  else \
  echo "$line"; \
  fi; \
  done && \
  echo "[BUILD STAGE] Python dependencies installed successfully" && \
  echo "[BUILD STAGE] Verifying installations..." && \
  python -c "import uvicorn; print('Uvicorn installed successfully')" && \
  python -c "import fastapi; print('FastAPI installed successfully')" && \
  python -c "import sentence_transformers; print('Sentence transformers installed successfully')" && \
  echo "[BUILD STAGE] All dependencies verified"

# Download spaCy model with tracking (after dependencies are installed)
RUN echo "[BUILD STAGE] Downloading spaCy English model..." && \
  (python -m spacy download en_core_web_sm 2>&1 | while IFS= read -r line; do \
  if echo "$line" | grep -E "Downloading.*\([0-9]+\.?[0-9]*\s*(MB|KB)\)" > /dev/null; then \
  size=$(echo "$line" | grep -oE "[0-9]+\.?[0-9]*\s*(MB|KB)" | head -1); \
  if echo "$size" | grep -E "[0-9]+\.?[0-9]*\s*MB" > /dev/null; then \
  echo "[MODEL DOWNLOAD] $line"; \
  else \
  echo "[MODEL DOWNLOAD] $line"; \
  fi; \
  elif echo "$line" | grep "Installing" > /dev/null; then \
  echo "[INSTALL] $line"; \
  elif echo "$line" | grep "Successfully" > /dev/null; then \
  echo "[SUCCESS] $line"; \
  elif echo "$line" | grep -E "(ERROR|Failed)" > /dev/null; then \
  echo "[ERROR] $line"; \
  else \
  echo "[MODEL DOWNLOAD] $line"; \
  fi; \
  done) || echo "[MODEL DOWNLOAD] spaCy model will be downloaded at runtime" && \
  echo "[BUILD STAGE] spaCy model download process completed"

# Copy application code
COPY . .

# Production stage
FROM python:3.10-slim as production

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
  PYTHONUNBUFFERED=1 \
  PYTHONPATH=/app

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
  curl \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get clean

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create app directory
WORKDIR /app

# Copy installed packages from builder stage
COPY --from=builder /usr/local/lib/python3.10/site-packages /usr/local/lib/python3.10/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

# Copy application code (excluding unnecessary files)
COPY --from=builder /app/app /app/app
COPY --from=builder /app/requirements.txt /app/requirements.txt
COPY --from=builder /app/pytest.ini /app/pytest.ini

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 5001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:5001/health || exit 1

# Set the default command - use python module approach for better compatibility
CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5001"]