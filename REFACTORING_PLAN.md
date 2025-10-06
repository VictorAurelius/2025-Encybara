# Encybara Project Refactoring Plan

## Current Structure
```
2025-Encybara/
├── backend-app/                    # Spring Boot backend
├── horizon-tailwind-react-ts-main/ # React frontend
├── content-scoring-service/        # Python service
├── pronunciation-assessment-service/ # Python service
├── ngrok-service/                  # Ngrok tunneling
└── build-docker/                   # Build and deployment files
    └── react-build/               # React build output
```

## Proposed New Structure
```
2025-Encybara/
├── backend-service/               # ✅ Renamed from backend-app
├── cms-service/                   # ✅ Renamed from horizon-tailwind-react-ts-main
├── content-scoring-service/       # ✅ Keep as is
├── pronunciation-assessment-service/ # ✅ Keep as is
├── ngrok-service/                 # ✅ Keep as is
├── deployment/                    # ✅ Renamed from build-docker
│   ├── backend/                  # Backend deployment configs
│   ├── cms/                      # CMS deployment configs
│   │   ├── nginx/               # Nginx configs for CMS
│   │   └── build/               # CMS build output
│   ├── nginx-gateway/           # Main nginx gateway
│   └── docker-compose/          # Docker compose files
└── README.md                      # ✅ Main project documentation
```

## Refactoring Tasks

### 1. Rename Directories
- [ ] `backend-app` → `backend-service`
- [ ] `horizon-tailwind-react-ts-main` → `cms-service`
- [ ] `build-docker` → `deployment`

### 2. Restructure Deployment Files
- [ ] Move backend Dockerfile to `deployment/backend/`
- [ ] Move CMS build files to `deployment/cms/build/`
- [ ] Move nginx configs to `deployment/cms/nginx/`
- [ ] Create separate nginx gateway in `deployment/nginx-gateway/`
- [ ] Move docker-compose files to `deployment/docker-compose/`

### 3. Separate Nginx and CMS Containers
- [ ] Create dedicated nginx container for CMS
- [ ] Create dedicated CMS container
- [ ] Update docker-compose configurations

### 4. Update References
- [ ] Update all build scripts
- [ ] Update docker-compose files
- [ ] Update README and documentation
- [ ] Update service URLs and paths

### 5. Create Main README
- [ ] Project overview
- [ ] Architecture diagram
- [ ] Quick start guide
- [ ] Service documentation links

## Benefits
- ✅ Clear naming conventions
- ✅ Better separation of concerns
- ✅ Easier to understand structure
- ✅ Scalable architecture
- ✅ Professional organization

## Migration Steps
1. Create new directory structure
2. Move files to new locations
3. Update configurations
4. Test all services
5. Update documentation

---
**Status:** Planning
**Date:** 2025-10-06
