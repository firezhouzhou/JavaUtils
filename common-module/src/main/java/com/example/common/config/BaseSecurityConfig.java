package com.example.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一Spring Security配置
 * 为所有模块提供统一的安全配置，避免重复配置
 */
@Configuration
@EnableWebSecurity
@ConditionalOnClass(name = "org.springframework.security.web.SecurityFilterChain")
@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@Order(99)  // 设置较低优先级，让模块特定配置优先
public class BaseSecurityConfig {

    @Autowired
    private SecurityProperties securityProperties;

    /**
     * 配置安全过滤器链
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 获取无需认证的路径
        List<String> permitAllPaths = securityProperties.getPermitAllPaths();
        String[] pathArray = permitAllPaths.toArray(new String[0]);

        http.csrf().disable()
                .authorizeRequests()
                // 动态配置无需认证的路径
                .antMatchers(pathArray).permitAll()
                // 所有其他请求需要认证
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .headers()
                .frameOptions().sameOrigin();

        return http.build();

    }

    /**
     * 配置统一用户详情服务
     * 根据配置文件动态创建用户账号
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        List<UserDetails> userDetailsList = new ArrayList<>();
        
        // 根据配置创建用户账号
        for (SecurityProperties.UserAccount userAccount : securityProperties.getUsers()) {
            UserDetails userDetails = User.builder()
                    .username(userAccount.getUsername())
                    .password(passwordEncoder.encode(userAccount.getPassword()))
                    .roles(userAccount.getRole())
                    .authorities("ROLE_" + userAccount.getRole())
                    .build();
            userDetailsList.add(userDetails);
        }
        
        return new InMemoryUserDetailsManager(userDetailsList);
    }

    /**
     * 密码编码器
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}