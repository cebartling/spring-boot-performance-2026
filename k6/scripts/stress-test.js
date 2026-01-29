import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '2m', target: 100 },    // Ramp up to 100 users
    { duration: '3m', target: 100 },    // Stay at 100
    { duration: '2m', target: 200 },    // Ramp up to 200
    { duration: '3m', target: 200 },    // Stay at 200
    { duration: '2m', target: 500 },    // Ramp up to 500
    { duration: '3m', target: 500 },    // Stay at 500
    { duration: '5m', target: 1000 },   // Ramp up to 1000
    { duration: '5m', target: 1000 },   // Stay at 1000
    { duration: '5m', target: 0 },      // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],  // More lenient under stress
    errors: ['rate<0.05'],              // Allow 5% error rate
  },
};

export default function () {
  const endpoints = [
    '/api/customers',
    '/api/orders',
  ];

  const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(`${BASE_URL}${endpoint}`);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  errorRate.add(res.status !== 200);
  sleep(0.5);
}
