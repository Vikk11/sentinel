package com.sentinel.monitoring.internal;

import java.time.Instant;
import java.util.UUID;

record MonitoredService(
        UUID id,
        UUID tenantId,
        String name,
        String url,
        int checkIntervalSeconds,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}