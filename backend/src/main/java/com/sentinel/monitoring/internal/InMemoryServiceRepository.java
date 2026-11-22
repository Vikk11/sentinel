package com.sentinel.monitoring.internal;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
class InMemoryServiceRepository implements ServiceRepository {
    private final ConcurrentMap<UUID, MonitoredService> services = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();

    @Override
    public boolean createIfUrlUnique(MonitoredService service) {
        synchronized (writeLock) {
            boolean duplicate = services.values().stream()
                    .anyMatch(existing ->
                            existing.tenantId().equals(service.tenantId())
                                    && existing.normalizedUrl().equals(
                                    service.normalizedUrl()
                            )
                    );

            if (duplicate || services.containsKey(service.id())) {
                return false;
            }

            services.put(service.id(), service);
            return true;
        }
    }

    @Override
    public Optional<MonitoredService> findByIdAndTenantId(UUID id, UUID tenantId){
        MonitoredService service = services.get(id);

        if (service == null || !service.tenantId().equals(tenantId)) {
            return Optional.empty();
        }

        return Optional.of(service);
    }

    @Override
    public ReplaceResult replaceIfUrlUnique(MonitoredService replacement) {
        MonitoredService existing = services.get(replacement.id());

        if (existing == null || !existing.tenantId().equals(replacement.tenantId())) {
            return ReplaceResult.NOT_FOUND;
        }

        boolean duplicate = services.values().stream()
                .anyMatch(service ->
                        !service.id().equals(replacement.id()
                        ) && service.tenantId().equals(replacement.tenantId()
                        ) && service.normalizedUrl().equals(
                                replacement.normalizedUrl()
                        )
                );

        if (duplicate) {
            return ReplaceResult.DUPLICATE_URL;
        }

        services.put(replacement.id(), replacement);

        return ReplaceResult.UPDATED;
    }

    @Override
    public boolean deleteByIdAndTenantId(UUID id, UUID tenantId) {
        synchronized (writeLock) {
            MonitoredService existing = services.get(id);

            if (existing == null || !existing.tenantId().equals(tenantId)){
                return false;
            }

            return services.remove(id, existing);
        }
    }
}