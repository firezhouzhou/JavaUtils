package com.example.auth.service;

import com.example.auth.entity.AuthUserDetails;
import com.example.auth.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义用户详情服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 简单的内存用户存储（实际项目中应该使用数据库）
    private final Map<String, AuthUserDetails> users = new ConcurrentHashMap<>();
    private final AtomicLong userIdGenerator = new AtomicLong(2); // 从2开始，1留给admin
    
    // 初始化默认用户
    public CustomUserDetailsService() {
        // 默认admin用户会在第一次调用时初始化
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 先检查内存中是否有用户
        AuthUserDetails user = users.get(username);
        if (user != null) {
            return user;
        }
        
        // 如果是admin用户，初始化默认admin
        if ("admin".equals(username)) {
            initializeAdminUser();
            return users.get(username);
        }
        
        throw new UsernameNotFoundException("用户不存在: " + username);
    }
    
    /**
     * 初始化默认admin用户
     */
    private synchronized void initializeAdminUser() {
        if (!users.containsKey("admin")) {
            Role adminRole = new Role("管理员", "ADMIN");
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            
            AuthUserDetails adminUser = AuthUserDetails.builder()
                .userId(1L)
                .username("admin")
                .password("$2a$10$ztESRnI3.iwi4XYDJlN0GOJLWh5q0k8ERYQIxp0Fe.dDbBe0toTT.") // admin123
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();
            
            users.put("admin", adminUser);
        }
    }
    
    /**
     * 注册新用户
     */
    public AuthUserDetails registerUser(String username, String password, String email) {
        // 检查用户名是否已存在
        if (users.containsKey(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建普通用户角色
        Role userRole = new Role("用户", "USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        // 生成新用户ID
        Long userId = userIdGenerator.getAndIncrement();
        
        // 创建新用户
        AuthUserDetails newUser = AuthUserDetails.builder()
            .userId(userId)
            .username(username)
            .password(passwordEncoder.encode(password)) // 使用密码编码器
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .roles(roles)
            .build();
        
        // 保存用户
        users.put(username, newUser);
        
        System.out.println("用户注册成功 - 用户名: " + username + ", 用户ID: " + userId + ", 邮箱: " + email);
        
        return newUser;
    }
    
    /**
     * 获取所有用户（调试用）
     */
    public Map<String, AuthUserDetails> getAllUsers() {
        return new ConcurrentHashMap<>(users);
    }
}