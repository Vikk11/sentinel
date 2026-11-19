package com.sentinel.monitoring.internal;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
class InMemoryServiceRepository implements ServiceRepository {
    private final ConcurrentMap<UUID, MonitoredService> services = new ConcurrentHashMap<>();

    @Override
    public MonitoredService save(MonitoredService service) {
        services.put(service.id(), service);
        return service;
    }

    @Override
    public Optional<MonitoredService> findByIdAndTenantId(UUID id, UUID tenantId){
        return Optional.ofNullable(services.get(id))
                .filter(service -> service.tenantId().equals(tenantId));
    }
}