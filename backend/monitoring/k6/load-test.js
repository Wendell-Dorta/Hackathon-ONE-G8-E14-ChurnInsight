import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 }, // Ramp up to 10 users
    { duration: '1m', target: 50 },  // Peak load test (50 concurrent users)
    { duration: '30s', target: 0 },  // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% das requests devem responder em menos de 500ms
    http_req_failed: ['rate<0.01'],   // Menos de 1% de erro
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000/api';
// Default credential admin:Admin123 -> Base64 YWRtaW46QWRtaW4xMjM=
const AUTH_HEADER = 'Basic YWRtaW46QWRtaW4xMjM=';

export default function () {
  const payload = JSON.stringify({
      "userId": `user_loadtest_${__VU}_${__ITER}`,
      "creditScore": 650,
      "geography": "France",
      "gender": "Female",
      "age": 42,
      "tenure": 5,
      "balance": 0.0,
      "numOfProducts": 1,
      "hasCrCard": 1,
      "isActiveMember": 1,
      "estimatedSalary": 101348.88
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': AUTH_HEADER,
    },
  };

  const res = http.post(`${BASE_URL}/predict`, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'prediction returned': (r) => r.json('prediction') !== undefined,
  });

  sleep(1);
}

