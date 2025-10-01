# Multi-stage Dockerfile with download tracking for Pronunciation Assessment Service
FROM condaforge/mambaforge:latest AS base

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV CONDA_DEFAULT_ENV=aligner
ENV PATH="/opt/conda/envs/aligner/bin:$PATH"

# Set timezone to avoid interactive prompts
ENV TZ=Asia/Ho_Chi_Minh
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies with retry logic and progress tracking
RUN echo "[INSTALL] Installing system dependencies..." && \
    # Update package list with retry
    for i in 1 2 3; do \
    apt-get update && break || { \
    echo "[INSTALL] Update attempt $i failed, retrying..."; \
    sleep 5; \
    }; \
    done && \
    # Install packages with retry and --fix-missing
    for i in 1 2 3; do \
    apt-get install -y --fix-missing \
    tzdata \
    sox \
    libsox-fmt-all \
    ffmpeg \
    curl \
    pv \
    && break || { \
    echo "[INSTALL] Install attempt $i failed, retrying..."; \
    apt-get clean && \
    apt-get update && \
    sleep 10; \
    }; \
    done && \
    # Configure timezone
    ln -fs /usr/share/zoneinfo/$TZ /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata && \
    # Clean up
    rm -rf /var/lib/apt/lists/* && \
    echo "[INSTALL] ✓ System dependencies installed"

# Stage 2: Create conda environment with download tracking
FROM base AS conda-env

# Create conda environment with MFA and dependencies
RUN echo "[LARGE DOWNLOAD] Creating conda environment with MFA packages..." && \
    echo "[LARGE DOWNLOAD] This may take several minutes..." && \
    for attempt in 1 2 3; do \
    echo "[LARGE DOWNLOAD] Attempt $attempt/3 - Creating conda environment..." && \
    mamba create -n aligner -c conda-forge \
    montreal-forced-aligner \
    python=3.10 \
    librosa \
    praatio \
    numpy \
    scipy \
    flask \
    pysoundfile \
    textgrid \
    -y && break || \
    (echo "[LARGE DOWNLOAD] Attempt $attempt failed, retrying in 30s..." && sleep 30); \
    done && \
    echo "[LARGE DOWNLOAD] Installing additional packages via pip..." && \
    /opt/conda/envs/aligner/bin/pip install pympi-ling && \
    mamba clean -afy && \
    echo "[LARGE DOWNLOAD] ✓ Conda environment created successfully"

# Stage 3: Application setup
FROM conda-env AS app-setup

# Activate the environment
SHELL ["/bin/bash", "-c"]
RUN echo "source activate aligner" >> ~/.bashrc

# Set working directory
WORKDIR /app

# Copy requirements first for better caching
COPY requirements.txt requirements-py313.txt ./

# Install Python packages with progress tracking
RUN echo "[INSTALL] Installing Python packages..." && \
    source activate aligner && \
    # Use requirements.txt for Docker (stable versions)
    pip install --no-cache-dir -r requirements.txt \
    && echo "[INSTALL] ✓ Python packages installed successfully"

# Create directories for MFA data and temporary files
RUN mkdir -p /app/mfa_data /app/temp /app/logs && \
    chmod 777 /app/mfa_data /app/temp /app/logs

# Stage 4: Download MFA models with tracking
FROM app-setup AS model-download

# Download MFA acoustic model with progress tracking
RUN echo "[LARGE DOWNLOAD] Downloading MFA acoustic models..." && \
    echo "[LARGE DOWNLOAD] English US ARPA model (~100MB)" && \
    source activate aligner && \
    mfa model download acoustic english_us_arpa 2>&1 | \
    while IFS= read -r line; do \
    echo "[LARGE DOWNLOAD] $line"; \
    if [[ "$line" == *"100%"* ]] || [[ "$line" == *"complete"* ]]; then \
    echo "[LARGE DOWNLOAD] ✓ Acoustic model download completed"; \
    fi; \
    done \
    && echo "[LARGE DOWNLOAD] Downloading dictionary model..." && \
    mfa model download dictionary english_us_arpa 2>&1 | \
    while IFS= read -r line; do \
    echo "[LARGE DOWNLOAD] $line"; \
    if [[ "$line" == *"100%"* ]] || [[ "$line" == *"complete"* ]]; then \
    echo "[LARGE DOWNLOAD] ✓ Dictionary model download completed"; \
    fi; \
    done \
    && echo "[LARGE DOWNLOAD] ✓ All MFA models downloaded successfully"

# Stage 5: Final application image
FROM model-download AS final

# Copy application files
COPY . /app/

# Set proper permissions
RUN chmod +x /app/app.py && \
    find /app -name "*.py" -exec chmod +r {} \;

# Expose port
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Run application
CMD ["bash", "-c", "source activate aligner && echo '[START] Starting Pronunciation Assessment Service...' && python app.py"]