// The train goes into a tunnel: the app sends an order, gets no answer, and sends it again
// with the same Idempotency-Key. 50 orders, each submitted twice, all for dealer 2000.
// Correct system: exactly 50 orders written. Run it with ./lab double.
import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { BASE, randInt } from './lib.js';

const created = new Counter('orders_accepted');
const replayed = new Counter('orders_rejected');

export const options = {
  scenarios: { tunnel: { executor: 'per-vu-iterations', vus: 10, iterations: 5, maxDuration: '60s' } },
};

export default function () {
  const key = `${__VU}-${__ITER}-${Date.now()}-${randInt(0, 1e9)}`;
  const body = JSON.stringify({ dealerId: 2000, lines: [{ productId: randInt(21, 20000), quantity: 1 }] });
  const params = { headers: { 'Content-Type': 'application/json', 'Idempotency-Key': key }, tags: { name: 'place order' } };
  for (let attempt = 0; attempt < 2; attempt++) {
    const r = http.post(`${BASE}/api/orders`, body, params);
    if (r.status === 201) created.add(1); else replayed.add(1);
  }
}
