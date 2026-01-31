# K6 Load Testing (Docker-based)

## Quick Start

No K6 installation required! All tests run in Docker containers.

### Run a Single Quick Test

```bash
# Test WebFlux with baseline scenario (default)
./run-single-test.sh

# Test MVC with baseline scenario
./run-single-test.sh mvc

# Test WebFlux with specific scenario
./run-single-test.sh webflux read-heavy

# Test MVC with stress test
./run-single-test.sh mvc stress-test
```

### Run Full Test Suite

Runs all scenarios against both applications:

```bash
./run-tests-docker.sh
```

This will take approximately 30-40 minutes to complete (includes 30-second cooldown between tests).

## Available Scenarios

1. **baseline** - Gradual ramp-up (50-100 users, 5 minutes)
2. **read-heavy** - 80% reads, 20% writes (100 users, 5 minutes)
3. **stress-test** - Progressive stress (100-1000 users, 30 minutes)
4. **spike-test** - Sudden traffic bursts (50-1000 users, 5 minutes)

## Test Applications

- **WebFlux**: http://localhost:8080 (reactive, non-blocking)
- **MVC**: http://localhost:8081 (virtual threads, blocking)

## Results

Full test suite results are saved to `./results/` in JSON format with timestamps:
- `WebFlux-baseline-20260128-123456.json`
- `MVC-baseline-20260128-123456.json`
- etc.

## Monitoring

View real-time metrics during tests:
- **Grafana Dashboard**: http://localhost:3000/d/spring-boot-perf
- **Prometheus**: http://localhost:9090

## Extracting Metrics for Documentation

After running tests, extract performance metrics from Prometheus in blog-friendly formats:

### Setup (one-time)

```bash
# Create virtual environment and install dependencies
cd k6
python3 -m venv .venv
.venv/bin/pip install requests
```

### Usage

```bash
# Extract metrics from last 6 minutes (typical test duration)
./extract-metrics.sh --app webflux --duration 6m --format markdown

# Extract with specific time range
./extract-metrics.sh --app mvc --start "2026-01-30T14:00:00Z" --end "2026-01-30T14:06:00Z" --format json

# Compare both applications side-by-side
./extract-metrics.sh --compare --duration 6m --format markdown

# Save to file
./extract-metrics.sh --app webflux --duration 6m --format markdown --output webflux-baseline.md
```

### Available Formats

- **markdown** - Blog-ready Markdown table (default)
- **json** - Structured JSON for programmatic use
- **csv** - CSV for spreadsheet analysis

### Example Workflow

```bash
# Start monitoring stack
docker-compose up -d

# Run a test
./run-single-test.sh webflux baseline

# Extract metrics immediately after (queries last 6 minutes)
./extract-metrics.sh --app webflux --duration 6m --format markdown --output results/webflux-baseline.md

# View the results
cat results/webflux-baseline.md
```

### Extracted Metrics

- P95 Response Time
- Mean Response Time
- Request Rate
- Error Rate
- Thread Count
- Heap Memory Usage
- CPU Usage
- GC Pause Time
- Database Connections (R2DBC for WebFlux, HikariCP for MVC)
- Application Uptime

## Examples

```bash
# Quick smoke test of both apps
./run-single-test.sh webflux baseline
./run-single-test.sh mvc baseline

# Compare read-heavy performance
./run-single-test.sh webflux read-heavy
./run-single-test.sh mvc read-heavy

# Full comparison (30-40 minutes)
./run-tests-docker.sh
```

## Customization

Edit test parameters in `scripts/*.js`:
- Virtual users (VUs)
- Test duration
- Thresholds (P95 latency, error rate)
- Scenarios and stages

## Troubleshooting

**Docker not running:**
```bash
# Start Docker Desktop
open -a Docker
```

**Services not accessible:**
```bash
# Check services are running
docker-compose ps

# Verify health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

**View K6 Docker logs:**
```bash
# Tests run in ephemeral containers (--rm flag)
# Check output directly in terminal
```
