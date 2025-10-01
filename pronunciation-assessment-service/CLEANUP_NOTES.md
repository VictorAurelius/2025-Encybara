# Pronunciation Assessment Service - Cleanup Notes

## Files Removed

### 1. Stop Scripts (No longer needed)
- `scripts/stop-local.sh` - Removed (use Ctrl+C instead)
- `scripts/stop-local.bat` - Removed (use Ctrl+C instead)

**Reason**: Ctrl+C is sufficient for stopping the Flask development server. Stop scripts were only useful for background processes.

### 2. Original Requirements File
- Keep `requirements.txt` - Still needed for Docker builds (stable versions)
- Added `requirements-py313.txt` - For Python 3.13 compatibility

**Reason**: Docker uses stable pinned versions, while local development needs Python 3.13 compatible versions.

### 3. Windows Batch Files (Removed from documentation)
- `scripts/install-deps.bat` - Still exists but not documented
- `scripts/run-local.bat` - Still exists but not documented
- `scripts/test-local.bat` - Still exists but not documented

**Reason**: All .sh scripts work on Windows Git Bash, so batch files are redundant.

## Updated Container Files

### 1. docker-compose.yml
- **Port**: Changed from 5001 to 5000 (matching Flask default)
- **Service name**: pronunciation-assessment-service
- **Network**: pronunciation-network (subnet: 172.21.0.0/16)
- **Ngrok port**: 4041 (to avoid conflict with content-scoring-service)
- **Monitoring ports**: 9091, 3101 (to avoid conflicts)
- **Additional volumes**: Added temp directory mounting

### 2. Dockerfile
- **Multi-stage build**: Added download progress tracking
- **Download tracking**: Shows progress for large MFA model downloads
- **Requirements**: Supports both requirements.txt and requirements-py313.txt
- **Improved logging**: Better build stage visibility

### 3. build.sh
- **Service specific**: Adapted from content-scoring-service
- **Port**: Updated to 5000
- **Health check**: Uses correct endpoint
- **Nginx config**: Configured for pronunciation assessment with audio upload support

### 4. Ngrok Configuration
- **File**: `ngrok/ngrok.yml`
- **Target**: host.docker.internal:5000
- **Tunnel name**: pronunciation-assessment
- **Web interface**: Port 4041

## Port Allocation

To avoid conflicts between services:

| Service | Component | Port |
|---------|-----------|------|
| **content-scoring-service** | Main Service | 5001 |
| | Prometheus | 9090 |
| | Grafana | 3100 |
| | Ngrok UI | 4040 |
| | Redis | 6379 |
| | Nginx | 80, 443 |
| **pronunciation-assessment-service** | Main Service | 5000 |
| | Prometheus | 9091 |
| | Grafana | 3101 |
| | Ngrok UI | 4041 |
| | Redis | 6380 |
| | Nginx | 81, 444 |

## Network Configuration

Each service has its own Docker network to avoid conflicts:
- **content-scoring-service**: 172.20.0.0/16
- **pronunciation-assessment-service**: 172.21.0.0/16

## Usage Commands

### Container Build & Run
```bash
# Build and run basic service
./build.sh

# Build with monitoring
./build.sh --monitoring

# Build with ngrok tunnel
./build.sh --tunnel

# Clean build
./build.sh --clean --no-cache
```

### Local Development
```bash
# Install dependencies
./scripts/install-deps.sh

# Run service
./scripts/run-local.sh

# Test service
./scripts/test-local.sh

# Stop service: Ctrl+C
```

## Notes

1. **Requirements files**: Both `requirements.txt` and `requirements-py313.txt` are needed
2. **Stop scripts**: Removed because Ctrl+C is sufficient
3. **Batch files**: Still exist but not documented (use .sh files with Git Bash)
4. **Port conflicts**: All ports are configured to avoid conflicts between services
5. **Networks**: Separate networks prevent container conflicts