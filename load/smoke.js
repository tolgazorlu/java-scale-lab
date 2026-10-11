// Smoke: 1 user, 20 seconds. "Does every endpoint answer at all?"
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE, CATEGORIES, pick, randInt } from './lib.js';

export const options = {
  vus: 1,
  duration: '20s',
  thresholds: { checks: ['rate==1.0'] },
};

export default function () {
  check(http.get(`${BASE}/api/products?category=${pick(CATEGORIES)}`, { tags: { name: 'list products' } }),
    { 'list 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/products/${randInt(1, 20000)}`, { tags: { name: 'get product' } }),
    { 'get 200': (r) => r.status === 200 });
  check(http.get(`${BASE}/api/dealers/${randInt(1, 2000)}/orders?limit=10`, { tags: { name: 'dealer orders' } }),
    { 'history 200': (r) => r.status === 200 });
  const body = JSON.stringify({ dealerId: randInt(1, 2000), lines: [{ productId: randInt(1, 20), quantity: 1 }] });
  check(http.post(`${BASE}/api/orders`, body, { headers: { 'Content-Type': 'application/json' }, tags: { name: 'place order' } }),
    { 'order 201': (r) => r.status === 201 });
  sleep(0.5);
}
