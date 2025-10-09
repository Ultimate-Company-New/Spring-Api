#!/bin/bash

# =====================================================
# Ultimate Company Spring API - Local Development Setup
# =====================================================
# This script sets up the complete development environment for localhost
# - Starts MySQL server on port 3306
# - Builds the Spring Boot project
# - Runs tests
# - Runs the API in DEBUG mode (Port 4433) serving traffic on Port 4432

set -e  # Exit on any error

echo "🚀 Starting Ultimate Company Spring API Development Setup..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =====================================================
# STEP 1: Check and Start MySQL Server
# =====================================================
print_status "Checking MySQL server status..."

if pgrep mysqld > /dev/null; then
    print_success "MySQL server is already running"
else
    print_warning "MySQL server is not running. Starting MySQL..."

    # Try to start MySQL using brew services (macOS)
    if command -v brew &> /dev/null; then
        brew services start mysql
        sleep 3
    else
        print_error "Homebrew not found. Please start MySQL manually or install Homebrew."
        print_error "You can start MySQL with: sudo systemctl start mysql (Linux) or brew services start mysql (macOS)"
        exit 1
    fi

    # Verify MySQL started
    if pgrep mysqld > /dev/null; then
        print_success "MySQL server started successfully"
    else
        print_error "Failed to start MySQL server"
        exit 1
    fi
fi

# Test MySQL connection
print_status "Testing MySQL connection..."
if mysql -u root -e "SELECT 1;" &> /dev/null; then
    print_success "MySQL connection successful"
else
    print_error "Cannot connect to MySQL. Please check your MySQL configuration."
    exit 1
fi

# =====================================================
# STEP 2: Build the Project
# =====================================================
print_status "Building Spring Boot project..."

cd SpringApi

# Clean and build the project
if ./mvnw clean package -DskipTests; then
    print_success "Project built successfully"
else
    print_error "Project build failed"
    exit 1
fi

# # =====================================================
# # STEP 3: Run Tests
# # =====================================================
# print_status "Running tests..."

# # Run tests with localhost profile
# if ./mvnw test -Dspring.profiles.active=localhost; then
#     print_success "All tests passed"
# else
#     print_error "Some tests failed"
#     exit 1
# fi

# =====================================================
# STEP 4: Run the Application (IN DEBUG MODE)
# =====================================================
print_status "Starting Spring Boot application in DEBUG mode."
print_status "The application will WAIT for the debugger to attach on port 4433."
print_status "The API will serve traffic on port 4432."

# Set the active profile to localhost
export SPRING_PROFILES_ACTIVE=localhost

# Run the application with JVM Debug Arguments
# Debug port is 4433 (for VS Code)
# suspend=y ensures the JVM waits for the debugger connection
./mvnw spring-boot:run -Drun.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=4433"

print_success "Debugger attached. Application running. API available at http://localhost:4432"
print_status "Press Ctrl+C to stop the application and detach the debugger."


cd ..