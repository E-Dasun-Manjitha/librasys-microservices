# LibraSys - Smart Library and Digital Archive Management System

A distributed microservices system built with Spring Boot 3, Spring Cloud Gateway, Netflix Eureka Service Discovery, React 18, MongoDB, Redis, and Docker.

---

## 1. System Architecture

```
                                 +-------------------------------+
                                 |   Frontend Client App         |
                                 |   (React 18 + Vite - Port 3000)|
                                 +---------------+---------------+
                                                 |
                                                 | HTTP Requests / REST
                                                 v
+---------------------------------------------------------------------------------------------+
|                             API GATEWAY (Spring Cloud - Port 8080)                          |
|  * JWT Auth Filter          * Redis Rate Limiting (5 req/s)       * Global CORS (Port 3000) |
|  * Central Swagger UI Hub   * Dynamic Eureka Route Locator        * Path-Based Routing      |
+---------------+-------------------+-------------------+-------------------+-----------------+
                |                   |                   |                   |
                v                   v                   v                   v
      +------------------+  +------------------+  +------------------+  +------------------+
      |   Auth Service   |  |   Book Service   |  |   Loan Service   |  | Reservation Svc  |
      |   (Port 8081)    |  |   (Port 8082)    |  |   (Port 8083)    |  |   (Port 8084)    |
      +---------+--------+  +---------+--------+  +---------+--------+  +---------+--------+
                |                   |                   |                   |
                |                   |                   +-------------------+
                |                   |                   |  (Inter-Service REST Calls via RestTemplate)
                |                   |                   v
                |                   |         +------------------+
                |                   |         | Notification Svc |
                |                   |         |   (Port 8085)    |
                |                   |         +---------+--------+
                |                   |                   |
  +-------------+-------------------+-------------------+-------------------+-------------+
  |                                                                                       |
  |                             NETFLIX EUREKA SERVICE REGISTRY                           |
  |                                       (Port 8761)                                     |
  |      * Dynamic Service Discovery * Heartbeat Health Monitoring * Client-Side LB       |
  +---------------------------------------------------------------------------------------+
                                                 |
                       +-------------------------+-------------------------+
                       v                                                   v
       +-------------------------------+                   +-------------------------------+
       │   Dedicated MongoDB Instances │                   │   Redis In-Memory Cache       │
       │   (Ports 27019 - 27023)       │                   │   (Port 6379)                 │
       │   * Isolated DB per service   │                   │   * Reactive Rate Limiting    │
       │   * api_keys Collections      │                   │   * 5 req/sec + Burst 10      │
       +-------------------------------+                   +-------------------------------+
```

---

## 2. Team Member Work Breakdown Matrix

| Member Name | Role | Microservice | Database Port | Key Responsibilities & Endpoints |
|---|---|---|:---:|---|
| **E. Dasun Manjitha** (Lead) | Gateway & Security Lead | **API Gateway & Auth Service** | `27019` | Central Routing, JWT Filter, Redis Rate Limiting, Global CORS, Central Swagger Hub, `/api/auth/register`, `/api/auth/login`, `/api/members/{id}` |
| **W.G.C.M. Nimsara** | Backend Engineer | **Book Service** | `27020` | Catalog Management, `ApiKeyFilter`, Inventory tracking, DataLoader Seeding, `/api/books`, `/api/books/{id}`, `/api/books/{id}/decrement`, `/api/books/{id}/increment` |
| **W.A.S.I. Wijesinghe** | Backend Engineer | **Loan Service** | `27021` | Borrowing & Returning, Inter-Service RestTemplate calls to Book Service & Notification Service, Due Date calculation, `/api/loans`, `/api/loans/{id}/return`, `/api/loans/member/{id}`, `/api/loans/overdue` |
| **R.G.D.N. Wijesuriya** | Backend Engineer | **Reservation Service** | `27022` | Reservation Queuing, Stock Checking, Inter-Service Notification dispatch, `/api/reservations`, `/api/reservations/{id}`, `/api/reservations/member/{id}`, `/api/reservations/{id}/notify` |
| **R.T. Dinith Sasanga** | Full-Stack Engineer | **Notification Service & Client App** | `27023` | History Logging, Simulated Email & Alerts, React 18 SPA Frontend, `/api/notify/email`, `/api/notify/due-reminder`, `/api/notify/history/{memberId}` |

---

## 3. Services Overview & Port Allocation

| Component / Service | Host Port | Database / Cache | Direct API Key | Description |
|---|:---:|---|---|---|
| **Frontend Client App** | `3000` | Browser State / LocalStorage | - | React 18 + Vite SPA interface for end users |
| **API Gateway** | `8080` | Redis (`6379`) | - | Central API Gateway, JWT validation, Redis Rate Limiting, Central Swagger Hub |
| **Eureka Server** | `8761` | In-Memory Registry | - | Netflix Eureka Service Discovery Registry |
| **Auth Service** | `8081` | MongoDB (`27019`) | `auth-service-key-2026` | Member registration, BCrypt password hashing, JWT token issuance |
| **Book Service** | `8082` | MongoDB (`27020`) | `book-service-key-2026` | Book catalog CRUD, inventory tracking, stock increment/decrement |
| **Loan Service** | `8083` | MongoDB (`27021`) | `loan-service-key-2026` | Book loan lifecycle, overdue checking, inter-service alerts |
| **Reservation Service** | `8084` | MongoDB (`27022`) | `reservation-service-key-2026` | Reserving out-of-stock books, fulfillment alerts |
| **Notification Service** | `8085` | MongoDB (`27023`) | `notification-service-key-2026` | Email & overdue reminder alert log management |

