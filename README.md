# TinyURL Microservices Project

A distributed URL shortening service built using microservices architecture.

## Architecture

This project consists of several microservices:

- **URL Service**: Handles URL shortening and redirection
- **User Management Service**: Manages user accounts and authentication
- **Analytics Batch Service**: Processes historical analytics data
- **Analytics Realtime Service**: Processes real-time click events
- **Rate Limiting Service**: Controls API usage limits

## Infrastructure

The project uses the following infrastructure components:

- **MySQL**: Master-slave replication for data storage
- **Kafka**: Event streaming for inter-service communication
- **Redis**: Caching and rate limiting
- **Init Runner**: Fully automated database initialization and configuration

## Quick Start

### Starting the Application

```bash
# Start all services
docker-compose up -d

# The init-runner will automatically:
# 1. Set up MySQL master-slave replication
# 2. Initialize database schemas for all microservices
```

### Checking Initialization Status

```bash
# Check initialization logs
docker logs init-runner

# Verify database setup
docker exec -it mysql-master mysql -uroot -proot123 -e "SHOW DATABASES;"
```

## Development

### Prerequisites

- Docker and Docker Compose
- Java 17+ for backend services
- Node.js 18+ for frontend

### Project Structure

- `/tinyUrlBackend` - Java backend services
- `/tiny-url-app` - Next.js frontend application
- `/init-runner` - Database initialization automation
- `/configs` - Configuration files for services
