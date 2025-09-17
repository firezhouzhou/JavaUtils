package com.example.common.controller;

import com.example.common.web.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Security测试控制器
 * 用于测试统一的Spring Security配置
 */
@RestController
@RequestMapping("/common/security")
@Api(tags = "Security测试接口")
public class SecurityTestController {

    @GetMapping("/public")
    @ApiOperation("公开接口测试")
    public ApiResponse<String> publicEndpoint() {
        return ApiResponse.success("这是一个公开接口，无需认证");
    }

    @GetMapping("/authenticated")
    @ApiOperation("需要认证的接口测试")
    public ApiResponse<Map<String, Object>> authenticatedEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "认证成功");
        result.put("username", auth.getName());
        result.put("authorities", auth.getAuthorities());
        return ApiResponse.success(result);
    }

    @GetMapping("/admin")
    @ApiOperation("管理员权限接口测试")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminEndpoint() {
        return ApiResponse.success("管理员接口访问成功");
    }

    @GetMapping("/user")
    @ApiOperation("用户权限接口测试")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<String> userEndpoint() {
        return ApiResponse.success("用户接口访问成功");
    }

    @GetMapping("/developer")
    @ApiOperation("开发者权限接口测试")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ApiResponse<String> developerEndpoint() {
        return ApiResponse.success("开发者接口访问成功");
    }
}