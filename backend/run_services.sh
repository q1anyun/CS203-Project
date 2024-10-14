#!/bin/bash

# Function to check if a service is running on a given port
wait_for_service() {
  local port=$1
  local retries=15
  local wait=5
  local count=0

  echo "Waiting for service on port $port to be ready..."

  # Loop to check if the service is up by checking if the port is open
  while ! nc -z localhost $port; do
    count=$((count+1))
    if [ $count -ge $retries ]; then
      echo "Service on port $port failed to start after $((retries * wait)) seconds."
      exit 1
    fi
    echo "Service not yet available. Retrying in $wait seconds..."
    sleep $wait
  done

  echo "Service on port $port is up and running!"
}

# Function to run a service
run_service() {
    service_name=$1
    echo "Starting $service_name..."
    (cd $service_name && ./gradlew bootRun) &   # Run in the background
}

# Start Spring Cloud Config server using Gradle
echo "Starting Spring Cloud Config server..."
(cd config-server && ./gradlew bootRun) &

# Wait for Config server to be ready (assuming it runs on port 8888)
wait_for_service 8888

# Start the other microservices once Config server is ready
echo "Starting other microservices..."

# Run each service
run_service gateway
run_service auth-service
run_service player-service
run_service user-service
run_service tournament-service
run_service match-service
run_service elo-service
echo "All services started successfully!"

# Wait for all background processes to finish
wait