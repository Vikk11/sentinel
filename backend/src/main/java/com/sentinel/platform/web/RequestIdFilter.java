package com.sentinel.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(RequestIdFilter.class);

    private static final String REQUEST_ID = "requestId";
    private static final String HEADER_NAME = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        long startTime = System.nanoTime();

        MDC.put(REQUEST_ID, requestId);
        response.setHeader(HEADER_NAME, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                long durationMs =
                        (System.nanoTime() - startTime) / 1_000_000;

                String method = request.getMethod();
                String path = request.getRequestURI();
                int status = response.getStatus();

                if (path.equals("/actuator") || path.startsWith("/actuator/")) {
                    log.debug(
                            "HTTP {} {} {} {}ms",
                            method, path, status, durationMs
                    );
                } else {
                    log.info(
                            "HTTP {} {} {} {}ms",
                            method, path, status, durationMs
                    );
                }
            } finally {
                MDC.remove(REQUEST_ID);
            }
        }
    }
}