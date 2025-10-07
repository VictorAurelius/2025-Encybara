FROM python:3.10-slim

ENV PYTHONUNBUFFERED=1
ENV CUDA_VISIBLE_DEVICES=""

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    git \
    execstack \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN pip install --no-cache-dir --upgrade pip

RUN pip install --no-cache-dir \
    flask==3.0.0 \
    numpy==1.24.3 \
    werkzeug==3.0.1 \
    gunicorn==21.2.0 \
    psutil==5.9.6

RUN pip install --no-cache-dir \
    torch==2.1.0+cpu \
    --index-url https://download.pytorch.org/whl/cpu

RUN pip install --no-cache-dir \
    openai-whisper --no-deps

RUN pip install --no-cache-dir \
    tiktoken \
    tqdm \
    regex \
    more-itertools \
    numba

COPY . /app/
RUN chmod +x /app/run.py

RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app

RUN find /usr/local/lib/python3.10/site-packages -name "*.so*" -exec execstack -c {} \; 2>/dev/null || true

USER appuser

EXPOSE 5000

CMD ["python", "run.py"]