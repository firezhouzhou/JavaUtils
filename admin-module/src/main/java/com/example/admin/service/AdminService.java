package com.example.admin.service;

import com.example.admin.controller.AdminController.*;
import com.example.common.web.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理服务
 */
@Service
public class AdminService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 获取系统概览
     */
    public SystemOverview getSystemOverview() {
        // 模拟数据，实际项目中应该从数据库查询
        long totalUsers = 1250L;
        long activeUsers = 890L;
        long totalFiles = 5680L;
        long totalFileSize = 2048000000L; // 2GB
        long todayLogins = 156L;
        long todayRegistrations = 23L;
        
        // 获取系统资源使用情况
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();
        double diskUsage = getDiskUsage();
        
        return new SystemOverview(totalUsers, activeUsers, totalFiles, totalFileSize,
            todayLogins, todayRegistrations, cpuUsage, memoryUsage, diskUsage);
    }
    
    /**
     * 获取用户统计
     */
    public UserStatistics getUserStatistics(String startDate, String endDate) {
        // 模拟数据，实际项目中应该根据日期范围查询数据库
        long totalUsers = 1250L;
        long newUsers = 45L;
        long activeUsers = 890L;
        long inactiveUsers = 360L;
        
        return new UserStatistics(totalUsers, newUsers, activeUsers, inactiveUsers);
    }
    
    /**
     * 获取文件统计
     */
    public FileStatistics getFileStatistics(String startDate, String endDate) {
        // 模拟数据
        long totalFiles = 5680L;
        long totalSize = 2048000000L;
        long imageFiles = 3200L;
        long documentFiles = 2100L;
        long otherFiles = 380L;
        
        return new FileStatistics(totalFiles, totalSize, imageFiles, documentFiles, otherFiles);
    }
    
    /**
     * 获取系统日志
     */
    public PageResponse<SystemLog> getSystemLogs(int page, int size, String level, String keyword) {
        // 模拟日志数据
        List<SystemLog> logs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            SystemLog log = new SystemLog();
            log.setId((long) (page - 1) * size + i + 1);
            log.setLevel(level != null ? level : "INFO");
            log.setMessage("系统日志消息 " + (i + 1));
            log.setLogger("com.example.service.UserService");
            log.setCreateTime(LocalDateTime.now().minusMinutes(i * 5));
            logs.add(log);
        }
        
        return new PageResponse<>((long) page, (long) size, 1000L, logs);
    }
    
    /**
     * 获取登录日志
     */
    public PageResponse<LoginLog> getLoginLogs(int page, int size, String username, String ip) {
        // 模拟登录日志数据
        List<LoginLog> logs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            LoginLog log = new LoginLog();
            log.setId((long) (page - 1) * size + i + 1);
            log.setUsername(username != null ? username : "user" + (i + 1));
            log.setIp(ip != null ? ip : "192.168.1." + (100 + i));
            log.setSuccess(i % 10 != 0); // 10%失败率
            log.setMessage(log.isSuccess() ? "登录成功" : "密码错误");
            log.setCreateTime(LocalDateTime.now().minusHours(i));
            logs.add(log);
        }
        
        return new PageResponse<>((long) page, (long) size, 500L, logs);
    }
    
    /**
     * 获取操作日志
     */
    public PageResponse<OperationLog> getOperationLogs(int page, int size, Long userId, String operationType) {
        // 模拟操作日志数据
        List<OperationLog> logs = new ArrayList<>();
        String[] operations = {"创建用户", "删除文件", "修改配置", "上传文件", "下载文件"};
        String[] types = {"CREATE", "DELETE", "UPDATE", "UPLOAD", "DOWNLOAD"};
        
        for (int i = 0; i < size; i++) {
            OperationLog log = new OperationLog();
            log.setId((long) (page - 1) * size + i + 1);
            log.setUserId(userId != null ? userId : (long) (i + 1));
            log.setUsername("user" + (i + 1));
            log.setOperation(operations[i % operations.length]);
            log.setOperationType(operationType != null ? operationType : types[i % types.length]);
            log.setDescription("操作描述 " + (i + 1));
            log.setIp("192.168.1." + (100 + i));
            log.setCreateTime(LocalDateTime.now().minusMinutes(i * 10));
            logs.add(log);
        }
        
        return new PageResponse<>((long) page, (long) size, 800L, logs);
    }
    
    /**
     * 清理缓存
     */
    public void clearCache(List<String> cacheKeys) {
        for (String key : cacheKeys) {
            if ("all".equals(key)) {
                // 清理所有缓存
                Set<String> keys = redisTemplate.keys("*");
                if (!keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } else {
                // 清理指定缓存
                Set<String> keys = redisTemplate.keys(key + "*");
                if (!keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            }
        }
    }
    
    /**
     * 获取系统配置
     */
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("system.name", "JavaUtils管理系统");
        config.put("system.version", "1.0.0");
        config.put("file.upload.maxSize", "10MB");
        config.put("user.login.maxAttempts", 5);
        config.put("jwt.expiration", 86400);
        config.put("cache.ttl", 3600);
        
        return config;
    }
    
    /**
     * 更新系统配置
     */
    public void updateSystemConfig(Map<String, Object> config) {
        // 实际项目中应该保存到数据库或配置文件
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = "config:" + entry.getKey();
            redisTemplate.opsForValue().set(key, entry.getValue());
        }
    }
    
    /**
     * 获取系统健康状态
     */
    public SystemHealth getSystemHealth() {
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();
        double diskUsage = getDiskUsage();
        boolean databaseConnected = checkDatabaseConnection();
        boolean redisConnected = checkRedisConnection();
        
        String status = "HEALTHY";
        if (cpuUsage > 80 || memoryUsage > 80 || diskUsage > 90 || !databaseConnected || !redisConnected) {
            status = "UNHEALTHY";
        } else if (cpuUsage > 60 || memoryUsage > 60 || diskUsage > 70) {
            status = "WARNING";
        }
        
        return new SystemHealth(status, cpuUsage, memoryUsage, diskUsage, databaseConnected, redisConnected);
    }
    
    /**
     * 备份数据库
     */
    public String backupDatabase() {
        // 模拟数据库备份
        String backupPath = "/backup/database_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
        
        // 实际项目中应该执行真正的数据库备份命令
        System.out.println("正在备份数据库到: " + backupPath);
        
        return backupPath;
    }
    
    /**
     * 获取在线用户
     */
    public List<OnlineUser> getOnlineUsers() {
        List<OnlineUser> onlineUsers = new ArrayList<>();
        
        // 从Redis中获取在线用户信息
        Set<String> loginKeys = redisTemplate.keys("login:*");
        if (loginKeys != null) {
            for (String key : loginKeys) {
                Long userId = (Long) redisTemplate.opsForValue().get(key);
                if (userId != null) {
                    OnlineUser user = new OnlineUser();
                    user.setUserId(userId);
                    user.setUsername("user" + userId);
                    user.setIp("192.168.1.100");
                    user.setLoginTime(LocalDateTime.now().minusHours(2));
                    user.setLastActiveTime(LocalDateTime.now().minusMinutes(5));
                    onlineUsers.add(user);
                }
            }
        }
        
        return onlineUsers;
    }
    
    /**
     * 强制用户下线
     */
    public void forceUserOffline(Long userId) {
        // 删除用户的所有登录token
        Set<String> loginKeys = redisTemplate.keys("login:*");
        if (loginKeys != null) {
            for (String key : loginKeys) {
                Long cachedUserId = (Long) redisTemplate.opsForValue().get(key);
                if (userId.equals(cachedUserId)) {
                    redisTemplate.delete(key);
                    
                    // 将token加入黑名单
                    String token = key.substring(6); // 去掉"login:"前缀
                    String blacklistKey = "token_blacklist:" + token;
                    redisTemplate.opsForValue().set(blacklistKey, "forced_offline", 86400, 
                        java.util.concurrent.TimeUnit.SECONDS);
                }
            }
        }
    }
    
    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getProcessCpuLoad() * 100;
        }
        return Math.random() * 100; // 模拟数据
    }
    
    /**
     * 获取内存使用率
     */
    private double getMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long used = memoryBean.getHeapMemoryUsage().getUsed();
        long max = memoryBean.getHeapMemoryUsage().getMax();
        return (double) used / max * 100;
    }
    
    /**
     * 获取磁盘使用率
     */
    private double getDiskUsage() {
        File disk = new File("/");
        long total = disk.getTotalSpace();
        long free = disk.getFreeSpace();
        long used = total - free;
        return (double) used / total * 100;
    }
    
    /**
     * 检查数据库连接
     */
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查Redis连接
     */
    private boolean checkRedisConnection() {
        try {
            redisTemplate.opsForValue().set("health_check", "ok", 10, 
                java.util.concurrent.TimeUnit.SECONDS);
            return "ok".equals(redisTemplate.opsForValue().get("health_check"));
        } catch (Exception e) {
            return false;
        }
    }
}