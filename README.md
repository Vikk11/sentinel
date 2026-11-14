
# Sentinel

**A multi-tenant monitoring, incident management, and security insights platform.**

## What is Sentinel?

Sentinel is a multi-tenant monitoring and incident management platform that helps teams monitor their services, detect outages, and respond to incidents in real time. It provides automated health checks, alerting, incident tracking, and public status pages, with future plans to expand into application monitoring and security insights.

## Planned Features

- **Multi-tenancy** – Isolated data and resources for each customer.
- **Authentication & RBAC** – User management, roles, permissions, and API keys.
- **Service Monitoring** – Automated health checks, uptime tracking, and latency monitoring.
- **Incident Management** – Automatic incident detection, acknowledgment, resolution, and event timelines.
- **Alerting** – Configurable alert rules with email, Slack, and webhook notifications.
- **Dashboard** – Live service status, uptime statistics, latency charts, and incident history.
- **Public Status Pages** – Share real-time service availability with customers.
- **Data Ingestion** – Event, error, and metric ingestion through API keys and Kafka.
- **Security Insights** – TLS certificate monitoring, port exposure checks on user-owned services, and security analysis.
- **AI-Powered Insights** – Anomaly detection and AI-generated incident summaries using RAG.
- **Billing** - Plans, usage metering and payment handling (Stripe test mode)

## Planned Tech Stack

| Category | Technology |
|---|---|
| Backend | Java, Spring Boot, Spring Security |
| Frontend | React, TypeScript |
| Database | PostgreSQL |
| Caching | Redis |
| Message Streaming | Apache Kafka |
| Background Processing | Scheduled Workers, Async Consumers |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Orchestration | Kubernetes |
| Infrastructure as Code | Terraform |
| Cloud | Microsoft Azure |
| Observability | Prometheus, Grafana, OpenTelemetry |
| Security | OWASP, Secrets Management, Vulnerability Scanning |
| AI & Data | Python, Anomaly Detection, RAG |

## Project Status

**Week 1 complete**
- Repository
- ADR-0001 and API draft
- No runnable code yet

**Week 2 complete**
- Runnable Spring Boot backend, module structure, typed configuration, request ID logging.

## Technology Stack

- Java 21 LTS
- Spring Boot 4.1
- Maven
- Maven Wrapper

## Project Structure

The backend is organized into feature-oriented modules:

- `monitoring` - Service monitoring and health checks.
- `incidents` - Incident management.
- `identity` - User identity and authentication.
- `billing` - Subscription and billing functionality.
- `platform` - Technical cross-cutting infrastructure.

## How to Run Locally

### Prerequisites

- JDK 21
- Git

Maven does not need to be installed separately because
the project includes the Maven Wrapper.

### Start the application

Open a terminal in the `backend/` directory and run:

```powershell
.\mvnw.cmd spring-boot:run
```

```bash
./mvnw spring-boot:run
```

The application starts on:

http://localhost:8080

### Run tests

From the `backend/` directory:

```powershell
.\mvnw.cmd test
```

```bash
./mvnw test
```

### Check application health

With the application running, execute:

```powershell
curl.exe -i http://localhost:8080/actuator/health
```

```bash
curl -i http://localhost:8080/actuator/health
```

A successful response should return HTTP 200 and report the application status as `UP`.

The response includes an `X-Request-Id` header for request correlation.

## Configuration

Monitoring settings can be configured through environment variables.

| Environment variable | Default | Valid values |
|---|---|---|
| SENTINEL_CHECKS_TIMEOUT | 5s| 1-30 seconds|
| SENTINEL_CHECKS_MAX_REDIRECTS | 3| 0-10  |
| SENTINEL_CHECKS_USER_AGENT  | Sentinel-Monitor/1.0 | Non-blank string |

Environment variables override values defined in `application.yml`.

### Development profile

The development profile can be activated with:

```
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

The development profile enables DEBUG logging for com.sentinel.

To return to the default profile:

```
Remove-Item Env:SPRING_PROFILES_ACTIVE
```

## Development

Run the automated tests before committing changes:

```
.\mvnw.cmd test
```

---

*Sentinel is being developed as a hands-on engineering project to explore backend architecture, distributed systems, cloud infrastructure, cybersecurity, and modern observability practices.*
