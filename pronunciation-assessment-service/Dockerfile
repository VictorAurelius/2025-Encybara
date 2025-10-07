# WhisperX CPU-Only Dockerfile - No CUDA downloads
FROM python:3.10-slim

# Environment variables to force CPU-only
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV CUDA_VISIBLE_DEVICES=""
ENV TORCH_CUDA_ARCH_LIST=""

# Install minimal system dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        git \
        ffmpeg \
        build-essential \
        && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Upgrade pip
RUN pip install --no-cache-dir --upgrade pip

# Install basic packages first
RUN pip install --no-cache-dir \
    numpy==1.24.3 \
    scipy==1.11.4 \
    flask==3.0.0 \
    werkzeug==3.0.1 \
    gunicorn==21.2.0 \
    psutil==5.9.6

# Install PyTorch CPU-only first
RUN pip install --no-cache-dir \
    torch==2.1.0+cpu \
    torchaudio==2.1.0+cpu \
    --index-url https://download.pytorch.org/whl/cpu

# Install audio processing
RUN pip install --no-cache-dir \
    librosa==0.10.1 \
    soundfile==0.12.1 \
    praatio==6.0.0

# Install transformers and faster-whisper with version constraints to avoid torch upgrade
RUN pip install --no-cache-dir \
    "transformers>=4.21.0,<4.40.0" \
    "faster-whisper>=0.9.0,<1.0.0"

# Install WhisperX with minimal dependencies (skip pyannote.audio which pulls CUDA)
RUN pip install --no-cache-dir --no-deps git+https://github.com/m-bain/whisperx.git

# Install only essential WhisperX dependencies manually
RUN pip install --no-cache-dir \
    "ctranslate2>=3.0.0,<4.0.0" \
    pandas \
    tqdm

# Create directories
RUN mkdir -p /app/models /app/temp /app/logs && \
    chmod 777 /app/models /app/temp /app/logs

# Copy application files
COPY . /app/

# Set permissions
RUN chmod +x /app/run.py && \
    find /app -name "*.py" -exec chmod +r {} \;

# Create non-root user
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

EXPOSE 5000

# Health check with extended time for model download
HEALTHCHECK --interval=30s --timeout=60s --start-period=300s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Environment for CPU-only operation
ENV WHISPERX_DEVICE=cpu
ENV WHISPERX_COMPUTE_TYPE=float32

CMD ["python", "run.py"]