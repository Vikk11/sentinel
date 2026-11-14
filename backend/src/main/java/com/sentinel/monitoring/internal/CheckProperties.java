package com.sentinel.monitoring.internal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.AssertTrue;

import java.time.Duration;

@ConfigurationProperties(prefix = "sentinel.checks")
@Validated
public record CheckProperties(

        @NotNull
        Duration timeout,

        @Min(0)
        @Max(10)
        int maxRedirects,

        @NotBlank
        String userAgent

) {
    @AssertTrue(message = "timeout must be between 1 and 30 seconds")
    public boolean isTimeoutWithinRange() {
        return timeout == null ||
                (timeout.compareTo(Duration.ofSeconds(1)) >= 0
                        && timeout.compareTo(Duration.ofSeconds(30)) <= 0);
    }
}