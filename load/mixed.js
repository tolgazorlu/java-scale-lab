// A normal day: dealers browse the price list while others place orders.
// BROWSE (default 30) price list requests/s and RPS (default 10) orders/s, for DURATION (default 60s).
// The summary shows p95 per endpoint, so you can see one endpoint hurting another.
import http from 'k6/http';
import { check } from 'k6';
import { BASE, CATEGORIES, pick, randInt } from './lib.js';

const BROWSE = parseInt(__ENV.BROWSE || '30');
const RPS = parseInt(__ENV.RPS || '10');
const DURATION = __ENV.DURATION || '60s';

export const options = {
  scenarios: {
    browse: { executor: 'constant-arrival-rate', exec: 'browse', rate: BROWSE, timeUnit: '1s',
      duration: DURATION, preAllocatedVUs: 20, maxVUs: 300 },
    orders: { executor: 'constant-arrival-rate', exec: 'order', rate: RPS, timeUnit: '1s',
      duration: DURATION, preAllocatedVUs: 20, maxVUs: 300 },
  },
  thresholds: {
    'http_req_duration{name:list products}': ['p(95)<200'],
    'http_req_duration{name:place order}': ['p(95)<1000'],
    'http_req_failed{name:place order}': ['rate<0.01'],
  },
};

export function browse() {
  const r = http.get(`${BASE}/api/products?category=${pick(CATEGORIES)}`, { tags: { name: 'list products' }, timeout: '30s' });
  check(r, { 'list 200': (x) => x.status === 200 });
}

export function order() {
  const body = JSON.stringify({ dealerId: randInt(1, 2000), lines: [{ productId: randInt(21, 20000), quantity: 1 }] });
  const r = http.post(`${BASE}/api/orders`, body,
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'place order' }, timeout: '30s' });
  check(r, { 'order 201': (x) => x.status === 201 });
}
