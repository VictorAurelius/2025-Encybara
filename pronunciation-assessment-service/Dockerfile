# Multi-stage Dockerfile for WhisperX-based Pronunciation Assessment Service
FROM python:3.10-slim AS base

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

# Set timezone to avoid interactive prompts
ENV TZ=Asia/Ho_Chi_Minh
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies
RUN echo "[INSTALL] Installing system dependencies..." && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        tzdata \
        sox \
        libsox-fmt-all \
        ffmpeg \
        curl \
        git \
        build-essential \
        && \
    ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata && \
    rm -rf /var/lib/apt/lists/* && \
    echo "[INSTALL] ✓ System dependencies installed"

# Stage 2: Python environment setup
FROM base AS python-env

# Set working directory
WORKDIR /app

# Install PyTorch (CPU version for broader compatibility, can be overridden for GPU)
RUN echo "[INSTALL] Installing PyTorch..." && \
    pip install --no-cache-dir \
        torch==2.1.0+cpu \
        torchaudio==2.1.0+cpu \
        -f https://download.pytorch.org/whl/torch_stable.html && \
    echo "[INSTALL] ✓ PyTorch installed"

# Copy requirements first for better caching
COPY requirements.txt ./

# Install Python packages
RUN echo "[INSTALL] Installing Python packages..." && \
    pip install --no-cache-dir -r requirements.txt && \
    echo "[INSTALL] ✓ Python packages installed"

# Stage 3: WhisperX models download
FROM python-env AS model-download

# Create directories
RUN mkdir -p /app/models /app/temp /app/logs && \
    chmod 777 /app/models /app/temp /app/logs

# Download WhisperX models
RUN echo "[DOWNLOAD] Pre-downloading WhisperX models..." && \
    python -c "
import whisperx
import torch

# Download base model for transcription
print('[DOWNLOAD] Downloading WhisperX base model...')
device = 'cpu'  # Use CPU for compatibility
model = whisperx.load_model('base', device=device)
print('[DOWNLOAD] ✓ WhisperX base model downloaded')

# Download alignment model for English
print('[DOWNLOAD] Downloading alignment model...')
try:
    align_model, metadata = whisperx.load_align_model(language_code='en', device=device)
    print('[DOWNLOAD] ✓ Alignment model downloaded')
except Exception as e:
    print(f'[DOWNLOAD] Warning: Alignment model download failed: {e}')
    print('[DOWNLOAD] Models will be downloaded on first use')

print('[DOWNLOAD] ✓ Model download completed')
" && \
    echo "[DOWNLOAD] ✓ WhisperX models ready"

# Stage 4: Final application image
FROM model-download AS final

# Copy application files
COPY . /app/

# Set proper permissions
RUN chmod +x /app/run.py && \
    find /app -name "*.py" -exec chmod +r {} \;

# Create non-root user for security
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Run application
CMD ["python", "run.py"]