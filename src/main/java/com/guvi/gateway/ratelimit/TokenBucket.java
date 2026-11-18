package com.guvi.gateway.ratelimit;

public class TokenBucket {
    private final int capacity;
    private final int refillTokens;
    private final long refillNanos;
    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(int capacity, int refillTokens, long refillSeconds) {
        if (capacity < 1 || refillTokens < 1 || refillSeconds < 1) {
            throw new IllegalArgumentException("Rate-limit settings must be positive");
        }
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillNanos = refillSeconds * 1_000_000_000L;
        this.tokens = capacity;
        this.lastRefillNanos = System.nanoTime();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized long secondsUntilNextToken() {
        refill();
        if (tokens >= 1) return 0;
        double nanosPerToken = (double) refillNanos / refillTokens;
        return Math.max(1, (long) Math.ceil((1 - tokens) * nanosPerToken / 1_000_000_000d));
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsed = Math.max(0, now - lastRefillNanos);
        double added = ((double) elapsed / refillNanos) * refillTokens;
        if (added > 0) {
            tokens = Math.min(capacity, tokens + added);
            lastRefillNanos = now;
        }
    }
}
