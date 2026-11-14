package com.sentinel.monitoring;

import com.sentinel.monitoring.internal.CheckProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CheckPropertiesTest {

    @Autowired
    private CheckProperties checkProperties;

    @Test
    void shouldBindCheckPropertiesCorrectly() {

        assertEquals(
                Duration.ofSeconds(5),
                checkProperties.timeout()
        );

        assertEquals(
                3,
                checkProperties.maxRedirects()
        );

        assertEquals(
                "Sentinel-Monitor/1.0",
                checkProperties.userAgent()
        );
    }
}