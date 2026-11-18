package com.guvi.gateway.ratelimit;

import com.guvi.gateway.config.AppProperties;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final AppProperties.RateLimit config;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiterService(AppProperties properties) { this.config = properties.rateLimit(); }

    public boolean allow(String key) {
        return buckets.computeIfAbsent(key, ignored ->
                new TokenBucket(config.capacity(), config.refillTokens(), config.refillSeconds())).tryConsume();
    }

    public long retryAfterSeconds(String key) {
        TokenBucket bucket = buckets.get(key);
        return bucket == null ? 1 : bucket.secondsUntilNextToken();
    }
}
