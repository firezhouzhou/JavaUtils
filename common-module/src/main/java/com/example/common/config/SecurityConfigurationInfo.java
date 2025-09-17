package com.example.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Security配置信息输出
 * 用于启动时显示当前的安全配置信息
 */
@Component
public class SecurityConfigurationInfo implements CommandLineRunner {

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void run(String... args) throws Exception {
        if (securityProperties.isEnabled()) {
            System.out.println("=== Spring Security 统一配置已启用 ===");
            System.out.println("配置的用户账号:");
            for (SecurityProperties.UserAccount user : securityProperties.getUsers()) {
                System.out.println("  - 用户名: " + user.getUsername() + ", 角色: " + user.getRole());
            }
            System.out.println("无需认证的路径:");
            for (String path : securityProperties.getPermitAllPaths()) {
                System.out.println("  - " + path);
            }
            System.out.println("========================================");
        }
    }
}