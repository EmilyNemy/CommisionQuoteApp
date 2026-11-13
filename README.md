# Commission Quote Generator

A full-stack web application that captures loan parameters and retrieves a broker commission quote from a simulated, API-key-secured third-party vendor service.

## Architecture & Design Decisions

### 1. Backend-for-Frontend (BFF) Proxy Pattern
Instead of calling the third-party Commission Quote API directly from the browser, the React client sends requests to an internal Spring Boot endpoint (`POST /api/quotes`).
* **Security:** The third-party `api-key` header is injected server-side via `QuoteController` and is never exposed in frontend bundles or browser network logs.
* **Resilience & Decoupling:** Upstream `503 Service Unavailable` or network errors from the external vendor are translated into clean `502 Bad Gateway` responses with user-friendly messages.

### 2. Simulated Third-Party Vendor API (`POST /external-vendor/commission`)
* **Authentication:** Rejects requests missing a valid `api-key` header with `401 Unauthorized`.
* **Chaos & Latency Simulation:** Introduces configurable artificial latency (`vendor.latency.ms`) and a configurable failure rate (`vendor.failure.rate`, defaulting to 25%) to mimic real-world network instability.
* **Financial Precision:** Uses Java `BigDecimal` with explicit `RoundingMode.HALF_UP` in `CommissionCalculatorService` to prevent floating-point rounding inaccuracies.

### 3. Trade-offs & Future Improvements (Live-Coding Ready)
* **Automatic Retries:** Currently, transient vendor failures surface to the UI with a manual **Retry** button. In production, adding Resilience4j or Spring Retry with exponential backoff on `QuoteController` would absorb transient 503s transparently.
* **Persistence & Caching:** Quotes are currently stateless. Adding a persistence layer (e.g., PostgreSQL/JPA) would allow quote history lookups and audit logging.
* **Externalised Secrets:** The API key is configured in `application.properties` for local ease of running; in production, this would be injected via environment variables or a secrets manager.

---

## Project Structure

```text
CommissionQuoteApp/
├── src/                  # Spring Boot 4 (Java) Backend & Mock Vendor API
│   ├── main/java/...     # DTOs, CommissionCalculatorService, QuoteController, MockVendorController
│   └── test/java/...     # JUnit 5 unit tests & MockMvc/MockRestServiceServer integration tests
└── frontend/             # React + TypeScript (Vite) Frontend
    └── src/              # UI components, API client, and Vitest + React Testing Library tests
```

---

## Getting Started

### Prerequisites
* **Java 17+** (or Java 21)
* **Node.js 20+** and **npm**

### 1. Run the Backend
From the project root:
```bash
./gradlew bootRun
```
The backend starts on `http://localhost:8080`.

To run backend unit and integration tests:
```bash
./gradlew test
```

### 2. Run the Frontend
In a separate terminal:
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

To run frontend component tests:
```bash
cd frontend
npm test
```

---

## AI Usage Disclosure

In accordance with the challenge guidelines, AI tools were used as an engineering accelerator during development while maintaining full ownership and verification of all submitted code:
* **Architecture & Scaffolding:** Used AI to brainstorm the BFF proxy separation and scaffold initial Spring Boot DTOs, controllers, and React form boilerplate.
* **Testing & Debugging:** Used AI to generate unit/integration test cases (JUnit 5, `MockMvc`, `MockRestServiceServer`, Vitest) and troubleshoot Spring Boot 4 test-slice imports and Vitest `jsdom` configuration.
* **Review & Ownership:** Every generated file was manually reviewed, tested, and verified end-to-end to ensure full comprehension ahead of the collaborative live-coding session.
