package com.sentinel.monitoring.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Service
class ServiceService {
    private static final Logger log = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository repository;
    private final TenantContext tenantContext;

    ServiceService(ServiceRepository repository, TenantContext tenantContext){
        this.repository = repository;
        this.tenantContext = tenantContext;
    }

    ServiceResponse create(CreateServiceRequest request){
        UUID tenantId = tenantContext.currentTenantId();

        validateUrl(request.url());

        Instant now = Instant.now();

        MonitoredService service = new MonitoredService(
                UUID.randomUUID(),
                tenantId,
                request.name(),
                request.url(),
                request.checkIntervalSeconds(),
                request.enabled(),
                now,
                now
        );

        repository.save(service);

        log.info(
                "Service created: serviceId={}, tenantId={}",
                service.id(),
                service.tenantId()
        );

        return ServiceResponse.from(service);
    }

    ServiceResponse getById(UUID id) {
        UUID tenantId = tenantContext.currentTenantId();

        MonitoredService service = repository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        return ServiceResponse.from(service);
    }

    private void validateUrl(String url){
        URI uri = URI.create(url);
        String scheme = uri.getScheme();

        if (scheme == null
                || !(scheme.equalsIgnoreCase("http")
                || scheme.equalsIgnoreCase("https"))
                || uri.getHost() == null
                || uri.getHost().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "URL must have an http or https scheme and a valid host"
            );
        }

        //TODO(phase 5): SSRF validation
    }
}