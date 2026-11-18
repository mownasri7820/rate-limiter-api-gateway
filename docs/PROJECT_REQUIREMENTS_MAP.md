# GUVI PDF requirements map

| PDF requirement | Project implementation | Verification |
|---|---|---|
| Central API entry point | `GatewayController` under `/gateway/**` | Call `/gateway/users`, `/gateway/products`, `/gateway/orders` |
| Route to dummy/internal services | `DemoBackendController` under `/internal/**`; gateway forwards to it | Compare gateway response with internal demo data |
| Filters/interceptors | `GatewayFilter` and `RequestLoggingFilter` | Send a gateway request with and without JWT |
| Per-user rate limiting | `RateLimiterService` + `TokenBucket`; key is authenticated username | Send requests rapidly using one token |
| HTTP 429 | `GatewayFilter` returns 429 and `Retry-After` | Exceed configured capacity |
| Configurable limit | `RATE_LIMIT_CAPACITY`, `RATE_LIMIT_REFILL_TOKENS`, `RATE_LIMIT_REFILL_SECONDS` | Change environment values and restart |
| JWT authentication | `AuthController` + `JwtService` | Login and use returned Bearer token |
| HTTP 401 | `GatewayFilter` rejects missing/invalid/expired JWT | Call gateway without token |
| Request logging | `ApiLog`, `ApiLogRepository`, `RequestLoggingFilter` | Inspect MongoDB `api_logs` collection |
| Required log fields | Timestamp, user, IP, endpoint, status, duration, violation flag | Inspect saved documents |
| MongoDB | Spring Data MongoDB configured by `MONGODB_URI` | Start app with valid Atlas/local URI |
| Layered architecture | Separate auth, gateway, backend, config, logging, monitoring, ratelimit, web packages | Inspect source tree |
| System design explanation | README design trade-offs section | Explain Token Bucket, in-memory state and distributed limitation |
| GitHub and README | README included | Push repository and confirm README renders |
| Deployment | Dockerfile and deployment instructions included | Deploy and test public URL |
| Environment variables / no secrets | `.env.example`, `.gitignore`, external env configuration | Check GitHub for accidental credentials |

## Important scope notes

- The PDF says “e.g. 100 requests per minute”; this project defaults to capacity 100 and refill 100 tokens per 60 seconds. All three values are configurable.
- The implementation uses an in-memory Token Bucket per authenticated username. It is appropriate for a single running instance, not a globally consistent multi-instance deployment. A production distributed version should use a shared store such as Redis with atomic updates.
- Demo login credentials are for local learning only. Change them through environment variables before deployment. Do not use real personal passwords.
- The Postman collection is a starting test suite. Set `demoUsername` and `demoPassword` collection variables to match the environment before running Login.
- The project must still be built and tested on the learner's machine and deployed before it can be called submission-ready.
