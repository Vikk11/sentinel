package com.sentinel.monitoring.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceUrlNormalizerTest {

    @Test
    void lowercasesSchemeAndHost() {
        assertEquals(
                "https://example.com/",
                ServiceUrlNormalizer.normalize("HTTPS://Example.COM")
        );
    }

    @Test
    void normalizesEmptyPathAndTrailingSlash() {
        assertEquals(
                ServiceUrlNormalizer.normalize("https://example.com"),
                ServiceUrlNormalizer.normalize("https://example.com/")
        );
    }

    @Test
    void removesDefaultHttpsPort() {
        assertEquals(
                "https://example.com/",
                ServiceUrlNormalizer.normalize("https://example.com:443/")
        );
    }

    @Test
    void removesDefaultHttpPort() {
        assertEquals(
                "http://example.com/",
                ServiceUrlNormalizer.normalize("http://example.com:80/")
        );
    }

    @Test
    void preservesNonDefaultPort() {
        assertEquals(
                "https://example.com:8443/",
                ServiceUrlNormalizer.normalize("HTTPS://Example.COM:8443")
        );
    }

    @Test
    void preservesPathCase() {
        assertEquals(
                "https://example.com/API",
                ServiceUrlNormalizer.normalize("HTTPS://EXAMPLE.COM/API")
        );
    }

    @Test
    void dropsFragment() {
        assertEquals(
                "https://example.com/api",
                ServiceUrlNormalizer.normalize("https://example.com/api#one")
        );

        assertEquals(
                ServiceUrlNormalizer.normalize("https://example.com/api#one"),
                ServiceUrlNormalizer.normalize("https://example.com/api#two")
        );
    }

    @Test
    void preservesQuery() {
        assertEquals(
                "https://example.com/api?key=value",
                ServiceUrlNormalizer.normalize(
                        "HTTPS://Example.COM/api?key=value"
                )
        );
    }

    @Test
    void normalizesIpv6() {
        assertEquals(
                "http://[::1]/",
                ServiceUrlNormalizer.normalize("http://[::1]/")
        );
    }
}