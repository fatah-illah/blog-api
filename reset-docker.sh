#!/bin/bash

echo "Stopping all containers..."
docker-compose down

echo "Stopping any running postgres containers..."
docker ps -a | grep postgres | awk '{print $1}' | xargs -r docker stop

echo "Removing volume..."
docker volume rm blog-api_postgres_data || true

echo "Building and starting containers..."
docker-compose up --build