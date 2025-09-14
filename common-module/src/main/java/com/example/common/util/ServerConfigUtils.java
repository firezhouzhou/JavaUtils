package com.example.common.util;

import com.example.common.config.ServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 服务器配置工具类
 * 提供便捷的方法获取各种服务器配置信息
 */
@Component
public class ServerConfigUtils {
    
    @Autowired
    private ServerConfig serverConfig;
    
    private static ServerConfig staticServerConfig;
    
    /**
     * 初始化静态配置（供非Spring环境使用）
     */
    @PostConstruct
    public void init() {
        staticServerConfig = serverConfig;
    }
    
    /**
     * 获取数据库连接URL
     */
    public static String getDatabaseUrl(String databaseName) {
        if (staticServerConfig != null) {
            return staticServerConfig.getDatabase().getUrl(databaseName);
        }
        // 默认配置
        return String.format("jdbc:mysql://localhost:3306/%s?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8", 
                databaseName);
    }
    
    /**
     * 获取Redis主机地址
     */
    public static String getRedisHost() {
        if (staticServerConfig != null) {
            return staticServerConfig.getRedis().getHost();
        }
        return "localhost";
    }
    
    /**
     * 获取Redis端口
     */
    public static int getRedisPort() {
        if (staticServerConfig != null) {
            return staticServerConfig.getRedis().getPort();
        }
        return 6379;
    }
    
    /**
     * 获取Redis密码
     */
    public static String getRedisPassword() {
        if (staticServerConfig != null) {
            return staticServerConfig.getRedis().getPassword();
        }
        return "";
    }
    
    /**
     * 获取Redis数据库编号
     */
    public static int getRedisDatabase() {
        if (staticServerConfig != null) {
            return staticServerConfig.getRedis().getDatabase();
        }
        return 0;
    }
    
    /**
     * 获取服务主机地址
     */
    public static String getServiceHost() {
        if (staticServerConfig != null) {
            return staticServerConfig.getServices().getHost();
        }
        return "localhost";
    }
    
    /**
     * 获取认证服务URL
     */
    public static String getAuthServiceUrl() {
        if (staticServerConfig != null) {
            return "http://" + staticServerConfig.getServices().getHost() + ":" + staticServerConfig.getServices().getAuth().getPort();
        }
        return "http://localhost:8081";
    }
    
    /**
     * 获取用户服务URL
     */
    public static String getUserServiceUrl() {
        if (staticServerConfig != null) {
            return "http://" + staticServerConfig.getServices().getHost() + ":" + staticServerConfig.getServices().getUser().getPort();
        }
        return "http://localhost:8082";
    }
    
    /**
     * 获取文件服务URL
     */
    public static String getFileServiceUrl() {
        if (staticServerConfig != null) {
            return "http://" + staticServerConfig.getServices().getHost() + ":" + staticServerConfig.getServices().getFile().getPort();
        }
        return "http://localhost:8083";
    }
    
    /**
     * 获取管理服务URL
     */
    public static String getAdminServiceUrl() {
        if (staticServerConfig != null) {
            return "http://" + staticServerConfig.getServices().getHost() + ":" + staticServerConfig.getServices().getAdmin().getPort();
        }
        return "http://localhost:8084";
    }
    
    /**
     * 获取日志服务URL
     */
    public static String getLogServiceUrl() {
        if (staticServerConfig != null) {
            return "http://" + staticServerConfig.getServices().getHost() + ":" + staticServerConfig.getServices().getLog().getPort();
        }
        return "http://localhost:8085";
    }
    
    /**
     * 获取网关URL
     */
    public static String getGatewayUrl() {
        if (staticServerConfig != null) {
            return "http://localhost:" + staticServerConfig.getGateway().getPort();
        }
        return "http://localhost:8080";
    }
    
    /**
     * 获取允许的IP地址
     */
    public static String getAllowedIp() {
        if (staticServerConfig != null) {
            return staticServerConfig.getSecurity().getAllowedIp();
        }
        return "127.0.0.1";
    }
    
    /**
     * 获取允许的IP地址列表
     */
    public static String[] getAllowedIps() {
        if (staticServerConfig != null) {
            return staticServerConfig.getSecurity().getAllowedIps();
        }
        return new String[]{"127.0.0.1", "localhost"};
    }
    
    /**
     * 生成模拟IP地址（用于测试）
     */
    public static String generateMockIp(int index) {
        return "192.168.1." + (100 + index);
    }
    
    /**
     * 获取当前服务端口
     */
    public static int getServicePort(String serviceName) {
        if (staticServerConfig == null) {
            // 默认端口配置
            switch (serviceName.toLowerCase()) {
                case "auth": return 8081;
                case "user": return 8082;
                case "file": return 8083;
                case "admin": return 8084;
                case "log": return 8085;
                case "gateway": return 8080;
                default: return 8080;
            }
        }
        
        ServerConfig.Services services = staticServerConfig.getServices();
        switch (serviceName.toLowerCase()) {
            case "auth": return services.getAuth().getPort();
            case "user": return services.getUser().getPort();
            case "file": return services.getFile().getPort();
            case "admin": return services.getAdmin().getPort();
            case "log": return services.getLog().getPort();
            case "gateway": return staticServerConfig.getGateway().getPort();
            default: return 8080;
        }
    }
    
    /**
     * 检查IP是否在允许列表中
     */
    public static boolean isIpAllowed(String ip) {
        if (ip == null) {
            return false;
        }
        
        String[] allowedIps = getAllowedIps();
        for (String allowedIp : allowedIps) {
            if (allowedIp.equals(ip) || allowedIp.equals("localhost") && "127.0.0.1".equals(ip)) {
                return true;
            }
        }
        return false;
    }
}
