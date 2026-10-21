package com.chronode.healthcheck;

import java.time.Instant;

public record HealthCheckResponse(
    String status,
    String application,
    String version,
    String environment,
    Instant timestamp
) {
}
