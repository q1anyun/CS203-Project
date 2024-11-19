# Chess Tournament Management System

## Overview
This project is designed to manage chess tournaments using a microservice architecture deployed on AWS. It comprises multiple Spring Boot backend services and a React-based frontend.

## Project Structure
### Microservices
- **config-server**: Centralized configuration service.
- **gateway**: API gateway for routing client requests.
- **auth-service**: Manages authentication and authorization.
- **user-service**: Handles user management.
- **match-service**: Processes match data.
- **player-service**: Maintains player information.
- **s3-upload-service**: Facilitates file uploads to S3.
- **elo-service**: Calculates player ratings using the Elo system.
- **tournament-service**: Manages tournament data.

## Tech Stack

- **Frontend**:
  - ![React](https://img.shields.io/badge/-React-61DAFB?logo=react&logoColor=white&style=flat) React 18+ (with Vite for fast development)
  - ![Material-UI](https://img.shields.io/badge/-MUI-007FFF?logo=mui&logoColor=white&style=flat) MUI (Material-UI) for UI components
  - ![Bootstrap](https://img.shields.io/badge/-Bootstrap-7952B3?logo=bootstrap&logoColor=white&style=flat) Bootstrap

- **Backend**:
  - ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat) Spring Boot 3.3, built using Java 21

- **Database**:
  - ![MySQL](https://img.shields.io/badge/-MySQL-4479A1?logo=mysql&logoColor=white&style=flat) MySQL hosted on Amazon RDS

- **Build Tools**:
  - ![Gradle](https://img.shields.io/badge/-Gradle-02303A?logo=gradle&logoColor=white&style=flat) Gradle for backend services
  - ![npm](https://img.shields.io/badge/-npm-CB3837?logo=npm&logoColor=white&style=flat) npm for frontend package management and build processes

## CICD Pipeline
- **Pipeline Automation**:
  - **GitHub Actions**: Used to automate the build, test, and deployment stages.
  - **Docker**: Facilitates the containerization and management of multi-container applications.
- **Automated Deployment Workflow**:
  1. **Build Phase**: Triggered on push events to the `main` branch. GitHub Actions use Gradle to compile and build JAR files for each microservice.
  2. **Containerization**: Docker Buildx automatically builds Docker images for the microservices and tags them with the appropriate version.
  3. **Push to ECR**: The workflow pushes these images to Amazon ECR.
  4. **Deployment**: GitHub Actions leverage the AWS CLI and AWS ECS deploy actions to update ECS services with the latest image, forcing new deployments and ensuring seamless service restarts.

## Basic Features
- **User Management**: Simple user registration and authentication system.
- **Tournament Management**: Basic CRUD operations for creating, updating, and managing tournaments.
- **Leaderboard**: View player rankings.
- **Profile Page**: Profile pages with player details, statistics and match history.
- **Player Statistics**: Insights on player performance and elo history.
- **Match Management**: Auto generation of matches between players.
- **Tournament Recommendations**: Suggested tournaments based on player Elo ratings and availability.
- **RESTful APIs**: Standardized endpoints to ensure consistent communication between microservices.
- **Database Integration**: Utilizes MySQL for persistent data storage and effective data retrieval.
- **Configuration Management**: Centralized configuration managed through Spring Config Server to streamline property distribution.
- **Error Handling**: Comprehensive logging and error capture across all microservices to identify and address issues effectively.
  
## Advanced Features
- **Microservice Architecture**: Designed for scalability and independent development of service modules.
- **AWS Integration**:
  - **ECS (Fargate)**: Manages container orchestration and scaling.
  - **RDS (MySQL)**: Provides a managed relational database backend.
  - **S3**: Used for hosting and serving frontend assets.
  - **ECR (Elastic Container Registry)**: Secure storage for Docker container images.
  - **Service Discovery**: Employs AWS Cloud Map for efficient service lookup within a private VPC.
  - **Load Balancing**: Uses AWS Application Load Balancer (ALB) for distributing incoming traffic across services.
  - **Monitoring**: Integrated Amazon CloudWatch for logs and metrics.
- **Security**: Robust JWT-based authentication and authorization mechanisms. Access control based on user roles.
- **Algorithms**: Implements advanced Elo calculation for competitive ranking and matchmaking with Swiss and Knockout formats.
- **CICD Pipeline**: Fully automated deployment pipeline using Gradle, Docker, GitHub Workflows, and AWS services.

## Architecture Diagram
![Architecture Diagram](./diagram.png)

## Team Members
- [Philip](https://github.com/philipljh)  
- [Vicki](https://github.com/Milikciv)  
- [Xavier](https://github.com/teystyxavy)
- [Qian Yun](https://github.com/q1anyun)
- [Jia Kai](https://github.com/jiakai-2002)

## Configuration Table
  
   | Components       | Technology                                                                                  | 
   | :---             |    :----:                                                                                   |   
   | Frontend         | ![React](https://img.shields.io/badge/React-18+-blue?logo=react) ![Vite](https://img.shields.io/badge/Vite-Development-purple?logo=vite) ![Bootstrap](https://img.shields.io/badge/Bootstrap-5.0-blue?logo=bootstrap) ![MUI](https://img.shields.io/badge/MUI-Material--UI-blue?logo=mui) |
   | Backend          | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green?logo=springboot) ![Java](https://img.shields.io/badge/Java-21-red?logo=java) |
   | Security         | ![JWT](https://img.shields.io/badge/JWT-Auth-yellow) |
   | Database         | ![MySQL](https://img.shields.io/badge/MySQL-Relational-blue?logo=mysql) |
   | API Documentation| ![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-green?logo=swagger) |
   | Client Build     | ![npm](https://img.shields.io/badge/npm-Build-red?logo=npm) |
   | Server Build     | ![Gradle](https://img.shields.io/badge/Gradle-Building-brightgreen?logo=gradle) |
   | API Testing      | ![Postman](https://img.shields.io/badge/Postman-API%20Testing-orange?logo=postman) |
   | Tool             | ![Visual Studio Code](https://img.shields.io/badge/VS%20Code-Editor-blue?logo=visualstudiocode) |


## Local Copy
A local copy can be found in the local-copy branch. Database script with dummy data can be found in the db folder.
To run, ensure there is an env file in every microservice with the necessary variables.
