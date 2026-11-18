package com.guvi.gateway.logging;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ApiLogRepository extends MongoRepository<ApiLog, String> {
    List<ApiLog> findTop100ByOrderByTimestampDesc();
    long countByResponseStatus(int responseStatus);
}
