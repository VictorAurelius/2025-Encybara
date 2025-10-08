#!/bin/bash

# Quick script to restart CMS with new port configuration

echo "Stopping CMS container..."
cd deployment
docker-compose stop cms-service

echo "Removing CMS container..."
docker-compose rm -f cms-service

echo "Starting CMS with new port configuration..."
docker-compose up -d cms-service

echo "Checking CMS status..."
docker ps | grep encybara-cms

echo "CMS should now be available on both port 80 and 3000"