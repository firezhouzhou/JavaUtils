package com.example.gateway.controller;

import com.example.common.util.IdUtils;
import com.example.common.util.ServerConfigUtils;
import com.example.common.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关测试控制器
 * 用于测试gateway-module对common-module的集成
 */
@RestController
@RequestMapping("/gateway/test")
public class GatewayTestController {
    
    /**
     * 测试雪花算法ID生成
     */
    @GetMapping("/id")
    public Mono<ApiResponse<Map<String, Object>>> testIdGeneration() {
        Map<String, Object> result = new HashMap<>();
        
        // 使用common-module的IdUtils
        Long id = IdUtils.generateId();
        String idStr = IdUtils.generateIdStr();
        
        result.put("id", id);
        result.put("idStr", idStr);
        result.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ApiResponse.success(result));
    }
    
    /**
     * 测试服务配置
     */
    @GetMapping("/config")
    public Mono<ApiResponse<Map<String, Object>>> testServerConfig() {
        Map<String, Object> result = new HashMap<>();
        
        // 使用common-module的ServerConfigUtils
        result.put("authServiceUrl", ServerConfigUtils.getAuthServiceUrl());
        result.put("userServiceUrl", ServerConfigUtils.getUserServiceUrl());
        result.put("fileServiceUrl", ServerConfigUtils.getFileServiceUrl());
        result.put("adminServiceUrl", ServerConfigUtils.getAdminServiceUrl());
        result.put("logServiceUrl", ServerConfigUtils.getLogServiceUrl());
        result.put("gatewayUrl", ServerConfigUtils.getGatewayUrl());
        
        // 数据库配置
        result.put("databaseUrl", ServerConfigUtils.getDatabaseUrl("test_db"));
        result.put("redisHost", ServerConfigUtils.getRedisHost());
        result.put("redisPort", ServerConfigUtils.getRedisPort());
        
        return Mono.just(ApiResponse.success(result));
    }
    
    /**
     * 测试网关健康状态
     */
    @GetMapping("/health")
    public Mono<ApiResponse<Map<String, Object>>> testHealth() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("status", "healthy");
        result.put("service", "gateway-module");
        result.put("timestamp", System.currentTimeMillis());
        result.put("message", "Gateway module successfully integrated with common-module");
        
        // 生成一个示例ID来验证集成
        result.put("sampleId", IdUtils.generateId());
        
        return Mono.just(ApiResponse.success(result));
    }
}
