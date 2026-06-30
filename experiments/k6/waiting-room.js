import http from 'k6/http';
import { check, sleep } from 'k6';

const PROFILE = __ENV.PROFILE || 'steady';

export const options = {
  scenarios: scenarioOptions(PROFILE),
  thresholds: {
    http_req_failed: [`rate<${__ENV.HTTP_FAILED_RATE || '0.10'}`],
    http_req_duration: [`p(95)<${__ENV.HTTP_P95_MS || '2500'}`],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USERNAME = __ENV.MANAGER_USERNAME || 'admin';
const PASSWORD = __ENV.MANAGER_PASSWORD || 'admin123';

export function setup() {
  const suffix = `${PROFILE}-${Date.now()}`;
  const login = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
    username: USERNAME,
    password: PASSWORD,
  }), jsonHeaders());
  check(login, { 'login ok': (r) => r.status === 200 });
  const managerToken = login.json('accessToken');

  const resource = http.post(`${BASE_URL}/api/v1/resources`, JSON.stringify({
    name: `resource-${suffix}`,
    totalQuantity: Number(__ENV.RESOURCE_TOTAL || 100),
  }), jsonHeaders(managerToken, `resource-${suffix}`));
  check(resource, { 'resource created': (r) => r.status === 201 });

  const queue = http.post(`${BASE_URL}/api/v1/queues`, JSON.stringify({
    name: `queue-${suffix}`,
    resourceId: resource.json('id'),
    maxQuantityPerParticipant: Number(__ENV.MAX_QUANTITY || 3),
    holdDurationSeconds: Number(__ENV.HOLD_SECONDS || 30),
    workerIntervalMs: 1000,
    maxBatchSize: Number(__ENV.BATCH_SIZE || 20),
  }), jsonHeaders(managerToken, `queue-${suffix}`));
  check(queue, { 'queue created': (r) => r.status === 201 });

  const open = http.post(`${BASE_URL}/api/v1/queues/${queue.json('id')}/open`, null, authHeaders(managerToken));
  check(open, { 'queue open': (r) => r.status === 200 });

  return { queueId: queue.json('id') };
}

export default function (data) {
  const participantKey = `vu-${__VU}-iter-${__ITER}`;
  const quantity = randomIntBetween(1, Number(__ENV.MAX_QUANTITY || 3));
  const joined = http.post(`${BASE_URL}/api/v1/queues/${data.queueId}/entries`, JSON.stringify({
    participantKey,
    quantity,
  }), jsonHeaders(null, `entry-${participantKey}`));

  check(joined, {
    'entry accepted or conflict': (r) => r.status === 201 || r.status === 409,
  });

  if (joined.status !== 201) {
    sleep(1);
    return;
  }

  const entryId = joined.json('id');
  const entryToken = joined.json('entryToken');

  for (let i = 0; i < Number(__ENV.POLL_ATTEMPTS || 10); i++) {
    const entry = http.get(`${BASE_URL}/api/v1/entries/${entryId}`, authHeaders(entryToken));
    check(entry, { 'entry read': (r) => r.status === 200 });

    if (entry.json('state') === 'HOLD_GRANTED') {
      const reservationId = entry.json('reservationId');
      const confirm = http.post(`${BASE_URL}/api/v1/reservations/${reservationId}/confirm`, null,
        jsonHeaders(entryToken, `confirm-${entryId}`));
      check(confirm, { 'reservation confirmed': (r) => r.status === 200 || r.status === 409 });
      return;
    }

    if (['CANCELLED', 'EXPIRED', 'UNFULFILLABLE', 'CONFIRMED'].includes(entry.json('state'))) {
      return;
    }

    sleep(Number(__ENV.POLL_SLEEP_SECONDS || 1));
  }
}

function jsonHeaders(token, idempotencyKey) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (idempotencyKey) {
    headers['Idempotency-Key'] = idempotencyKey;
  }
  return { headers };
}

function authHeaders(token) {
  return { headers: { Authorization: `Bearer ${token}` } };
}

function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function scenarioOptions(profile) {
  if (profile === 'spike') {
    return {
      spike: {
        executor: 'ramping-vus',
        stages: [
          { duration: __ENV.STAGE_RAMP_UP || '30s', target: Number(__ENV.SPIKE_VUS || 80) },
          { duration: __ENV.STAGE_HOLD || '2m', target: Number(__ENV.SPIKE_VUS || 80) },
          { duration: __ENV.STAGE_RAMP_DOWN || '30s', target: 0 },
        ],
      },
    };
  }

  return {
    [profile]: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 5),
      duration: __ENV.DURATION || '1m',
    },
  };
}
