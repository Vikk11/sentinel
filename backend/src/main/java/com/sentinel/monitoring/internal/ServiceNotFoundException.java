package com.sentinel.monitoring.internal;

import java.util.UUID;

class ServiceNotFoundException extends RuntimeException {
    ServiceNotFoundException(UUID serviceId) {
        super("Service not found: " + serviceId);
    }
}