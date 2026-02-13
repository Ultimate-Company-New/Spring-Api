#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR/SpringApi"

if [ -z "${SONAR_HOST_URL-}" ] || [ -z "${SONAR_TOKEN-}" ]; then
  echo "Please set SONAR_HOST_URL and SONAR_TOKEN environment variables."
  echo "Example: SONAR_HOST_URL=http://localhost:9000 SONAR_TOKEN=xxxx ./scan-sonar.sh"
  exit 1
fi

if [ -x "./mvnw" ]; then
  ./mvnw -Dsonar.host.url="${SONAR_HOST_URL}" -Dsonar.login="${SONAR_TOKEN}" -DskipTests sonar:sonar
else
  mvn -Dsonar.host.url="${SONAR_HOST_URL}" -Dsonar.login="${SONAR_TOKEN}" -DskipTests sonar:sonar
fi
