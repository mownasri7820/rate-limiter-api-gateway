package com.guvi.gateway.ratelimit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {
    @Test void allowsOnlyCapacityBeforeRefill() {
        TokenBucket bucket = new TokenBucket(2, 1, 60);
        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }
}
