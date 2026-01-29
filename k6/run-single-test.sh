#!/bin/bash

# Quick single test runner using Docker
# Usage: ./run-single-test.sh [webflux|mvc] [baseline|read-heavy|stress-test|spike-test]

GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Get the absolute path to the k6 directory
K6_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Parse arguments
APP_TYPE=${1:-webflux}
SCENARIO=${2:-baseline}

# Set URL based on app type
if [ "$APP_TYPE" = "mvc" ]; then
    APP_URL="http://host.docker.internal:8081"
    APP_NAME="MVC"
else
    APP_URL="http://host.docker.internal:8080"
    APP_NAME="WebFlux"
fi

SCRIPT_FILE="${SCENARIO}.js"

# Validate script exists
if [ ! -f "${K6_DIR}/scripts/${SCRIPT_FILE}" ]; then
    echo -e "${RED}Error: Script ${SCRIPT_FILE} not found${NC}"
    echo "Available scenarios: baseline, read-heavy, stress-test, spike-test"
    exit 1
fi

echo -e "${BLUE}=== K6 Test (Docker) ===${NC}"
echo -e "App: ${GREEN}${APP_NAME}${NC} (${APP_URL})"
echo -e "Scenario: ${GREEN}${SCENARIO}${NC}\n"

# Run K6 in Docker
docker run --rm -i \
    -v "${K6_DIR}/scripts:/scripts:ro" \
    grafana/k6:latest run \
    -e BASE_URL="${APP_URL}" \
    "/scripts/${SCRIPT_FILE}"

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}✓ Test completed successfully${NC}"
else
    echo -e "\n${RED}✗ Test failed${NC}"
    exit 1
fi
