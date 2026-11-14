# ADR-0002: Technology Baseline

## Status

Accepted — 2026-11-14

## Context

Sentinel needs a stable technology foundation for its backend.

The project will need to support service monitoring, health checks,
incident management, authentication, and billing.

The technology choices should make the project maintainable,
easy to develop, and suitable for adding new features over time.

## Decision

The backend will use the following technologies:

- **Java 21 LTS** as the programming language and runtime.
- **Spring Boot 4.1** as the application framework.
- **Maven** as the build and dependency management tool,
  using the Maven Wrapper to ensure consistent builds.

Java 21 was selected because it is a long-term support release
and provides modern Java features while maintaining stability.

Spring Boot was selected because it provides dependency
injection, configuration management, validation, and
integration with common backend technologies.

Maven was selected because it is widely used in Java projects
and provides a predictable way to manage dependencies and builds.
The Maven Wrapper allows developers to run the project without
having to install Maven separately.

## Alternatives Considered

### Gradle instead of Maven

Gradle offers flexible build configuration and can be faster
for larger projects with incremental builds.

However, Maven has a more familiar and convention-based structure.
For this project, its simplicity and predictable configuration
are preferred.

### Spring Boot 3.x instead of Spring Boot 4.1

Spring Boot 3.x is more established and has a larger amount of
available tutorials and community examples.

Spring Boot 4.1 was selected as the project baseline to use
the newer framework generation.

One disadvantage is that Spring Boot 4.1 is relatively new,
so some tutorials and examples still target Spring Boot 3.x.
This may require checking documentation and adapting older examples.

## Consequences

### Positive

- Java 21 provides a modern and stable LTS foundation.
- Spring Boot simplifies application configuration and development.
- Maven provides a predictable build structure.
- The Maven Wrapper makes builds more consistent across environments.
- The selected technologies have strong integration with the Java ecosystem.

### Negative

- Spring Boot 4.1 is newer, so fewer tutorials may be available.
- Some libraries and examples written for Spring Boot 3.x
  may require changes.
- Maven can require more configuration than Gradle for
  certain complex build scenarios.

These trade-offs are accepted for the initial Sentinel backend.