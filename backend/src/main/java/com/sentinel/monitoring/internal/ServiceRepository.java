package com.sentinel.monitoring.internal;

import java.util.Optional;
import java.util.UUID;

interface ServiceRepository {
    boolean createIfUrlUnique(MonitoredService service);
    Optional<MonitoredService> findByIdAndTenantId(UUID id, UUID tenantId);
    ReplaceResult replaceIfUrlUnique(MonitoredService service);
    boolean deleteByIdAndTenantId(UUID id, UUID tenantId);
}

enum ReplaceResult {
    UPDATED,
    DUPLICATE_URL,
    NOT_FOUND
}