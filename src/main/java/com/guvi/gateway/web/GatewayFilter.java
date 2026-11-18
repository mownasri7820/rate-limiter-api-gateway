
package com.guvi.gateway.web;

import com.guvi.gateway.auth.JwtService;
import com.guvi.gateway.ratelimit.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class GatewayFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RateLimiterService rateLimiter;

    public GatewayFilter(JwtService jwtService, RateLimiterService rateLimiter) {
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/gateway/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            writeError(response, 401, "Missing or invalid Bearer token");
            return;
        }

        final String username;

        try {
            username = jwtService.validateAndGetSubject(header.substring(7));
        } catch (Exception ex) {
            writeError(response, 401, "Invalid or expired token");
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String key = "user:" + username;

        if (!rateLimiter.allow(key)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(
                    "Retry-After",
                    String.valueOf(rateLimiter.retryAfterSeconds(key))
            );
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}"
            );
            return;
        }

        request.setAttribute("authenticatedUser", username);
        chain.doFilter(request, response);
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":" + status +
                ",\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
        );
    }
}
