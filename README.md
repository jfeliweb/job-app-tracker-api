# Job Application Tracker — API

A production Spring Boot REST API powering an AI-assisted job application tracking platform. Built as a portfolio project to demonstrate enterprise Java engineering — JWT authentication, relational data modeling, background job scheduling, and OpenAI integration.

**Live backend:** `https://job-app-tracker-api-production.up.railway.app`  
**Frontend:** `https://job-tracker-frontend-blush.vercel.app`  
**Frontend repo:** `https://github.com/jfeliweb/job-tracker-frontend`

---

## What it does

Job seekers can track applications across companies, monitor status changes, set follow-up reminders, and generate tailored cover letters using AI. The API handles all data persistence, authentication, and external service integration.

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Data | Spring Data JPA + Hibernate 7 |
| Database | PostgreSQL 17 (Neon serverless) |
| Connection pool | HikariCP |
| AI | OpenAI API (GPT-4o-mini) via RestTemplate |
| Scheduling | Spring `@Scheduled` task executor |
| Build | Maven |
| Hosting | Railway |

---

## Architecture

```
Next.js (Vercel)
      │
      │  /api/backend/* rewrites
      ▼
Spring Boot on Railway
      │
      ├── Spring Security (JWT filter chain)
      ├── REST Controllers
      ├── Service layer (business logic)
      ├── Spring Data JPA Repositories
      │
      └── Neon PostgreSQL (serverless)
            + OpenAI API
```

The frontend proxies all API calls through Next.js rewrites — the backend URL is never exposed to the browser.

---

## Project structure

```
src/main/java/com/jobtracker/api/
├── ApiApplication.java              # Entry point, @EnableScheduling
├── config/
│   ├── SecurityConfig.java          # JWT filter chain, BCrypt, CORS
│   └── CorsConfig.java              # Allowed origins per environment
├── controller/
│   ├── AuthController.java          # POST /auth/register, /auth/login
│   ├── ApplicationController.java   # CRUD /applications
│   ├── CompanyController.java       # GET /companies
│   ├── ReminderController.java      # CRUD /reminders
│   └── CoverLetterController.java   # POST /cover-letter/generate
├── dto/                             # Request/response shapes (API boundary)
├── model/                           # JPA entities (User, Company, Application, Reminder)
├── repository/                      # Spring Data interfaces
├── security/
│   ├── JwtUtil.java                 # Token generation and validation
│   ├── JwtFilter.java               # Per-request authentication filter
│   └── UserDetailsServiceImpl.java  # Bridges Spring Security to UserRepository
└── service/
    ├── AuthService.java             # Registration, login, BCrypt hashing
    ├── ApplicationService.java      # Application CRUD with ownership checks
    ├── ReminderService.java         # Reminder creation and retrieval
    ├── ReminderScheduler.java       # @Scheduled background job
    └── OpenAiService.java           # Cover letter generation via OpenAI
```

---

## API endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Create account, returns JWT |
| POST | `/auth/login` | No | Authenticate, returns JWT |
| GET | `/applications` | Yes | List all user's applications |
| POST | `/applications` | Yes | Create application |
| GET | `/applications/{id}` | Yes | Get single application |
| PUT | `/applications/{id}` | Yes | Update application |
| DELETE | `/applications/{id}` | Yes | Delete application |
| GET | `/reminders` | Yes | List all user's reminders |
| POST | `/reminders` | Yes | Set a follow-up reminder |
| POST | `/cover-letter/generate` | Yes | Generate AI cover letter |
| GET | `/actuator/health` | No | Health check |

All protected endpoints require `Authorization: Bearer <token>` header.

---

## Key engineering decisions

**JWT over sessions** — the API is fully stateless. Every request carries its own signed token. No server-side session storage means horizontal scaling works without sticky sessions or shared state.

**Repository-level ownership enforcement** — `findByIdAndUserId()` queries are used on all mutations. A user cannot read, edit, or delete another user's data by guessing an ID, even with a valid JWT.

**DTO separation** — models map to database tables; DTOs define the API contract. The `User` model's password field never appears in any response object.

**`@Scheduled` reminder processor** — runs every 60 seconds on Spring's `scheduling-1` thread pool, independent of the HTTP request threads. Finds due reminders (`remindAt <= now AND isSent = false`), marks them sent, and logs the action. Designed to be extended with email delivery without architectural changes.

**Find-or-create company pattern** — when creating an application, the service checks for an existing company record under that user before inserting. Keeps the `companies` table normalized without requiring the frontend to manage company IDs.

---

## Database schema

```sql
users (id, email, password, name, created_at)
companies (id, name, website, user_id → users)
applications (id, user_id, company_id, job_title, status, job_url, notes, applied_date, created_at, updated_at)
reminders (id, application_id, user_id, remind_at, message, is_sent, created_at)
```

Schema is created manually (Phase 1 SQL) and validated on startup via `spring.jpa.hibernate.ddl-auto=validate`. Hibernate confirms the tables match the entity models — any mismatch fails fast on startup rather than silently corrupting data.

---

## Local setup

**Prerequisites:** Java 21, Maven 3.9+

```bash
# Clone
git clone https://github.com/jfeliweb/job-app-tracker-api
cd job-app-tracker-api/api

# Configure
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Fill in your Neon DB URL, JWT secret, and OpenAI key

# Run
mvn spring-boot:run

# Test
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com","password":"password123"}'
```

---

## Environment variables (production)

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | Neon pooled JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Neon username |
| `SPRING_DATASOURCE_PASSWORD` | Neon password |
| `APP_JWT_SECRET` | JWT signing key (min 256-bit) |
| `APP_JWT_EXPIRATION_MS` | Token TTL in milliseconds (86400000 = 24h) |
| `OPENAI_API_KEY` | OpenAI secret key |
| `OPENAI_MODEL` | Model name (gpt-4o-mini) |
| `FRONTEND_URL` | Vercel URL for CORS allowlist |

---

## Deployment

Deployed to Railway using the Metal build environment. Railway detects the Maven project automatically, runs `mvn clean package -DskipTests`, and starts the JAR with the configured start command.

The start command sets JVM flags for Railway's 512 MB container:

```
java -Xmx300m -Xss512k -XX:+UseSerialGC -Dserver.port=$PORT -jar target/*.jar
```

The app binds to Railway's dynamically assigned `$PORT` via `server.port=${PORT:8080}`.