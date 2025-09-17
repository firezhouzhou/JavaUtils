package com.example.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 统一配置属性
 */
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /**
     * 是否启用安全配置
     */
    private boolean enabled = true;

    /**
     * 无需认证的路径
     */
    private List<String> permitAllPaths = Arrays.asList(
        "/swagger-ui/**",
        "/v3/api-docs/**", 
        "/swagger-resources/**",
        "/webjars/**",
        "/druid/**",
        "/actuator/**",
        "/auth/login",
        "/auth/register",
        "/user/check-username",
        "/user/check-email",
        "/common/id/**"
    );

    /**
     * 用户账号配置
     */
    private List<UserAccount> users = Arrays.asList(
        new UserAccount("admin", "admin123", "ADMIN"),
        new UserAccount("user", "user123", "USER")
    );

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPermitAllPaths() {
        return permitAllPaths;
    }

    public void setPermitAllPaths(List<String> permitAllPaths) {
        this.permitAllPaths = permitAllPaths;
    }

    public List<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccount> users) {
        this.users = users;
    }

    /**
     * 用户账号配置类
     */
    public static class UserAccount {
        private String username;
        private String password;
        private String role;

        public UserAccount() {}

        public UserAccount(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}