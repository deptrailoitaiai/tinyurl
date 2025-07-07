# TinyURL Init Runner

This container is responsible for automating the initialization process of the TinyURL microservices project's database setup. It automates the following tasks:

1. Waiting for MySQL master, slave, and Redis instances to be healthy and ready
2. Setting up MySQL master-slave replication
3. Running the schema initialization scripts
4. Validating the setup with comprehensive checks

## How It Works

The init-runner container starts after all required services (mysql-master, mysql-slave, and redis) are healthy. It performs the following steps:

1. Waits for all dependent services to be ready and accepting connections
2. Sets up the replication between master and slave
3. Verifies the replication is working correctly with a test database
4. Applies the schema.sql to create all necessary databases and tables
5. Validates that all databases and tables are correctly created
6. Confirms the replication of schemas to the slave

## Key Features

- **Cross-Platform Compatibility**: Works on any platform that supports Docker
- **Automatic Initialization**: No need to manually run scripts
- **Health Check Integration**: Only runs when dependent services are healthy
- **Enhanced Validation**: Comprehensive checks to ensure correct setup
- **Visual Feedback**: Color-coded output for easier monitoring
- **Persistent Logs**: Keeps the container running to review logs if needed

## Directory Structure

```
init-runner/
├── Dockerfile             # Container configuration
└── scripts/
    ├── entrypoint.sh      # Main entry point script
    ├── setup-replication.sh # Configures MySQL replication
    ├── run-schema.sh      # Runs the schema on MySQL master
    └── schema.sql         # Database schema definition
```

## Usage

The init-runner is automatically started as part of the docker-compose deployment. You don't need to manually start it.

```bash
docker-compose up -d
```

To check logs for the initialization process:

```bash
docker logs init-runner
```

## Database Structure

The initialization process creates the following microservice databases:

1. **user_management_service**
   - `users` - User account information

2. **url_shortener_service**
   - `urls` - Short URL mappings
   - `service_references` - Cross-service references

3. **analytics_batch_service**
   - `url_daily_stats` - Aggregated daily click statistics
   - `service_references` - Cross-service references

4. **analytics_realtime_service**
   - `click_events` - Individual click events with foreign keys
   - `devices` - Device information
   - `locations` - Geographic location data
   - `service_references` - Cross-service references

The init-runner validates that all these tables are correctly created and that the proper foreign key constraints are in place.
