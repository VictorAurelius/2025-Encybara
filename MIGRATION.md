# Migration Guide - Project Refactoring

## Overview

The Encybara project has undergone a major refactoring to improve organization, maintainability, and scalability.

## 🔄 Changes Summary

### Directory Renames

| Old Name | New Name | Reason |
|----------|----------|--------|
| `backend-app` | `backend-service` | Consistent naming convention |
| `horizon-tailwind-react-ts-main` | `cms-service` | Descriptive, clear purpose |
| `build-docker` | `deployment` | Better reflects content |

### Structural Changes

**Before:**
```
build-docker/
├── Dockerfile.nginx
├── default.conf
└── react-build/
```

**After:**
```
deployment/
├── cms/                      # CMS deployment configs
│   ├── Dockerfile
│   ├── nginx/
│   │   └── nginx.conf
│   └── build/
│       └── react-build/
├── nginx-gateway/           # Main gateway
│   ├── Dockerfile
│   ├── nginx.conf
│   └── conf.d/
│       └── default.conf
└── docker-compose/          # Docker compose files
    └── docker-compose.yml
```

## 📋 Migration Steps

### 1. Update Git Remotes (if needed)

```bash
# Pull latest changes
git pull origin main

# Verify structure
ls -la
```

### 2. Update Local Development

**Old commands:**
```bash
cd backend-app
./gradlew bootRun
```

**New commands:**
```bash
cd backend-service
./gradlew bootRun
```

### 3. Update Docker Commands

**Old:**
```bash
docker-compose -f build-docker/docker-compose.yml up
```

**New:**
```bash
docker-compose -f deployment/docker-compose/docker-compose.yml up
```

### 4. Update Scripts

All build scripts have been updated automatically. Use:

```bash
# Build all
./build-all.sh

# Start all
./start-all.sh

# Stop all
./stop-all.sh
```

### 5. Update IDE Projects

**IntelliJ IDEA / VS Code:**
- Close project
- Re-open from root directory
- IDE will detect the new structure

**Backend Service:**
- Previous: `backend-app/`
- Current: `backend-service/`

**CMS Service:**
- Previous: `horizon-tailwind-react-ts-main/`
- Current: `cms-service/`

## 🔧 Configuration Updates

### Docker Compose

**Old reference:**
```yaml
build:
  context: ../backend-app
```

**New reference:**
```yaml
build:
  context: ../../backend-service
```

### Environment Variables

No changes required. All environment variables remain the same.

### Service URLs

No changes required. Service URLs are configured in `application.properties`:

```properties
content-scoring.service.url=${CONTENT_SCORING_SERVICE_URL:http://localhost:5001}
pronunciation-assessment.service.url=${PRONUNCIATION_SERVICE_URL:http://localhost:5000}
```

## 🎯 What Stays the Same

✅ **API Endpoints** - All unchanged
✅ **Service Ports** - All unchanged
✅ **Environment Variables** - All unchanged
✅ **Database Schema** - All unchanged
✅ **Service Functionality** - All unchanged

## 📦 New Features

### Separated Containers

**CMS Container:**
- Dedicated container for React SPA
- Own Nginx instance
- Independent scaling

**Nginx Gateway:**
- Main entry point
- Routes to all services
- Centralized configuration

### Benefits

✅ **Better Separation** - Each service is independent
✅ **Easier Scaling** - Scale CMS and backend separately
✅ **Clearer Structure** - More intuitive organization
✅ **Better Debugging** - Isolated logs per service

## 🧪 Testing After Migration

### 1. Verify Services Start

```bash
# Start all services
./start-all.sh

# Check health
curl http://localhost:8080/actuator/health
curl http://localhost:5001/health
curl http://localhost:5000/health
curl http://localhost:3000
```

### 2. Test API

```bash
# Test backend
curl http://localhost:8080/api/v1/users

# Test through gateway
curl http://localhost/api/v1/users
```

### 3. Test CMS

```bash
# Access CMS directly
curl http://localhost:3000

# Access CMS through gateway
curl http://localhost/
```

## 🚨 Common Issues

### Issue: "Directory not found"

**Cause:** Old directory names in commands/scripts

**Solution:**
```bash
# Update commands to use new names
cd backend-service  # not backend-app
cd cms-service      # not horizon-tailwind-react-ts-main
```

### Issue: "Docker build fails"

**Cause:** Old context paths

**Solution:**
```bash
# Clean rebuild
./build-all.sh --clean --no-cache
```

### Issue: "Services can't communicate"

**Cause:** Old container references

**Solution:**
```bash
# Stop all
./stop-all.sh

# Clean restart
docker system prune -f
./start-all.sh
```

## 📚 Updated Documentation

All documentation has been updated:

- ✅ [README.md](README.md) - Main project documentation
- ✅ [BUILD_AND_RUN.md](BUILD_AND_RUN.md) - Build and deployment guide
- ✅ [SERVICE_URL_CONFIGURATION.md](SERVICE_URL_CONFIGURATION.md) - URL config
- ✅ [README_QUICK.md](README_QUICK.md) - Quick reference

## 🔄 Rollback (if needed)

If you need to rollback:

```bash
# Checkout previous version
git checkout <previous-commit>

# Or manually rename back
mv backend-service backend-app
mv cms-service horizon-tailwind-react-ts-main
mv deployment build-docker
```

## ✅ Migration Checklist

- [ ] Pull latest code
- [ ] Update local IDE projects
- [ ] Update any custom scripts
- [ ] Test all services start correctly
- [ ] Test API endpoints
- [ ] Test CMS access
- [ ] Update documentation references
- [ ] Update CI/CD pipelines (if any)
- [ ] Notify team members

## 📞 Support

If you encounter issues:

1. Check this migration guide
2. Review [TROUBLESHOOTING.md](pronunciation-assessment-service/TROUBLESHOOTING.md)
3. Check [README.md](README.md) for updated commands
4. Create an issue with details

## 🎉 Summary

**What Changed:**
- Directory names (3 renames)
- Deployment structure (better organized)
- Container separation (CMS + Nginx Gateway)

**What Didn't Change:**
- API endpoints
- Service ports
- Functionality
- Environment variables
- Database

**Result:**
- ✅ Clearer structure
- ✅ Better maintainability
- ✅ Easier to understand
- ✅ Professional organization

---

**Migration Date:** 2025-10-06
**Version:** 1.0.0
**Status:** ✅ Completed
