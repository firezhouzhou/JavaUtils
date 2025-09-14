# 雪花算法ID生成器使用指南

## 概述

本项目在 `common-module` 中实现了雪花算法（Snowflake Algorithm）ID生成器，为分布式系统提供全局唯一、递增的64位长整型ID。

## 雪花算法原理

雪花算法生成的64位ID结构如下：

```
0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
|   |-------------------------------------------|   |-----|   |-----|   |----------|
|                    41位时间戳                    |  5位数据中心ID | 5位机器ID |   12位序列号
|
符号位(固定为0)
```

- **1位符号位**：固定为0，保证生成的ID为正数
- **41位时间戳**：精确到毫秒，可使用约69年（2^41 / (1000 * 60 * 60 * 24 * 365) ≈ 69年）
- **5位数据中心ID**：支持32个数据中心（0-31）
- **5位机器ID**：每个数据中心支持32台机器（0-31）
- **12位序列号**：每毫秒可生成4096个ID（0-4095）

## 核心组件

### 1. SnowflakeIdGenerator（雪花算法生成器）

位置：`common-module/src/main/java/com/example/common/util/SnowflakeIdGenerator.java`

核心功能：
- 线程安全的ID生成
- 时钟回拨检测和处理
- 自动序列号递增
- 支持高并发场景

### 2. SnowflakeConfig（配置类）

位置：`common-module/src/main/java/com/example/common/config/SnowflakeConfig.java`

配置项：
- `snowflake.datacenter-id`：数据中心ID（默认：1）
- `snowflake.machine-id`：机器ID（默认：1）

### 3. IdUtils（工具类）

位置：`common-module/src/main/java/com/example/common/util/IdUtils.java`

提供静态方法：
- `generateId()`：生成Long类型ID
- `generateIdStr()`：生成String类型ID
- `parseTimestamp(id)`：解析时间戳
- `parseDatacenterId(id)`：解析数据中心ID
- `parseMachineId(id)`：解析机器ID
- `parseSequence(id)`：解析序列号
- `getGeneratorInfo()`：获取生成器信息

### 4. IdController（REST API）

位置：`common-module/src/main/java/com/example/common/controller/IdController.java`

提供HTTP接口：
- `GET /common/id/generate`：生成单个ID
- `GET /common/id/generate/string`：生成字符串格式ID
- `GET /common/id/generate/batch`：批量生成ID
- `GET /common/id/parse/{id}`：解析ID
- `POST /common/id/parse/batch`：批量解析ID
- `GET /common/id/info`：获取生成器信息
- `GET /common/id/health`：健康检查

## 使用方法

### 1. 在其他模块中使用

#### 添加依赖

确保在模块的 `pom.xml` 中依赖 `common-module`：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>common-module</artifactId>
</dependency>
```

#### 启用组件扫描

在主应用类中添加组件扫描：

```java
@SpringBootApplication(scanBasePackages = {"com.example.yourmodule", "com.example.common"})
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

#### 代码示例

```java
import com.example.common.util.IdUtils;

@Service
public class UserService {
    
    public User createUser(String username) {
        User user = new User();
        user.setId(IdUtils.generateId());  // 生成唯一ID
        user.setUsername(username);
        return userRepository.save(user);
    }
    
    public void logUserAction(Long userId, String action) {
        AccessLog log = new AccessLog();
        log.setId(IdUtils.generateId());  // 生成日志ID
        log.setUserId(userId);
        log.setAction(action);
        log.setTimestamp(System.currentTimeMillis());
        logRepository.save(log);
    }
}
```

### 2. 配置项

在 `application.yml` 中配置：

```yaml
# 雪花算法配置
snowflake:
  datacenter-id: 1    # 数据中心ID (0-31)
  machine-id: 1       # 机器ID (0-31)
```

### 3. 注入使用

```java
@Service
public class OrderService {
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    
    public Order createOrder() {
        Order order = new Order();
        order.setId(idGenerator.nextId());  // 直接使用生成器
        order.setOrderNo("ORD" + IdUtils.generateIdStr());  // 生成订单号
        return order;
    }
}
```

## API 接口使用

### 生成ID

```bash
# 生成单个ID
curl http://localhost:8080/common/id/generate

# 响应示例
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1702873234567890944,
    "idStr": "1702873234567890944",
    "timestamp": 1702873234567,
    "generatedAt": "2023-12-18 14:20:34"
  },
  "timestamp": 1702873234567
}
```

### 批量生成ID

```bash
# 批量生成10个ID
curl "http://localhost:8080/common/id/generate/batch?count=10"

# 响应示例
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "count": 10,
    "ids": [1702873234567890944, 1702873234567890945, ...],
    "idStrs": ["1702873234567890944", "1702873234567890945", ...],
    "timestamp": 1702873234567,
    "generatedAt": "2023-12-18 14:20:34"
  }
}
```

### 解析ID

