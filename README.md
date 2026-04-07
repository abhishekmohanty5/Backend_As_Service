# 🛡️ Aegis Infra — Core SaaS Engine

[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=java&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot 3.2+](https://img.shields.io/badge/Spring_Boot-3.2%2B-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Azure Deployment](https://img.shields.io/badge/Azure-Web_App_for_Containers-0078D4?logo=microsoft-azure&logoColor=white)](https://azure.microsoft.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Aegis Infra** is a high-performance, enterprise-grade SaaS Backend Engine designed to simplify multi-tenant management and user subscription lifecycles. Powered by **Java 17** and **Spring Boot**, it provides absolute security, AI-driven churn prediction, and a scalable architecture for modern software-as-a-service providers.

---

## 🚀 Key Strategic Features

-   **🏗️ Multi-Tenanting Architecture:** Effortlessly manage multiple client workspaces (tenants) with isolated data and custom subscription tiers.
-   **🔐 Advanced Security:** Full **JWT (JSON Web Token)** authentication flow with refresh tokens and granular role-based access control (RBAC).
-   **🤖 AI Insights (Gemini):** Integrated **Google Gemini AI** for generating subscriber plans and predictive churn analytics based on user signals.
-   **📧 Professional Emailing:** Enterprise SMTP relay via **Brevo** with premium HTML-glassmorphic templates for verification and renewal reminders.
-   **📦 Containerized Deployment:** Fully Dockerized and optimized for **Azure Web App for Containers** with automated GitHub Actions CI/CD.
-   **⚡ High Performance:** Persistence layer powered by **MySQL (Aiven)** with optimized JPA/Hibernate queries and database seeding.

---

## 🛠️ Technical Stack

-   **Language:** Java 17 (LTS)
-   **Framework:** Spring Boot 3.2.x
-   **Security:** Spring Security (Stateless JWT)
-   **Database:** MySQL 8.0 (Managed via Aiven)
-   **Documentation:** Swagger UI / OpenAPI 3.0
-   **Messaging:** Java Mail Sender (SMTP Relay via Brevo)
-   **Build Tool:** Maven

---

## 🌐 Production Environment Variables

To run the Aegis Core in production, ensure the following variables are configured in your environment (Azure/Local):

| Variable | Description |
| :--- | :--- |
| `DATABASE_URL` | JDBC URL for the MySQL instance |
| `DATABASE_USERNAME` | Production DB Username |
| `DATABASE_PASSWORD` | Production DB Password |
| `JWT_SECRET_KEY` | 256-bit safe secret for signing tokens |
| `MAIL_PASSWORD` | Brevo (SMTP) Master API Key |
| `MAIL_USERNAME` | Brevo SMTP Relay Username |
| `GEMINI_API_KEY` | API Key for Google Gemini AI Engine |
| `CORS_ALLOWED_ORIGINS` | `https://aegisinfra.me` (Whitelisted Frontend) |

---

## 🛠️ Getting Started (Local Setup)

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/abhishekmohanty5/saas-backend.git
    ```

2.  **Configuration:**
    Update `src/main/resources/application.yml` or set environment variables locally. Ensure a MySQL database is accessible.

3.  **Build & Run:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

4.  **Access Swagger Documentation:**
    Once running, open: `http://localhost:8080/swagger-ui.html`

---

## ☁️ Deployment Architecture

This engine utilizes a professional CI/CD pipeline:
1.  **GitHub Actions:** Triggers on `push` to `main`.
2.  **Docker Hub:** Builds and pushes a production-optimized image (`abhishekmohanty0505/aegisinfra-backend`).
3.  **Azure App Service:** Pulls the latest container and deploys with Zero Downtime.

---

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

---
© 2024 Aegis Infra Team. Built with passion for high-scale SaaS.
