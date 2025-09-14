package com.example.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流过滤器
 */
@Component
public class RateLimitFilter implements GatewayFilter {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 限流配置
    private static final int DEFAULT_LIMIT = 100; // 默认每分钟100次请求
    private static final int WINDOW_SIZE = 60; // 时间窗口60秒
    
    // Lua脚本实现滑动窗口限流
    private static final String RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local window = tonumber(ARGV[1])\n" +
        "local limit = tonumber(ARGV[2])\n" +
        "local current = tonumber(ARGV[3])\n" +
        "\n" +
        "redis.call('zremrangebyscore', key, '-inf', current - window)\n" +
        "local count = redis.call('zcard', key)\n" +
        "if count < limit then\n" +
        "    redis.call('zadd', key, current, current)\n" +
        "    redis.call('expire', key, window)\n" +
        "    return {1, limit - count - 1}\n" +
        "else\n" +
        "    return {0, 0}\n" +
        "end";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 获取客户端标识（IP + 用户ID）
        String clientId = getClientId(request);
        String rateLimitKey = "rate_limit:" + clientId;
        
        // 执行限流检查
        if (!checkRateLimit(rateLimitKey)) {
            return handleRateLimitExceeded(exchange);
        }
        
        return chain.filter(exchange);
    }
    
    /**
     * 检查限流
     */
    private boolean checkRateLimit(String key) {
        try {
            DefaultRedisScript<Long[]> script = new DefaultRedisScript<>();
            script.setScriptText(RATE_LIMIT_SCRIPT);
            script.setResultType(Long[].class);
            
            long currentTime = System.currentTimeMillis() / 1000;
            Long[] result = (Long[]) redisTemplate.execute(
                script,
                Collections.singletonList(key),
                WINDOW_SIZE,
                DEFAULT_LIMIT,
                currentTime
            );
            
            return result != null && result[0] == 1;
        } catch (Exception e) {
            // 限流检查失败时，允许请求通过（降级策略）
            return true;
        }
    }
    
    /**
     * 获取客户端标识
     */
    private String getClientId(ServerHttpRequest request) {
        // 优先使用用户ID（如果已认证）
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }
        
        // 使用IP地址
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    /**
     * 处理限流超出
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("message", "请求过于频繁，请稍后再试");
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