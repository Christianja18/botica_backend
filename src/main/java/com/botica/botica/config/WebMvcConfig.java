package com.botica.botica.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;
    private final AuthAuthorizationInterceptor authAuthorizationInterceptor;
    private final SecurityHeadersInterceptor securityHeadersInterceptor;

    @Value("${botica.security.cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:8080,http://127.0.0.1:3000,http://127.0.0.1:4200}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")
                .exposedHeaders("Authorization", "Content-Type", "X-Total-Count", "X-Total-Pages")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityHeadersInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                );

        registry.addInterceptor(authAuthorizationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                );
    }
}
