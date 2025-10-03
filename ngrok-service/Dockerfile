FROM alpine:latest

# Install dependencies
RUN apk add --no-cache curl jq bash

# Download and install ngrok
RUN curl -L https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz \
    | tar -xz -C /usr/local/bin/ && \
    chmod +x /usr/local/bin/ngrok

# Create ngrok directory
WORKDIR /ngrok

# Copy configuration
COPY ngrok.yml /ngrok/ngrok.yml

# Copy utility scripts
COPY get-tunnel-urls.sh /ngrok/get-tunnel-urls.sh
COPY monitor.sh /ngrok/monitor.sh

# Make scripts executable
RUN chmod +x /ngrok/get-tunnel-urls.sh /ngrok/monitor.sh

# Expose ngrok web interface port
EXPOSE 4040

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:4040/api/tunnels || exit 1

# Default command
CMD ["ngrok", "start", "--all", "--config=ngrok.yml", "--log=stdout"]