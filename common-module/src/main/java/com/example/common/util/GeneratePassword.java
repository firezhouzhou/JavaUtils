package com.example.common.util;// 运行: cd /Users/zhangsan/githubproject/JavaUtils && java -cp "$(find ~/.m2/repository -name 'spring-security-crypto-*.jar' | head -1)" GeneratePassword.java

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "admin123";
        String encoded = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt编码: " + encoded);
        
        // 验证
        boolean matches = encoder.matches(password, encoded);
        System.out.println("验证结果: " + matches);
        
        // 验证现有的编码
        String existingEncoded = "$2a$10$7JB720yubVSQLvm9zS6.VeIh6utbT/zK.rDVvIOHpuLjMELhalK4O";
        boolean existingMatches = encoder.matches(password, existingEncoded);
        System.out.println("现有编码验证: " + existingMatches);
    }
}
