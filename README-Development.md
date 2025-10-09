# Ultimate Company Spring API - Development Setup

## Local Development Environment

This project is configured to run on localhost with the following setup:

- **MySQL Server**: Port 3306
- **Spring Boot API**: Port 4433
- **Database**: UltimateCompanyDatabase

## Quick Start Scripts

### Option 1: Full Setup (Recommended for first time)

This script will check MySQL, start it if needed, build the project, and run the API.

```bash
./run-localhost.sh
```

### Option 2: Quick Start (For development iterations)

This script assumes MySQL is already running and just builds and runs the API.

```bash
./quick-start.sh
```

## Manual Setup (Alternative)

If you prefer to run commands manually:

1. **Start MySQL** (if not running):

   ```bash
   brew services start mysql
   ```

2. **Build the project**:

   ```bash
   cd SpringApi
   ./mvnw clean package -DskipTests
   ```

3. **Run the application**:

   ```bash
   export SPRING_PROFILES_ACTIVE=localhost
   ./mvnw spring-boot:run
   ```

## API Endpoints

Once running, the API will be available at:

- **Base URL**: `http://localhost:4433`
- **Swagger UI**: `http://localhost:4433/swagger-ui.html`

## Database Configuration

The application uses the `localhost` Spring profile which connects to:

- **Host**: localhost
- **Port**: 3306
- **Database**: UltimateCompanyDatabase
- **Username**: root
- **Password**: (empty)

## Troubleshooting

1. **Port 4433 already in use**:

   ```bash
   lsof -i :4433
   kill -9 <PID>
   ```

2. **MySQL connection issues**:
   - Ensure MySQL is running: `brew services list | grep mysql`
   - Check MySQL status: `mysql -u root -e "SELECT 1;"`

3. **Build failures**:

   ```bash
   cd SpringApi
   ./mvnw clean
   ./mvnw dependency:resolve
   ```
