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
        // 检查登录尝试次数
        checkLoginAttempts(username, clientIp);
        
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 获取用户详情
            AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            
            // 生成JWT token
            String accessToken = jwtUtil.generateToken(username, userId);
            String refreshToken = jwtUtil.generateRefreshToken(username, userId);
            
            // 缓存登录状态
            cacheLoginStatus(accessToken, userId, 86400); // 24小时
            cacheRefreshToken(refreshToken, userId, 604800); // 7天
            
            // 清除登录失败记录
            clearLoginAttempts(username, clientIp);
            
            // 记录登录日志
            recordLoginLog(userId, username, clientIp, true, "登录成功");
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 86400);
            result.put("refreshExpiresIn", 604800);
            result.put("userId", userId);
            result.put("username", username);
            result.put("authorities", userDetails.getAuthorities());
            
            return result;
            
        } catch (AuthenticationException e) {
            // 记录登录失败
            recordLoginAttempt(username, clientIp);
            recordLoginLog(null, username, clientIp, false, e.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }
    
    /**
     * 刷新Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        // 去掉Bearer前缀
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
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
    }
    
    /**
     * 退出登录
     */
    public void logout(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 将token加入黑名单
        addTokenToBlacklist(token);
        
        // 清除登录状态缓存
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId != null) {
            clearLoginStatus(token);
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
    public void register(String username, String password, String email) {
        // 检查用户名是否已存在
        try {
            UserDetails existingUser = userDetailsService.loadUserByUsername(username);
            if (existingUser != null) {
                throw new RuntimeException("用户名已存在");
            }
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // 用户不存在，可以注册
        }
        
        // 这里应该调用用户服务进行注册
        // 由于是认证模块，我们只做基本的验证，实际的用户创建应该通过用户服务
        // 为了演示，这里只是记录日志
        System.out.println("用户注册请求 - 用户名: " + username + ", 邮箱: " + email);
        
        // 实际项目中应该：
        // 1. 调用用户服务的注册接口
        // 2. 发送验证邮件
        // 3. 记录注册日志
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
        return (Long) redisTemplate.opsForValue().get(cacheKey);
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
        String key = TOKEN_BLACKLIST_PREFIX + token;
        long expiration = jwtUtil.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
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
}