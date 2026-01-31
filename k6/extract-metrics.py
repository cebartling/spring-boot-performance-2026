#!/usr/bin/env python3
"""
Prometheus Metrics Extraction Tool for K6 Load Test Results

Queries Prometheus for performance metrics after K6 load tests and outputs
results in blog-friendly formats (JSON, CSV, Markdown).

Usage:
    ./extract-metrics.py --app webflux --duration 6m --format markdown
    ./extract-metrics.py --app mvc --start "2026-01-30T14:00:00Z" --end "2026-01-30T14:06:00Z" --format json
    ./extract-metrics.py --compare --duration 6m --format markdown --output comparison.md
"""

import argparse
import json
import sys
from datetime import datetime, timedelta, timezone
from typing import Dict, Optional, Any
from urllib.parse import quote

try:
    import requests
except ImportError:
    print("Error: 'requests' library not found. Install with: pip install requests", file=sys.stderr)
    sys.exit(1)


# Application names in Prometheus
APP_NAMES = {
    "webflux": "reactive-webflux",
    "mvc": "non-reactive-mvc"
}

# Prometheus configuration
PROMETHEUS_URL = "http://localhost:9090"
QUERY_ENDPOINT = f"{PROMETHEUS_URL}/api/v1/query"


def query_prometheus(query: str, time: Optional[datetime] = None) -> Optional[float]:
    """
    Execute an instant query against Prometheus.

    Args:
        query: PromQL query string
        time: Optional timestamp for the query (defaults to now)

    Returns:
        Query result as float, or None if no data or error
    """
    params = {"query": query}
    if time:
        params["time"] = time.isoformat()

    try:
        response = requests.get(QUERY_ENDPOINT, params=params, timeout=10)
        response.raise_for_status()

        data = response.json()

        if data["status"] != "success":
            print(f"Warning: Query failed: {query}", file=sys.stderr)
            return None

        result = data["data"]["result"]
        if not result:
            return None

        # Extract the value from the first result
        value = result[0]["value"][1]

        # Handle special cases
        if value == "NaN" or value == "+Inf" or value == "-Inf":
            return None

        return float(value)

    except requests.exceptions.RequestException as e:
        print(f"Error querying Prometheus: {e}", file=sys.stderr)
        return None
    except (KeyError, ValueError, IndexError) as e:
        print(f"Error parsing Prometheus response: {e}", file=sys.stderr)
        return None


def get_metrics_config(app: str) -> Dict[str, Dict[str, Any]]:
    """
    Get PromQL queries for the specified application.

    Args:
        app: Application short name (webflux or mvc)

    Returns:
        Dictionary of metric configurations with PromQL queries
    """
    app_name = APP_NAMES[app]

    # Common metrics for both applications
    metrics = {
        "p95_response_time": {
            "query": f'histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{{application="{app_name}",uri=~".*"}}[5m])) by (le))',
            "unit": "s",
            "format": ".3f"
        },
        "mean_response_time": {
            "query": f'sum(rate(http_server_requests_seconds_sum{{application="{app_name}"}}[5m])) / sum(rate(http_server_requests_seconds_count{{application="{app_name}"}}[5m]))',
            "unit": "s",
            "format": ".3f"
        },
        "request_rate": {
            "query": f'sum(rate(http_server_requests_seconds_count{{application="{app_name}"}}[5m]))',
            "unit": "req/s",
            "format": ".2f"
        },
        "error_rate": {
            "query": f'(sum(rate(http_server_requests_seconds_count{{application="{app_name}",status=~"5.."}}[5m])) / sum(rate(http_server_requests_seconds_count{{application="{app_name}"}}[5m]))) * 100',
            "unit": "%",
            "format": ".2f"
        },
        "thread_count": {
            "query": f'avg_over_time(jvm_threads_live_threads{{application="{app_name}"}}[5m])',
            "unit": "threads",
            "format": ".1f"
        },
        "heap_memory": {
            "query": f'sum(avg_over_time(jvm_memory_used_bytes{{application="{app_name}",area="heap"}}[5m]))',
            "unit": "bytes",
            "format": ".0f",
            "display_unit": "MB",
            "converter": lambda x: x / (1024 * 1024)
        },
        "cpu_usage": {
            "query": f'avg_over_time(process_cpu_usage{{application="{app_name}"}}[5m]) * 100',
            "unit": "%",
            "format": ".2f"
        },
        "gc_pause_time": {
            "query": f'sum(rate(jvm_gc_pause_seconds_sum{{application="{app_name}"}}[5m])) * 1000',
            "unit": "ms",
            "format": ".2f"
        },
        "uptime": {
            "query": f'process_uptime_seconds{{application="{app_name}"}} / 3600',
            "unit": "h",
            "format": ".2f"
        }
    }

    # Add application-specific metrics
    if app == "webflux":
        metrics["r2dbc_connections"] = {
            "query": f'avg_over_time(r2dbc_pool_acquired_connections{{application="{app_name}"}}[5m])',
            "unit": "connections",
            "format": ".1f"
        }
    elif app == "mvc":
        metrics["hikaricp_connections"] = {
            "query": f'avg_over_time(hikaricp_connections_active{{application="{app_name}"}}[5m])',
            "unit": "connections",
            "format": ".1f"
        }

    return metrics


