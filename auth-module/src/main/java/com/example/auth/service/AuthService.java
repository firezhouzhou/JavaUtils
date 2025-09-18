package com.example.auth.service;

import com.example.auth.entity.AuthUserDetails;
import com.example.common.util.JwtUtil;
import com.example.common.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password, String clientIp) {
        System.out.println("=== AuthService.login START ===");
        System.out.println("Login attempt - Username: " + username + ", IP: " + clientIp);
        
        try {
            // 检查登录尝试次数
            System.out.println("Checking login attempts for user: " + username);
            checkLoginAttempts(username, clientIp);
            System.out.println("Login attempts check passed");

            // 认证用户
            System.out.println("Authenticating user with AuthenticationManager...");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            System.out.println("Authentication successful");

            // 获取用户详情
            AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            System.out.println("User details retrieved - UserId: " + userId + ", Username: " + userDetails.getUsername());

            // 生成JWT token
            System.out.println("Generating JWT tokens...");
            String accessToken = jwtUtil.generateToken(username, userId);
            String refreshToken = jwtUtil.generateRefreshToken(username, userId);
            System.out.println("JWT tokens generated successfully");
            System.out.println("Access token length: " + accessToken.length());
            System.out.println("Refresh token length: " + refreshToken.length());

            // 缓存登录状态
            System.out.println("Caching login status and refresh token...");
            cacheLoginStatus(accessToken, userId, 86400); // 24小时
            cacheRefreshToken(refreshToken, userId, 604800); // 7天
            System.out.println("Login status cached successfully");

            // 清除登录失败记录
            clearLoginAttempts(username, clientIp);

            // 记录登录日志
            recordLoginLog(userId, username, clientIp, true, "登录成功");

            // 更新用户登录信息
            System.out.println("Updating user login info...");
            userDetailsService.updateLoginInfo(username);
            System.out.println("User login info updated");

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 86400);
            result.put("refreshExpiresIn", 604800);
            result.put("userId", userId);
            result.put("username", username);
            result.put("authorities", userDetails.getAuthorities());

            System.out.println("Login result prepared successfully");
            System.out.println("=== AuthService.login SUCCESS ===");
            return result;

        } catch (AuthenticationException e) {
            System.err.println("Authentication failed for user: " + username);
            System.err.println("Authentication error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            
            // 记录登录失败
            recordLoginAttempt(username, clientIp);
            recordLoginLog(null, username, clientIp, false, e.getMessage());
            
            System.out.println("=== AuthService.login FAILED ===");
            throw new BadCredentialsException("用户名或密码错误");
        } catch (Exception e) {
            System.err.println("Unexpected error during login for user: " + username);
            System.err.println("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== AuthService.login ERROR ===");
            throw e;
        }
    }
    
    /**
     * 刷新Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            // 输入验证
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new RuntimeException("Refresh token不能为空");
            }
            
            // 去掉Bearer前缀（如果存在）
            if (refreshToken.startsWith("Bearer ")) {
                refreshToken = refreshToken.substring(7);
            }
            
            // 再次检查token格式
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new RuntimeException("Refresh token格式无效");
            }
            
            // 检查JWT格式（应该包含两个点）
            String[] tokenParts = refreshToken.split("\\.");
            if (tokenParts.length != 3) {
                throw new RuntimeException("Refresh token格式无效，不是有效的JWT。当前格式: " + tokenParts.length + " 部分，应为3部分");
            }
            
            // 检查token是否在黑名单中
            if (isTokenBlacklisted(refreshToken)) {
                throw new RuntimeException("Token已失效");
            }
            
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            if (username != null && !jwtUtil.isTokenExpired(refreshToken)) {
                // 验证refresh token缓存
                Long cachedUserId = getCachedRefreshToken(refreshToken);
                if (cachedUserId == null || !cachedUserId.equals(userId)) {
                    throw new RuntimeException("Refresh token无效");
                }
                
                // 生成新的access token
                String newAccessToken = jwtUtil.generateToken(username, userId);
                String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);
                
                // 将旧的refresh token加入黑名单
                addTokenToBlacklist(refreshToken);
                
                // 缓存新的token
                cacheLoginStatus(newAccessToken, userId, 86400);
                cacheRefreshToken(newRefreshToken, userId, 604800);
                
                Map<String, Object> result = new HashMap<>();
                result.put("accessToken", newAccessToken);
                result.put("refreshToken", newRefreshToken);
                result.put("tokenType", "Bearer");
                result.put("expiresIn", 86400);
                result.put("refreshExpiresIn", 604800);
                
                return result;
            }
            
            throw new RuntimeException("Refresh token无效或已过期");
            
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new RuntimeException("Refresh token格式错误: " + e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("Refresh token已过期");
        } catch (io.jsonwebtoken.SignatureException e) {
            throw new RuntimeException("Refresh token签名无效");
        } catch (Exception e) {
            throw new RuntimeException("刷新token失败: " + e.getMessage());
        }
    }
    
    /**
     * 退出登录
     */
    public void logout(String token) {
        System.out.println("=== AuthService.logout START ===");
        System.out.println("Logout token received: " + (token != null ? token.substring(0, Math.min(token.length(), 50)) + "..." : "null"));
        
        try {
            // 处理Bearer前缀
            String originalToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                System.out.println("Removed Bearer prefix, token length now: " + token.length());
            } else {
                System.out.println("No Bearer prefix found, token length: " + (token != null ? token.length() : 0));
            }
            
            // 验证token格式
            if (token == null || token.trim().isEmpty()) {
                System.err.println("Token is null or empty after processing");
                throw new RuntimeException("Token is null or empty");
            }
            
            // 检查JWT格式
            String[] tokenParts = token.split("\\.");
            System.out.println("Token parts count: " + tokenParts.length);
            if (tokenParts.length != 3) {
                System.err.println("Invalid JWT format - expected 3 parts, got " + tokenParts.length);
                System.err.println("Token: " + token);
                throw new RuntimeException("Invalid JWT format");
            }
            
            System.out.println("Token format validation passed");
            
            // 将token加入黑名单
            System.out.println("Adding token to blacklist...");
            addTokenToBlacklist(token);
            System.out.println("Token added to blacklist successfully");
            
            // 清除登录状态缓存
            System.out.println("Clearing login status cache...");
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                System.out.println("Retrieved userId from token: " + userId);
                if (userId != null) {
                    clearLoginStatus(token);
                    System.out.println("Login status cleared successfully");
                } else {
                    System.out.println("UserId is null, skipping login status clear");
                }
            } catch (Exception e) {
                System.err.println("Failed to get userId from token during logout: " + e.getMessage());
                e.printStackTrace();
                // 继续执行，不影响logout流程
            }
            
            System.out.println("=== AuthService.logout SUCCESS ===");
            
        } catch (Exception e) {
            System.err.println("Logout failed");
            System.err.println("Logout error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== AuthService.logout FAILED ===");
            throw e;
        }
    }
    
    /**
    /**
     * 验证token有效性
     */
    public boolean validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                return false;
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            return username != null && !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 用户注册
     */
    public Map<String, Object> register(String username, String password, String email) {
        try {
            // 调用用户详情服务进行注册
            AuthUserDetails newUser = userDetailsService.registerUser(username, password, email);
            
            // 记录注册日志
            recordRegistrationLog(newUser.getUserId(), username, email, true, "注册成功");
            
            // 返回注册结果
            Map<String, Object> result = new HashMap<>();
            result.put("userId", newUser.getUserId());
            result.put("username", newUser.getUsername());
            result.put("message", "注册成功");
            result.put("timestamp", System.currentTimeMillis());
            
            return result;
            
        } catch (RuntimeException e) {
            // 记录注册失败日志
            recordRegistrationLog(null, username, email, false, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 检查登录尝试次数
     */
    private void checkLoginAttempts(String username, String clientIp) {
        String key = LOGIN_ATTEMPTS_PREFIX + username + ":" + clientIp;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        
        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            throw new RuntimeException("登录失败次数过多，账户已被锁定" + LOCKOUT_DURATION_MINUTES + "分钟");
        }
    }
    
    /**
     * 记录登录尝试
     */
    private void recordLoginAttempt(String username, String clientIp) {
        String key = LOGIN_ATTEMPTS_PREFIX + username + ":" + clientIp;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        attempts = attempts == null ? 1 : attempts + 1;
        
        redisTemplate.opsForValue().set(key, attempts, LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 清除登录失败记录
     */
    private void clearLoginAttempts(String username, String clientIp) {
        String key = LOGIN_ATTEMPTS_PREFIX + username + ":" + clientIp;
        redisTemplate.delete(key);
    }
    
    /**
     * 缓存登录状态
     */
    private void cacheLoginStatus(String token, Long userId, int expireSeconds) {
        String cacheKey = "login:" + token;
        redisTemplate.opsForValue().set(cacheKey, userId, expireSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 缓存refresh token
     */
    private void cacheRefreshToken(String refreshToken, Long userId, int expireSeconds) {
        String cacheKey = "refresh:" + refreshToken;
        redisTemplate.opsForValue().set(cacheKey, userId, expireSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * 获取缓存的refresh token
     */
    private Long getCachedRefreshToken(String refreshToken) {
        String cacheKey = "refresh:" + refreshToken;
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue == null) {
            return null;
        }
        
        // 处理不同的数值类型
        if (cachedValue instanceof Long) {
            return (Long) cachedValue;
        } else if (cachedValue instanceof Integer) {
            return ((Integer) cachedValue).longValue();
        } else if (cachedValue instanceof Number) {
            return ((Number) cachedValue).longValue();
        } else {
            // 尝试字符串转换
            try {
                return Long.parseLong(cachedValue.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
    
    /**
     * 清除登录状态
     */
    private void clearLoginStatus(String token) {
        redisTemplate.delete("login:" + token);
    }
    
    /**
     * 将token加入黑名单
     */
    private void addTokenToBlacklist(String token) {
        try {
            // 验证token格式
            if (token == null || token.trim().isEmpty()) {
                return;
            }
            
            // 检查JWT格式（应该包含两个点）
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                System.err.println("Invalid JWT format for blacklist: " + token.length() + " characters, " + tokenParts.length + " parts");
                return;
            }
            
            String key = TOKEN_BLACKLIST_PREFIX + token;
            long expiration = jwtUtil.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
            if (expiration > 0) {
                redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            System.err.println("Failed to add token to blacklist: " + e.getMessage());
            // 继续执行，不影响logout流程
        }
    }
    
    /**
     * 检查token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, String clientIp, boolean success, String message) {
        // 这里可以记录到数据库或日志文件
        String logMessage = String.format("用户登录 - 用户ID: %s, 用户名: %s, IP: %s, 成功: %s, 消息: %s, 时间: %s",
            userId, username, clientIp, success, message, LocalDateTime.now());
        System.out.println(logMessage);
    }
    
    /**
     * 记录注册日志
     */
    private void recordRegistrationLog(Long userId, String username, String email, boolean success, String message) {
        // 这里可以记录到数据库或日志文件
        String logMessage = String.format("用户注册 - 用户ID: %s, 用户名: %s, 邮箱: %s, 成功: %s, 消息: %s, 时间: %s",
            userId, username, email, success, message, LocalDateTime.now());
        System.out.println(logMessage);
    }
}