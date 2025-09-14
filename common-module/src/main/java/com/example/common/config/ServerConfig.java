package com.example.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务器配置类
 * 统一管理所有模块的IP地址和端口配置
 */
@Configuration
@ConfigurationProperties(prefix = "server.config")
public class ServerConfig {
    
    /**
     * 数据库服务器配置
     */
    private Database database = new Database();
    
    /**
     * Redis服务器配置
     */
    private Redis redis = new Redis();
    
    /**
     * 微服务配置
     */
    private Services services = new Services();
    
    /**
     * 网关配置
     */
    private Gateway gateway = new Gateway();
    
    /**
     * 安全配置
     */
    private Security security = new Security();
    
    // Getters and Setters
    public Database getDatabase() {
        return database;
    }
    
    public void setDatabase(Database database) {
        this.database = database;
    }
    
    public Redis getRedis() {
        return redis;
    }
    
    public void setRedis(Redis redis) {
        this.redis = redis;
    }
    
    public Services getServices() {
        return services;
    }
    
    public void setServices(Services services) {
        this.services = services;
    }
    
    public Gateway getGateway() {
        return gateway;
    }
    
    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }
    
    public Security getSecurity() {
        return security;
    }
    
    public void setSecurity(Security security) {
        this.security = security;
    }
    
    /**
     * 数据库配置
     */
    public static class Database {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "19990626ZYX";
        
        // Getters and Setters
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
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
        
        /**
         * 获取数据库连接URL
         */
        public String getUrl(String databaseName) {
            return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8",
                    host, port, databaseName);
        }
        
        /**
         * 获取数据库连接URL（带参数）
         */
        public String getUrl(String databaseName, String additionalParams) {
            return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%%2B8&%s",
                    host, port, databaseName, additionalParams);
        }
    }
    
    /**
     * Redis配置
     */
    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        
        // Getters and Setters
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public int getDatabase() {
            return database;
        }
        
        public void setDatabase(int database) {
            this.database = database;
        }
    }
    
    /**
     * 微服务配置
     */
    public static class Services {
        private String host = "localhost";
        private Auth auth = new Auth();
        private User user = new User();
        private File file = new File();
        private Admin admin = new Admin();
        private Log log = new Log();
        
        // Getters and Setters
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public Auth getAuth() {
            return auth;
        }
        
        public void setAuth(Auth auth) {
            this.auth = auth;
        }
        
        public User getUser() {
            return user;
        }
        
        public void setUser(User user) {
            this.user = user;
        }
        
        public File getFile() {
            return file;
        }
        
        public void setFile(File file) {
            this.file = file;
        }
        
        public Admin getAdmin() {
            return admin;
        }
        
        public void setAdmin(Admin admin) {
            this.admin = admin;
        }
        
        public Log getLog() {
            return log;
        }
        
        public void setLog(Log log) {
            this.log = log;
        }
        
        /**
         * 认证服务配置
         */
        public static class Auth {
            private int port = 8081;
            
            public int getPort() {
                return port;
            }
            
            public void setPort(int port) {
                this.port = port;
            }
        }
        
        /**
         * 用户服务配置
         */
        public static class User {
            private int port = 8082;
            
            public int getPort() {
                return port;
            }
            
            public void setPort(int port) {
                this.port = port;
            }
        }
        
        /**
         * 文件服务配置
         */
        public static class File {
            private int port = 8083;
            
            public int getPort() {
                return port;
            }
            
            public void setPort(int port) {
                this.port = port;
            }
        }
        
        /**
         * 管理服务配置
         */
        public static class Admin {
            private int port = 8084;
            
            public int getPort() {
                return port;
            }
            
            public void setPort(int port) {
                this.port = port;
            }
        }
        
        /**
         * 日志服务配置
         */
        public static class Log {
            private int port = 8085;
            
            public int getPort() {
                return port;
            }
            
            public void setPort(int port) {
                this.port = port;
            }
        }
    }
    
    /**
     * 网关配置
     */
    public static class Gateway {
        private int port = 8080;
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
    }
    
    /**
     * 安全配置
     */
    public static class Security {
        private String allowedIp = "127.0.0.1";
        private String[] allowedIps = {"127.0.0.1", "localhost"};
        
        public String getAllowedIp() {
            return allowedIp;
        }
        
        public void setAllowedIp(String allowedIp) {
            this.allowedIp = allowedIp;
        }
        
        public String[] getAllowedIps() {
            return allowedIps;
        }
        
        public void setAllowedIps(String[] allowedIps) {
            this.allowedIps = allowedIps;
        }
    }
}