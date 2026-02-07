Here is the comprehensive **README.md** for your project. You can copy and paste this directly into your GitHub repository to ensure your team is perfectly aligned for tomorrow's integration.

---

# JobHunt SaaS - Backend Documentation

This repository contains the Spring Boot backend for the JobHunt SaaS platform. It manages user authentication, subscription plans, and project tracking.

## 🚀 Quick Start for Frontend

* **Base URL:** `http://localhost:8080`
* **CORS:** Allowed for `http://localhost:3000`
* **Authentication:** JWT (Stateless)

---

## 🔐 Authentication Flow

1. **Register:** `POST /api/auth/reg`
2. **Login:** `POST /api/auth/log` -> Returns a JWT.
3. **Authorized Requests:** Add the token to the `Authorization` header.

### **Header Format**

```http
Authorization: Bearer <your_jwt_token>

```

---

## 🛠 API Endpoints

### **1. Public APIs (No Auth)**

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/public` | Fetches all available subscription plans. |

### **2. Subscription APIs (USER Role)**

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/subscriptions/subscribe/{id}` | Subscribe to a specific plan ID. |
| `GET` | `/api/subscriptions` | Get current active subscription. |
| `PUT` | `/api/subscriptions/cancel` | Cancel current subscription. |

### **3. Admin APIs (ADMIN Role)**

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/admin/plan` | Create a new plan (`PlanRequest` body). |
| `PUT` | `/api/admin/plan/{id}/activate` | Enable a plan. |
| `PUT` | `/api/admin/plan/{id}/deactivate` | Disable a plan. |

---

## 📦 Data Models (DTOs)

### **Registration Request**

```json
{
  "userName": "string",
  "email": "user@example.com",
  "password": "Password123!" // Must have Upper, Lower, Digit, & Special Char
}

```

### **Subscription Response**

```json
{
  "message": "success",
  "data": {
    "id": 1,
    "planId": 10,
    "startDate": "2026-02-07T...",
    "endDate": "2026-03-07T..."
  }
}

```

---

## ⚠️ Error Handling

All responses use a standard wrapper. In case of errors, check the `status` and `message` fields.

* **400 Bad Request:** Validation failed (e.g., email already exists or password too weak).
* **401 Unauthorized:** JWT is missing or expired.
* **403 Forbidden:** You do not have the required Role (`USER`/`ADMIN`).

---

## 🗄️ Database Schema

The backend uses a relational structure (PostgreSQL/MySQL).

* **Users:** Stores credentials and `Role` (`USER`, `ADMIN`).
* **Plans:** Stores pricing, duration, and `active` status.
* **Subscriptions:** Links a `User` to a `Plan` with specific dates.
* **Projects:** Linked to `ownerUserId`.

---

**Would you like me to also generate a Postman Collection file (JSON) so your team can test these endpoints without writing any code?**
