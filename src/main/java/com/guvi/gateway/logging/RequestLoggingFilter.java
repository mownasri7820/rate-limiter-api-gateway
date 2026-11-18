package com.guvi.gateway.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final ApiLogRepository repository;

    public RequestLoggingFilter(ApiLogRepository repository) { this.repository = repository; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String uri = request.getRequestURI();
            String username = (String) request.getAttribute("authenticatedUser");
            int status = response.getStatus();
            if (uri.startsWith("/gateway/")) {
                try {
                    repository.save(new ApiLog(Instant.now(), username, request.getRemoteAddr(),
                            request.getMethod(), uri, status, duration, status == 429));
                } catch (Exception ex) {
                    log.warn("Could not persist API log to MongoDB: {}", ex.getMessage());
                }
            }
            log.info("{} {} -> {} ({} ms)", request.getMethod(), uri, status, duration);
        }
    }
}
