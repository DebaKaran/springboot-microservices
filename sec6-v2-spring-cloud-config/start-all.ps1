# File: start-all.ps1

# 1. Create external network (if not exists)
$networkName = "easybank-net"
$netExists = docker network ls --format "{{.Name}}" | Select-String "^$networkName$"
if (-not $netExists) {
    Write-Host "Creating network $networkName..."
    docker network create $networkName
} else {
    Write-Host "Network $networkName already exists"
}

# 2. Start Kafka stack (docker-compose-kafka.yaml)
Write-Host "Starting Kafka stack..."
docker compose -f .\docker-compose-kafka.yaml up -d --build

# Wait a few seconds to make sure Kafka & Zookeeper are healthy
Start-Sleep -Seconds 10

# 3. Start Microservices + ConfigServer stack (docker-compose.yaml)
Write-Host "Starting microservices + configserver..."
docker compose -f .\docker-compose.yaml  up -d --build

Write-Host "All services started successfully!"
