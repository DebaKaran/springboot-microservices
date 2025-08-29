#!/bin/bash

# 1. Create external network (if not exists)
NETWORK_NAME="easybank-net"
if ! docker network ls --format '{{.Name}}' | grep -wq "$NETWORK_NAME"; then
  echo "Creating network $NETWORK_NAME..."
  docker network create $NETWORK_NAME
else
  echo "Network $NETWORK_NAME already exists"
fi

# 2. Start Kafka stack (assume docker-compose-kafka.yml is BU A’s file)
echo "Starting Kafka stack..."
docker compose -f docker-compose-kafka.yaml up -d --build

# 3. Start Microservices + ConfigServer stack (your BU)
echo "Starting microservices + configserver..."
docker compose -f docker-compose.yaml up -d --build
