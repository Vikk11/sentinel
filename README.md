
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

**Week 1: Project Setup**

- Repository structure and API design

---

*Sentinel is being developed as a hands-on engineering project to explore backend architecture, distributed systems, cloud infrastructure, cybersecurity, and modern observability practices.*
