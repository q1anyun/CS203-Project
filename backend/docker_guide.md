# Docker Guide

## To run the services, use the following command:
```bash
docker-compose up --build -d
```

## To stop the services, use the following command:
```bash
docker-compose down
```

## To view all running containers, use the following command:
```bash
docker ps
```

## To view logs, use the following command:
## Example: docker-compose logs c48c3074ac74
```bash
docker-compose logs [container_name] or [container_id]
```

## Additional Docker Commands:

### To build a Docker image:
### Example: docker build -t gateway:latest .
```bash
docker build -t [image_name]:[tag] .
```

### To run a container: 
### Example: docker run -d -p 8080:8080 --name gateway gateway:latest
```bash
docker run -d -p [host_port]:[container_port] --name [container_name] [image_name]:[tag]
```

### To remove a container:
```bash
docker rm [container_name] or [container_id]
```

### To remove an image:
```bash
docker rmi [image_name]:[tag] or [image_id]
```

### To check the status of Docker services:
```bash
docker-compose ps
```

### To stop a specific container:
### Example: docker stop c48c3074ac74
```bash
docker stop [container_name] or [container_id]
```

### To start a specific container:
### Example: docker start c48c3074ac74
```bash
docker start [container_name] or [container_id]
```
