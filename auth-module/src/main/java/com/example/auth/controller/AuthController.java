package com.example.auth.controller;

import com.example.auth.entity.AuthUserDetails;
import com.example.auth.entity.User;
import com.example.auth.service.AuthService;
import com.example.auth.service.CustomUserDetailsService;
import com.example.common.web.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                                  HttpServletRequest httpRequest) {
        System.out.println("=== LOGIN REQUEST START ===");
        System.out.println("Login request received - Username: " + request.getUsername());
        System.out.println("Login request headers: " + java.util.Collections.list(httpRequest.getHeaderNames()));
        
        try {
            String clientIp = getClientIp(httpRequest);
            System.out.println("Client IP: " + clientIp);
            
            Map<String, Object> result = authService.login(request.getUsername(), request.getPassword(), clientIp);
            System.out.println("Login successful for user: " + request.getUsername());
            System.out.println("=== LOGIN REQUEST SUCCESS ===");
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            System.err.println("Login failed for user: " + request.getUsername());
            System.err.println("Login error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== LOGIN REQUEST FAILED ===");
            throw e;
        }
    }
    
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.register(request.getUsername(), request.getPassword(), request.getEmail());
        return ApiResponse.success("注册成功", result);
    }
    
    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                    @RequestHeader(value = "Bearer", required = false) String bearerHeader,
                                                   @RequestBody(required = false) RefreshTokenRequest request) {
        String token = null;
        if (bearerHeader != null && !bearerHeader.trim().isEmpty()) {
            // 优先使用Bearer头中的token（Swagger当前发送的）
            token = bearerHeader;
            System.out.println("Using token from Bearer header");
        } else if (authHeader != null && !authHeader.trim().isEmpty()) {
            // 如果Bearer头没有，使用Authorization头中的Bearer token
            token = authHeader;
        } else if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().trim().isEmpty()) {
            // 如果header中没有，从请求体获取 (备用方式)
            token = request.getRefreshToken();
        }
        
        if (token == null || token.trim().isEmpty()) {
            return ApiResponse.error(400, "请在Authorization头中提供Bearer token或在请求体中提供refreshToken");
        }
        
        try {
            Map<String, Object> result = authService.refreshToken(token);
            return ApiResponse.success("刷新成功", result);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                     @RequestHeader(value = "Bearer", required = false) String bearerHeader,
                                     HttpServletRequest httpRequest) {
        System.out.println("=== LOGOUT REQUEST START ===");
        System.out.println("Logout request received");
        System.out.println("Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 50)) + "..." : "null"));
        System.out.println("Bearer header: " + (bearerHeader != null ? bearerHeader.substring(0, Math.min(bearerHeader.length(), 50)) + "..." : "null"));
        
        // 确定使用哪个token
        String token = null;
        if (bearerHeader != null && !bearerHeader.trim().isEmpty()) {
            // 优先使用Bearer头中的token（Swagger当前发送的）
            token = bearerHeader;
            System.out.println("Using token from Bearer header");
        } else if (authHeader != null && !authHeader.trim().isEmpty() && authHeader.startsWith("Bearer ")) {
            // 如果Bearer头没有，使用Authorization头中的Bearer token
            token = authHeader;
            System.out.println("Using token from Authorization header");
        } else {
            System.err.println("No valid token found in either Bearer or Authorization headers");
            return ApiResponse.error(400, "请提供有效的Bearer token");
        }
        
        try {
            System.out.println("Calling authService.logout with token...");
            authService.logout(token);
            System.out.println("Logout successful");
            System.out.println("=== LOGOUT REQUEST SUCCESS ===");
            return ApiResponse.success("退出成功");
        } catch (Exception e) {
            System.err.println("Logout failed");
            System.err.println("Logout error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== LOGOUT REQUEST FAILED ===");
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @ApiOperation("查看所有用户（调试用）")
    @GetMapping("/users")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userDetailsService.getAllUsers();
        return ApiResponse.success("获取用户列表成功", users);
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        return request.getRemoteAddr();
    }
    
    // 内部类定义请求对象
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}