<div align="center">

# ⚡ Aegis Infra — SaaS Subscription Backend

**A production-ready, containerized SaaS subscription management platform built with Spring Boot, Docker, and GitHub Actions CI/CD.**

[![CI — Build & Test](https://github.com/abhishekmohanty5/Saas_Subscription-/actions/workflows/ci.yml/badge.svg)](https://github.com/abhishekmohanty5/Saas_Subscription-/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

---

## 📋 Project Overview

Aegis Infra is a backend platform that handles **multi-tier SaaS subscription management**, including:

- 🔐 **JWT-based stateless authentication** with Spring Security filter chain
- 📦 **Subscription lifecycle management** (subscribe, cancel, auto-expire)
- 👑 **Role-based access control** (`USER` / `ADMIN`)
- ⚙️ **Admin plan management** (create, activate, deactivate plans)
- 📧 **Email notifications** via SMTP (Gmail)
- 🤖 **AI-powered features** via Gemini API integration
- 🔒 **Distributed task locking** via ShedLock (prevents duplicate scheduled jobs)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.2 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Build Tool | Maven |
| Email | Spring Mail (SMTP / Gmail) |
| Task Locking | ShedLock |
| AI | Google Gemini API |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Network                         │
│                                                             │
│  ┌──────────────────────┐     ┌──────────────────────────┐  │
│  │   Spring Boot App    │────▶│     MySQL 8.0 (db)       │  │
│  │   (saas-app:8080)    │     │   (saas-mysql:3306)      │  │
│  │                      │     │   Volume: mysql_data      │  │
│  │  ┌────────────────┐  │     └──────────────────────────┘  │
│  │  │ Security Layer │  │                                    │
│  │  │  JWT Filter    │  │                                    │
│  │  └────────────────┘  │                                    │
│  │  ┌────────────────┐  │                                    │
│  │  │  REST APIs     │  │                                    │
│  │  │  Controllers   │  │                                    │
│  │  └────────────────┘  │                                    │
│  │  ┌────────────────┐  │                                    │
│  │  │ Service Layer  │  │                                    │
│  │  └────────────────┘  │                                    │
│  └──────────────────────┘                                    │
└─────────────────────────────────────────────────────────────┘
              │
              ▼ Port 8080 exposed to host
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

# Start all services (Spring Boot app + MySQL)
bash setup.sh -d
```

### Manual Docker Compose Commands

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
| MySQL 8.0 | `saas-mysql` | `3306` | Database |

App is available at: `http://localhost:8080`

---

## ⚙️ Environment Configuration

The project uses **Spring Profile-based configuration** for environment separation:

| Profile | File | When Used |
|---|---|---|
| `dev` | `application-dev.yml` | Local development (default) |
| `prod` | `application-prod.yml` | Docker / AWS deployment |
| `test` | `src/test/resources/application.yml` | CI / unit tests (H2 in-memory) |

### Setup .env (Local Dev)

Create a `.env` file in the project root:

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/saasdb?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_hex_secret_min_32_chars
JWT_EXPIRATION=86400000

# Mail (Gmail)
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password

# AI
GEMINI_API_KEY=your_gemini_api_key

# App
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

> **Note:** `.env` is in `.gitignore` — secrets are never committed.

---

## 🔄 CI/CD Pipeline

GitHub Actions CI runs automatically on every push or pull request to `main` / `development`.

```
Push to GitHub
      │
      ▼
┌─────────────────────────────┐
│  GitHub Actions (ubuntu)   │
│                             │
│  1. Checkout code           │
│  2. Setup JDK 21 + cache   │
│  3. mvn clean compile      │
│  4. mvn test (H2 in-mem)   │  ← No MySQL needed in CI
│  5. mvn package            │
│  6. Upload JAR artifact    │
└─────────────────────────────┘
```

- ✅ **No secrets required** in CI — tests use H2 in-memory database
- ✅ Maven dependency cache reduces build time
- ✅ Built JAR uploaded as a GitHub Actions artifact (downloadable)

---

## ☁️ AWS EC2 Deployment

### Steps to deploy on EC2

```bash
# 1. Launch EC2 instance (Ubuntu 22.04, t2.micro or t3.small)
# 2. Open Security Group: port 8080 (app) + 22 (SSH)

# 3. SSH into instance
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

# 4. Install Docker
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# 5. Clone the repo
git clone https://github.com/abhishekmohanty5/Saas_Subscription-.git
cd Saas_Subscription-

# 6. Set environment variables
nano .env  # Fill in production credentials

# 7. Launch the stack
bash setup.sh -d

# App is now live at:  http://<EC2_PUBLIC_IP>:8080
```

---

## 🔐 Authentication Flow

```
POST /api/auth/reg   →  Register a new user
POST /api/auth/log   →  Login → returns JWT token
                         ↓
         All protected routes require:
         Authorization: Bearer <jwt_token>
```

---

## 📡 API Endpoints

### Public (No Auth)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/public` | Fetch all active subscription plans |

### User Endpoints (Role: USER)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/subscriptions/subscribe/{planId}` | Subscribe to a plan |
| `GET` | `/api/subscriptions` | View current active subscription |
| `PUT` | `/api/subscriptions/cancel` | Cancel current subscription |

### Admin Endpoints (Role: ADMIN)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/admin/plan` | Create a new subscription plan |
| `PUT` | `/api/admin/plan/{id}/activate` | Activate a plan |
| `PUT` | `/api/admin/plan/{id}/deactivate` | Deactivate a plan |

---

## 🗄️ Database Schema

```
users          → id, username, email, password (BCrypt), role
plans          → id, name, price, duration_days, active
subscriptions  → id, user_id (FK), plan_id (FK), start_date, end_date, status
shedlock       → name, lock_until, locked_at, locked_by  (distributed lock table)
```

---

## 🧪 Running Tests

```bash
# Run all unit + integration tests (uses H2, no MySQL needed)
mvn test

# Run with verbose output
mvn test -Dsurefire.useFile=false
```

---

## 📁 Project Structure

```
saas-Backend/
├── .github/
│   └── workflows/
│       └── ci.yml              # GitHub Actions CI pipeline
├── src/
│   ├── main/
│   │   ├── java/com/jobhunt/saas/
│   │   │   ├── config/         # Security, CORS, JWT config
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # JPA repositories
│   │   │   ├── model/          # JPA entities
│   │   │   ├── dto/            # Request/Response DTOs
│   │   │   └── filter/         # JWT Security filter
│   │   └── resources/
│   │       ├── application.yml       # Base config
│   │       ├── application-dev.yml   # Dev profile
│   │       └── application-prod.yml  # Prod profile
│   └── test/
│       ├── java/.../           # Unit & integration tests
│       └── resources/
│           └── application.yml # Test config (H2 in-memory)
├── Dockerfile                  # Multi-stage Docker build
├── docker-compose.yml          # Full stack orchestration
├── setup.sh                   # Automated startup script
├── pom.xml
└── .env                       # Local secrets (git-ignored)
```

---

## 👤 Author

**Abhishek Mohanty**
- GitHub: [@abhishekmohanty5](https://github.com/abhishekmohanty5)
