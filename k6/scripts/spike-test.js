import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '30s', target: 50 },    // Normal load
    { duration: '10s', target: 500 },   // Sudden spike
    { duration: '1m', target: 500 },    // Sustained spike
    { duration: '10s', target: 50 },    // Drop to normal
    { duration: '1m', target: 50 },     // Normal load
    { duration: '10s', target: 1000 },  // Massive spike
    { duration: '1m', target: 1000 },   // Sustained massive spike
    { duration: '30s', target: 0 },     // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // Lenient during spikes
    errors: ['rate<0.1'],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/api/customers`);
  check(res, { 'status is 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  sleep(0.3);
}
