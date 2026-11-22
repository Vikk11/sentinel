package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryServiceRepositoryTest {

    private InMemoryServiceRepository repository;

    private UUID tenantA;
    private UUID tenantB;

    @BeforeEach
    void setUp() {
        repository = new InMemoryServiceRepository();
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
    }

    private MonitoredService service(UUID id, UUID tenantId, String url) {
        Instant now = Instant.now();

        return new MonitoredService(
                id,
                tenantId,
                "Test Service",
                url,
                ServiceUrlNormalizer.normalize(url),
                60,
                true,
                now,
                now
        );
    }

    @Test
    void createIfUrlUniqueAcceptsFirstService() {
        MonitoredService service = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        assertTrue(repository.createIfUrlUnique(service));
    }

    @Test
    void createIfUrlUniqueRejectsDuplicateInSameTenant() {
        MonitoredService first = service(
                UUID.randomUUID(),
                tenantA,
                "HTTPS://Example.COM"
        );

        MonitoredService second = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com/"
        );

        assertTrue(repository.createIfUrlUnique(first));
        assertFalse(repository.createIfUrlUnique(second));
    }

    @Test
    void createIfUrlUniqueAllowsDifferentTenants() {
        MonitoredService first = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        MonitoredService second = service(
                UUID.randomUUID(),
                tenantB,
                "https://example.com"
        );

        assertTrue(repository.createIfUrlUnique(first));
        assertTrue(repository.createIfUrlUnique(second));
    }

    @Test
    void findByIdIsTenantScoped() {
        MonitoredService saved = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        repository.createIfUrlUnique(saved);

        assertTrue(
                repository.findByIdAndTenantId(
                        saved.id(),
                        tenantA
                ).isPresent()
        );

        assertTrue(
                repository.findByIdAndTenantId(
                        saved.id(),
                        tenantB
                ).isEmpty()
        );
    }

    @Test
    void replaceKeepingOwnUrlSucceeds() {
        MonitoredService existing = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        assertTrue(repository.createIfUrlUnique(existing));

        MonitoredService replacement = new MonitoredService(
                existing.id(),
                existing.tenantId(),
                "Updated",
                existing.url(),
                existing.normalizedUrl(),
                120,
                false,
                existing.createdAt(),
                Instant.now()
        );

        assertEquals(
                ReplaceResult.UPDATED,
                repository.replaceIfUrlUnique(replacement)
        );
    }

    @Test
    void replaceWithAnotherServiceUrlConflicts() {
        MonitoredService first = service(
                UUID.randomUUID(),
                tenantA,
                "https://one.example.com"
        );

        MonitoredService second = service(
                UUID.randomUUID(),
                tenantA,
                "https://two.example.com"
        );

        assertTrue(repository.createIfUrlUnique(first));
        assertTrue(repository.createIfUrlUnique(second));

        MonitoredService replacement = new MonitoredService(
                second.id(),
                second.tenantId(),
                second.name(),
                first.url(),
                first.normalizedUrl(),
                second.checkIntervalSeconds(),
                second.enabled(),
                second.createdAt(),
                Instant.now()
        );

        assertEquals(
                ReplaceResult.DUPLICATE_URL,
                repository.replaceIfUrlUnique(replacement)
        );
    }

    @Test
    void replaceCannotChangeAnotherTenantService() {
        MonitoredService existing = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        assertTrue(repository.createIfUrlUnique(existing));

        MonitoredService attemptedReplacement = new MonitoredService(
                existing.id(),
                tenantB,
                existing.name(),
                existing.url(),
                existing.normalizedUrl(),
                existing.checkIntervalSeconds(),
                existing.enabled(),
                existing.createdAt(),
                Instant.now()
        );

        assertEquals(
                ReplaceResult.NOT_FOUND,
                repository.replaceIfUrlUnique(attemptedReplacement)
        );
    }

    @Test
    void deleteIsTenantScoped() {
        MonitoredService existing = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        assertTrue(repository.createIfUrlUnique(existing));

        assertFalse(
                repository.deleteByIdAndTenantId(
                        existing.id(),
                        tenantB
                )
        );

        assertTrue(
                repository.findByIdAndTenantId(
                        existing.id(),
                        tenantA
                ).isPresent()
        );
    }

    @Test
    void deleteRemovesService() {
        MonitoredService existing = service(
                UUID.randomUUID(),
                tenantA,
                "https://example.com"
        );

        assertTrue(repository.createIfUrlUnique(existing));

        assertTrue(
                repository.deleteByIdAndTenantId(
                        existing.id(),
                        tenantA
                )
        );

        assertFalse(
                repository.findByIdAndTenantId(
                        existing.id(),
                        tenantA
                ).isPresent()
        );

        assertFalse(
                repository.deleteByIdAndTenantId(
                        existing.id(),
                        tenantA
                )
        );
    }

    @Test
    void concurrentRepositoryCreatesAllowOnlyOneDuplicateUrl() throws Exception {
        int count = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(count);

        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch start = new CountDownLatch(1);

        java.util.concurrent.atomic.AtomicInteger successful =
                new java.util.concurrent.atomic.AtomicInteger();

        try {
            Future<?>[] futures = new Future<?>[count];

            for (int i = 0; i < count; i++) {
                final int index = i;

                futures[i] = executor.submit(() -> {
                    ready.countDown();

                    start.await();

                    MonitoredService service = service(
                            UUID.randomUUID(),
                            tenantA,
                            "https://same.example.com"
                    );

                    if (repository.createIfUrlUnique(service)) {
                        successful.incrementAndGet();
                    }

                    return null;
                });
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));

            start.countDown();

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertEquals(1, successful.get());

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}