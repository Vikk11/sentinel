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

        String normalizedUrl = ServiceUrlNormalizer.normalize(request.url());

        Instant now = Instant.now();

        MonitoredService service = new MonitoredService(
                UUID.randomUUID(),
                tenantId,
                request.name(),
                request.url(),
                normalizedUrl,
                request.checkIntervalSeconds(),
                request.enabled(),
                now,
                now
        );

        if (!repository.createIfUrlUnique(service)) {
            throw new DuplicateServiceUrlException();
        }

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
        URI uri;

        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new InvalidServiceUrlException();
        }

        String scheme = uri.getScheme();

        if (scheme == null
                || !(scheme.equalsIgnoreCase("http")
                || scheme.equalsIgnoreCase("https"))
                || uri.getHost() == null
                || uri.getHost().isBlank()
                || uri.getUserInfo() != null
        ) {
            throw new InvalidServiceUrlException();
        }

        //TODO(phase 5): SSRF validation
    }

    ServiceResponse replace(UUID id, ReplaceServiceRequest request) {
        UUID tenantId = tenantContext.currentTenantId();

        MonitoredService existing = repository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        validateUrl(request.url());

        String normalizedUrl = ServiceUrlNormalizer.normalize(request.url());

        MonitoredService candidate = new MonitoredService(
                existing.id(),
                existing.tenantId(),
                request.name(),
                request.url(),
                normalizedUrl,
                request.checkIntervalSeconds(),
                request.enabled(),
                existing.createdAt(),
                existing.updatedAt()
        );

        if (candidate.equals(existing)){
            return ServiceResponse.from(existing);
        }

        Instant now = Instant.now();

        if (!now.isAfter(existing.updatedAt())) {
            now = existing.updatedAt().plusNanos(1);
        }

        MonitoredService replacement = new MonitoredService(
                existing.id(),
                existing.tenantId(),
                request.name(),
                request.url(),
                normalizedUrl,
                request.checkIntervalSeconds(),
                request.enabled(),
                existing.createdAt(),
                now
        );

        ReplaceResult result = repository.replaceIfUrlUnique(replacement);

        return switch (result) {
            case UPDATED -> ServiceResponse.from(replacement);
            case DUPLICATE_URL -> throw new DuplicateServiceUrlException();
            case NOT_FOUND -> throw new ServiceNotFoundException(id);
        };
    }

    void delete(UUID id) {
        UUID tenantId = tenantContext.currentTenantId();

        if (!repository.deleteByIdAndTenantId(id, tenantId)) {
            throw new ServiceNotFoundException(id);
        }
    }
}