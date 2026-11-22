package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceServiceTest {

    private ServiceRepository repository;
    private TenantContext tenantContext;
    private ServiceService service;

    private UUID tenantA;
    private UUID tenantB;

    @BeforeEach
    void setUp() {
        repository = mock(ServiceRepository.class);
        tenantContext = mock(TenantContext.class);

        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();

        when(tenantContext.currentTenantId()).thenReturn(tenantA);

        service = new ServiceService(repository, tenantContext);
    }

    private MonitoredService existing(
            UUID tenantId,
            String name,
            String url,
            int interval,
            boolean enabled
    ) {
        Instant createdAt =
                Instant.parse("2026-01-01T10:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-01-01T10:00:00Z");

        return new MonitoredService(
                UUID.randomUUID(),
                tenantId,
                name,
                url,
                ServiceUrlNormalizer.normalize(url),
                interval,
                enabled,
                createdAt,
                updatedAt
        );
    }

    @Test
    void serviceIsVisibleOnlyToItsTenant() {
        ServiceRepository repository = new InMemoryServiceRepository();
        TenantContext tenantContext = () -> tenantA;
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
        TenantContext tenantBContext = () -> tenantB;
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
        TenantContext tenantContext = () -> tenantA;

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
        TenantContext tenantContext = () -> tenantA;

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
        TenantContext tenantContext = () -> tenantA;

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

    @Test
    void createStoresNormalizedUrl() {
        when(repository.createIfUrlUnique(any()))
                .thenAnswer(invocation -> true);

        CreateServiceRequest request = new CreateServiceRequest(
                "Test",
                "HTTPS://Example.COM",
                60,
                true
        );

        ServiceResponse response = service.create(request);

        assertEquals("HTTPS://Example.COM", response.url());

        verify(repository).createIfUrlUnique(
                argThat(saved ->
                        saved.normalizedUrl().equals(
                                "https://example.com/"
                        )
                )
        );
    }

    @Test
    void createDuplicateThrowsConflict() {
        when(repository.createIfUrlUnique(any()))
                .thenReturn(false);

        CreateServiceRequest request = new CreateServiceRequest(
                "Duplicate",
                "https://example.com",
                60,
                true
        );

        assertThrows(
                DuplicateServiceUrlException.class,
                () -> service.create(request)
        );
    }

    @Test
    void replaceChangesAllFieldsButPreservesIdentity() {
        MonitoredService existing = existing(
                tenantA,
                "Old Name",
                "https://old.example.com",
                60,
                true
        );

        when(repository.findByIdAndTenantId(
                existing.id(), tenantA
        )).thenReturn(Optional.of(existing));

        when(repository.replaceIfUrlUnique(any()))
                .thenReturn(ReplaceResult.UPDATED);

        ReplaceServiceRequest request = new ReplaceServiceRequest(
                "New Name",
                "https://new.example.com",
                300,
                false
        );

        ServiceResponse response =
                service.replace(existing.id(), request);

        assertEquals(existing.id(), response.id());
        assertEquals(existing.createdAt(), response.createdAt());

        assertEquals("New Name", response.name());
        assertEquals("https://new.example.com", response.url());
        assertEquals(300, response.checkIntervalSeconds());
        assertFalse(response.enabled());

        assertTrue(response.updatedAt().isAfter(existing.updatedAt()));

        verify(repository).replaceIfUrlUnique(
                argThat(replacement ->
                        replacement.id().equals(existing.id())
                                && replacement.tenantId().equals(tenantA)
                                && replacement.createdAt().equals(
                                existing.createdAt()
                        )
                                && replacement.updatedAt().isAfter(
                                existing.updatedAt()
                        )
                )
        );
    }

    @Test
    void replaceWithIdenticalValuesDoesNotSaveOrChangeUpdatedAt() {
        MonitoredService existing = existing(
                tenantA,
                "Service",
                "https://example.com",
                300,
                true
        );

        when(repository.findByIdAndTenantId(
                existing.id(), tenantA
        )).thenReturn(Optional.of(existing));

        ServiceResponse response = service.replace(
                existing.id(),
                new ReplaceServiceRequest(
                        existing.name(),
                        existing.url(),
                        existing.checkIntervalSeconds(),
                        existing.enabled()
                )
        );

        assertEquals(existing.updatedAt(), response.updatedAt());

        verify(repository, never()).replaceIfUrlUnique(any());
    }

    @Test
    void replaceWithSameNormalizedUrlButDifferentCaseIsNotDuplicate() {
        MonitoredService existing = existing(
                tenantA,
                "Service",
                "https://example.com",
                60,
                true
        );

        when(repository.findByIdAndTenantId(
                existing.id(), tenantA
        )).thenReturn(Optional.of(existing));

        when(repository.replaceIfUrlUnique(any()))
                .thenReturn(ReplaceResult.UPDATED);

        ServiceResponse response = service.replace(
                existing.id(),
                new ReplaceServiceRequest(
                        "Updated",
                        "HTTPS://Example.COM",
                        60,
                        true
                )
        );

        assertEquals(existing.id(), response.id());
        assertEquals("HTTPS://Example.COM", response.url());

        verify(repository).replaceIfUrlUnique(
                argThat(replacement ->
                        replacement.normalizedUrl().equals(
                                "https://example.com/"
                        )
                )
        );
    }

    @Test
    void replaceDuplicateThrowsConflict() {
        MonitoredService existing = existing(
                tenantA,
                "A",
                "https://a.example.com",
                60,
                true
        );

        when(repository.findByIdAndTenantId(
                existing.id(), tenantA
        )).thenReturn(Optional.of(existing));

        when(repository.replaceIfUrlUnique(any()))
                .thenReturn(ReplaceResult.DUPLICATE_URL);

        assertThrows(
                DuplicateServiceUrlException.class,
                () -> service.replace(
                        existing.id(),
                        new ReplaceServiceRequest(
                                "A",
                                "https://b.example.com",
                                60,
                                true
                        )
                )
        );
    }

    @Test
    void replaceMissingServiceThrowsNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdAndTenantId(id, tenantA))
                .thenReturn(Optional.empty());

        assertThrows(
                ServiceNotFoundException.class,
                () -> service.replace(
                        id,
                        new ReplaceServiceRequest(
                                "A",
                                "https://example.com",
                                60,
                                true
                        )
                )
        );
    }

    @Test
    void replaceMissingServiceMapsToNotFound() {
        MonitoredService otherTenantService = existing(
                tenantB,
                "Other",
                "https://example.com",
                60,
                true
        );

        when(repository.findByIdAndTenantId(
                otherTenantService.id(), tenantA
        )).thenReturn(Optional.empty());

        assertThrows(
                ServiceNotFoundException.class,
                () -> service.replace(
                        otherTenantService.id(),
                        new ReplaceServiceRequest(
                                "Changed",
                                "https://example.com",
                                60,
                                true
                        )
                )
        );
    }

    @Test
    void deleteMissingOrCrossTenantServiceReturnsNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.deleteByIdAndTenantId(id, tenantA))
                .thenReturn(false);

        assertThrows(
                ServiceNotFoundException.class,
                () -> service.delete(id)
        );
    }

    @Test
    void deleteExistingServiceSucceeds() {
        UUID id = UUID.randomUUID();

        when(repository.deleteByIdAndTenantId(id, tenantA))
                .thenReturn(true);

        assertDoesNotThrow(() -> service.delete(id));
    }

    @Test
    void putOnAnotherTenantServiceReturns404() {
        InMemoryServiceRepository realRepository =
                new InMemoryServiceRepository();

        MonitoredService otherTenantService = existing(
                tenantB,
                "Other Tenant",
                "https://other.example.com",
                60,
                true
        );

        assertTrue(
                realRepository.createIfUrlUnique(otherTenantService)
        );

        ServiceService realService = new ServiceService(
                realRepository,
                tenantContext
        );

        assertThrows(
                ServiceNotFoundException.class,
                () -> realService.replace(
                        otherTenantService.id(),
                        new ReplaceServiceRequest(
                                "Changed",
                                "https://changed.example.com",
                                120,
                                false
                        )
                )
        );
    }

    @Test
    void deleteOnAnotherTenantServiceReturns404() {
        InMemoryServiceRepository realRepository =
                new InMemoryServiceRepository();

        MonitoredService otherTenantService = existing(
                tenantB,
                "Other Tenant",
                "https://other.example.com",
                60,
                true
        );

        assertTrue(
                realRepository.createIfUrlUnique(otherTenantService)
        );

        ServiceService realService = new ServiceService(
                realRepository,
                tenantContext
        );

        assertThrows(
                ServiceNotFoundException.class,
                () -> realService.delete(otherTenantService.id())
        );

        // The other tenant's service still exists.
        assertTrue(
                realRepository.findByIdAndTenantId(
                        otherTenantService.id(),
                        tenantB
                ).isPresent()
        );
    }
}