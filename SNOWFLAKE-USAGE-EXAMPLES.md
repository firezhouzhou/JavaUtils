# 雪花算法ID使用示例

本文档提供了在各个模块中使用雪花算法ID生成器的具体示例。

## 1. 用户模块（user-module）示例

### 在用户注册时生成唯一ID

```java
// UserService.java
package com.example.user.service;

import com.example.common.util.IdUtils;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User registerUser(String username, String email, String password) {
        User user = new User();
        user.setId(IdUtils.generateId());  // 使用雪花算法生成用户ID
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedTime(System.currentTimeMillis());
        
        return userRepository.save(user);
    }
    
    public String generateUserToken(Long userId) {
        // 生成用户会话令牌ID
        Long tokenId = IdUtils.generateId();
        return "TOKEN_" + tokenId + "_" + userId;
    }
}
```

### 用户实体类更新

```java
// User.java
package com.example.user.entity;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    private Long id;  // 使用雪花算法生成的ID，不使用@GeneratedValue
    
    @Column(unique = true)
    private String username;
    
    private String email;
    private String password;
    private Long createdTime;
    
    // 构造函数、getter和setter
    public User() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    // ... 其他getter和setter
}
```

## 2. 文件模块（file-module）示例

### 文件上传时生成文件ID

```java
// FileService.java
package com.example.file.service;

import com.example.common.util.IdUtils;
import com.example.file.entity.FileInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    
    public FileInfo uploadFile(MultipartFile file, Long userId) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(IdUtils.generateId());  // 文件记录ID
        fileInfo.setUserId(userId);
        fileInfo.setOriginalName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setUploadTime(System.currentTimeMillis());
        
        // 生成存储文件名，避免重名
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String storedFileName = IdUtils.generateIdStr() + fileExtension;
        fileInfo.setStoredName(storedFileName);
        
        // 保存文件逻辑...
        saveFileToStorage(file, storedFileName);
        
        return fileRepository.save(fileInfo);
    }
    
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }
}
```

## 3. 订单模块示例

### 创建订单和订单项

```java
// OrderService.java
package com.example.order.service;

import com.example.common.util.IdUtils;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    
    @Transactional
    public Order createOrder(Long userId, List<OrderItemRequest> items) {
        // 创建订单
        Order order = new Order();
        order.setId(IdUtils.generateId());  // 订单ID
        order.setUserId(userId);
        order.setOrderNo("ORD" + IdUtils.generateIdStr());  // 订单号
        order.setStatus("PENDING");
        order.setCreateTime(System.currentTimeMillis());
        
        // 创建订单项
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : items) {
            OrderItem item = new OrderItem();
            item.setId(IdUtils.generateId());  // 订单项ID
            item.setOrderId(order.getId());
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());
            orderItems.add(item);
        }
        
        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }
    
    public String generatePaymentId(Long orderId) {
        return "PAY_" + IdUtils.generateIdStr() + "_" + orderId;
    }
}
```

## 4. 日志模块（log-module）示例

### 访问日志记录

```java
// AccessLogService.java - 已更新使用雪花算法
package com.example.log.service;

import com.example.common.util.IdUtils;
import com.example.log.entity.AccessLog;
import org.springframework.stereotype.Service;

@Service
public class AccessLogService {
    
    public void logAccess(String userId, String method, String url, String ip) {
        AccessLog log = new AccessLog();
        log.setId(IdUtils.generateId());  // 使用雪花算法生成日志ID
        log.setUserId(userId);
        log.setMethod(method);
        log.setUrl(url);
        log.setIpAddress(ip);
        log.setRequestTime(new Date());
        log.setModuleName(extractModuleName(url));
        
        accessLogRepository.save(log);
    }
    
    // 批量记录日志，提高性能
    public void batchLogAccess(List<AccessLogRequest> requests) {
        List<AccessLog> logs = requests.stream().map(request -> {
            AccessLog log = new AccessLog();
            log.setId(IdUtils.generateId());  // 每条日志都有唯一ID
            log.setUserId(request.getUserId());
            log.setMethod(request.getMethod());
            log.setUrl(request.getUrl());
            log.setIpAddress(request.getIp());
            log.setRequestTime(new Date());
            return log;
        }).collect(Collectors.toList());
        
        accessLogRepository.saveAll(logs);
    }
}
```

