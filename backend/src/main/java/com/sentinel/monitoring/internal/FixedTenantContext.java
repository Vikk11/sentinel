package com.sentinel.monitoring.internal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile({"dev", "test"})
class FixedTenantContext implements TenantContext {
    private static final UUID FIXED_TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Override
    public UUID currentTenantId() {
        // Development/test stand-in only
        return FIXED_TENANT_ID;
    }
}