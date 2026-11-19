package com.sentinel.monitoring.internal;

import java.util.UUID;

interface TenantContext {
    UUID currentTenantId();
}