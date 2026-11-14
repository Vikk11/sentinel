package com.sentinel.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @AfterEach
    void cleanUp() {
        MDC.remove("requestId");
    }

    @Test
    void shouldGenerateRequestIdAndAddItToHeaderAndMdc()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/actuator/health");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AtomicReference<String> requestIdInsideChain =
                new AtomicReference<>();

        FilterChain customChain = (req, res) -> {
            requestIdInsideChain.set(MDC.get("requestId"));
        };

        filter.doFilter(request, response, customChain);

        String responseId = response.getHeader("X-Request-Id");

        assertNotNull(responseId);
        assertDoesNotThrow(() -> UUID.fromString(responseId));

        assertEquals(responseId, requestIdInsideChain.get());
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldClearMdcEvenWhenFilterChainThrowsException() {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain throwingChain = (req, res) -> {
            assertNotNull(MDC.get("requestId"));
            throw new ServletException("Simulated failure");
        };

        assertThrows(
                ServletException.class,
                () -> filter.doFilter(request, response, throwingChain)
        );

        assertNull(MDC.get("requestId"));
    }
}