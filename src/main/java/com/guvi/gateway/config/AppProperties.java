package com.guvi.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Demo demo, RateLimit rateLimit, Routing routing) {
    public record Jwt(String secret, long expirationMinutes) {}
    public record Demo(String username, String password) {}
    public record RateLimit(int capacity, int refillTokens, long refillSeconds) {}
    public record Routing(String baseUrl) {}
}
