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

## 🐳 Container Management

### Container Operations
```bash
# Start container
docker-compose up -d

# Stop container
docker-compose down

# Restart container
docker-compose restart

# View logs
docker-compose logs -f

# Container status
docker-compose ps

# Build image
docker-compose build --no-cache
```

### Health Monitoring
```bash
# Check container health
docker-compose ps

# Monitor from host
./monitor.sh

# API health check
curl http://localhost:4040/api/tunnels
```

### Volume Management
- **Configuration**: `./ngrok.yml` mounted as read-only
- **Logs**: `./logs/` directory for persistent logs
- **Network**: Custom bridge network for isolation

##  Service Mapping

| Service | Local Port | Tunnel Name | Target |
|---------|------------|-------------|--------|
| Pronunciation Assessment | 5000 | `pronunciation-assessment` | `host.docker.internal:5000` |
| Content Scoring | 5001 | `content-scoring` | `host.docker.internal:5001` |

## 🔑 Authentication

Make sure to update the `authtoken` in `ngrok.yml` with your own Ngrok auth token:

1. Sign up at [ngrok.com](https://ngrok.com)
2. Get your auth token from the dashboard
3. Replace the token in `ngrok.yml`

## 🌐 Container Benefits

This containerized setup provides:
- **Isolation**: Ngrok runs in isolated container environment
- **Portability**: Same behavior across different systems
- **Resource Control**: Container resource limits and management
- **Easy Deployment**: Simple docker-compose commands
- **Health Monitoring**: Built-in health checks
- **Log Management**: Persistent log storage
- **Network Isolation**: Custom Docker network
- **Pooling-Enabled**: Multiple tunnels from single container

## 🛠️ Troubleshooting

### Check container status:
```bash
docker-compose ps
curl http://localhost:4040/api/tunnels
```

### View container logs:
```bash
docker-compose logs -f
docker logs unified-ngrok-service
```

### Restart container:
```bash
# Restart service
docker-compose restart

# Full rebuild and restart
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Common Issues:
- **Port 4040 in use**: Stop other ngrok instances
- **Docker not running**: Start Docker Desktop
- **Config errors**: Check `ngrok.yml` syntax
- **Network issues**: Verify `host.docker.internal` resolution

## 📋 Migration Notes

### Container Migration:
This containerized service replaces:
- Individual ngrok configurations in services
- Manual ngrok CLI installations
- Host-based ngrok processes

### Benefits of Container Approach:
- ✅ **No CLI Installation**: Ngrok included in container
- ✅ **Consistent Environment**: Same behavior everywhere
- ✅ **Easy Management**: Docker Compose commands
- ✅ **Resource Control**: Container limits and monitoring
- ✅ **Clean Isolation**: No host system dependencies
- ✅ **Health Monitoring**: Built-in health checks

### Backward Compatibility:
- Same web interface: `http://localhost:4040`
- Same API endpoints for tunnel information
- Same tunnel URLs and functionality