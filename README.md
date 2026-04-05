# Finance Dashboard System

This is a fresh full-stack assessment project built from scratch with:

- Spring Boot for the backend API
- React + Vite for the frontend dashboard
- SQLite for simple local persistence

It is designed to satisfy the assignment requirements around:

- user and role management
- financial record CRUD
- dashboard summary APIs
- backend access control
- validation and error handling
- persistence

## Tech Stack

- Backend: Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation
- Frontend: React 18, Vite
- Database: SQLite

## Project Structure

```text
finance-dashboard-system/
|-- backend/
|   |-- pom.xml
|   `-- src/main/java/com/finance/dashboard
|       |-- config
|       |-- controller
|       |-- dto
|       |-- exception
|       |-- model
|       |-- repository
|       |-- security
|       |-- service
|       `-- specification
|-- frontend/
|   |-- package.json
|   |-- vite.config.js
|   `-- src
|       |-- api.js
|       |-- App.jsx
|       |-- main.jsx
|       `-- styles.css
`-- README.md
```

## Role Model

- `VIEWER`: can view records and dashboard summaries
- `ANALYST`: can view records and dashboard summaries
- `ADMIN`: can manage users and perform full record CRUD

Access control is enforced in the backend through `AccessControlService`.

## Mock Authentication

To keep the project simple and focused on backend design, authentication is mocked using the request header:

- `X-User-Id: <seeded-user-id>`

Seeded users:

- `1` -> Admin User
- `2` -> Analyst User
- `3` -> Viewer User
- `4` -> Inactive Viewer

The frontend includes a dropdown to simulate switching between these users.

## Core APIs

### Users

- `GET /api/users` -> admin only
- `GET /api/users/{id}` -> admin only
- `POST /api/users` -> admin only
- `PUT /api/users/{id}` -> admin only

### Financial Records

- `GET /api/records` -> viewer, analyst, admin
- `GET /api/records/{id}` -> viewer, analyst, admin
- `POST /api/records` -> admin only
- `PUT /api/records/{id}` -> admin only
- `DELETE /api/records/{id}` -> admin only

Supported filters:

- `startDate`
- `endDate`
- `category`
- `type`

Example:

```http
GET /api/records?startDate=2026-01-01&endDate=2026-04-30&type=EXPENSE
X-User-Id: 2
```

### Dashboard Summary

- `GET /api/dashboard/summary` -> viewer, analyst, admin

Returns:

- total income
- total expenses
- net balance
- expense category totals
- monthly trends
- recent activity

## Validation and Error Handling

The backend demonstrates:

- bean validation on request bodies
- proper HTTP status codes
- conflict handling for duplicate emails
- forbidden and unauthorized responses for access control issues
- consistent JSON error responses

## Running the Project

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173`.

## Notes and Assumptions

- SQLite was chosen to keep setup simple and make evaluation easy.
- Authentication is intentionally mocked to focus on authorization, backend structure, and business logic.
- Analysts currently have read-only access, matching the requirement that they can access records and insights.
- Records are hard deleted for simplicity.
- Seed data is included so the system can be explored immediately after startup.

## Suggested Improvements

If you want to extend this further, sensible next steps would be:

- JWT authentication
- pagination
- search
- soft delete
- audit logging
- unit and integration tests
- Swagger/OpenAPI documentation