## 5. 认证模块（auth-module）示例

### 生成会话和令牌ID

```java
// AuthService.java
package com.example.auth.service;

import com.example.common.util.IdUtils;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    public String generateSessionId(Long userId) {
        Long sessionId = IdUtils.generateId();
        return "SESSION_" + sessionId + "_" + userId;
    }
    
    public String generateRefreshToken(Long userId) {
        Long tokenId = IdUtils.generateId();
        // 将tokenId和userId组合，便于后续解析
        return Base64.getEncoder().encodeToString(
            (tokenId + ":" + userId + ":" + System.currentTimeMillis()).getBytes()
        );
    }
    
    public void recordLoginAttempt(String username, boolean success) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setId(IdUtils.generateId());  // 登录尝试记录ID
        attempt.setUsername(username);
        attempt.setSuccess(success);
        attempt.setAttemptTime(System.currentTimeMillis());
        attempt.setIpAddress(getCurrentIpAddress());
        
        loginAttemptRepository.save(attempt);
    }
}
```

## 6. 管理模块（admin-module）示例

### 生成操作日志ID

```java
// AdminService.java
package com.example.admin.service;

import com.example.common.util.IdUtils;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    
    public void recordAdminOperation(Long adminId, String operation, String details) {
        AdminOperationLog log = new AdminOperationLog();
        log.setId(IdUtils.generateId());  // 操作日志ID
        log.setAdminId(adminId);
        log.setOperation(operation);
        log.setDetails(details);
        log.setOperationTime(System.currentTimeMillis());
        log.setIpAddress(getCurrentIpAddress());
        
        operationLogRepository.save(log);
    }
    
    public String generateSystemTaskId() {
        return "TASK_" + IdUtils.generateIdStr();
    }
    
    public void scheduleSystemMaintenance() {
        SystemTask task = new SystemTask();
        task.setId(IdUtils.generateId());
        task.setTaskId(generateSystemTaskId());
        task.setTaskType("MAINTENANCE");
        task.setStatus("SCHEDULED");
        task.setScheduledTime(System.currentTimeMillis() + 3600000); // 1小时后
        
        systemTaskRepository.save(task);
    }
}
```

## 7. 网关模块（gateway-module）示例

### 请求追踪ID

```java
// RequestTraceFilter.java
package com.example.gateway.filter;

import com.example.common.util.IdUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestTraceFilter implements GlobalFilter, Ordered {
    
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 为每个请求生成唯一的追踪ID
        String traceId = "TRACE_" + IdUtils.generateIdStr();
        
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();
        
        // 记录请求日志
        logRequest(traceId, request);
        
        return chain.filter(exchange.mutate().request(request).build());
    }
    
    private void logRequest(String traceId, ServerHttpRequest request) {
        System.out.println("Request [" + traceId + "] " + 
                          request.getMethod() + " " + request.getURI());
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
```

## 8. 通用工具类示例

### ID工具扩展

```java
// CustomIdUtils.java
package com.example.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomIdUtils {
    
    /**
     * 生成带前缀的ID
     */
    public static String generatePrefixedId(String prefix) {
        return prefix + "_" + IdUtils.generateIdStr();
    }
    
    /**
     * 生成用户ID
     */
    public static Long generateUserId() {
        return IdUtils.generateId();
    }
    
    /**
     * 生成订单号（包含日期）
     */
    public static String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD" + dateStr + IdUtils.generateIdStr();
    }
    
    /**
     * 生成文件存储路径
     */
    public static String generateFilePath(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return dateStr + "/" + IdUtils.generateIdStr() + extension;
    }
    
    /**
     * 解析ID中的时间信息，用于数据分析
     */
    public static LocalDateTime getIdGenerateTime(Long id) {
        long timestamp = IdUtils.parseTimestamp(id);
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            java.time.ZoneId.systemDefault()
        );
    }
}
```

