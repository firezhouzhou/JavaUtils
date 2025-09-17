package com.example.auth.service;

import com.example.auth.entity.AuthUserDetails;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 自定义用户详情服务
 */
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return convertToAuthUserDetails(user);
        }
        
        // 如果是admin用户且数据库中不存在，创建默认admin
        if ("admin".equals(username)) {
            User adminUser = createDefaultAdminUser();
            return convertToAuthUserDetails(adminUser);
        }
        
        throw new UsernameNotFoundException("用户不存在: " + username);
    }
    
    /**
     * 创建默认admin用户
     */
    private synchronized User createDefaultAdminUser() {
        // 检查数据库中是否已存在admin用户
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            return existingAdmin.get();
        }
        
        // 创建新的admin用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("$2a$10$ztESRnI3.iwi4XYDJlN0GOJLWh5q0k8ERYQIxp0Fe.dDbBe0toTT."); // admin123
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
        adminUser.setEnabled(true);
        adminUser.setAccountNonExpired(true);
        adminUser.setAccountNonLocked(true);
        adminUser.setCredentialsNonExpired(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        
        // 保存到数据库
        return userRepository.save(adminUser);
    }
    
    /**
     * 注册新用户
     */
    public AuthUserDetails registerUser(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户实体
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // 使用密码编码器
        newUser.setEmail(email);
        newUser.setRole("USER");
        newUser.setEnabled(true);
        newUser.setAccountNonExpired(true);
        newUser.setAccountNonLocked(true);
        newUser.setCredentialsNonExpired(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        // 保存到数据库
        User savedUser = userRepository.save(newUser);
        
        System.out.println("用户注册成功 - 用户名: " + username + ", 用户ID: " + savedUser.getId() + ", 邮箱: " + email);
        
        // 转换为AuthUserDetails返回
        return convertToAuthUserDetails(savedUser);
    }
    
    /**
     * 获取所有用户（调试用）
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 更新用户登录信息
     */
    public void updateLoginInfo(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementLoginCount();
            userRepository.save(user);
        }
    }
    
    /**
     * 将User实体转换为AuthUserDetails
     */
    private AuthUserDetails convertToAuthUserDetails(User user) {
        // 创建角色集合
        Role role = new Role(user.getRole().equals("ADMIN") ? "管理员" : "用户", user.getRole());
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        
        return AuthUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .enabled(user.getEnabled())
                .accountNonExpired(user.getAccountNonExpired())
                .accountNonLocked(user.getAccountNonLocked())
                .credentialsNonExpired(user.getCredentialsNonExpired())
                .roles(roles)
                .build();
    }
    
    /**
     * 根据用户ID获取用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 根据邮箱查找用户
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}