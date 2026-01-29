#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}=== Spring Boot Performance Comparison - K6 Tests (Docker) ===${NC}\n"

# Get the absolute path to the k6 directory
K6_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Ensure results directory exists
mkdir -p "${K6_DIR}/results"

# Test both applications with all scenarios
APPS=("http://host.docker.internal:8080" "http://host.docker.internal:8081")
APP_NAMES=("WebFlux" "MVC")
SCENARIOS=("baseline.js" "read-heavy.js" "stress-test.js" "spike-test.js")

for i in "${!APPS[@]}"; do
    APP_URL="${APPS[$i]}"
    APP_NAME="${APP_NAMES[$i]}"

    echo -e "${GREEN}Testing ${APP_NAME} at ${APP_URL}${NC}\n"

    for scenario in "${SCENARIOS[@]}"; do
        echo -e "${BLUE}Running scenario: ${scenario}${NC}"

        RESULT_FILE="${K6_DIR}/results/${APP_NAME}-${scenario%.js}-$(date +%Y%m%d-%H%M%S).json"

        docker run --rm -i \
            -v "${K6_DIR}/scripts:/scripts:ro" \
            -v "${K6_DIR}/results:/results" \
            grafana/k6:latest run \
            -e BASE_URL="${APP_URL}" \
            --out json="/results/$(basename ${RESULT_FILE})" \
            "/scripts/${scenario}"

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ ${scenario} completed${NC}\n"
        else
            echo -e "${RED}✗ ${scenario} failed${NC}\n"
        fi

        # Cool-down period between tests
        echo "Cooling down for 30 seconds..."
        sleep 30
    done

    echo -e "\n"
done

echo -e "${GREEN}All tests completed!${NC}"
echo "Results saved in ${K6_DIR}/results/"
