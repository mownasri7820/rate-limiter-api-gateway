package com.guvi.gateway.logging;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "api_logs")
public class ApiLog {
    @Id private String id;
    private Instant timestamp;
    private String username;
    private String clientIp;
    private String method;
    private String endpoint;
    private int responseStatus;
    private long durationMs;
    private boolean rateLimitViolation;

    public ApiLog() {}
    public ApiLog(Instant timestamp, String username, String clientIp, String method, String endpoint,
                  int responseStatus, long durationMs, boolean rateLimitViolation) {
        this.timestamp = timestamp; this.username = username; this.clientIp = clientIp;
        this.method = method; this.endpoint = endpoint; this.responseStatus = responseStatus;
        this.durationMs = durationMs; this.rateLimitViolation = rateLimitViolation;
    }
    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getClientIp() { return clientIp; }
    public String getMethod() { return method; }
    public String getEndpoint() { return endpoint; }
    public int getResponseStatus() { return responseStatus; }
    public long getDurationMs() { return durationMs; }
    public boolean isRateLimitViolation() { return rateLimitViolation; }
}