```bash
# 解析单个ID
curl http://localhost:8080/common/id/parse/1702873234567890944

# 响应示例
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "originalId": 1702873234567890944,
    "originalIdStr": "1702873234567890944",
    "timestamp": 1702873234567,
    "generatedTime": "2023-12-18 14:20:34",
    "datacenterId": 1,
    "machineId": 1,
    "sequence": 0,
    "ageMs": 1234,
    "ageSeconds": 1,
    "ageMinutes": 0
  }
}
```

### 健康检查

```bash
# 检查ID生成器健康状态
curl http://localhost:8080/common/id/health

# 响应示例
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "healthy",
    "testCount": 100,
    "durationMs": 15,
    "avgTimePerIdMs": 0.15,
    "idsPerSecond": 6666,
    "uniqueIds": true,
    "increasingOrder": true,
    "firstId": 1702873234567890944,
    "lastId": 1702873234567891043,
    "checkTime": "2023-12-18 14:20:34"
  }
}
```

## 性能特点

### 优势

1. **高性能**：单机每秒可生成400万个ID
2. **全局唯一**：在分布式环境中保证ID唯一性
3. **趋势递增**：生成的ID大致按时间递增
4. **高可用**：无需依赖外部系统
5. **信息可读**：ID中包含时间戳等信息

### 性能指标

- **单机QPS**：400万+/秒
- **ID长度**：64位（8字节）
- **时间精度**：毫秒级
- **使用年限**：约69年
- **并发支持**：线程安全

## 最佳实践

### 1. 部署建议

```yaml
# 不同环境配置示例

# 开发环境
snowflake:
  datacenter-id: 0
  machine-id: 0

# 测试环境  
snowflake:
  datacenter-id: 0
  machine-id: 1

# 生产环境机器1
snowflake:
  datacenter-id: 1
  machine-id: 1

# 生产环境机器2
snowflake:
  datacenter-id: 1
  machine-id: 2
```

### 2. 数据库设计

```sql
-- 推荐的表结构设计
CREATE TABLE user (
    id BIGINT PRIMARY KEY COMMENT '雪花算法ID',
    username VARCHAR(50) NOT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_time (created_time)
);

-- 分表示例（按ID范围）
CREATE TABLE order_202312 (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. 缓存策略

```java
@Service
public class IdCacheService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    // 预生成ID缓存
    public void preGenerateIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(IdUtils.generateIdStr());
        }
        redisTemplate.opsForList().rightPushAll("id_pool", ids);
    }
    
    // 从缓存获取ID
    public Long getIdFromCache() {
        String id = redisTemplate.opsForList().leftPop("id_pool");
        return id != null ? Long.parseLong(id) : IdUtils.generateId();
    }
}
```

## 注意事项

### 1. 时钟同步

- 确保各服务器时钟同步
- 避免系统时钟回拨
- 建议使用NTP服务

### 2. 机器ID分配

- 不同机器必须配置不同的 `datacenter-id` 和 `machine-id`
- 建议建立机器ID分配表
- 避免ID冲突

### 3. 高并发场景

```java
// 高并发下的使用示例
@Component
public class HighConcurrencyIdService {
    
    private final AtomicLong counter = new AtomicLong(0);
    
    @Async
    public CompletableFuture<Long> generateIdAsync() {
        return CompletableFuture.completedFuture(IdUtils.generateId());
    }
    
    // 批量生成避免频繁调用
    public List<Long> batchGenerate(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> IdUtils.generateId())
                .collect(Collectors.toList());
    }
}
```

### 4. 监控告警

```java
@Component
public class SnowflakeMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Counter idGenerateCounter;
    private final Timer idGenerateTimer;
    
    public SnowflakeMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.idGenerateCounter = Counter.builder("snowflake.id.generated")
                .description("Generated snowflake IDs count")
                .register(meterRegistry);
        this.idGenerateTimer = Timer.builder("snowflake.id.generate.time")
                .description("Time taken to generate snowflake ID")
                .register(meterRegistry);
    }
    
    public Long generateIdWithMetrics() {
        return idGenerateTimer.recordCallable(() -> {
            idGenerateCounter.increment();
            return IdUtils.generateId();
        });
    }
}
```

## 故障排查

### 常见问题

1. **ID重复**
   - 检查机器ID配置是否重复
   - 确认时钟是否同步

2. **性能问题**
   - 检查是否频繁单个调用
   - 考虑使用批量生成

3. **时钟回拨**
   - 检查系统日志中的时钟回拨警告
   - 配置NTP服务

### 日志监控

```yaml
# logback-spring.xml 配置
<logger name="com.example.common.util.SnowflakeIdGenerator" level="INFO"/>
```

## 版本历史

- **v1.0.0**：基础雪花算法实现
- **v1.1.0**：添加REST API接口
- **v1.2.0**：增加批量操作和健康检查
- **v1.3.0**：完善监控和错误处理

## 参考资料

- [Twitter Snowflake算法](https://github.com/twitter-archive/snowflake)
- [分布式ID生成方案](https://tech.meituan.com/2017/04/21/mt-leaf.html)
- [Spring Boot配置参考](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