def collect_metrics(app: str, end_time: datetime) -> Dict[str, Any]:
    """
    Collect all metrics for the specified application.

    Args:
        app: Application short name (webflux or mvc)
        end_time: End time for the query window

    Returns:
        Dictionary containing metrics data
    """
    metrics_config = get_metrics_config(app)
    metrics_data = {}

    for metric_name, config in metrics_config.items():
        value = query_prometheus(config["query"], end_time)

        if value is not None:
            # Apply converter if specified (e.g., bytes to MB)
            if "converter" in config:
                value = config["converter"](value)

            metrics_data[metric_name] = {
                "value": value,
                "unit": config.get("display_unit", config["unit"]),
                "format": config["format"]
            }
        else:
            metrics_data[metric_name] = {
                "value": None,
                "unit": config.get("display_unit", config["unit"]),
                "format": config["format"]
            }

    return metrics_data


def format_value(value: Optional[float], fmt: str) -> str:
    """Format a metric value or return 'N/A' if None."""
    if value is None:
        return "N/A"
    return f"{value:{fmt}}"


def format_markdown(app: str, metrics: Dict[str, Any], start_time: datetime, end_time: datetime) -> str:
    """Format metrics as a Markdown table."""
    duration_minutes = (end_time - start_time).total_seconds() / 60

    # Human-readable metric names
    metric_labels = {
        "p95_response_time": "P95 Response Time",
        "mean_response_time": "Mean Response Time",
        "request_rate": "Request Rate",
        "error_rate": "Error Rate",
        "thread_count": "Thread Count",
        "heap_memory": "Heap Memory",
        "cpu_usage": "CPU Usage",
        "gc_pause_time": "GC Pause Time",
        "r2dbc_connections": "R2DBC Connections",
        "hikaricp_connections": "HikariCP Connections",
        "uptime": "Uptime"
    }

    lines = [
        f"# Performance Metrics - {APP_NAMES[app]}",
        f"Period: {start_time.strftime('%Y-%m-%d %H:%M:%S')} - {end_time.strftime('%H:%M:%S')} ({duration_minutes:.0f} minutes)",
        "",
        "| Metric | Value | Unit |",
        "|--------|-------|------|"
    ]

    for metric_name, data in metrics.items():
        label = metric_labels.get(metric_name, metric_name)
        value_str = format_value(data["value"], data["format"])
        unit = data["unit"]
        lines.append(f"| {label} | {value_str} | {unit} |")

    return "\n".join(lines) + "\n"


def format_json(app: str, metrics: Dict[str, Any], start_time: datetime, end_time: datetime) -> str:
    """Format metrics as JSON."""
    duration_minutes = (end_time - start_time).total_seconds() / 60

    # Convert metrics to simpler format for JSON
    metrics_simple = {}
    for metric_name, data in metrics.items():
        metrics_simple[metric_name] = {
            "value": data["value"],
            "unit": data["unit"]
        }

    output = {
        "application": APP_NAMES[app],
        "period": {
            "start": start_time.isoformat(),
            "end": end_time.isoformat(),
            "duration_minutes": round(duration_minutes, 1)
        },
        "metrics": metrics_simple
    }

    return json.dumps(output, indent=2) + "\n"


