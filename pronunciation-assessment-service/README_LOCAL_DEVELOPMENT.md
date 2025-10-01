# Pronunciation Assessment Service - Local Development Guide

## Overview
This guide helps you run the Pronunciation Assessment Service locally without Docker for development purposes.

## Prerequisites
- Python 3.6 or higher (tested with 3.8+, supports 3.13)
- pip (Python package installer)
- curl (for testing)

**Note**:
- The scripts will automatically detect whether your Python is installed as `python` or `python3` command
- For Python 3.13, compatible package versions are automatically selected

## Quick Start

**Important**: Run all commands from the `pronunciation-assessment-service` directory (project root), not from the `scripts/` directory.

### For Linux/macOS/Windows:
```bash
# Navigate to project root (if not already there)
cd pronunciation-assessment-service

# 1. Install dependencies
./scripts/install-deps.sh

# 2. Run the service
./scripts/run-local.sh

# 3. Test the service (in another terminal)
./scripts/test-local.sh

# 4. Stop the service
# Just press Ctrl+C in the terminal running the service
```

**Note**: All scripts work on Windows Git Bash, Linux, and macOS.

## Available Scripts

### Installation Scripts
- **`install-deps.sh`**: Sets up Python virtual environment and installs all dependencies
  - Creates `venv/` directory if it doesn't exist
  - Installs packages from `requirements.txt` or `requirements-py313.txt` (Python 3.13)
  - Handles cross-platform compatibility (Windows/Linux/macOS)

### Runtime Scripts
- **`run-local.sh`**: Starts the Flask service locally
  - Activates virtual environment
  - Sets Flask environment variables
  - Runs on `http://localhost:5000`
  - Shows startup logs and keeps running
  - Works from both project root and scripts directory

### Testing Scripts
- **`test-local.sh`**: Comprehensive service testing
  - Tests service availability
  - Validates health check endpoint
  - Tests API endpoints
  - Provides detailed test results

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

**Linux/macOS:**
```bash
# Remove and recreate
rm -rf venv/
# Reinstall
./scripts/install-deps.sh
```

**Windows:**
```cmd
# Remove and recreate
rmdir /s venv
# Reinstall
scripts\install-deps.bat
```

**Git Bash on Windows:**
```bash
# Remove and recreate
rm -rf venv/
# Reinstall (Git Bash can run .sh files)
./scripts/install-deps.sh
```

### Virtual Environment Activation Issues (Windows)
If you see "venv/bin/activate: No such file or directory":
- This is automatically handled by the updated scripts
- Windows uses `venv/Scripts/activate` instead of `venv/bin/activate`
- The scripts now detect the correct path automatically

### Python Command Detection Issues
If the script says "Python 3 is not installed" but you have Python:
```bash
# Check what Python commands are available
python --version    # Should show Python 3.x.x
python3 --version   # May or may not be available

# If only 'python' is available (common on Windows), the script will auto-detect it
# If you see version like "Python 3.13.7", you're all set
```

### Python 3.13 Compatibility Issues
If you encounter `AttributeError: module 'pkgutil' has no attribute 'ImpImporter'`:
- This is automatically handled by the install script
- Python 3.13 uses `requirements-py313.txt` with compatible package versions
- The script will automatically detect Python 3.13 and use the correct file

**For manual installation:**
```bash
# Python 3.13 users
pip install -r requirements-py313.txt

# Other Python versions
pip install -r requirements.txt
```

### Dependencies Issues
If packages fail to install:

**Basic dependency installation:**
```bash
# Upgrade pip first
python -m pip install --upgrade pip

# Install dependencies manually
pip install -r requirements.txt
```

**Setuptools/Build Backend Issues:**
If you see errors like "Cannot import 'setuptools.build_meta'":
```bash
# Install/upgrade essential build tools
pip install --upgrade pip setuptools wheel

# Then try installing dependencies again
pip install -r requirements.txt
```

**For corrupted virtual environment:**
```bash
# Remove and recreate virtual environment
rm -rf venv/  # Linux/macOS
rmdir /s venv  # Windows

# Run install script again
./scripts/install-deps.sh  # Linux/macOS
scripts\install-deps.bat  # Windows
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