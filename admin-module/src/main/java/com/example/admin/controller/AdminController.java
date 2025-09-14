package com.example.admin.controller;

import com.example.admin.service.AdminService;
import com.example.common.web.ApiResponse;
import com.example.common.web.PageResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@Api(tags = "系统管理")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @ApiOperation("获取系统概览")
    @GetMapping("/dashboard")
    public ApiResponse<SystemOverview> getSystemOverview() {
        SystemOverview overview = adminService.getSystemOverview();
        return ApiResponse.success(overview);
    }
    
    @ApiOperation("获取用户统计")
    @GetMapping("/statistics/users")
    public ApiResponse<UserStatistics> getUserStatistics(
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate) {
        UserStatistics statistics = adminService.getUserStatistics(startDate, endDate);
        return ApiResponse.success(statistics);
    }
    
    @ApiOperation("获取文件统计")
    @GetMapping("/statistics/files")
    public ApiResponse<FileStatistics> getFileStatistics(
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate) {
        FileStatistics statistics = adminService.getFileStatistics(startDate, endDate);
        return ApiResponse.success(statistics);
    }
    
    @ApiOperation("获取系统日志")
    @GetMapping("/logs")
    public ApiResponse<PageResponse<SystemLog>> getSystemLogs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") int size,
            @ApiParam("日志级别") @RequestParam(required = false) String level,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        PageResponse<SystemLog> logs = adminService.getSystemLogs(page, size, level, keyword);
        return ApiResponse.success(logs);
    }
    
    @ApiOperation("获取登录日志")
    @GetMapping("/logs/login")
    public ApiResponse<PageResponse<LoginLog>> getLoginLogs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") int size,
            @ApiParam("用户名") @RequestParam(required = false) String username,
            @ApiParam("IP地址") @RequestParam(required = false) String ip) {
        PageResponse<LoginLog> logs = adminService.getLoginLogs(page, size, username, ip);
        return ApiResponse.success(logs);
    }
    
    @ApiOperation("获取操作日志")
    @GetMapping("/logs/operation")
    public ApiResponse<PageResponse<OperationLog>> getOperationLogs(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") int size,
            @ApiParam("用户ID") @RequestParam(required = false) Long userId,
            @ApiParam("操作类型") @RequestParam(required = false) String operationType) {
        PageResponse<OperationLog> logs = adminService.getOperationLogs(page, size, userId, operationType);
        return ApiResponse.success(logs);
    }
    
    @ApiOperation("清理系统缓存")
    @PostMapping("/cache/clear")
    public ApiResponse<String> clearCache(@RequestBody @Valid ClearCacheRequest request) {
        adminService.clearCache(request.getCacheKeys());
        return ApiResponse.success("缓存清理成功");
    }
    
    @ApiOperation("获取系统配置")
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = adminService.getSystemConfig();
        return ApiResponse.success(config);
    }
    
    @ApiOperation("更新系统配置")
    @PutMapping("/config")
    public ApiResponse<String> updateSystemConfig(@RequestBody Map<String, Object> config) {
        adminService.updateSystemConfig(config);
        return ApiResponse.success("配置更新成功");
    }
    
    @ApiOperation("系统健康检查")
    @GetMapping("/health")
    public ApiResponse<SystemHealth> getSystemHealth() {
        SystemHealth health = adminService.getSystemHealth();
        return ApiResponse.success(health);
    }
    
    @ApiOperation("备份数据库")
    @PostMapping("/backup/database")
    public ApiResponse<String> backupDatabase() {
        String backupPath = adminService.backupDatabase();
        return ApiResponse.success("数据库备份成功", backupPath);
    }
    
    @ApiOperation("获取在线用户")
    @GetMapping("/users/online")
    public ApiResponse<List<OnlineUser>> getOnlineUsers() {
        List<OnlineUser> onlineUsers = adminService.getOnlineUsers();
        return ApiResponse.success(onlineUsers);
    }
    
    @ApiOperation("强制用户下线")
    @PostMapping("/users/{userId}/offline")
    public ApiResponse<String> forceUserOffline(@PathVariable Long userId) {
        adminService.forceUserOffline(userId);
        return ApiResponse.success("用户已强制下线");
    }
    
    // 内部类定义
    public static class SystemOverview {
        private long totalUsers;
        private long activeUsers;
        private long totalFiles;
        private long totalFileSize;
        private long todayLogins;
        private long todayRegistrations;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        
        // 构造函数
        public SystemOverview(long totalUsers, long activeUsers, long totalFiles, long totalFileSize,
                            long todayLogins, long todayRegistrations, double cpuUsage, double memoryUsage, double diskUsage) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.totalFiles = totalFiles;
            this.totalFileSize = totalFileSize;
            this.todayLogins = todayLogins;
            this.todayRegistrations = todayRegistrations;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.diskUsage = diskUsage;
        }
        
        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        
        public long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(long totalFiles) { this.totalFiles = totalFiles; }
        
        public long getTotalFileSize() { return totalFileSize; }
        public void setTotalFileSize(long totalFileSize) { this.totalFileSize = totalFileSize; }
        
        public long getTodayLogins() { return todayLogins; }
        public void setTodayLogins(long todayLogins) { this.todayLogins = todayLogins; }
        
        public long getTodayRegistrations() { return todayRegistrations; }
        public void setTodayRegistrations(long todayRegistrations) { this.todayRegistrations = todayRegistrations; }
        
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
    }
    
    public static class UserStatistics {
        private long totalUsers;
        private long newUsers;
        private long activeUsers;
        private long inactiveUsers;
        
        public UserStatistics(long totalUsers, long newUsers, long activeUsers, long inactiveUsers) {
            this.totalUsers = totalUsers;
            this.newUsers = newUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
        }
        
        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getNewUsers() { return newUsers; }
        public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
        
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        
        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }
    }
    
    public static class FileStatistics {
        private long totalFiles;
        private long totalSize;
        private long imageFiles;
        private long documentFiles;
        private long otherFiles;
        
        public FileStatistics(long totalFiles, long totalSize, long imageFiles, long documentFiles, long otherFiles) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.imageFiles = imageFiles;
            this.documentFiles = documentFiles;
            this.otherFiles = otherFiles;
        }
        
        // Getters and Setters
        public long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(long totalFiles) { this.totalFiles = totalFiles; }
        
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        
        public long getImageFiles() { return imageFiles; }
        public void setImageFiles(long imageFiles) { this.imageFiles = imageFiles; }
        
        public long getDocumentFiles() { return documentFiles; }
        public void setDocumentFiles(long documentFiles) { this.documentFiles = documentFiles; }
        
        public long getOtherFiles() { return otherFiles; }
        public void setOtherFiles(long otherFiles) { this.otherFiles = otherFiles; }
    }
    
    public static class SystemLog {
        private Long id;
        private String level;
        private String message;
        private String logger;
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getLogger() { return logger; }
        public void setLogger(String logger) { this.logger = logger; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    public static class LoginLog {
        private Long id;
        private String username;
        private String ip;
        private boolean success;
        private String message;
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    public static class OperationLog {
        private Long id;
        private Long userId;
        private String username;
        private String operation;
        private String operationType;
        private String description;
        private String ip;
        private LocalDateTime createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    public static class SystemHealth {
        private String status;
        private double cpuUsage;
        private double memoryUsage;
        private double diskUsage;
        private boolean databaseConnected;
        private boolean redisConnected;
        private LocalDateTime checkTime;
        
        public SystemHealth(String status, double cpuUsage, double memoryUsage, double diskUsage,
                          boolean databaseConnected, boolean redisConnected) {
            this.status = status;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.diskUsage = diskUsage;
            this.databaseConnected = databaseConnected;
            this.redisConnected = redisConnected;
            this.checkTime = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }
        
        public double getMemoryUsage() { return memoryUsage; }
        public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
        
        public double getDiskUsage() { return diskUsage; }
        public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }
        
        public boolean isDatabaseConnected() { return databaseConnected; }
        public void setDatabaseConnected(boolean databaseConnected) { this.databaseConnected = databaseConnected; }
        
        public boolean isRedisConnected() { return redisConnected; }
        public void setRedisConnected(boolean redisConnected) { this.redisConnected = redisConnected; }
        
        public LocalDateTime getCheckTime() { return checkTime; }
        public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }
    }
    
    public static class OnlineUser {
        private Long userId;
        private String username;
        private String ip;
        private LocalDateTime loginTime;
        private LocalDateTime lastActiveTime;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        
        public LocalDateTime getLastActiveTime() { return lastActiveTime; }
        public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    }
    
    public static class ClearCacheRequest {
        @NotEmpty(message = "缓存键不能为空")
        private List<String> cacheKeys;
        
        public List<String> getCacheKeys() { return cacheKeys; }
        public void setCacheKeys(List<String> cacheKeys) { this.cacheKeys = cacheKeys; }
    }
}