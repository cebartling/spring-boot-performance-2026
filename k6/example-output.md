# Example Output - extract-metrics.sh

This file shows example outputs from the `extract-metrics.sh` tool.

## Markdown Format (Single Application)

```bash
./extract-metrics.sh --app webflux --duration 6m --format markdown
```

**Output:**
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

## JSON Format (Single Application)

```bash
./extract-metrics.sh --app mvc --duration 6m --format json
```

**Output:**
```json
{
  "application": "non-reactive-mvc",
  "period": {
    "start": "2026-01-30T14:00:00+00:00",
    "end": "2026-01-30T14:06:00+00:00",
    "duration_minutes": 6.0
  },
  "metrics": {
    "p95_response_time": {
      "value": 0.198,
      "unit": "s"
    },
    "mean_response_time": {
      "value": 0.105,
      "unit": "s"
    },
    "request_rate": {
      "value": 52.3,
      "unit": "req/s"
    },
    "error_rate": {
      "value": 0.0,
      "unit": "%"
    },
    "thread_count": {
      "value": 45.2,
      "unit": "threads"
    },
    "heap_memory": {
      "value": 312.8,
      "unit": "MB"
    },
    "cpu_usage": {
      "value": 18.5,
      "unit": "%"
    },
    "gc_pause_time": {
      "value": 3.2,
      "unit": "ms"
    },
    "hikaricp_connections": {
      "value": 8.1,
      "unit": "connections"
    },
    "uptime": {
      "value": 2.5,
      "unit": "h"
    }
  }
}
```

## CSV Format (Single Application)

```bash
./extract-metrics.sh --app webflux --duration 6m --format csv
```

**Output:**
```csv
metric,value,unit
p95_response_time,0.245,s
mean_response_time,0.123,s
request_rate,45.20,req/s
error_rate,0.00,%
thread_count,12.3,threads
heap_memory,256.4,MB
cpu_usage,15.30,%
gc_pause_time,2.10,ms
r2dbc_connections,5.2,connections
uptime,2.50,h
```

## Comparison Format (Markdown)

```bash
./extract-metrics.sh --compare --duration 6m --format markdown
```

**Output:**
```markdown
# Performance Comparison - WebFlux vs MVC
Period: 2026-01-30 14:00:00 - 14:06:00 (6 minutes)

| Metric | WebFlux | MVC | Unit |
|--------|---------|-----|------|
| P95 Response Time | 0.245 | 0.198 | s |
| Mean Response Time | 0.123 | 0.105 | s |
| Request Rate | 45.20 | 52.30 | req/s |
| Error Rate | 0.00 | 0.00 | % |
| Thread Count | 12.3 | 45.2 | threads |
| Heap Memory | 256.4 | 312.8 | MB |
| CPU Usage | 15.30 | 18.50 | % |
| GC Pause Time | 2.10 | 3.20 | ms |
| Uptime | 2.50 | 2.50 | h |

## Database Connections

| Application | Metric | Value | Unit |
|-------------|--------|-------|------|
| WebFlux | R2DBC Connections | 5.2 | connections |
| MVC | HikariCP Connections | 8.1 | connections |
```

## Comparison Format (JSON)

```bash
./extract-metrics.sh --compare --duration 6m --format json
```

**Output:**
```json
{
  "webflux": {
    "application": "reactive-webflux",
    "period": {
      "start": "2026-01-30T14:00:00+00:00",
      "end": "2026-01-30T14:06:00+00:00",
      "duration_minutes": 6.0
    },
    "metrics": {
      "p95_response_time": {"value": 0.245, "unit": "s"},
      "mean_response_time": {"value": 0.123, "unit": "s"},
      "request_rate": {"value": 45.2, "unit": "req/s"},
      "error_rate": {"value": 0.0, "unit": "%"},
      "thread_count": {"value": 12.3, "unit": "threads"},
      "heap_memory": {"value": 256.4, "unit": "MB"},
      "cpu_usage": {"value": 15.3, "unit": "%"},
      "gc_pause_time": {"value": 2.1, "unit": "ms"},
      "r2dbc_connections": {"value": 5.2, "unit": "connections"},
      "uptime": {"value": 2.5, "unit": "h"}
    }
  },
  "mvc": {
    "application": "non-reactive-mvc",
    "period": {
      "start": "2026-01-30T14:00:00+00:00",
      "end": "2026-01-30T14:06:00+00:00",
      "duration_minutes": 6.0
    },
    "metrics": {
      "p95_response_time": {"value": 0.198, "unit": "s"},
      "mean_response_time": {"value": 0.105, "unit": "s"},
      "request_rate": {"value": 52.3, "unit": "req/s"},
      "error_rate": {"value": 0.0, "unit": "%"},
      "thread_count": {"value": 45.2, "unit": "threads"},
      "heap_memory": {"value": 312.8, "unit": "MB"},
      "cpu_usage": {"value": 18.5, "unit": "%"},
      "gc_pause_time": {"value": 3.2, "unit": "ms"},
      "hikaricp_connections": {"value": 8.1, "unit": "connections"},
      "uptime": {"value": 2.5, "unit": "h"}
    }
  }
}
```

## Notes

- All example values are mock data for demonstration purposes
- Actual values will vary based on test load, duration, and application performance
- "N/A" may appear for metrics with no data (e.g., error_rate when no errors occurred)
