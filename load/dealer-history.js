// Dealers open "my last orders". RPS (default 50) requests per second for 60 s.
import http from 'k6/http';
import { check } from 'k6';
import { BASE, DEFAULT_THRESHOLDS, randInt } from './lib.js';

const RPS = parseInt(__ENV.RPS || '50');

export const options = {
  scenarios: {
    history: {
      executor: 'constant-arrival-rate',
      rate: RPS,
      timeUnit: '1s',
      duration: __ENV.DURATION || '60s',
      preAllocatedVUs: 20,
      maxVUs: 300,
    },
  },
  thresholds: DEFAULT_THRESHOLDS,
};

export default function () {
  const r = http.get(`${BASE}/api/dealers/${randInt(1, 2000)}/orders?limit=20`, { tags: { name: 'dealer orders' } });
  check(r, { '200': (x) => x.status === 200 });
}
