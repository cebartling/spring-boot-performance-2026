# Prometheus Metrics Extraction Tool

## Overview

The `extract-metrics.sh` script queries Prometheus to extract single-value performance statistics after K6 load tests. It outputs metrics in blog-friendly formats (JSON, CSV, Markdown) for use in documentation.

## Installation

### One-Time Setup

```bash
cd k6
python3 -m venv .venv
.venv/bin/pip install requests
```

This creates a virtual environment with the required `requests` library.

## Usage

### Basic Commands

```bash
# Extract from last 6 minutes (typical test duration)
./extract-metrics.sh --app webflux --duration 6m --format markdown

# Extract with specific time range
./extract-metrics.sh --app mvc --start "2026-01-30T14:00:00Z" --end "2026-01-30T14:06:00Z" --format json

# Compare both apps side-by-side
./extract-metrics.sh --compare --duration 6m --format markdown

# Save to file
./extract-metrics.sh --app webflux --duration 6m --format markdown --output results.md
```

### Command-Line Options

- `--app {webflux,mvc}` - Application to extract metrics for (required unless --compare)
- `--compare` - Compare both WebFlux and MVC applications
- `--duration DURATION` - Duration to look back (e.g., '6m', '1h', '30s')
- `--start START` - Start time (ISO format: 2026-01-30T14:00:00Z)
- `--end END` - End time (ISO format: 2026-01-30T14:06:00Z)
- `--format {json,csv,markdown}` - Output format (default: markdown)
- `--output OUTPUT` - Output file (default: stdout)

## Output Formats

### Markdown (Default)

Perfect for blog posts and documentation:

```markdown
# Performance Metrics - reactive-webflux
Period: 2026-01-30 14:00:00 - 14:06:00 (6 minutes)

| Metric | Value | Unit |
|--------|-------|------|
| P95 Response Time | 0.245 | s |
| Mean Response Time | 0.123 | s |
| Request Rate | 45.20 | req/s |
| Error Rate | 0.00 | % |
| Thread Count | 12.3 | threads |
| Heap Memory | 256.4 | MB |
| CPU Usage | 15.30 | % |
| GC Pause Time | 2.10 | ms |
| R2DBC Connections | 5.2 | connections |
| Uptime | 2.50 | h |
```

### JSON

Structured data for programmatic use:

```json
{
  "application": "reactive-webflux",
  "period": {
    "start": "2026-01-30T14:00:00Z",
    "end": "2026-01-30T14:06:00Z",
    "duration_minutes": 6.0
  },
  "metrics": {
    "p95_response_time": {"value": 0.245, "unit": "s"},
    "mean_response_time": {"value": 0.123, "unit": "s"},
    ...
  }
}
```

### CSV

For spreadsheet analysis:

```csv
metric,value,unit
p95_response_time,0.245,s
mean_response_time,0.123,s
...
```

## Workflow Examples

### After Manual Testing

```bash
# Start monitoring stack
docker-compose up -d

# Run K6 test
./run-single-test.sh webflux baseline

# Extract metrics (queries last 6 minutes)
./extract-metrics.sh --app webflux --duration 6m --format markdown --output webflux-baseline.md
```

### After Full Test Suite

```bash
# Run all tests
./run-tests-docker.sh

# Extract metrics for each test with specific time ranges
# (Use test start/end times from K6 output or logs)
./extract-metrics.sh --app webflux --start "2026-01-30T14:00:00Z" --end "2026-01-30T14:06:00Z" --format markdown --output webflux-metrics.md
./extract-metrics.sh --app mvc --start "2026-01-30T14:06:30Z" --end "2026-01-30T14:12:30Z" --format markdown --output mvc-metrics.md
```

### Comparison Mode

```bash
# Side-by-side comparison of both apps
./extract-metrics.sh --compare --duration 6m --format markdown --output comparison.md
```

## Metrics Extracted

The script queries the following metrics from Prometheus:

### Common Metrics (Both Apps)

- **P95 Response Time** - 95th percentile latency
- **Mean Response Time** - Average response time
- **Request Rate** - Requests per second
- **Error Rate** - Percentage of 5xx responses
- **Thread Count** - JVM thread count
- **Heap Memory** - JVM heap usage (converted to MB)
- **CPU Usage** - Process CPU usage percentage
- **GC Pause Time** - Garbage collection pause duration
- **Uptime** - Application uptime in hours

### WebFlux-Specific

- **R2DBC Connections** - Acquired R2DBC pool connections

### MVC-Specific

- **HikariCP Connections** - Active HikariCP pool connections

## PromQL Queries

The script uses the exact same PromQL queries as the Grafana dashboards to ensure consistency:

```promql
# P95 Response Time
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application="reactive-webflux"}[5m])) by (le))

# Mean Response Time
sum(rate(http_server_requests_seconds_sum{application="reactive-webflux"}[5m])) / sum(rate(http_server_requests_seconds_count{application="reactive-webflux"}[5m]))

# Request Rate
sum(rate(http_server_requests_seconds_count{application="reactive-webflux"}[5m]))

# Error Rate
(sum(rate(http_server_requests_seconds_count{application="reactive-webflux",status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count{application="reactive-webflux"}[5m]))) * 100
```

All queries use a 5-minute lookback window (`[5m]`) with `avg_over_time` or `rate` functions.

## Troubleshooting

### Prometheus Not Running

```
Error: Cannot connect to Prometheus at http://localhost:9090
Make sure Prometheus is running (try: docker-compose up -d)
```

**Solution**: Start the monitoring stack:
```bash
docker-compose up -d
```

### Missing Metrics (N/A values)

Some metrics may show "N/A" if:
- No data exists for the time range
- The application wasn't running
- No errors occurred (error rate calculation returns null)

This is expected behavior and the script handles it gracefully.

### Python Module Not Found

```
ModuleNotFoundError: No module named 'requests'
```

**Solution**: Install dependencies in the virtual environment:
```bash
cd k6
python3 -m venv .venv
.venv/bin/pip install requests
```

### Invalid Time Format

Use ISO 8601 format with timezone:
```bash
# Correct
--start "2026-01-30T14:00:00Z"

# Also correct (with timezone offset)
--start "2026-01-30T14:00:00+00:00"
```

## Implementation Details

### Query Strategy

The script uses **instant queries** (`/api/v1/query`) with a `time` parameter:
- Queries are executed at the end of the test period
- The `avg_over_time[5m]` and `rate[5m]` functions automatically look back 5 minutes
- This matches the Grafana dashboard behavior

### Time Ranges

- `--duration` mode: Calculates `end_time = now()` and `start_time = now() - duration`
- `--start/--end` mode: Uses exact timestamps provided
- All times are in UTC

### Error Handling

- Checks Prometheus connectivity before querying
- Handles missing metrics gracefully (returns None → "N/A")
- Validates time ranges and duration formats
- Provides clear error messages

## Future Enhancements

Potential improvements:
- Auto-detect test times from K6 JSON output files
- Integration with `run-single-test.sh` for automatic extraction
- Historical comparison across multiple test runs
- Threshold validation and alerts
- HTML report generation with embedded charts
