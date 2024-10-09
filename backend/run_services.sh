#!/bin/bash

# Function to run the service
run_service() {
    service_name=$1
    echo "Starting $service_name..."
    (cd $service_name && ./gradlew bootrun) &   # Run in background
}

# Run each service
run_service user-service
run_service tournament-service
run_service matchmaking-service
run_service result-service
run_service leaderboard-service
run_service player-service

wait  # Wait for all background processes to finish
