package com.chronode.healthcheck;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HealthCheckServiceImpl implements HealthCheckService {

    @Override
    public HealthCheckResponse checkHealth() {
        return new HealthCheckResponse(
            "UP",
            "Chronode Backend",
            "1.0.0",
            "development",
            Instant.now()
        );
    }
}
