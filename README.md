<div align="center">

#  Aegis Infra — Multi-Tenant SaaS Subscription Platform

**A production-ready, containerized, multi-tenant SaaS subscription management platform built with Spring Boot 3, Docker, and GitHub Actions CI/CD. Features AI-powered churn analysis, API-key metering, email automation, and a full developer SDK.**

[![CI — Build & Test](https://github.com/abhishekmohanty5/Saas_Subscription-/actions/workflows/ci.yml/badge.svg)](https://github.com/abhishekmohanty5/Saas_Subscription-/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![AI](https://img.shields.io/badge/AI-Google%20Gemini-purple?logo=google)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

---

##  Project Overview

**Aegis Infra** is a backend-as-a-service (BaaS) platform that allows **Tenants (SaaS companies)** to onboard, manage subscription plans, and offer their own subscription products to **End Users** — all through a clean, secured REST API surface.

### What it does

| Feature | Description |
|---|---|
|  **Multi-Tenancy** | Each tenant gets isolated data, billing plans, and API credentials |
| 🔐 **JWT Auth** | Stateless Spring Security filter chain with BCrypt password hashing |
| 💳 **Subscription Lifecycle** | Subscribe, cancel, auto-expire with ShedLock-protected scheduling |
| 🤖 **AI Churn Analysis** | Google Gemini API predicts churn risk per user subscription |
| 📧 **Email Automation** | Renewal reminders and expiry alerts via Spring Mail (SMTP/Gmail) |
| 🔑 **API Key Metering** | Tenants get `clientId` + `clientSecret`; API usage is tracked per-call |
| 📊 **Analytics Dashboard** | Real-time subscription stats, plan health, and insights |
| 🐳 **Containerized** | Fully Dockerized with multi-stage builds and Docker Compose orchestration |
| ⚙️ **CI/CD** | GitHub Actions pipeline — build, test (H2 in-memory), package, and upload JAR |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.4.2 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Build Tool | Maven (Wrapper included) |
| Email | Spring Mail (SMTP / Gmail App Password) |
| Distributed Locking | ShedLock 5.10.0 (JDBC Provider) |
| AI/ML | Google Gemini REST API |
| Caching | Spring Cache |
| Testing | JUnit 5, Spring Boot Test, H2 In-Memory |

---

## 🏗 Architecture

```
                        ┌──────────────────────────────────────────────────────────────┐
                        │                     Docker Network: saas-network             │
                        │                                                              │
  Client / Postman ────▶│  ┌────────────────────────────────────┐                     │
         Port 8080       │  │        Spring Boot App             │                     │
                        │  │        (saas-app:8080)             │                     │
                        │  │                                    │     ┌─────────────┐  │
                        │  │  ┌──────────────────────────────┐  │────▶│  MySQL 8.0  │  │
                        │  │  │     Security Layer           │  │     │ (saas-mysql)│  │
                        │  │  │  JwtAuthFilter ──▶ API Keys  │  │     │  Port 3306  │  │
                        │  │  └──────────────────────────────┘  │     │ Vol: mysql_ │  │
                        │  │                                    │     │       _data │  │
                        │  │  ┌──────────────────────────────┐  │     └─────────────┘  │
                        │  │  │       REST Controllers       │  │                     │
                        │  │  │  Auth / Tenant / User /      │  │                     │
                        │  │  │  Plans / Dashboard / AI      │  │                     │
                        │  │  └──────────────────────────────┘  │                     │
                        │  │                                    │                     │
                        │  │  ┌──────────────────────────────┐  │                     │
                        │  │  │  Service Layer               │  │                     │
                        │  │  │  Auth / Plan / Sub /         │  │           ┌────────┐│
                        │  │  │  Email / AI / Scheduler      │  │──────────▶│ Gemini ││
                        │  │  └──────────────────────────────┘  │           │  API   ││
                        │  └────────────────────────────────────┘           └────────┘│
                        └──────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
                               GitHub Actions CI/CD
                          (Build → Test → Package → Upload JAR)
```

### Request Flow — Secured Endpoints

```
HTTP Request
    │
    ▼
JwtAuthFilter (extracts & validates Bearer token)
    │
    ├── /api/tenant/**  → ApiKeyInterceptor (validates clientId + clientSecret)
    │                     → ApiUsageInterceptor (increments API call count per tenant)
    │
    ├── /api/developer/** → JWT + Tenant Role required
    │
    ├── /api/admin/**   → JWT + ADMIN role required
    │
    ├── /api/user-subscriptions/** → JWT + USER role required
    │
    └── /api/public/**  → No auth (open)
```

---

## 📁 Project Structure

```
saas-Backend/
├── .github/
│   └── workflows/
│       └── ci.yml                    # GitHub Actions CI (build → test → package)
│
├── src/
│   ├── main/
│   │   ├── java/com/jobhunt/saas/
│   │   │   ├── SaasApplication.java
│   │   │   │
│   │   │   ├── auth/                 # Auth context — current user resolution
│   │   │   ├── tenant/              # TenantContext — thread-local tenant isolation
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java     # Spring Security + JWT filter chain
│   │   │   │   ├── ApiKeyInterceptor.java  # Validates clientId/clientSecret
│   │   │   │   ├── ApiUsageInterceptor.java # API call metering per tenant
│   │   │   │   ├── SchedulerConfig.java    # ShedLock distributed lock config
│   │   │   │   ├── DataInitializer.java    # Seed plans + super admin on startup
│   │   │   │   └── WebConfig.java          # CORS + interceptor registration
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthControllerInfra.java         # Register / Login
│   │   │   │   ├── DashboardController.java         # Tenant dashboard summary
│   │   │   │   ├── PlanController.java              # Admin: global plan management
│   │   │   │   ├── EngineSubscriptionController.java # Tenant's SaaS plan subscription
│   │   │   │   ├── UserSubscriptionController.java  # End-user subscription CRUD
│   │   │   │   ├── TenantDeveloperController.java   # API keys, tenant plans, user stats
│   │   │   │   ├── TenantPublicApiController.java   # Tenant's public-facing endpoints
│   │   │   │   ├── MockPaymentController.java       # Simulated payment gateway
│   │   │   │   ├── SuperAdminController.java        # Super-admin operations
│   │   │   │   ├── PublicController.java            # Public plan listing
│   │   │   │   └── TestBackendController.java       # Health check
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── PlanService.java
│   │   │   │   ├── TenantPlanService.java
│   │   │   │   ├── UserSubscriptionService.java     # Core subscription logic
│   │   │   │   ├── EngineSubscriptionService.java   # Tenant's own subscription logic
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── EmailService.java                # SMTP email dispatch
│   │   │   │   ├── AiService.java                   # Gemini API connector
│   │   │   │   ├── AiChurnService.java              # AI-powered churn risk analysis
│   │   │   │   ├── UserSubscriptionReminder.java    # Scheduled expiry reminder
│   │   │   │   ├── UserSubscriptionRenewalReminder.java # Renewal reminder scheduler
│   │   │   │   ├── ApplicationSubscriptionCleanup.java  # Auto-expire old subs
│   │   │   │   └── EngineSubscriptionReminder.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── Users.java           # Platform user (belongs to tenant)
│   │   │   │   ├── Tenant.java          # SaaS company tenant
│   │   │   │   ├── Plan.java            # Global plans (BASIC, PRO, ENTERPRISE)
│   │   │   │   ├── TenantPlan.java      # Plans defined by each tenant
│   │   │   │   ├── TenantSubscription.java  # Tenant subscribed to a global plan
│   │   │   │   ├── UserSubscription.java    # End-user subscribed to a tenant plan
│   │   │   │   ├── Role.java            # Enum: USER, ADMIN, SUPER_ADMIN
│   │   │   │   ├── SubscriptionStatus.java # Enum: ACTIVE, CANCELLED, EXPIRED
│   │   │   │   └── BillingCycle.java    # Enum: MONTHLY, YEARLY, etc.
│   │   │   │
│   │   │   ├── dto/                     # Request / Response DTOs
│   │   │   ├── repository/              # Spring Data JPA repositories
│   │   │   └── exception/              # Global exception handling
│   │   │
│   │   └── resources/
│   │       ├── application.yml          # Base configuration
│   │       ├── application-dev.yml      # Dev profile (local MySQL)
│   │       └── application-prod.yml     # Prod profile (Docker / Cloud)
│   │
│   └── test/
│       ├── java/.../                    # Unit & integration tests
│       └── resources/
│           └── application.yml         # Test config (H2 in-memory, no MySQL needed)
│
├── Dockerfile                          # Multi-stage build (build → runtime)
├── docker-compose.yml                  # Full stack (app + MySQL)
├── setup.sh                           # One-command startup script
├── run.ps1                            # Windows PowerShell equivalent
├── .env.example                       # Template for secrets
└── pom.xml                            # Maven dependencies
```

---

## 🔐 Authentication & Authorization

### Auth Flow

```
POST /api/auth/reg    →  Register new user (username, email, password, role)
POST /api/auth/log    →  Login → returns { token: "Bearer eyJ..." }

All secured routes require:
Authorization: Bearer <jwt_token>
```

### Roles

| Role | Access Level |
|---|---|
| `USER` | Own subscription management (user-subscriptions, insights, stats) |
| `ADMIN` | User access + global plan management |
| `SUPER_ADMIN` | Full access including tenant management |

---

## 📡 API Reference

### 🔑 Auth Endpoints (`/api/auth`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/reg` | None | Register a user |
| `POST` | `/api/auth/log` | None | Login, receive JWT |

---

### 🌐 Public Endpoints (`/api/public`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/public` | None | List all active global plans |

---

### 👤 User Subscription Endpoints (`/api/user-subscriptions`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/user-subscriptions` | JWT | Create/add a new subscription |
| `GET` | `/api/user-subscriptions` | JWT | Get all own subscriptions |
| `GET` | `/api/user-subscriptions/active` | JWT | Get active subscriptions |
| `GET` | `/api/user-subscriptions/tenant-plan/{id}` | JWT | Filter by tenant plan |
| `PUT` | `/api/user-subscriptions/update/{id}` | JWT | Update a subscription |
| `PUT` | `/api/user-subscriptions/cancel/{id}` | JWT | Cancel a subscription |
| `GET` | `/api/user-subscriptions/upcoming?days=7` | JWT | Renewals due in N days |
| `GET` | `/api/user-subscriptions/stats` | JWT | Subscription statistics |
| `GET` | `/api/user-subscriptions/insights` | JWT | AI-powered insights |

---

### 🏢 Admin / Plan Management (`/api/admin`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/admin/plan` | JWT + ADMIN | Create a global plan |
| `PUT` | `/api/admin/plan/{id}/activate` | JWT + ADMIN | Activate a plan |
| `PUT` | `/api/admin/plan/{id}/deactivate` | JWT + ADMIN | Deactivate a plan |

---

### 🧑‍💻 Developer / Tenant Endpoints (`/api/developer`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/developer/keys` | JWT | Get own clientId + clientSecret |
| `POST` | `/api/developer/tenant-plans` | JWT | Create a tenant subscription plan |
| `GET` | `/api/developer/tenant-plans` | JWT | List own tenant plans |
| `PUT` | `/api/developer/tenant-plans/{id}` | JWT | Update a tenant plan |
| `DELETE` | `/api/developer/tenant-plans/{id}` | JWT | Delete a tenant plan |
| `GET` | `/api/developer/users` | JWT | List all end-users on this tenant |
| `GET` | `/api/developer/user-subscriptions` | JWT | List all user subscriptions |
| `GET` | `/api/developer/tenant-stats` | JWT | Stats: total, active, cancelled, expired |
| `GET` | `/api/developer/tenant-subscribers` | JWT | Full subscriber list |

---

### 🏷️ Tenant Public API (`/api/tenant` — API Key Auth)

> These endpoints are callable by end users using the tenant's `clientId` and `clientSecret`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/tenant/subscribe` | API Key | Subscribe an end user |
| `GET` | `/api/tenant/subscriptions` | API Key | User's subscriptions |
| `PUT` | `/api/tenant/cancel/{id}` | API Key | Cancel user subscription |
| `GET` | `/api/tenant/plans` | API Key | List tenant's active plans |

---

### 💳 Mock Payment (`/api/payment`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/payment/process` | JWT | Simulate payment processing |
| `GET` | `/api/payment/history` | JWT | View payment history |

---

### 📊 Dashboard (`/api/dashboard`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/dashboard` | JWT | Full tenant dashboard (plan, expiry, API usage, stats) |

---

### 🔓 Super Admin (`/api/super-admin`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/super-admin/tenants` | JWT + SUPER_ADMIN | List all tenants |

---

## 🗄️ Database Schema

```
┌─────────────┐        ┌───────────────────┐        ┌────────────────────┐
│    users    │        │      tenant        │        │       plan         │
│─────────────│        │───────────────────│        │────────────────────│
│ id          │───────▶│ id                │        │ id                 │
│ username    │  N:1   │ name              │        │ name               │
│ email       │        │ clientId (UUID)   │        │ price              │
│ password    │        │ clientSecret      │        │ durationInDays     │
│ role        │        │ plan_id (FK)      │──N:1──▶│ active             │
│ tenant_id   │        │ status            │        └────────────────────┘
└─────────────┘        │ apiCallCount      │
                       │ createdAt         │
                       └───────────────────┘
                                │
                 ┌──────────────┴──────────────┐
                 ▼                             ▼
    ┌────────────────────┐       ┌───────────────────────┐
    │   tenant_plan      │       │  tenant_subscription  │
    │────────────────────│       │───────────────────────│
    │ id                 │       │ id                    │
    │ tenant_id (FK)     │       │ tenant_id (FK)        │
    │ name               │       │ plan_id (FK)          │
    │ price              │       │ startDate             │
    │ billingCycle       │       │ endDate               │
    │ features (text)    │       │ status                │
    │ active             │       └───────────────────────┘
    └────────────────────┘
              │
              ▼ 1:N
    ┌──────────────────────┐
    │  user_subscription   │
    │──────────────────────│
    │ id                   │
    │ user_id (FK)         │
    │ tenant_plan_id (FK)  │
    │ subscriptionName     │
    │ amount               │
    │ billingCycle         │
    │ startDate            │
    │ nextBillingDate      │
    │ status               │
    │ notes                │
    └──────────────────────┘

┌─────────────────────────────────┐
│           shedlock              │  ← Prevents duplicate scheduler runs
│─────────────────────────────────│
│ name, lock_until, locked_at,    │
│ locked_by                       │
└─────────────────────────────────┘
```

---

## 🤖 AI Integration — Google Gemini

**`AiChurnService`** calls the Gemini REST API to predict user churn risk based on subscription patterns:

```
Input  → userId, subscription history, billing cycles, active status
Output → { riskLevel: "HIGH|MEDIUM|LOW", reason: "...", recommendation: "..." }
```

**`UserSubscriptionService.getSubscriptionInsights()`** exposes AI-generated insights via `GET /api/user-subscriptions/insights`.

> Set `GEMINI_API_KEY` in your `.env` file to activate AI features.

---

## ⏰ Scheduled Jobs (ShedLock Protected)

All scheduled jobs use **ShedLock** to prevent duplicate execution in a multi-instance deployment.

| Job | Schedule | Function |
|---|---|---|
| `ApplicationSubscriptionCleanup` | Configurable CRON | Auto-expires overdue user subscriptions |
| `UserSubscriptionReminder` | Configurable CRON | Sends email alerts for expiring subscriptions |
| `UserSubscriptionRenewalReminder` | Configurable CRON | Sends renewal reminders N days before expiry |
| `EngineSubscriptionReminder` | Configurable CRON | Reminds tenants when their own SaaS plan is expiring |
| `EngineSubscriptionCleanup` | Configurable CRON | Expires overdue tenant plan subscriptions |

---

## 🔑 API Key Metering

Every tenant gets a unique `clientId` + `clientSecret` (generated on registration).

```
API Call → ApiKeyInterceptor validates credentials
        → ApiUsageInterceptor increments tenant.apiCallCount
        → Dashboard shows current usage vs. limit (default: 50,000 calls)
```

---

## 🐳 Docker Usage

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- `.env` file configured (see [Environment Configuration](#️-environment-configuration))

### Quick Start (Recommended)

```bash
# Clone the repo
git clone https://github.com/abhishekmohanty5/Saas_Subscription-.git
cd Saas_Subscription-

# Copy and fill in your environment variables
cp .env.example .env
# Edit .env with your credentials

# Linux/Mac — Start everything
bash setup.sh -d

# Windows PowerShell
.\run.ps1
```

### Manual Docker Compose

```bash
# Build and start all services in background
docker compose up --build -d

# View live logs
docker compose logs -f app

# Stop all services
docker compose down

# Stop and wipe the database (fresh start)
docker compose down -v
```

### Services

| Service | Container | Port | Description |
|---|---|---|---|
| Spring Boot App | `saas-app` | `8080` | Main backend API |
| MySQL 8.0 | `saas-mysql` | `3306` | Relational database |

App is available at: `http://localhost:8080`

---

## ⚙️ Environment Configuration

The project uses **Spring Profile-based configuration** for environment separation:

| Profile | File | When Used |
|---|---|---|
| `dev` | `application-dev.yml` | Local development (default) |
| `prod` | `application-prod.yml` | Docker / AWS deployment |
| `test` | `src/test/resources/application.yml` | CI / unit tests (H2 in-memory) |

### `.env` File Setup

```env
# ── Database ──────────────────────────────────────
DB_URL=jdbc:mysql://localhost:3306/saasdb?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=your_password

# ── Security ──────────────────────────────────────
JWT_SECRET=your_hex_secret_min_32_chars_long
JWT_EXPIRATION=86400000

# ── Mail (Gmail SMTP) ──────────────────────────────
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_gmail_app_password   # Not your login password

# ── AI ────────────────────────────────────────────
GEMINI_API_KEY=your_gemini_api_key

# ── Server ────────────────────────────────────────
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

> **Security Note:** `.env` is in `.gitignore` — secrets are never committed.

---

## 🔄 CI/CD Pipeline

GitHub Actions triggers automatically on every push or PR to `main` / `development`:

```
Push to GitHub
      │
      ▼
┌──────────────────────────────────────┐
│  GitHub Actions (ubuntu-latest)      │
│                                      │
│  1. Checkout repository              │
│  2. Setup JDK 21 (Temurin) + cache  │
│  3. mvn clean compile               │
│  4. mvn test (H2 in-memory)         │  ← No MySQL needed in CI
│  5. mvn package -DskipTests         │
│  6. Upload JAR as build artifact    │  ← Retention: 7 days
└──────────────────────────────────────┘
```

- ✅ No secrets required in CI — tests use H2 in-memory database
- ✅ Maven dependency cache reduces build time
- ✅ Built JAR uploaded as a GitHub Actions artifact (downloadable)

---

## 🧪 Running Tests

```bash
# Run all tests (uses H2, no MySQL needed)
mvn test

# Run with verbose output
mvn test -Dsurefire.useFile=false

# Run a specific test class
mvn test -Dtest=AuthServiceTest
```

---

## ☁️ AWS EC2 Deployment

```bash
# 1. Launch EC2 Ubuntu 22.04, t2.micro (Open port 8080 + 22 in Security Group)

# 2. SSH in
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

# 3. Install Docker
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# 4. Clone & configure
git clone https://github.com/abhishekmohanty5/Saas_Subscription-.git
cd Saas_Subscription-
nano .env   # Fill in production values

# 5. Launch
bash setup.sh -d

# App live at: http://<EC2_PUBLIC_IP>:8080
```

---

## 👤 Author

**Abhishek Mohanty**
- GitHub: [@abhishekmohanty5](https://github.com/abhishekmohanty5)
- Project: [Saas_Subscription-](https://github.com/abhishekmohanty5/Saas_Subscription-)
