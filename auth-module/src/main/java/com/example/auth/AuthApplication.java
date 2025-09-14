package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 权限认证模块启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.common", "com.example.auth"})
@EntityScan(basePackages = {"com.example.auth.entity"})
public class AuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}