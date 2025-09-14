package com.example.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 文件模块启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.common", "com.example.file"})
@EntityScan(basePackages = {"com.example.file.entity"})
@EnableJpaRepositories(basePackages = {"com.example.file.repository"})
public class FileApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}