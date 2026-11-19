package com.sentinel.monitoring.internal;

import java.time.Instant;
import java.util.UUID;

record ServiceResponse(
        UUID id,
        String name,
        String url,
        int checkIntervalSeconds,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    static ServiceResponse from(MonitoredService service) {
        return new ServiceResponse(
                service.id(),
                service.name(),
                service.url(),
                service.checkIntervalSeconds(),
                service.enabled(),
                service.createdAt(),
                service.updatedAt()
        );
    }
}