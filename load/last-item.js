// Race: product 20000 gets stock = 10, then 100 dealers try to buy 1 each at the same moment.
// Correct system: exactly 10 orders succeed and final stock is 0.
// Run ./lab sql "UPDATE products SET stock = 10 WHERE id = 20000" first (./lab race does both).
import http from 'k6/http';
import { BASE, randInt } from './lib.js';
import { Counter } from 'k6/metrics';

const ok = new Counter('orders_accepted');
const rejected = new Counter('orders_rejected');

export const options = {
  scenarios: {
    race: { executor: 'per-vu-iterations', vus: 100, iterations: 1, maxDuration: '30s' },
  },
};

export default function () {
  const r = http.post(`${BASE}/api/orders`,
    JSON.stringify({ dealerId: randInt(1, 2000), lines: [{ productId: 20000, quantity: 1 }] }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'place order' } });
  if (r.status === 201) ok.add(1); else rejected.add(1);
}
