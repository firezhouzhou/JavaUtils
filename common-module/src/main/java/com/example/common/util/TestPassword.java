package com.example.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String rawPassword = "admin123";
        String encodedPassword = "$2a$10$7JB720yubVSQLvm9zS6.VeIh6utbT/zK.rDVvIOHpuLjMELhalK4O";
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("编码密码: " + encodedPassword);
        
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码匹配: " + matches);
        
        // 生成新的编码密码
        String newEncoded = encoder.encode(rawPassword);
        System.out.println("新编码密码: " + newEncoded);
        
        // 测试其他可能的密码
        String[] testPasswords = {"admin", "123456", "admin123", "password"};
        for (String testPwd : testPasswords) {
            boolean testMatch = encoder.matches(testPwd, encodedPassword);
            System.out.println("测试密码 '" + testPwd + "': " + testMatch);
        }
    }
}
