package com.guvi.gateway.monitoring;

import com.guvi.gateway.logging.ApiLog;
import com.guvi.gateway.logging.ApiLogRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {
    private final ApiLogRepository repository;
    public MonitoringController(ApiLogRepository repository) { this.repository = repository; }

    @GetMapping("/logs")
    public List<ApiLog> recentLogs() { return repository.findTop100ByOrderByTimestampDesc(); }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of("totalStoredLogs", repository.count(),
                "unauthorizedResponses", repository.countByResponseStatus(401),
                "rateLimitViolations", repository.countByResponseStatus(429));
    }
}
