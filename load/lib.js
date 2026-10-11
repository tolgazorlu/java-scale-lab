// Shared helpers for all load scenarios.
export const BASE = __ENV.BASE_URL || 'http://host.docker.internal:4180';

export const CATEGORIES = ['icecek', 'atistirmalik', 'temizlik', 'kisisel-bakim', 'kirtasiye',
  'sut-urunleri', 'konserve', 'dondurulmus', 'bebek', 'evcil-hayvan'];

export function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

export function randInt(min, max) {
  return min + Math.floor(Math.random() * (max - min + 1));
}

// Thresholds = pass/fail rules. A red threshold makes `./lab load` exit non-zero.
export const DEFAULT_THRESHOLDS = {
  http_req_failed: ['rate<0.01'],
  http_req_duration: ['p(95)<500'],
};
