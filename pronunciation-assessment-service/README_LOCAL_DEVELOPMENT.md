# Pronunciation Assessment Service - Local Development Guide

## Overview
This guide helps you run the Pronunciation Assessment Service locally without Docker for development purposes.

## Prerequisites
- Python 3.8 or higher
- pip (Python package installer)
- curl (for testing)

## Quick Start

### For Linux/macOS:
```bash
# 1. Install dependencies
./scripts/install-deps.sh

# 2. Run the service
./scripts/run-local.sh

# 3. Test the service (in another terminal)
./scripts/test-local.sh

# 4. Stop the service
./scripts/stop-local.sh
```

## Available Scripts

### Installation Scripts
- **`install-deps.sh`**: Sets up Python virtual environment and installs all dependencies
  - Creates `venv/` directory if it doesn't exist
  - Installs packages from `requirements.txt`
  - Handles cross-platform compatibility

### Runtime Scripts
- **`run-local.sh`**: Starts the Flask service locally
  - Activates virtual environment
  - Sets Flask environment variables
  - Runs on `http://localhost:5000`
  - Shows startup logs and keeps running

### Testing Scripts
- **`test-local.sh`**: Comprehensive service testing
  - Tests service availability
  - Validates health check endpoint
  - Tests API endpoints
  - Provides detailed test results

### Cleanup Scripts
- **`stop-local.sh`**: Gracefully stops the running service
  - Finds and terminates Flask process
  - Cleans up port usage
  - Provides stop confirmation

## Service Endpoints

Once running, the service provides these endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Service root - returns service info |
| `/health` | GET | Health check endpoint |
| `/api/info` | GET | Detailed service information |
| `/api/pronunciation-assessment` | POST | Pronunciation assessment with audio file |

## Testing the Service

### Basic Health Check
```bash
curl http://localhost:5000/health
```

### Service Information
```bash
curl http://localhost:5000/api/info
```

### Pronunciation Assessment
```bash
curl -X POST http://localhost:5000/api/pronunciation-assessment \
  -F "audio=@your_audio_file.wav" \
  -F "transcript=Hello world"
```

## Development Workflow

1. **Setup Environment** (first time only):
   ```bash
   ./scripts/install-deps.sh
   ```

2. **Start Development**:
   ```bash
   ./scripts/run-local.sh
   ```

3. **Make Code Changes**: Edit files in the `app/` directory

4. **Test Changes**: The Flask development server will auto-reload on file changes

5. **Run Tests**:
   ```bash
   ./scripts/test-local.sh
   ```

6. **Stop Service**:
   ```bash
   ./scripts/stop-local.sh
   ```

## Troubleshooting

### Port Already in Use
If port 5000 is already in use:
```bash
# Find what's using the port
lsof -ti:5000  # Linux/macOS
netstat -ano | findstr :5000  # Windows

# Kill the process
kill -9 <PID>  # Linux/macOS
taskkill /PID <PID> /F  # Windows
```

### Virtual Environment Issues
If you encounter virtual environment problems:
```bash
# Remove and recreate
rm -rf venv/  # Linux/macOS
rmdir /s venv  # Windows

# Reinstall
./scripts/install-deps.sh  # Linux/macOS
scripts\install-deps.bat  # Windows
```

### Dependencies Issues
If packages fail to install:
```bash
# Upgrade pip first
python -m pip install --upgrade pip

# Install dependencies manually
pip install -r requirements.txt
```

### Permission Issues (Linux/macOS)
If scripts can't execute:
```bash
chmod +x scripts/*.sh
```

## File Structure
```
pronunciation-assessment-service/
├── app/
│   ├── __init__.py
│   ├── main.py          # Flask application
│   ├── models.py        # Data models
│   └── services/        # Service logic
├── scripts/
│   ├── install-deps.sh/.bat    # Dependency installation
│   ├── run-local.sh/.bat       # Service startup
│   ├── test-local.sh/.bat      # Service testing
│   └── stop-local.sh/.bat      # Service shutdown
├── tests/               # Test files
├── venv/               # Virtual environment (created by scripts)
├── requirements.txt    # Python dependencies
└── README_LOCAL_DEVELOPMENT.md  # This file
```

## Environment Variables

The service supports these environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `FLASK_ENV` | `development` | Flask environment |
| `FLASK_DEBUG` | `1` | Enable Flask debug mode |
| `HOST` | `0.0.0.0` | Service host |
| `PORT` | `5000` | Service port |

## Docker vs Local Development

| Aspect | Docker | Local Development |
|--------|--------|------------------|
| **Setup Time** | Longer (build image) | Faster (install deps once) |
| **Resource Usage** | Higher | Lower |
| **Development Speed** | Slower (rebuild for changes) | Faster (auto-reload) |
| **Debugging** | More complex | Easier |
| **Isolation** | Better | Less isolated |
| **Deployment Match** | Exact | Close |

## Next Steps

After local development:
1. Test thoroughly with `./scripts/test-local.sh`
2. Build Docker image for production: `docker build -t pronunciation-assessment .`
3. Deploy using Docker Compose for production workloads

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Ensure all prerequisites are installed
3. Verify Python version compatibility
4. Check the service logs for error details