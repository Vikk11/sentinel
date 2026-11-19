package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceServiceTest {
    private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void serviceIsVisibleOnlyToItsTenant() {
        ServiceRepository repository = new InMemoryServiceRepository();
        TenantContext tenantContext = () -> TENANT_A;
        ServiceService service = new ServiceService(repository, tenantContext);

        CreateServiceRequest request = new CreateServiceRequest(
                "Example",
                "https://example.com",
                60,
                true
        );

        ServiceResponse created = service.create(request);

        // Tenant A can read its own service
        ServiceResponse found = service.getById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("Example", found.name());

        // Tenant B cannot read Tenant A's service
        TenantContext tenantBContext = () -> TENANT_B;
        ServiceService serviceForTenantB = new ServiceService(repository, tenantBContext);

        assertThrows(
                ServiceNotFoundException.class,
                () -> serviceForTenantB.getById(created.id())
        );

        // A nonexistent service behaves identically
        assertThrows(
                ServiceNotFoundException.class,
                () -> serviceForTenantB.getById(UUID.randomUUID())
        );
    }
}