def format_csv(app: str, metrics: Dict[str, Any], start_time: datetime, end_time: datetime) -> str:
    """Format metrics as CSV."""
    lines = ["metric,value,unit"]

    for metric_name, data in metrics.items():
        value_str = format_value(data["value"], data["format"])
        unit = data["unit"]
        lines.append(f"{metric_name},{value_str},{unit}")

    return "\n".join(lines) + "\n"


def format_comparison(webflux_metrics: Dict[str, Any], mvc_metrics: Dict[str, Any],
                     start_time: datetime, end_time: datetime) -> str:
    """Format side-by-side comparison of WebFlux and MVC metrics in Markdown."""
    duration_minutes = (end_time - start_time).total_seconds() / 60

    metric_labels = {
        "p95_response_time": "P95 Response Time",
        "mean_response_time": "Mean Response Time",
        "request_rate": "Request Rate",
        "error_rate": "Error Rate",
        "thread_count": "Thread Count",
        "heap_memory": "Heap Memory",
        "cpu_usage": "CPU Usage",
        "gc_pause_time": "GC Pause Time",
        "uptime": "Uptime"
    }

    lines = [
        "# Performance Comparison - WebFlux vs MVC",
        f"Period: {start_time.strftime('%Y-%m-%d %H:%M:%S')} - {end_time.strftime('%H:%M:%S')} ({duration_minutes:.0f} minutes)",
        "",
        "| Metric | WebFlux | MVC | Unit |",
        "|--------|---------|-----|------|"
    ]

    # Common metrics
    for metric_name in metric_labels.keys():
        if metric_name in webflux_metrics and metric_name in mvc_metrics:
            label = metric_labels[metric_name]
            wf_value = format_value(webflux_metrics[metric_name]["value"],
                                   webflux_metrics[metric_name]["format"])
            mvc_value = format_value(mvc_metrics[metric_name]["value"],
                                    mvc_metrics[metric_name]["format"])
            unit = webflux_metrics[metric_name]["unit"]
            lines.append(f"| {label} | {wf_value} | {mvc_value} | {unit} |")

    # Add application-specific metrics
    lines.extend([
        "",
        "## Database Connections",
        "",
        "| Application | Metric | Value | Unit |",
        "|-------------|--------|-------|------|"
    ])

    if "r2dbc_connections" in webflux_metrics:
        value = format_value(webflux_metrics["r2dbc_connections"]["value"],
                           webflux_metrics["r2dbc_connections"]["format"])
        unit = webflux_metrics["r2dbc_connections"]["unit"]
        lines.append(f"| WebFlux | R2DBC Connections | {value} | {unit} |")

    if "hikaricp_connections" in mvc_metrics:
        value = format_value(mvc_metrics["hikaricp_connections"]["value"],
                           mvc_metrics["hikaricp_connections"]["format"])
        unit = mvc_metrics["hikaricp_connections"]["unit"]
        lines.append(f"| MVC | HikariCP Connections | {value} | {unit} |")

    return "\n".join(lines) + "\n"


def parse_duration(duration_str: str) -> timedelta:
    """
    Parse duration string like '6m', '1h', '30s'.

    Args:
        duration_str: Duration string (e.g., '6m', '1h', '30s')

    Returns:
        timedelta object

    Raises:
        ValueError: If duration format is invalid
    """
    duration_str = duration_str.strip()

    if duration_str.endswith('s'):
        return timedelta(seconds=int(duration_str[:-1]))
    elif duration_str.endswith('m'):
        return timedelta(minutes=int(duration_str[:-1]))
    elif duration_str.endswith('h'):
        return timedelta(hours=int(duration_str[:-1]))
    else:
        raise ValueError(f"Invalid duration format: {duration_str}. Use format like '6m', '1h', '30s'")


