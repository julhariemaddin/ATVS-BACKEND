package com.example.acquitance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public, unauthenticated health check. Used by Docker's HEALTHCHECK, load
 * balancers, and uptime monitors — kept intentionally lightweight (no auth,
 * no heavy queries) so it can be polled frequently.
 */
@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> database = new LinkedHashMap<>();
        boolean dbUp;
        try (Connection connection = dataSource.getConnection()) {
            dbUp = connection.isValid(2);
            database.put("status", dbUp ? "UP" : "DOWN");
        } catch (Exception e) {
            dbUp = false;
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", dbUp ? "UP" : "DEGRADED");
        body.put("timestamp", Instant.now().toString());
        body.put("database", database);

        HttpStatus statusCode = dbUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(statusCode).body(body);
    }
}
