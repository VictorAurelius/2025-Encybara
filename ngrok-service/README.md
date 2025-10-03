# Ngrok Service - Unified Tunnel Management

This directory contains the unified ngrok configuration for both `pronunciation-assessment-service` and `content-scoring-service`.

## 📁 Structure

```
ngrok-service/
├── ngrok.yml              # Unified ngrok configuration
├── start-ngrok.sh         # Start ngrok service
├── get-tunnel-urls.sh     # Get public URLs for both tunnels
└── README.md             # This file
```

## 🔧 Configuration

### ngrok.yml
Contains unified configuration for both services:
- **pronunciation-assessment**: Maps to `host.docker.internal:5000`
- **content-scoring**: Maps to `host.docker.internal:5001`
- **Web Interface**: Available at `http://localhost:4040`

## 🚀 Usage

### Start Unified Ngrok Service
```bash
# From project root
./start-ngrok-services.sh

# Or from ngrok-service directory
cd ngrok-service
./start-ngrok.sh
```

### Get Public URLs
```bash
cd ngrok-service
./get-tunnel-urls.sh
```

### Check Web Interface
Open your browser and navigate to: `http://localhost:4040`

## 📝 Service Mapping

| Service | Local Port | Tunnel Name | Target |
|---------|------------|-------------|--------|
| Pronunciation Assessment | 5000 | `pronunciation-assessment` | `host.docker.internal:5000` |
| Content Scoring | 5001 | `content-scoring` | `host.docker.internal:5001` |

## 🔑 Authentication

Make sure to update the `authtoken` in `ngrok.yml` with your own Ngrok auth token:

1. Sign up at [ngrok.com](https://ngrok.com)
2. Get your auth token from the dashboard
3. Replace the token in `ngrok.yml`

## 🌐 Pooling-Enabled

This configuration enables pooling for both services, allowing:
- Multiple tunnels from a single ngrok instance
- Efficient resource usage
- Centralized management
- Single web interface for monitoring

## 🛠️ Troubleshooting

### Check if ngrok is running:
```bash
curl http://localhost:4040/api/tunnels
```

### View logs:
```bash
# Check ngrok process
ps aux | grep ngrok

# If running in Docker
docker logs ngrok-container-name
```

### Restart service:
```bash
# Stop any running ngrok processes
pkill ngrok

# Start again
./start-ngrok.sh
```

## 📋 Migration Notes

This unified service replaces the individual ngrok configurations that were previously located in:
- `pronunciation-assessment-service/ngrok/`
- `content-scoring-service/ngrok/`

All ngrok-related files have been centralized here for better management and to avoid conflicts.