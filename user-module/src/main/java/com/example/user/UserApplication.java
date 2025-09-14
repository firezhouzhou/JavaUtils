package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 用户模块启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.common", "com.example.user"})
@EntityScan(basePackages = {"com.example.user.entity"})
@EnableJpaRepositories(basePackages = {"com.example.user.repository"})
public class UserApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}