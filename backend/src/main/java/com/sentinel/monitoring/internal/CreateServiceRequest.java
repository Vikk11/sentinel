package com.sentinel.monitoring.internal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record CreateServiceRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 2048)
        String url,

        @NotNull
        @Min(30)
        @Max(3600)
        Integer checkIntervalSeconds,

        Boolean enabled
) {
    CreateServiceRequest {
        if (enabled == null) {
            enabled = true;
        }
    }
}