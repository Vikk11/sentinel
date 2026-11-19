package com.sentinel.monitoring.internal;

import java.time.Instant;
import java.util.UUID;

record MonitoredService(
        UUID id,
        UUID tenantId,
        String name,
        String url,
        Integer checkIntervalSeconds,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}