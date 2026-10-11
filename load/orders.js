// Steady order traffic: RPS (default 20) new orders per second for 60 s.
// 80% of orders contain one of the 20 campaign products (hot rows).
import http from 'k6/http';
import { check } from 'k6';
import { BASE, DEFAULT_THRESHOLDS, randInt } from './lib.js';

const RPS = parseInt(__ENV.RPS || '20');

export const options = {
  scenarios: {
    orders: {
      executor: 'constant-arrival-rate',
      rate: RPS,
      timeUnit: '1s',
      duration: __ENV.DURATION || '60s',
      preAllocatedVUs: 20,
      maxVUs: 400,
    },
  },
  thresholds: DEFAULT_THRESHOLDS,
};

export default function () {
  const lines = [];
  const n = randInt(1, 4);
  for (let i = 0; i < n; i++) {
    const productId = Math.random() < 0.8 ? randInt(1, 20) : randInt(21, 20000);
    lines.push({ productId, quantity: randInt(1, 3) });
  }
  const r = http.post(`${BASE}/api/orders`, JSON.stringify({ dealerId: randInt(1, 2000), lines }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'place order' } });
  check(r, { '201': (x) => x.status === 201 });
}