def check_prometheus_connectivity() -> bool:
    """Check if Prometheus is reachable."""
    try:
        response = requests.get(f"{PROMETHEUS_URL}/-/healthy", timeout=5)
        return response.status_code == 200
    except requests.exceptions.RequestException:
        return False


def main():
    parser = argparse.ArgumentParser(
        description="Extract Prometheus metrics from K6 load test results",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Extract from last 6 minutes
  %(prog)s --app webflux --duration 6m --format markdown

  # Extract with specific time range
  %(prog)s --app mvc --start "2026-01-30T14:00:00Z" --end "2026-01-30T14:06:00Z" --format json

  # Compare both apps
  %(prog)s --compare --duration 6m --format markdown

  # Save to file
  %(prog)s --app webflux --duration 6m --format markdown --output results.md
        """
    )

    parser.add_argument("--app", choices=["webflux", "mvc"],
                       help="Application to extract metrics for (required unless --compare)")
    parser.add_argument("--compare", action="store_true",
                       help="Compare both WebFlux and MVC applications")
    parser.add_argument("--duration",
                       help="Duration to look back (e.g., '6m', '1h')")
    parser.add_argument("--start",
                       help="Start time (ISO format: 2026-01-30T14:00:00Z)")
    parser.add_argument("--end",
                       help="End time (ISO format: 2026-01-30T14:06:00Z)")
    parser.add_argument("--format", choices=["json", "csv", "markdown"],
                       default="markdown",
                       help="Output format (default: markdown)")
    parser.add_argument("--output",
                       help="Output file (default: stdout)")

    args = parser.parse_args()

    # Validate arguments
    if not args.compare and not args.app:
        parser.error("Either --app or --compare must be specified")

    if args.duration and (args.start or args.end):
        parser.error("Cannot specify both --duration and --start/--end")

    if not args.duration and not (args.start and args.end):
        parser.error("Must specify either --duration or both --start and --end")

    if args.compare and args.format == "csv":
        parser.error("CSV format not supported for comparison mode")

    # Check Prometheus connectivity
    if not check_prometheus_connectivity():
        print(f"Error: Cannot connect to Prometheus at {PROMETHEUS_URL}", file=sys.stderr)
        print("Make sure Prometheus is running (try: docker-compose up -d)", file=sys.stderr)
        sys.exit(1)

    # Calculate time range
    if args.duration:
        duration = parse_duration(args.duration)
        end_time = datetime.now(timezone.utc)
        start_time = end_time - duration
    else:
        try:
            start_time = datetime.fromisoformat(args.start.replace('Z', '+00:00'))
            end_time = datetime.fromisoformat(args.end.replace('Z', '+00:00'))
        except ValueError as e:
            print(f"Error parsing time: {e}", file=sys.stderr)
            sys.exit(1)

    # Collect metrics
    if args.compare:
        print("Collecting WebFlux metrics...", file=sys.stderr)
        webflux_metrics = collect_metrics("webflux", end_time)
        print("Collecting MVC metrics...", file=sys.stderr)
        mvc_metrics = collect_metrics("mvc", end_time)

        if args.format == "markdown":
            output = format_comparison(webflux_metrics, mvc_metrics, start_time, end_time)
        elif args.format == "json":
            # For JSON comparison, output both as separate keys
            output = json.dumps({
                "webflux": json.loads(format_json("webflux", webflux_metrics, start_time, end_time)),
                "mvc": json.loads(format_json("mvc", mvc_metrics, start_time, end_time))
            }, indent=2) + "\n"
    else:
        print(f"Collecting {args.app} metrics...", file=sys.stderr)
        metrics = collect_metrics(args.app, end_time)

        if args.format == "markdown":
            output = format_markdown(args.app, metrics, start_time, end_time)
        elif args.format == "json":
            output = format_json(args.app, metrics, start_time, end_time)
        elif args.format == "csv":
            output = format_csv(args.app, metrics, start_time, end_time)

    # Write output
    if args.output:
        with open(args.output, 'w') as f:
            f.write(output)
        print(f"Output written to {args.output}", file=sys.stderr)
    else:
        print(output)


if __name__ == "__main__":
    main()
