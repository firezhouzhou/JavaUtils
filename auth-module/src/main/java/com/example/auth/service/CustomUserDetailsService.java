package com.example.auth.service;

import com.example.auth.entity.AuthUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户详情服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里应该从数据库查询用户信息，为了演示简化处理
        // 实际项目中需要注入UserService来查询用户信息
        
        if ("admin".equals(username)) {
            return AuthUserDetails.builder()
                .userId(1L)
                .username("admin")
                .password("$2a$10$7JB720yubVSQLvm9zS6.VeIh6utbT/zK.rDVvIOHpuLjMELhalK4O") // admin123
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        }
        
        throw new UsernameNotFoundException("用户不存在: " + username);
    }
}