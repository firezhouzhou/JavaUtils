package com.example.gateway.config;

import com.example.gateway.filter.AuthenticationFilter;
import com.example.gateway.filter.LoggingFilter;
import com.example.gateway.filter.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 网关配置
 */
@Configuration
public class GatewayConfig {
    
    @Autowired
    private AuthenticationFilter authenticationFilter;
    
    @Autowired
    private LoggingFilter loggingFilter;
    
    @Autowired
    private RateLimitFilter rateLimitFilter;
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // 认证服务路由
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(loggingFilter)
                    .filter(rateLimitFilter))
                .uri("http://localhost:8081"))
            
            // 用户服务路由
            .route("user-service", r -> r
                .path("/api/user/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(authenticationFilter)
                    .filter(loggingFilter)
                    .filter(rateLimitFilter))
                .uri("http://localhost:8082"))
            
            // 文件服务路由
            .route("file-service", r -> r
                .path("/api/file/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(authenticationFilter)
                    .filter(loggingFilter)
                    .filter(rateLimitFilter))
                .uri("http://localhost:8083"))
            
            // 管理服务路由
            .route("admin-service", r -> r
                .path("/api/admin/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(authenticationFilter)
                    .filter(loggingFilter)
                    .filter(rateLimitFilter))
                .uri("http://localhost:8084"))
            
            .build();
    }
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}