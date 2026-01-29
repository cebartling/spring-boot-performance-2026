import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Simulates 80% reads, 20% writes
export const options = {
  scenarios: {
    read_customers: {
      executor: 'constant-vus',
      vus: 40,
      duration: '5m',
      exec: 'readCustomers',
    },
    read_orders: {
      executor: 'constant-vus',
      vus: 40,
      duration: '5m',
      exec: 'readOrders',
    },
    write_operations: {
      executor: 'constant-vus',
      vus: 20,
      duration: '5m',
      exec: 'writeOperations',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    errors: ['rate<0.01'],
  },
};

export function readCustomers() {
  const res = http.get(`${BASE_URL}/api/customers`);
  check(res, { 'status is 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  sleep(1);
}

export function readOrders() {
  const res = http.get(`${BASE_URL}/api/orders`);
  check(res, { 'status is 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  sleep(1);
}

export function writeOperations() {
  const payload = JSON.stringify({
    name: `Customer ${Date.now()}`,
    email: `customer${Date.now()}@example.com`,
    address: `${Date.now()} Test Street`,
  });

  const res = http.post(`${BASE_URL}/api/customers`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'status is 201': (r) => r.status === 201 });
  errorRate.add(res.status !== 201);
  sleep(2);
}
