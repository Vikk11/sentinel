# ADR-0001: Choose a Modular Monolith Architecture

## Status
Accepted, 2026-11-12

## Context

Sentinel is a multi-tenant monitoring and incident management platform. It needs to support service health checks, incident management, alerting, user authentication, and role-based access control.

In the future, Sentinel will also support event, error, and metric ingestion through Apache Kafka, which will introduce additional background processing and asynchronous communication.

Since this project is being developed by a single developer, keeping the architecture manageable is important. I want to focus on learning backend development, distributed systems, security, and DevOps without introducing unnecessary complexity too early.

The main architectural options considered were a modular monolith and microservices.

## Decision

I will use a modular monolith architecture with Spring Boot.

The application will be deployed as a single backend application but organized into separate modules with clear responsibilities and boundaries.

As the project grows, background workers and Kafka consumers may be extracted into separate services if there is a clear need for independent scaling or deployment.

### Module Boundaries

To prevent modules from becoming tightly coupled:

- Each module will have its own package (e.g. `monitoring`, `incidents`, `identity`).
- Modules can communicate only through small, explicitly defined public interfaces.
- Internal implementation details must not be accessed directly by other modules.
- As the project grows, automated architecture tests will be introduced to detect dependency violations.

### Background Workers

For example, the Monitoring module will contain a scheduled worker
that periodically executes health checks and stores their results.

Initially, this worker will run inside the main Spring Boot application.
It will communicate with other modules through their public interfaces
rather than accessing their internal implementation directly.

## Alternatives Considered

### Microservices

Microservices were considered because they allow independent deployment, scaling, and failure isolation.

However, they would introduce additional complexity, including inter-service communication, network failures, distributed transactions, service discovery, and multiple deployment pipelines.

For a solo developer, maintaining all these components would take time away from implementing Sentinel's actual features.

## Consequences

### Positive

- Simpler development, testing, debugging, and deployment.
- No network communication required between internal modules.
- Less infrastructure and operational overhead for a solo developer.
- Easier to introduce new features without managing multiple services.

### Negative

- All modules must be deployed together.
- Individual modules cannot be scaled independently.
- A failure in the application can affect the entire backend, for example a runaway check worker could exhaust threads or memory and take the API down with it.
- Extracting modules into independent services later may require refactoring.
- Module boundaries are enforced only by convention, so they can erode.

## Future Considerations

As Sentinel evolves, Apache Kafka will be introduced for asynchronous event ingestion and processing.

Background workers and Kafka consumers may be extracted into separate
deployable services when measurements show that check execution
measurably affects API latency or when background processing requires
independent scaling to meet defined performance targets.

This decision will be supported by monitoring and performance measurements,
rather than assumptions about future scaling requirements.

