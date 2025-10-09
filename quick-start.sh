#!/bin/bash

# =====================================================
# Quick Start - Ultimate Company Spring API
# =====================================================
# Runs the API on port 4433 (assumes MySQL is already running)
# Usage: ./quick-start.sh

set -e

echo "🚀 Quick Starting Ultimate Company Spring API..."

cd SpringApi

# Set the active profile to localhost
export SPRING_PROFILES_ACTIVE=localhost

echo "📍 API will be available at http://localhost:4433"
echo "🛑 Press Ctrl+C to stop"

# Run the application
./mvnw spring-boot:run