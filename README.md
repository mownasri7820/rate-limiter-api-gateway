# Rate Limiter + API Gateway

A learning project for the GUVI SDE AI with MongoDB course. It demonstrates a centralized Spring Boot API gateway with JWT authentication, a per-user Token Bucket rate limiter, backend routing, and MongoDB request monitoring.

## Features
- `POST /auth/login` issues a signed JWT for the configured demo user.
- `/gateway/**` is the single protected entry point. Requests require `Authorization: Bearer <token>`.
- Token Bucket rate limiting is applied per authenticated username. Defaults: capacity 10, refill 10 tokens every 60 seconds.
- Exceeded requests return HTTP `429` and a `Retry-After` header. Missing/invalid tokens return HTTP `401`.
- Gateway requests are logged to MongoDB collection `api_logs`.
- Demo backend routes: users, products, orders.
- Monitoring endpoints return recent logs and basic counters.

## Tech stack
Java 21, Spring Boot 3.5.4, Spring Security, Spring Data MongoDB, MongoDB Atlas, JJWT, Maven, Postman, Spring Boot Actuator.

## Requirements
- JDK 21
- Maven 3.9+
- MongoDB local instance or MongoDB Atlas connection string
- Postman (recommended)

## Run locally
1. Clone the repository.
2. Create a MongoDB database (for example `gatewaydb`) and copy its connection string.
3. Set environment variables before starting the app:
   - `MONGODB_URI`
   - `JWT_SECRET` (at least 32 bytes; use a strong random value)
   - Optional: `DEMO_USERNAME`, `DEMO_PASSWORD`, `RATE_LIMIT_CAPACITY`, `RATE_LIMIT_REFILL_TOKENS`, `RATE_LIMIT_REFILL_SECONDS`, `SERVER_PORT`
4. Run:
   ```bash
   mvn clean test
   mvn spring-boot:run
   ```
5. App base URL: `http://localhost:8080`

For quick local evaluation only, defaults are `demo` / `demo123` and a development JWT secret. Replace them before deployment.

## API examples

### 1. Login
```http
POST /auth/login
Content-Type: application/json

{"username":"demo","password":"demo123"}
```
Copy `accessToken` from the response.

### 2. Call gateway (JWT required)
```http
GET /gateway/users
Authorization: Bearer <accessToken>
```
Other examples:
- `GET /gateway/users/1`
- `GET /gateway/products`
- `GET /gateway/orders`
- `POST /gateway/orders` with JSON body `{"productId":101,"quantity":2}`

### 3. Rate-limit test
With the default capacity of 10, send more than 10 gateway requests quickly using the same token. The request exceeding available tokens returns `429 Too Many Requests`. Change capacity/refill settings with environment variables.

### 4. Monitoring
- `GET /monitoring/logs` – latest 100 gateway request logs.
- `GET /monitoring/stats` – stored log count, 401 count, and 429 count.
- `GET /actuator/health` – application health.

## Architecture
Client -> Spring Boot Gateway -> JWT validation -> Token Bucket limiter -> route to internal demo service -> MongoDB request log.

The demo backend is hosted in the same Spring Boot process under `/internal/**`; gateway requests are forwarded to those internal routes using `RestClient`. This keeps the project runnable without separate infrastructure while still demonstrating gateway routing.

## Design decisions and trade-offs
- **Token Bucket:** permits short bursts up to bucket capacity while replenishing tokens over time. A fixed/sliding window is simpler but may allow less smooth traffic patterns.
- **In-memory buckets:** fast and simple for a single instance. In a multi-instance deployment each instance has independent buckets, so limits are not globally consistent. Production scaling should use Redis with atomic Lua scripts or another shared rate-limit store.
- **MongoDB logs:** flexible document storage for request metadata. Logging failures are caught so a database outage does not prevent API routing.
- **JWT:** stateless authentication avoids a session lookup on every request. Rotate and securely store the signing key in production.
- **Demo credentials:** simple course demonstration only; replace with a persistent user store and password hashing for production.

## Deployment (Render / Railway)
1. Push this project to a GitHub repository.
2. Create a Web Service from the repository.
3. Build command: `mvn clean package -DskipTests`
4. Start command: `java -jar target/rate-limiter-api-gateway-1.0.0.jar`
5. Add environment variables: `MONGODB_URI`, `JWT_SECRET`, `DEMO_USERNAME`, `DEMO_PASSWORD`, `RATE_LIMIT_CAPACITY`, `RATE_LIMIT_REFILL_TOKENS`, `RATE_LIMIT_REFILL_SECONDS`.
6. Deploy and test `/actuator/health`, `/auth/login`, then `/gateway/users`.

Do not commit `.env` files, passwords, database credentials, or production JWT secrets.

## Important limitations
This is a course-level single-instance implementation. Rate-limit state is in memory, monitoring endpoints are open for demonstration, and demo backend services are in-process. Before production use, add role-based access to monitoring, persistent user accounts, shared distributed rate limiting, request-size/time-out controls, route allowlists, and stronger observability.

## Project structure
```text
src/main/java/com/guvi/gateway/
  auth/           JWT service and login controller
  backend/        demo backend APIs
  config/         application properties and security
  gateway/        gateway routing controller
  logging/        MongoDB API log document/repository/filter
  monitoring/     monitoring endpoints
  ratelimit/      Token Bucket algorithm and service
  web/            JWT + rate-limit gateway filter
```

## Postman collection
Import `postman/Rate-Limiter-API-Gateway.postman_collection.json` into Postman. Set collection variables `demoUsername` and `demoPassword` to match your configured demo credentials. Run the Login request first; its test script saves the JWT token for subsequent gateway requests.

## Mapping to the GUVI assessment
See `docs/PROJECT_REQUIREMENTS_MAP.md` for a requirement-by-requirement mapping between the assessment PDF and source files, plus the verification method for each requirement.

The default rate-limit configuration is capacity 100, refill 100 tokens every 60 seconds. These are configurable using environment variables.
