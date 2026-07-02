package com.example.demo.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // Guarda o balde de cada usuário na memória
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String identifier, String role) {
        if ("ROLE_PREMIUM".equals(role)) {
            return Bucket.builder()
                    .addLimit(Bandwidth.builder().capacity(10000).refillIntervally(10000, Duration.ofHours(1)).build())
                    .build();
        }
        if ("ROLE_USER".equals(role)) {
            return cache.computeIfAbsent(identifier, k -> {

                return Bucket.builder()
                        .addLimit(Bandwidth.builder().capacity(3).refillIntervally(1, Duration.ofHours(8)).build())
                        .build();
            });
        }
        // visitante
        return cache.computeIfAbsent(identifier, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(1).refillIntervally(1, Duration.ofDays(1)).build())
                .build());
    }
}