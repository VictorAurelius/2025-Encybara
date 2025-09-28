# Deployment Guide

## Overview

This guide covers deploying the Pronunciation Assessment Microservice in various environments, from local development to production cloud deployment.

## Deployment Options

### 1. Local Docker Deployment (Recommended for Development)
### 2. Docker Compose Deployment
### 3. Kubernetes Deployment
### 4. Cloud Provider Deployment (AWS, GCP, Azure)
### 5. Traditional Server Deployment

---

## 1. Local Docker Deployment

### Quick Deployment
```bash
# Build and run in one command
cd pronunciation-assessment-service
./scripts/build.sh && ./scripts/run.sh
```

### Manual Deployment
```bash
# Build image
docker build -t pronunciation-assessment .

# Run container
docker run -d \
  --name pronunciation-assessment-container \
  --publish 5000:5000 \
  --memory="3g" \
  --memory-swap="3g" \
  --restart=unless-stopped \
  pronunciation-assessment:latest
```

### Verification
```bash
# Check service health
curl http://localhost:5000/health

# Test assessment endpoint
curl -X POST \
  -F "audio=@sample.wav" \
  -F "transcript=hello world" \
  http://localhost:5000/api/pronunciation-assessment
```

---

## 2. Docker Compose Deployment

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  pronunciation-assessment:
    build: .
    container_name: pronunciation-assessment
    ports:
      - "5000:5000"
    environment:
      - SECRET_KEY=your-production-secret-key
      - FLASK_ENV=production
    memory: 3g
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:5000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    volumes:
      - ./logs:/app/logs  # Optional: persist logs
    
  # Optional: Add nginx reverse proxy
  nginx:
    image: nginx:alpine
    container_name: pronunciation-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro  # SSL certificates
    depends_on:
      - pronunciation-assessment
    restart: unless-stopped

  # Optional: Add monitoring
  prometheus:
    image: prom/prometheus
    container_name: pronunciation-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
    restart: unless-stopped
```

### Nginx Configuration (nginx.conf)
```nginx
events {
    worker_connections 1024;
}

