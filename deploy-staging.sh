#!/bin/bash
set -e

echo "Building Docker images..."
docker compose build

echo "Starting containers..."
docker compose up -d

echo "Deployment complete!"