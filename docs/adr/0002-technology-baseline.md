# ADR-0002: Technology Baseline

## Status

Accepted — 2026-11-14

## Context


Sentinel is a service-monitoring application that will need to:

- Execute scheduled HTTP health checks and handle network failures.
- Expose a REST API for managing monitored services and retrieving check results.
- Support authentication, authorization, and tenant isolation.
- Integrate with messaging infrastructure such as Kafka as the system evolves.
- Provide reliable automated testing, configuration validation, and operational
  visibility.

The project needs a Java ecosystem with mature libraries for web development, 
security, messaging, testing, and production operations. The initial 
technology choices should also be straightforward to run locally and maintain.

## Decision

The initial technology baseline is:

- **Java 21 LTS** as the programming language and runtime.
- **Spring Boot 4.1** as the application framework.
- **Maven** as the build and dependency management tool,
  using the Maven Wrapper to ensure consistent builds.

Java 21 is selected because it is the Java LTS version currently configured
and tested for this project. Spring Boot 4.1 supports Java 21, so this is a
supported combination. Java 25 is also an LTS release and is supported by
Spring Boot 4.1; upgrading the project's Java baseline can be evaluated
separately after testing the build, dependencies, and deployment environment.

Spring Boot is selected for its mature application framework and ecosystem.
It provides the foundation for REST APIs, dependency injection, externalized
configuration, validation, security integration, health checks, and testing.
Its ecosystem also provides established integration options for technologies
such as Kafka.

Spring Boot 4.1 was selected because it is a currently supported release line.
Spring Boot's published support policy provides at least 12 months of support
for minor releases. Version 4.1.0 was released on June 10, 2026, giving the
release line a meaningful support window for a new project. The project must
continue tracking supported patch releases rather than remain on its initial
4.1.x version indefinitely.

Maven is selected because it is familiar, convention-based, and sufficient
for the project's current build requirements. The Maven Wrapper ensures that
contributors can use the project's configured Maven version without installing
Maven separately.

## Alternatives Considered

### Java 25 instead of Java 21

Java 25 is a newer LTS release and is supported by Spring Boot 4.1. It is a
valid alternative and may become the project's baseline after compatibility
testing.

Java 21 was retained for the initial implementation because it is the version
already tested and configured in the project. This is a practical consistency
decision, not a claim that Java 21 has a longer support lifetime than Java 25.

### Gradle instead of Maven

Gradle offers flexible build configuration and can provide efficient
incremental builds. Maven was preferred because its conventions are familiar,
its build structure is predictable, and the project's current build needs do
not justify Gradle's additional flexibility.

### Kotlin instead of Java

Kotlin offers concise syntax, null-safety features, and Spring integration.
Java was selected to keep the initial implementation aligned with the
project's tested baseline and conventional Spring examples. Kotlin remains
a possible option for future work, but mixing languages is not justified
for the current scope.

### Spring Boot 3.x instead of Spring Boot 4.1

Spring Boot 3.x has a larger body of tutorials, examples, and community
experience. However, selecting an older release line for a new project
can shorten the remaining period of official support and bring forward
the need for a framework upgrade.

Spring Boot 4.1 was selected as a currently supported release line rather
than simply because it is newer. The trade-off is that Boot 4 is a newer
framework generation: many tutorials and examples still target Boot 3.x,
and some dependencies or migration instructions may require adaptation.

## Consequences

### Positive

- The project starts on a supported Spring Boot release line.
- Java 21 provides an established LTS baseline that has already been tested
  with the project.
- Spring provides mature options for REST APIs, security, Kafka integration,
  validation, and automated testing.
- Maven provides a conventional and reproducible build through its wrapper.
- The technology choices are widely used and have established documentation
  and community support.

### Negative

- Java 25 is a newer LTS release; retaining Java 21 means the project should
  revisit its Java baseline rather than assume 21 is the long-term choice.
- Spring Boot 4.x is relatively new, and many online tutorials still target
  Spring Boot 3.x.
- Some third-party libraries and examples may need compatibility checks
  or adaptation for Spring Boot 4.
- Maven can require more verbose configuration than Gradle for complex builds.
- Java is more verbose than Kotlin for some application code.