## 9. 数据库迁移示例

### 现有表结构升级

```sql
-- 用户表升级（如果已有自增ID）
-- 1. 添加新的雪花算法ID字段
ALTER TABLE users ADD COLUMN snowflake_id BIGINT;

-- 2. 为现有记录生成雪花算法ID（通过应用程序）
-- 3. 更新应用程序使用新ID
-- 4. 最终删除旧的自增ID（可选）

-- 新表直接使用雪花算法ID
CREATE TABLE orders (
    id BIGINT PRIMARY KEY COMMENT '雪花算法ID',
    user_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2),
    create_time BIGINT NOT NULL,
    update_time BIGINT,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_order_no (order_no)
);

-- 分表示例（按时间范围）
CREATE TABLE access_log_202312 (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(50),
    method VARCHAR(10),
    url VARCHAR(500),
    ip_address VARCHAR(50),
    request_time DATETIME,
    response_status INT,
    module_name VARCHAR(50),
    INDEX idx_user_id (user_id),
    INDEX idx_request_time (request_time),
    INDEX idx_module_name (module_name)
) PARTITION BY RANGE (TO_DAYS(request_time)) (
    PARTITION p202312 VALUES LESS THAN (TO_DAYS('2024-01-01')),
    PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
    PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01'))
);
```

## 10. 性能测试示例

### ID生成性能测试

```java
// IdPerformanceTest.java
package com.example.test;

import com.example.common.util.IdUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@SpringBootTest
public class IdPerformanceTest {
    
    @Test
    public void testSingleThreadPerformance() {
        int count = 100000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            IdUtils.generateId();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("单线程生成 " + count + " 个ID耗时: " + duration + "ms");
        System.out.println("平均每个ID耗时: " + (duration * 1.0 / count) + "ms");
        System.out.println("每秒生成ID数: " + (count * 1000 / duration));
    }
    
    @Test
    public void testMultiThreadPerformance() {
        int threadCount = 10;
        int idsPerThread = 10000;
        Set<Long> allIds = ConcurrentHashMap.newKeySet();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<Void>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < idsPerThread; j++) {
                        Long id = IdUtils.generateId();
                        allIds.add(id);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(futures).join();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        int totalIds = threadCount * idsPerThread;
        
        System.out.println("多线程生成 " + totalIds + " 个ID耗时: " + duration + "ms");
        System.out.println("ID唯一性: " + (allIds.size() == totalIds ? "通过" : "失败"));
        System.out.println("每秒生成ID数: " + (totalIds * 1000 / duration));
        
        executor.shutdown();
    }
    
    @Test
    public void testIdUniqueness() {
        int count = 1000000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        
        IntStream.range(0, count).parallel().forEach(i -> {
            Long id = IdUtils.generateId();
            ids.add(id);
        });
        
        System.out.println("生成ID数量: " + count);
        System.out.println("唯一ID数量: " + ids.size());
        System.out.println("唯一性测试: " + (ids.size() == count ? "通过" : "失败"));
    }
}
```

## 使用建议

1. **在实体类中使用**：直接将雪花算法ID作为主键，不使用数据库自增
2. **批量操作**：对于大量ID生成，考虑批量生成以提高性能
3. **缓存策略**：可以预生成一批ID缓存起来，减少实时生成压力
4. **监控告警**：监控ID生成性能和唯一性，及时发现问题
5. **配置管理**：不同环境使用不同的机器ID配置，避免冲突

这些示例展示了如何在各个业务模块中有效地使用雪花算法ID生成器，确保分布式系统中ID的全局唯一性和高性能。
