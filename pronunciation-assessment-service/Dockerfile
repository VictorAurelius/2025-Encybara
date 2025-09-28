FROM condaforge/mambaforge:latest

# Set environment variables
ENV PYTHONUNBUFFERED=1
ENV CONDA_DEFAULT_ENV=aligner
ENV PATH="/opt/conda/envs/aligner/bin:$PATH"

# Install system dependencies
RUN apt-get update && apt-get install -y \
    sox \
    libsox-fmt-all \
    ffmpeg \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create conda environment with MFA and dependencies
RUN mamba create -n aligner -c conda-forge \
    montreal-forced-aligner \
    python=3.10 \
    librosa \
    praatio \
    numpy \
    scipy \
    flask \
    soundfile \
    textgrid \
    pympi-ling \
    && mamba clean -afy

# Activate the environment
SHELL ["/bin/bash", "-c"]
RUN echo "source activate aligner" >> ~/.bashrc

# Set working directory
WORKDIR /app

# Copy application files
COPY . /app/

# Install Python packages
RUN source activate aligner && pip install -r requirements.txt

# Create directories for MFA data and temporary files
RUN mkdir -p /app/mfa_data /app/temp && \
    chmod 777 /app/mfa_data /app/temp

# Download MFA acoustic model (English)
RUN source activate aligner && \
    mfa model download acoustic english_us_arpa && \
    mfa model download dictionary english_us_arpa

# Expose port
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Run application
CMD ["bash", "-c", "source activate aligner && python app.py"]