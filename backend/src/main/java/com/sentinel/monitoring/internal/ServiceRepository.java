package com.sentinel.monitoring.internal;

import java.util.Optional;
import java.util.UUID;

interface ServiceRepository {
    MonitoredService save(MonitoredService service);
    Optional<MonitoredService> findByIdAndTenantId(UUID id, UUID tenantId);
}