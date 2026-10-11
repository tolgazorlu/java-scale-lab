// 09:00 - every dealer opens the shop and loads the price list.
// Arrival rate climbs from 10 to RPS (default 300) requests per second, holds, then stops.
// "Arrival rate" means k6 starts N new requests per second no matter how slow the server is,
// exactly like real users who do not wait for each other.
import http from 'k6/http';
import { check } from 'k6';
import { BASE, CATEGORIES, DEFAULT_THRESHOLDS, pick, randInt } from './lib.js';

const RPS = parseInt(__ENV.RPS || '300');

export const options = {
  scenarios: {
    rush: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      maxVUs: 500,
      stages: [
        { target: RPS, duration: '30s' },
        { target: RPS, duration: '60s' },
        { target: 0, duration: '10s' },
      ],
    },
  },
  thresholds: DEFAULT_THRESHOLDS,
};

export default function () {
  if (Math.random() < 0.7) {
    const r = http.get(`${BASE}/api/products?category=${pick(CATEGORIES)}`, { tags: { name: 'list products' } });
    check(r, { '200': (x) => x.status === 200 });
  } else {
    const r = http.get(`${BASE}/api/products/${randInt(1, 20000)}`, { tags: { name: 'get product' } });
    check(r, { '200': (x) => x.status === 200 });
  }
}
