package com.sentinel.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
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
            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            log.info(
                    "HTTP {} {} {} {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );

            MDC.remove(REQUEST_ID);
        }
    }
}