---

## 4. MongoDB Compass Database Connections

Each microservice utilizes its own isolated MongoDB database instance on dedicated ports:

| Microservice | Compass Connection URI | Database Name | Collections |
|---|---|---|---|
| **Auth Service** | `mongodb://localhost:27019` | `librasys_auth` | `members`, `api_keys` |
| **Book Service** | `mongodb://localhost:27020` | `librasys_books` | `books`, `api_keys` |
| **Loan Service** | `mongodb://localhost:27021` | `librasys_loans` | `loans`, `api_keys` |
| **Reservation Service** | `mongodb://localhost:27022` | `librasys_reservations` | `reservations`, `api_keys` |
| **Notification Service** | `mongodb://localhost:27023` | `librasys_notifications` | `notifications`, `api_keys` |

> **Note:** The authorized API keys for each microservice are stored in the database under the **`api_keys`** collection and verified dynamically at runtime.

---

## 5. API Key Security & Direct Access Enforcement

To satisfy Section 2.A (API Key Security) of the assignment guidelines:
- Every microservice enforces an `ApiKeyFilter`.
- Direct requests to backend ports (`8081`-`8085`) **MUST** include the `X-API-KEY` header.
- Requests without the key are rejected immediately with `HTTP 401 Unauthorized`.

### Required API Key Headers:
```http
X-API-KEY: auth-service-key-2026         # For Auth Service (Port 8081)
X-API-KEY: book-service-key-2026         # For Book Service (Port 8082)
X-API-KEY: loan-service-key-2026         # For Loan Service (Port 8083)
X-API-KEY: reservation-service-key-2026  # For Reservation Service (Port 8084)
X-API-KEY: notification-service-key-2026 # For Notification Service (Port 8085)
```

---

## 6. Swagger UI & Interactive Documentation

| Documentation Portal | URL | Description |
|---|---|---|
| **Central Swagger Hub (Gateway)** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Single hub aggregating all 5 microservices in a dropdown menu |
| **Auth Service Swagger** | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Direct OpenAPI Docs for Student 1 |
| **Book Service Swagger** | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) | Direct OpenAPI Docs for Student 2 |
| **Loan Service Swagger** | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) | Direct OpenAPI Docs for Student 3 |
| **Reservation Service Swagger** | [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) | Direct OpenAPI Docs for Student 4 |
| **Notification Service Swagger** | [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html) | Direct OpenAPI Docs for Student 5 |

---

## 7. Quick Start (Single Command Launch)

### Prerequisites:
- Docker Desktop installed and running
- Java 17 (JDK) & Maven 3.8+ (for local development)
- Node.js 18+ & npm (for client development)
- MongoDB Compass (for database inspection)

### Launching the Complete System:
```bash
# Clone the repository
git clone <your-repo-url> librasys
cd librasys

# Spin up all 14 containers (Microservices, Eureka, Gateway, Redis, MongoDBs, Client App)
docker compose up -d
```

### Live URLs:
- **Client Application:** [http://localhost:3000](http://localhost:3000)
- **Central Swagger Hub:** [http://localhost:8080](http://localhost:8080)
- **Eureka Service Registry:** [http://localhost:8761](http://localhost:8761)

---

## 8. Automated Testing & Postman Collection

### Automated System Verification Script:
Run the PowerShell test script to verify all 5 microservices, Eureka registration, security blocks, and end-to-end business flows:
```powershell
powershell -ExecutionPolicy Bypass -File .\test_system.ps1
```

### Postman Collection:
Import `librasys_postman_collection.json` into Postman:
- **Individual Student Folders (8081-8085):** Tests each member's microservice independently with API keys and verifies 401 Unauthorized security blocks.
- **API Gateway Folder (8080):** Tests complete end-to-end workflows (Register -> Login -> Browse Books -> Borrow -> Return -> Reserve -> Check Notifications).
- **Auto-Token Scripting:** Login tests automatically save the JWT token into `{{jwt_token}}` for seamless multi-step testing.

---

## 9. Security & Infrastructure Summary

- **Authentication & Authorization:** Stateless JWT tokens (HMAC-SHA384) with BCrypt salted password hashing.
- **Service Discovery:** Netflix Eureka dynamically tracks microservice instances with health checks.
- **Rate Limiting:** Redis Reactive Rate Limiter configured at 5 requests/sec with burst capacity of 10 to protect against DoS attacks.
- **CORS:** Safe cross-origin resource sharing configured globally on the Gateway for `http://localhost:3000`.
- **Database Isolation:** Database-per-service pattern using 5 independent MongoDB instances.
