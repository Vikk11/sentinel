package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceServiceConcurrencyTest {

    @Test
    void onlyOneConcurrentCreateCanUseSameUrl() throws Exception {
        UUID tenantId = UUID.randomUUID();

        TenantContext tenantContext = mock(TenantContext.class);

        when(tenantContext.currentTenantId())
                .thenReturn(tenantId);

        ServiceRepository repository =
                new InMemoryServiceRepository();

        ServiceService service =
                new ServiceService(repository, tenantContext);

        int requestCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch ready =
                new CountDownLatch(requestCount);

        CountDownLatch start =
                new CountDownLatch(1);

        AtomicInteger successes =
                new AtomicInteger();

        AtomicInteger duplicates =
                new AtomicInteger();

        try {
            Future<?>[] futures = new Future<?>[requestCount];

            for (int i = 0; i < requestCount; i++) {
                final int index = i;

                futures[i] = executor.submit(() -> {
                    ready.countDown();

                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timed out waiting for start signal"
                        );
                    }

                    try {
                        service.create(
                                new CreateServiceRequest(
                                        "Service " + index,
                                        "https://same.example.com",
                                        60,
                                        true
                                )
                        );

                        successes.incrementAndGet();
                    } catch (DuplicateServiceUrlException exception) {
                        duplicates.incrementAndGet();
                    }

                    return null;
                });
            }

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "Workers did not become ready"
            );

            start.countDown();

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            assertEquals(1, successes.get());
            assertEquals(19, duplicates.get());

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}