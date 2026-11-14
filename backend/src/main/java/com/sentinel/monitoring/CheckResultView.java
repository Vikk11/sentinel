package com.sentinel.monitoring;

import java.time.Instant;
import java.util.UUID;

public record CheckResultView(
        UUID id,
        String status,
        Integer statusCode,
        long latencyMs,
        String errorType,
        Instant checkedAt
) {
}