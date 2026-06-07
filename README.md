# LLM Code Reviewer

> An AI-powered GitHub Pull Request reviewer that automatically analyzes code changes and posts structured review comments — built with Spring Boot, OpenAI, and PostgreSQL.

***

## Overview

LLM Code Reviewer is a production-deployed webhook service that listens to GitHub Pull Request events, sends the diff to an OpenAI model for review, and persists the structured feedback to a PostgreSQL database. It exposes a REST API to retrieve paginated review history.

The project was built as a full-stack backend engineering exercise covering webhook security, AI integration, Spring Data JPA, and cloud deployment on Render.

***

## Features

- **Automated PR Reviews** — Triggered via GitHub webhook on `pull_request` events
- **HMAC-SHA256 Webhook Signature Verification** — Validates every incoming request using `X-Hub-Signature-256`
- **OpenAI Integration** — Sends PR diff to a configurable GPT model and parses structured review feedback via Spring AI
- **Review Persistence** — Stores each review and its comments in PostgreSQL with a full audit trail
- **Paginated Review History API** — `GET /reviews/history` returns past reviews ordered by newest first
- **Spring Profiles** — Separate `dev` and `prod` configurations with profile-specific property overrides
- **Swagger / OpenAPI UI** — Auto-generated API documentation available at `/swagger-ui.html`

***

## Architecture

```
GitHub PR Event
      │
      ▼
┌─────────────────────┐
│  Webhook Endpoint   │  POST /webhook/github
│  (Signature check)  │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│   GitHub Service    │  Fetches PR diff via GitHub API
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│    AI Review        │  Sends diff to OpenAI (Spring AI)
│    Service          │  Parses structured review response
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│  Persistence Layer  │  Saves PullRequestReviewEntity
│  (Spring Data JPA)  │  + ReviewCommentEntity (1:N)
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│    PostgreSQL DB    │  Hosted on Render
└─────────────────────┘
```

***

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.5 |
| AI Integration | Spring AI (OpenAI) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate 6 |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Build Tool | Maven |
| Deployment | Render (Web Service + Managed DB) |
| CI | Maven Surefire (`mvn test -B`) |

***

## Project Structure

```
src/
├── main/
│   ├── java/com/personalProject/code_reviewer/
│   │   ├── CodeReviewerApplication.java
│   │   ├── webhook/
│   │   │   ├── WebhookController.java       # POST /webhook/github
│   │   │   └── WebhookSignatureService.java # HMAC-SHA256 verification
│   │   ├── github/
│   │   │   └── GitHubService.java           # Fetches PR diff via GitHub API
│   │   ├── review/
│   │   │   ├── ReviewController.java        # GET /reviews/history
│   │   │   └── ReviewService.java           # Orchestrates AI review flow
│   │   ├── ai/
│   │   │   └── AiReviewService.java         # Spring AI / OpenAI integration
│   │   └── persistence/
│   │       ├── entity/
│   │       │   ├── PullRequestReviewEntity.java
│   │       │   └── ReviewCommentEntity.java
│   │       ├── repository/
│   │       │   └── PullRequestReviewRepository.java
│   │       └── service/
│   │           └── ReviewPersistenceService.java
│   └── resources/
│       ├── application.yml                  # Base config (dev defaults)
│       └── application-prod.yml             # Prod overrides (INFO logging, prod DB)
└── test/
    └── java/com/personalProject/code_reviewer/
        └── CodeReviewerApplicationTests.java
```

***

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL (local or cloud)
- OpenAI API key
- GitHub repository with webhook access

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/code-reviewer.git
cd code-reviewer
```

### 2. Configure Environment

Create a local `application-dev.yml` or set the following environment variables:

| Variable | Description |
|---|---|
| `APP_GITHUB_TOKEN` | GitHub Personal Access Token (repo scope) |
| `APP_GITHUB_WEBHOOK_SECRET` | Secret configured in your GitHub webhook |
| `SPRING_AI_OPENAI_API_KEY` | OpenAI API key |
| `SPRING_DATASOURCE_URL` | JDBC URL for your PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |

### 3. Run Locally

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080` by default.

### 4. Run Tests

```bash
mvn test -B
```

Tests use an in-memory H2 database — no external DB connection required.

***

## GitHub Webhook Setup

