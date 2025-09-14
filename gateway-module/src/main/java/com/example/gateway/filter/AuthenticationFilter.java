package com.example.gateway.filter;

import com.example.common.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器
 */
@Component
public class AuthenticationFilter implements GatewayFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 不需要认证的路径
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/auth/refresh",
        "/user/check-username",
        "/user/check-email",
        "/swagger-ui",
        "/v2/api-docs",
        "/swagger-resources",
        "/webjars"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查是否需要认证
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }
        
        // 获取token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return handleUnauthorized(exchange, "缺少认证token");
        }
        
        try {
            // 验证token
            if (!validateToken(token)) {
                return handleUnauthorized(exchange, "token无效或已过期");
            }
            
            // 获取用户信息并添加到请求头
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            return handleUnauthorized(exchange, "认证失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查路径是否需要排除认证
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::contains);
    }
    
    /**
     * 从请求中提取token
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 验证token
     */
    private boolean validateToken(String token) {
        try {
            // 检查token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return false;
            }
            
            // 验证token格式和过期时间
            String username = jwtUtil.getUsernameFromToken(token);
            return username != null && !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String key = "token_blacklist:" + token;
        try {
            // 使用响应式Redis操作，这里简化处理，实际项目中应该使用响应式方式
            return Boolean.TRUE.equals(reactiveRedisTemplate.hasKey(key).block());
        } catch (Exception e) {
            // Redis连接失败时，默认允许通过（可根据业务需求调整）
            return false;
        }
    }
    
    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("timestamp", System.currentTimeMillis());
        
        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
}