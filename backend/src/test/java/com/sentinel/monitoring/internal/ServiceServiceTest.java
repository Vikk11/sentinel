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

    @Test
    void rejectsInvalidUrls(){
        ServiceRepository repository = new InMemoryServiceRepository();
        TenantContext tenantContext = () -> TENANT_A;

        ServiceService service = new ServiceService(repository, tenantContext);

        assertInvalidUrl(service, "ftp://example.com");
        assertInvalidUrl(service, "https://");
        assertInvalidUrl(service, "javascript:alert(1)");
        assertInvalidUrl(service, "not-a-url");
        assertInvalidUrl(service, "https://user:password@example.com");
        assertInvalidUrl(service, "https://example.com/path with spaces");
    }

    private void assertInvalidUrl(ServiceService service, String url){
        CreateServiceRequest request = new CreateServiceRequest(
                "Example",
                url,
                60,
                true
        );

        assertThrows(
                InvalidServiceUrlException.class,
                () -> service.create(request)
        );
    }

    @Test
    void acceptsValidHttpsUrl() {
        ServiceRepository repository = new InMemoryServiceRepository();
        TenantContext tenantContext = () -> TENANT_A;

        ServiceService service = new ServiceService(repository, tenantContext);

        CreateServiceRequest request = new CreateServiceRequest(
                "Example",
                "https://example.com",
                60,
                true
        );

        ServiceResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals("https://example.com", response.url());
    }

    @Test
    void rejectsFtpUrl() {
        assertInvalidUrl("ftp://x");
    }

    @Test
    void rejectsUrlWithoutHost() {
        assertInvalidUrl("https://");
    }

    @Test
    void rejectsUrlWithCredentials() {
        assertInvalidUrl("https://user:password@example.com");
    }

    @Test
    void rejectsMalformedUrl() {
        assertInvalidUrl("https://bad host");
    }

    private void assertInvalidUrl(String url) {
        ServiceRepository repository = new InMemoryServiceRepository();
        TenantContext tenantContext = () -> TENANT_A;

        ServiceService service = new ServiceService(repository, tenantContext);

        CreateServiceRequest request = new CreateServiceRequest(
                "Example",
                url,
                60,
                true
        );

        InvalidServiceUrlException exception = assertThrows(
                InvalidServiceUrlException.class,
                () -> service.create(request)
        );

        assertFalse(exception.getMessage().contains(url));
    }
}