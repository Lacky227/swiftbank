package com.swiftbank.apigateway.config;

import com.swiftbank.apigateway.filters.AccessFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class RouteConfig {
    private final AccessFilter accessFilter;
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-protected", r -> r
                        .path("/auth/me")
                        .filters(f -> f.filter(accessFilter))
                        .uri("lb://auth-service"))
                .route("auth-service-open", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))
                .route("account-service", r -> r
                        .path("/account/user")
                        .filters(f -> f.filter(accessFilter))
                        .uri("lb://account-service"))
                .build();
    }
}
