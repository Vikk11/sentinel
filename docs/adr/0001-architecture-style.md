# ADR-0001: Choose a Modular Monolith Architecture

## Status
Accepted

## Context

Sentinel is a multi-tenant monitoring and incident management platform. It needs to support service health checks, incident management, alerting, user authentication, and role-based access control.

In the future, Sentinel will also support event, error, and metric ingestion through Apache Kafka, which will introduce additional background processing and asynchronous communication.

Since this project is being developed by a single developer, keeping the architecture manageable is important. I want to focus on learning backend development, distributed systems, security, and DevOps without introducing unnecessary complexity too early.

The main architectural options considered were a modular monolith and microservices.

## Decision

I will use a modular monolith architecture with Spring Boot.

The application will be deployed as a single backend application but organized into separate modules with clear responsibilities and boundaries.

As the project grows, background workers and Kafka consumers may be extracted into separate services if there is a clear need for independent scaling or deployment.

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

If background processing becomes resource-intensive or requires independent scaling, Kafka consumers and monitoring workers may be extracted into separate deployable services.

