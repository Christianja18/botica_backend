package com.botica.botica.config;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class RateLimitingConfig {

    /**
     * Configura el registro de rate limiters con configuración por defecto
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(defaultConfig);

        rateLimiterRegistry.getEventPublisher()
                .onEntryAdded(event -> log.info("RateLimiter agregado: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> log.info("RateLimiter removido: {}", event.getRemovedEntry().getName()));

        return rateLimiterRegistry;
    }

    /**
     * Rate limiter para el endpoint de login (más restrictivo)
     */
    @Bean
    public RateLimiter loginRateLimiter(RateLimiterRegistry registry) {
        RateLimiterConfig loginConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(5))
                .limitForPeriod(5)
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        return registry.rateLimiter("login-limiter", loginConfig);
    }

    /**
     * Rate limiter para API general (menos restrictivo)
     */
    @Bean
    public RateLimiter apiRateLimiter(RateLimiterRegistry registry) {
        RateLimiterConfig apiConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100)
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        return registry.rateLimiter("api-limiter", apiConfig);
    }

    /**
     * Rate limiter para operaciones sensitivas (muy restrictivo)
     */
    @Bean
    public RateLimiter sensitiveRateLimiter(RateLimiterRegistry registry) {
        RateLimiterConfig sensitiveConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofHours(1))
                .limitForPeriod(10)
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        return registry.rateLimiter("sensitive-limiter", sensitiveConfig);
    }
}