# Encybara - English Learning Platform

> A comprehensive microservices-based platform for English language learning with AI-powered features.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Development](#development)
- [Deployment](#deployment)
- [Documentation](#documentation)
- [Contributing](#contributing)

## 🎯 Overview

Encybara is a modern English learning platform built with microservices architecture, featuring:

- ✅ **Interactive Learning System** - Courses, lessons, and assessments
- ✅ **AI-Powered Content Scoring** - Automated essay evaluation
- ✅ **Pronunciation Assessment** - Speech analysis with MFA
- ✅ **Flashcard System** - Spaced repetition learning
- ✅ **Progress Tracking** - Detailed learning analytics
- ✅ **Admin CMS** - Content management system

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│           Nginx Gateway (Port 80)               │
│     Single Entry Point for All Services         │
└───────────┬─────────────────────────────────────┘
            │
    ┌───────┴───────┐
    │               │
┌───▼────┐    ┌────▼─────┐
│  CMS   │    │ Backend  │
│ React  │    │  Spring  │
│  SPA   │    │   Boot   │
│ :3000  │    │  :8080   │
└────────┘    └────┬─────┘
                   │
         ┌─────────┴──────────┐
         │                    │
    ┌────▼──────┐      ┌─────▼────────┐
    │  Content  │      │Pronunciation │
    │  Scoring  │      │  Assessment  │
    │  :5001    │      │    :5000     │
    └───────────┘      └──────────────┘
```

### Technology Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Spring Boot 3.x, Java 17 |
| **Frontend** | React 18, TypeScript, TailwindCSS |
| **Database** | MySQL 5.7 |
| **AI Services** | Python 3.10, Flask |
| **Proxy** | Nginx 1.23 |
| **Container** | Docker, Docker Compose |

## 🚀 Services

### 1. Backend Service
- **Port:** 8080
- **Tech:** Spring Boot
- **Purpose:** Main API, business logic, database operations
- **Path:** `backend-service/`
- [Documentation](backend-service/API_DOCUMENTATION.md)

### 2. CMS Service
- **Port:** 3000 (via Nginx)
- **Tech:** React + TypeScript
- **Purpose:** Admin panel, content management
- **Path:** `cms-service/`

### 3. Content Scoring Service
- **Port:** 5001
- **Tech:** Python/Flask
- **Purpose:** AI-powered essay/content evaluation
- **Path:** `content-scoring-service/`
- [Documentation](content-scoring-service/README.md)

### 4. Pronunciation Assessment Service
- **Port:** 5000
- **Tech:** Python/Flask + MFA
- **Purpose:** Speech pronunciation analysis
- **Path:** `pronunciation-assessment-service/`
- [Documentation](pronunciation-assessment-service/README.md)

### 5. Nginx Gateway
- **Port:** 80, 443
- **Tech:** Nginx
- **Purpose:** Main entry point, routing, load balancing
- **Path:** `deployment/nginx-gateway/`

## ⚡ Quick Start

### Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Docker Compose v3.8+
- 8GB RAM minimum
- 20GB disk space

### Option 1: All Services (Recommended)

```bash
# Clone repository
git clone <repository-url>
cd 2025-Encybara

# Build all services
./build-all.sh

# Start all services
./start-all.sh

# Check health
curl http://localhost:8080/actuator/health
curl http://localhost:5001/health
curl http://localhost:5000/health
```

### Option 2: Docker Compose

```bash
# Start with docker-compose
docker-compose -f docker-compose.all.yml up -d

# View logs
docker-compose -f docker-compose.all.yml logs -f

# Stop all
docker-compose -f docker-compose.all.yml down
```

### Accessing Services

| Service | URL | Description |
|---------|-----|-------------|
| **Main App** | http://localhost | Frontend application |
| **Admin CMS** | http://localhost:3000 | Content management |
| **API** | http://localhost:8080 | Backend REST API |
| **Swagger** | http://localhost:8080/swagger-ui.html | API Documentation |
| **Content Scoring** | http://localhost:5001 | AI scoring service |
| **Pronunciation** | http://localhost:5000 | Speech assessment |

## 📁 Project Structure

```
2025-Encybara/
├── backend-service/              # Spring Boot backend
│   ├── src/
│   ├── build.gradle.kts
│   └── Dockerfile
│
├── cms-service/                  # React admin panel
│   ├── src/
│   ├── package.json
│   └── README.md
│
├── content-scoring-service/      # AI content evaluation
│   ├── app/
│   ├── requirements.txt
│   └── Dockerfile
│
├── pronunciation-assessment-service/  # Speech analysis
│   ├── app/
│   ├── requirements.txt
│   └── Dockerfile
│
├── deployment/                   # Deployment configurations
│   ├── cms/                     # CMS deployment
│   │   ├── nginx/
│   │   ├── build/
│   │   └── Dockerfile
│   ├── nginx-gateway/           # Main Nginx gateway
│   │   ├── conf.d/
│   │   └── Dockerfile
│   └── docker-compose/          # Docker compose files
│       └── docker-compose.yml
│
├── ngrok-service/               # Tunneling (optional)
│
├── build-all.sh                 # Build all services
├── start-all.sh                 # Start all services
├── stop-all.sh                  # Stop all services
├── docker-compose.all.yml       # Main docker compose
│
└── README.md                    # This file
```

## 🛠️ Development

### Backend Service

```bash
cd backend-service

# Run locally (requires MySQL)
./gradlew bootRun

# Run tests
./gradlew test

# Build
./gradlew build
```

### CMS Service

```bash
cd cms-service

# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build
```

### Content Scoring Service

```bash
cd content-scoring-service

# Install dependencies
pip install -r requirements.txt

# Run locally
python app/main.py

# Run with Docker
docker-compose up -d
```

### Pronunciation Assessment Service

⚠️ **Windows Users:** Must use Docker (MFA not supported natively)

```bash
cd pronunciation-assessment-service

# Docker (recommended for all platforms)
docker-compose up -d

# Linux/Mac: Local development
conda create -n aligner -c conda-forge montreal-forced-aligner
conda activate aligner
mfa model download acoustic english_us_arpa
mfa model download dictionary english_us_arpa
python run.py
```

See [WINDOWS_SETUP.md](pronunciation-assessment-service/WINDOWS_SETUP.md) for Windows-specific instructions.

## 🚢 Deployment

### Production Deployment

```bash
# 1. Build all images
./build-all.sh --clean --no-cache

# 2. Tag images
docker tag encybara-backend:latest registry/encybara-backend:v1.0.0
docker tag encybara-cms:latest registry/encybara-cms:v1.0.0

# 3. Push to registry
docker push registry/encybara-backend:v1.0.0
docker push registry/encybara-cms:v1.0.0

# 4. Deploy
docker-compose -f deployment/docker-compose/docker-compose.yml up -d
```

### Environment Variables

Key environment variables for production:

```env
# Backend
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/encybara
CONTENT_SCORING_SERVICE_URL=http://content-scoring-service:5001
PRONUNCIATION_SERVICE_URL=http://pronunciation-assessment-service:5000

# Services
LOG_LEVEL=INFO
PORT=<service-port>
```

## 📚 Documentation

### Service Documentation

- [Backend API](backend-service/API_DOCUMENTATION.md)
- [Content Scoring](content-scoring-service/README.md)
- [Pronunciation Assessment](pronunciation-assessment-service/README.md)
  - [Optimizations](pronunciation-assessment-service/OPTIMIZATIONS.md)
  - [Windows Setup](pronunciation-assessment-service/WINDOWS_SETUP.md)
  - [Troubleshooting](pronunciation-assessment-service/TROUBLESHOOTING.md)

### Quick References

- [Quick Start Guide](README_QUICK.md)
- [Build and Run Guide](BUILD_AND_RUN.md)
- [Service URL Configuration](SERVICE_URL_CONFIGURATION.md)
- [Refactoring Plan](REFACTORING_PLAN.md)

## 🧪 Testing

### Backend Tests

```bash
cd backend-service
./test-content-scoring.sh
./test-pronunciation.sh
```

### Service Tests

```bash
# Content Scoring
cd content-scoring-service
./test-ngrok-public.sh

# Pronunciation
cd pronunciation-assessment-service
./test-optimized.sh
```

## 📊 Performance

### Pronunciation Service Optimization

Recent improvements have made the pronunciation service **60-70% faster**:

- **Before:** >30 seconds for 10-second audio
- **After:** ~8-12 seconds for 10-second audio

See [OPTIMIZATIONS.md](pronunciation-assessment-service/OPTIMIZATIONS.md) for details.

## 🐛 Troubleshooting

### Common Issues

**Services won't start:**
```bash
# Check Docker
docker info

# Check ports
docker ps
lsof -i :8080  # Backend
lsof -i :5001  # Content Scoring
lsof -i :5000  # Pronunciation
```

**Database connection issues:**
```bash
# Reset database
./stop-all.sh --volumes
./start-all.sh
```

**Windows pronunciation service error:**
- See [ERROR_WINDOWS.md](pronunciation-assessment-service/ERROR_WINDOWS.md)
- **Solution:** Must use Docker on Windows

## 🤝 Contributing

### Development Workflow

1. Create feature branch
2. Make changes
3. Test locally
4. Submit pull request

### Code Standards

- Backend: Java Code Conventions
- Frontend: ESLint + Prettier
- Python: PEP 8

## 📝 License

[Your License Here]

## 👥 Team

- Backend Team - Spring Boot development
- Frontend Team - React development
- AI Team - ML models and services

## 📧 Contact

For issues and support:
- Create GitHub issue
- Check documentation first
- Review troubleshooting guides

---

## Recent Updates

### Version 1.0.0 (2025-10-06)

**Major Refactoring:**
- ✅ Renamed `backend-app` → `backend-service`
- ✅ Renamed `horizon-tailwind-react-ts-main` → `cms-service`
- ✅ Renamed `build-docker` → `deployment`
- ✅ Separated CMS and Nginx into independent containers
- ✅ Created dedicated Nginx Gateway
- ✅ Reorganized deployment structure
- ✅ Improved documentation

**Performance Improvements:**
- ✅ Pronunciation service 60-70% faster
- ✅ Optimized MFA alignment
- ✅ Better error handling

**Documentation:**
- ✅ Comprehensive README
- ✅ Platform-specific guides
- ✅ Troubleshooting documentation

---

**Built with ❤️ by Encybara Team**

For more information, visit our [documentation](docs/) or create an [issue](issues/).
