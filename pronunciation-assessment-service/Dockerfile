FROM python:3.10-slim

ENV PYTHONUNBUFFERED=1
ENV CUDA_VISIBLE_DEVICES=""

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl git ffmpeg build-essential && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN pip install --no-cache-dir --upgrade pip

RUN pip install --no-cache-dir \
    flask==3.0.0 \
    librosa==0.10.1 \
    numpy==1.24.3 \
    scipy==1.11.4 \
    soundfile==0.12.1 \
    werkzeug==3.0.1 \
    gunicorn==21.2.0 \
    psutil==5.9.6 \
    praatio==6.0.0

RUN pip install --no-cache-dir \
    openai-whisper

COPY . /app/
RUN chmod +x /app/run.py

RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=30s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

CMD ["python", "run.py"]