http {
    upstream pronunciation_backend {
        server pronunciation-assessment:5000;
    }

    server {
        listen 80;
        server_name your-domain.com;
        
        # Redirect HTTP to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;

        # Security headers
        add_header X-Content-Type-Options nosniff;
        add_header X-Frame-Options DENY;
        add_header X-XSS-Protection "1; mode=block";

        # Rate limiting
        limit_req_zone $binary_remote_addr zone=api:10m rate=10r/m;

        location /api/pronunciation-assessment {
            limit_req zone=api burst=5 nodelay;
            
            # File upload limits
            client_max_body_size 6M;
            
            proxy_pass http://pronunciation_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Timeout settings for long processing
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 300s;
        }

        location / {
            proxy_pass http://pronunciation_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### Deploy with Docker Compose
```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Scale service (if needed)
docker-compose up -d --scale pronunciation-assessment=3

# Stop services
docker-compose down
```

---

## 3. Kubernetes Deployment

### Deployment Manifest (k8s-deployment.yaml)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pronunciation-assessment
  labels:
    app: pronunciation-assessment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: pronunciation-assessment
  template:
    metadata:
      labels:
        app: pronunciation-assessment
    spec:
      containers:
      - name: pronunciation-assessment
        image: pronunciation-assessment:latest
        ports:
        - containerPort: 5000
        env:
        - name: SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: pronunciation-secrets
              key: secret-key
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "3Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 5000
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /health
            port: 5000
          initialDelaySeconds: 30
          periodSeconds: 10
        volumeMounts:
        - name: temp-storage
          mountPath: /app/temp
      volumes:
      - name: temp-storage
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: pronunciation-assessment-service
spec:
  selector:
    app: pronunciation-assessment
  ports:
  - protocol: TCP
    port: 80
    targetPort: 5000
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: pronunciation-assessment-ingress
  annotations:
    nginx.ingress.kubernetes.io/proxy-body-size: "6m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "300"
    nginx.ingress.kubernetes.io/rate-limit: "10"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
spec:
  tls:
  - hosts:
    - your-domain.com
    secretName: pronunciation-tls
  rules:
  - host: your-domain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: pronunciation-assessment-service
            port:
              number: 80
---
apiVersion: v1
kind: Secret
metadata:
  name: pronunciation-secrets
type: Opaque
data:
  secret-key: <base64-encoded-secret-key>
```

### HorizontalPodAutoscaler (hpa.yaml)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: pronunciation-assessment-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: pronunciation-assessment
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Deploy to Kubernetes
```bash
# Create namespace
kubectl create namespace pronunciation

# Create secrets
kubectl create secret generic pronunciation-secrets \
  --from-literal=secret-key=your-secret-key \
  -n pronunciation

# Deploy application
kubectl apply -f k8s-deployment.yaml -n pronunciation
kubectl apply -f hpa.yaml -n pronunciation

# Check deployment
kubectl get pods -n pronunciation
kubectl get services -n pronunciation
kubectl get ingress -n pronunciation

# View logs
kubectl logs -f deployment/pronunciation-assessment -n pronunciation
```

---

## 4. Cloud Provider Deployment

### AWS ECS Deployment

#### Task Definition (task-definition.json)
```json
{
  "family": "pronunciation-assessment",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "2048",
  "memory": "3072",
  "executionRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::ACCOUNT:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "pronunciation-assessment",
      "image": "your-account.dkr.ecr.region.amazonaws.com/pronunciation-assessment:latest",
      "portMappings": [
        {
          "containerPort": 5000,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SECRET_KEY",
          "value": "your-secret-key"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/pronunciation-assessment",
          "awslogs-region": "us-west-2",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:5000/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

#### Deploy to ECS
```bash
# Build and push to ECR
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 123456789.dkr.ecr.us-west-2.amazonaws.com
docker build -t pronunciation-assessment .
docker tag pronunciation-assessment:latest 123456789.dkr.ecr.us-west-2.amazonaws.com/pronunciation-assessment:latest
docker push 123456789.dkr.ecr.us-west-2.amazonaws.com/pronunciation-assessment:latest

# Create ECS service
aws ecs register-task-definition --cli-input-json file://task-definition.json
aws ecs create-service \
  --cluster your-cluster \
  --service-name pronunciation-assessment \
  --task-definition pronunciation-assessment:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345,subnet-67890],securityGroups=[sg-12345],assignPublicIp=ENABLED}"
```

### Google Cloud Run Deployment

```bash
# Build and deploy
gcloud builds submit --tag gcr.io/PROJECT_ID/pronunciation-assessment
gcloud run deploy pronunciation-assessment \
  --image gcr.io/PROJECT_ID/pronunciation-assessment \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 3Gi \
  --cpu 2 \
  --max-instances 10 \
  --set-env-vars SECRET_KEY=your-secret-key
```

### Azure Container Instances

```bash
# Create resource group
az group create --name pronunciation-rg --location eastus

# Deploy container
az container create \
  --resource-group pronunciation-rg \
  --name pronunciation-assessment \
  --image pronunciation-assessment:latest \
  --cpu 2 \
  --memory 3 \
  --ports 5000 \
  --dns-name-label pronunciation-assessment \
  --environment-variables SECRET_KEY=your-secret-key
```

---

## 5. Traditional Server Deployment

### Ubuntu/Debian Server Setup

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Clone and deploy
git clone <repository-url>
cd pronunciation-assessment-service
./scripts/build.sh
./scripts/run.sh

# Setup systemd service
sudo tee /etc/systemd/system/pronunciation-assessment.service << EOF
[Unit]
Description=Pronunciation Assessment Service
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/bin/docker start pronunciation-assessment-container
ExecStop=/usr/bin/docker stop pronunciation-assessment-container
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl enable pronunciation-assessment
sudo systemctl start pronunciation-assessment
```

---

## Production Configuration

### Environment Variables
```bash
# Production environment
export SECRET_KEY="your-very-secure-secret-key"
export FLASK_ENV="production"
export MAX_CONTENT_LENGTH="6291456"  # 6MB in bytes
export MFA_ACOUSTIC_MODEL="english_us_arpa"
export MFA_DICTIONARY="english_us_arpa"
```

### Security Considerations

#### 1. API Security
- Implement API key authentication
- Use HTTPS with valid SSL certificates
- Add rate limiting (10 requests/minute recommended)
- Validate all inputs thoroughly
- Sanitize file uploads

#### 2. Network Security
- Use firewall rules to restrict access
- Deploy behind reverse proxy (Nginx/Apache)
- Implement DDoS protection
- Use VPN for internal access

#### 3. Container Security
- Run containers as non-root user
- Use minimal base images
- Scan images for vulnerabilities
- Keep dependencies updated
- Implement resource limits

### Monitoring and Logging

#### Prometheus Metrics (prometheus.yml)
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'pronunciation-assessment'
    static_configs:
      - targets: ['pronunciation-assessment:5000']
    metrics_path: '/metrics'  # If implemented
    scrape_interval: 30s
```

#### Log Management
```bash
# Configure log rotation
sudo tee /etc/logrotate.d/pronunciation-assessment << EOF
/var/log/pronunciation-assessment/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    create 644 root root
}
EOF
```

### Backup Strategy

#### 1. Configuration Backup
```bash
# Backup configuration files
tar -czf config-backup-$(date +%Y%m%d).tar.gz \
  docker-compose.yml \
  nginx.conf \
  .env
```

#### 2. Container Image Backup
```bash
# Save Docker image
docker save pronunciation-assessment:latest | gzip > pronunciation-assessment-backup.tar.gz

# Load Docker image
docker load < pronunciation-assessment-backup.tar.gz
```

### Performance Tuning

#### 1. Container Optimization
```yaml
# docker-compose.yml optimizations
services:
  pronunciation-assessment:
    # ... other config ...
    deploy:
      resources:
        limits:
          memory: 3G
          cpus: '2.0'
        reservations:
          memory: 2G
          cpus: '1.0'
    ulimits:
      nofile: 65536
    sysctls:
      - net.core.somaxconn=1024
```

#### 2. OS-level Optimization
```bash
# Increase file limits
echo "* soft nofile 65536" >> /etc/security/limits.conf
echo "* hard nofile 65536" >> /etc/security/limits.conf

# Optimize network settings
echo "net.core.somaxconn = 1024" >> /etc/sysctl.conf
echo "net.core.netdev_max_backlog = 5000" >> /etc/sysctl.conf
sysctl -p
```

### Health Checks and Alerts

#### Health Check Script
```bash
#!/bin/bash
# health-check.sh

HEALTH_URL="http://localhost:5000/health"
WEBHOOK_URL="your-slack-webhook-url"

response=$(curl -s -w "%{http_code}" -o /dev/null $HEALTH_URL)

if [ $response -ne 200 ]; then
    curl -X POST -H 'Content-type: application/json' \
        --data '{"text":"🚨 Pronunciation Assessment Service is DOWN!"}' \
        $WEBHOOK_URL
    exit 1
fi

# Check memory usage
memory_usage=$(curl -s $HEALTH_URL | jq -r '.memory_usage_mb')
if (( $(echo "$memory_usage > 2500" | bc -l) )); then
    curl -X POST -H 'Content-type: application/json' \
        --data "{\"text\":\"⚠️ High memory usage: ${memory_usage}MB\"}" \
        $WEBHOOK_URL
fi
```

### Scaling Considerations

#### Horizontal Scaling
- Deploy multiple container instances
- Use load balancer (HAProxy, Nginx, cloud ALB)
- Implement session affinity if needed
- Consider container orchestration (K8s, Docker Swarm)

#### Vertical Scaling
- Monitor resource usage patterns
- Adjust memory/CPU limits based on load
- Consider GPU acceleration for audio processing
- Optimize MFA model loading

---

## Troubleshooting Production Issues

### Common Issues

1. **High Memory Usage**
   - Check for memory leaks in logs
   - Restart containers periodically
   - Adjust memory limits
   - Monitor garbage collection

2. **Slow Response Times**
   - Check audio file sizes
   - Monitor MFA processing time
   - Verify sufficient CPU/memory
   - Consider request queuing

3. **Container Crashes**
   - Check container logs: `docker logs container-name`
   - Verify resource limits
   - Check for OOM kills: `dmesg | grep -i "killed process"`
   - Monitor disk space

4. **Network Issues**
   - Verify port configurations
   - Check firewall rules
   - Test network connectivity
   - Validate SSL certificates

### Maintenance Procedures

#### Regular Maintenance
```bash
# Weekly maintenance script
#!/bin/bash

# Update system packages
sudo apt update && sudo apt upgrade -y

# Clean Docker artifacts
docker system prune -f

# Restart service
docker-compose restart

# Check health
./scripts/test.sh health

# Backup configuration
tar -czf backup-$(date +%Y%m%d).tar.gz docker-compose.yml nginx.conf
```

#### Update Deployment
```bash
# Zero-downtime update
./scripts/build.sh
docker tag pronunciation-assessment:latest pronunciation-assessment:backup
./scripts/run.sh
# Test new version
# If successful, remove backup
docker rmi pronunciation-assessment:backup
```

---

*For development setup, see [DEVELOPMENT.md](DEVELOPMENT.md). For API usage, see [API.md](API.md).*