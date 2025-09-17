package com.example.file.config;

import com.example.file.filter.JwtAuthenticationFilter;
import com.example.common.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * 文件服务Security配置
 * 继承统一配置，添加JWT认证和文件服务特定规则
 */
@Configuration
@EnableWebSecurity
@Order(2) // 中等优先级
public class FileSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    @Primary
    public SecurityFilterChain fileFilterChain(HttpSecurity http) throws Exception {
        // 获取统一配置的无需认证路径
        List<String> permitAllPaths = securityProperties.getPermitAllPaths();
        String[] pathArray = permitAllPaths.toArray(new String[0]);
        
        return http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // 使用统一配置的无需认证路径
                .antMatchers(pathArray).permitAll()
                // 文件服务特定的认证规则
                .antMatchers("/file/upload", "/file/download/**", "/file/delete/**").authenticated()
                .antMatchers("/file/list", "/file/info/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