1. Go to your GitHub repository → **Settings** → **Webhooks** → **Add webhook**
2. Set the **Payload URL** to `https://<your-domain>/webhook/github`
3. Set **Content type** to `application/json`
4. Set a **Secret** — this must match `APP_GITHUB_WEBHOOK_SECRET` in your environment
5. Select **Pull requests** under individual events (or send everything)
6. Click **Add webhook**

Every PR open, synchronize, or reopen event will now trigger an automated review.

***

## API Reference

Full interactive documentation is available at `/swagger-ui.html` when the application is running.

### `GET /reviews/history`

Returns a paginated list of all past AI reviews, ordered newest first.

**Query Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | integer | `0` | Zero-based page number |
| `size` | integer | `10` | Number of reviews per page |

**Example Response**

```json
{
  "content": [
    {
      "id": 42,
      "repositoryName": "my-org/my-repo",
      "prNumber": 17,
      "prTitle": "Add user authentication",
      "reviewedAt": "2026-06-07T16:41:43Z",
      "comments": [
        {
          "filePath": "src/main/java/AuthService.java",
          "lineNumber": 34,
          "severity": "WARNING",
          "comment": "Password is stored as plain text. Use BCrypt or Argon2."
        }
      ]
    }
  ],
  "totalElements": 120,
  "totalPages": 12,
  "number": 0,
  "size": 10
}
```

### `POST /webhook/github`

Receives GitHub webhook payloads. Not intended for direct use — called by GitHub only.

**Headers required:**
- `X-Hub-Signature-256` — HMAC-SHA256 signature of the payload body
- `X-GitHub-Event` — Must be `pull_request`

***

## Deployment (Render)

### Web Service

1. Connect your GitHub repo to Render
2. Set **Build Command**: `mvn clean package -DskipTests`
3. Set **Start Command**: `java -jar target/code-reviewer-0.0.1-SNAPSHOT.jar`
4. Add all environment variables from the table above
5. Set `SPRING_PROFILES_ACTIVE=prod`

### Database

1. Create a **PostgreSQL** instance on Render
2. Copy the **Internal Database URL** into `SPRING_DATASOURCE_URL` on your web service
3. Schema is auto-created by Hibernate (`ddl-auto: update` in prod)

***

## Configuration Reference

### `application.yml` (base — dev defaults)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
  ai:
    openai:
      api-key: ${SPRING_AI_OPENAI_API_KEY}

app:
  github:
    token: ${APP_GITHUB_TOKEN}
    webhook-secret: ${APP_GITHUB_WEBHOOK_SECRET}

logging:
  level:
    com.personalProject.code_reviewer: DEBUG
```

### `application-prod.yml` (prod overrides)

```yaml
logging:
  level:
    com.personalProject.code_reviewer: INFO
```

***

## Key Design Decisions

### HMAC-SHA256 Webhook Verification
Every incoming webhook payload is verified against the `X-Hub-Signature-256` header before any processing occurs. This prevents spoofed requests from triggering AI reviews.

### `@Transactional(readOnly = true)` on Read Queries
The `getAllReviews()` service method uses `readOnly = true` to keep the Hibernate session open during `page.map()` — this resolves the `LazyInitializationException` that would otherwise occur when accessing the `comments` collection outside of a session boundary.

### Profile-Scoped Logging
`DEBUG` logging is enabled in the base config for local development. `application-prod.yml` overrides this to `INFO`, preventing verbose output — including potentially sensitive request data — from appearing in production log streams.

### `open-in-view: false`
Spring Boot's Open Session in View anti-pattern is explicitly disabled. Database sessions are scoped strictly to the service layer, not held open for the duration of the HTTP request.

***

## Lessons Learned

- **`LazyInitializationException` in prod** — Accessing lazy collections outside a Hibernate session is a silent failure in dev (where `open-in-view` is often enabled by default) but breaks in prod. Explicit `@Transactional` boundaries on service methods are the correct fix.
- **Webhook secrets are non-optional** — Exposing a public POST endpoint without signature verification would allow anyone to trigger AI reviews and incur OpenAI API costs.
- **Profile-specific properties are additive, not replacement** — Spring merges `application-prod.yml` on top of `application.yml`. Keys not present in the profile file are inherited from the base.
- **H2 for tests, PostgreSQL for prod** — Using an in-memory database for tests (`scope=test`) keeps the CI pipeline fast and removes the external dependency from `mvn test -B`.

***

## License

MIT