package com.botica.botica.config;

import com.botica.botica.exception.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        RateLimiter rateLimiter = selectRateLimiter(requestPath, method);
        if (rateLimiter == null) {
            return true;
        }

        try {
            rateLimiter.executeRunnable(() -> log.debug("Request permitido: {} {}", method, requestPath));
            return true;
        } catch (Exception e) {
            log.warn("Rate limit excedido para: {} {} - {}", method, requestPath, e.getMessage());
            throw new RateLimitExceededException("Demasiadas solicitudes. Por favor, intente mas tarde.");
        }
    }

    private RateLimiter selectRateLimiter(String path, String method) {
        if ("/api/auth/login".equals(path)) {
            return rateLimiterRegistry.rateLimiter("login-limiter");
        }
        if ("DELETE".equalsIgnoreCase(method) || path.contains("/admin")) {
            return rateLimiterRegistry.rateLimiter("sensitive-limiter");
        }
        if (path.startsWith("/api/")) {
            return rateLimiterRegistry.rateLimiter("api-limiter");
        }
        return null;
    }
}
