#!/bin/bash

set -e

# Create a Docker network for Grafana and Prometheus to communicate
docker network create monitoring || true

docker volume create grafana_data

docker run -d \
  --name grafana \
  --restart=always \
  --network monitoring \
  -p 3000:3000 \
  -v grafana_data:/var/lib/grafana \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  grafana/grafana:latest

