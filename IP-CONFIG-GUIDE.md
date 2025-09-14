# IP配置统一管理指南

## 概述

本项目已将所有模块的IP地址和端口配置统一收敛到`common-module`中，实现了配置的集中管理。现在只需要在`common-module`的配置文件中修改一处，就能影响所有模块的IP配置。

## 配置结构

### 1. 主配置文件

**文件位置**: `common-module/src/main/resources/application.yml`

```yaml
# 服务器配置 - 统一管理所有IP和端口
server:
  config:
    # 数据库配置
    database:
      host: localhost
      port: 3306
      username: root
      password: 19990626ZYX
    
    # Redis配置
    redis:
      host: localhost
      port: 6379
      password: ""
      database: 0
    
    # 微服务配置
    services:
      host: localhost
      auth:
        port: 8081
      user:
        port: 8082
      file:
        port: 8083
      admin:
        port: 8084
      log:
        port: 8085
    
    # 网关配置
    gateway:
      port: 8080
    
    # 安全配置
    security:
      allowed-ip: 127.0.0.1
      allowed-ips: 127.0.0.1,localhost
```

### 2. 配置类

**文件位置**: `common-module/src/main/java/com/example/common/config/ServerConfig.java`

提供类型安全的配置类，支持配置验证和IDE自动补全。

### 3. 工具类

**文件位置**: `common-module/src/main/java/com/example/common/util/ServerConfigUtils.java`

提供静态方法，方便在代码中获取各种配置信息。

## 各模块配置更新

### 1. Gateway Module

**更新内容**:
- 网关路由配置使用统一的服务URL
- Redis配置使用统一配置
- 服务端口使用统一配置

**配置示例**:
```yaml
spring:
  redis:
    host: ${server.config.redis.host}
    port: ${server.config.redis.port}
    password: ${server.config.redis.password}
    database: ${server.config.redis.database}
  
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://${server.config.services.host}:${server.config.services.auth.port}
        - id: user-service
          uri: http://${server.config.services.host}:${server.config.services.user.port}

server:
  port: ${server.config.gateway.port}
```

### 2. User Module

**更新内容**:
- 数据库连接使用统一配置
- Redis配置使用统一配置
- 服务端口使用统一配置

**配置示例**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://${server.config.database.host}:${server.config.database.port}/user_module?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: ${server.config.database.username}
    password: ${server.config.database.password}
  
  redis:
    host: ${server.config.redis.host}
    port: ${server.config.redis.port}

server:
  port: ${server.config.services.user.port}
```

### 3. Log Module

**更新内容**:
- 数据库连接使用统一配置
- 服务端口使用统一配置

**配置示例**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://${server.config.database.host}:${server.config.database.port}/log_module?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: ${server.config.database.username}
    password: ${server.config.database.password}

server:
  port: ${server.config.services.log.port}
```

### 4. 其他模块

所有模块（auth-module、admin-module、file-module）都已更新为使用统一的端口配置。

## Java代码中的使用

### 1. 使用配置工具类

```java
import com.example.common.util.ServerConfigUtils;

@Service
public class ExampleService {
    
    public void exampleMethod() {
        // 获取服务URL
        String authServiceUrl = ServerConfigUtils.getAuthServiceUrl();
        String userServiceUrl = ServerConfigUtils.getUserServiceUrl();
        
        // 获取数据库配置
        String dbUrl = ServerConfigUtils.getDatabaseUrl("my_database");
        
        // 获取Redis配置
        String redisHost = ServerConfigUtils.getRedisHost();
        int redisPort = ServerConfigUtils.getRedisPort();
        
        // 生成模拟IP（用于测试）
        String mockIp = ServerConfigUtils.generateMockIp(1);
    }
}
```

### 2. 注入配置类

```java
import com.example.common.config.ServerConfig;

@Service
public class ExampleService {
    
    @Autowired
    private ServerConfig serverConfig;
    
    public void exampleMethod() {
        // 获取数据库主机
        String dbHost = serverConfig.getDatabase().getHost();
        
        // 获取服务端口
        int userServicePort = serverConfig.getServices().getUser().getPort();
        
        // 获取安全配置
        String allowedIp = serverConfig.getSecurity().getAllowedIp();
    }
}
```

## 环境配置

### 1. 开发环境

**文件位置**: `common-module/src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://${server.config.database.host}:${server.config.database.port}/multi_module_dev?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: ${server.config.database.username}
    password: ${server.config.database.password}
  
  redis:
    host: ${server.config.redis.host}
    port: ${server.config.redis.port}
    password: ${server.config.redis.password}
    database: ${server.config.redis.database}
```

### 2. 生产环境配置示例

```yaml
server:
  config:
    # 生产环境数据库配置
    database:
      host: 192.168.1.100
      port: 3306
      username: prod_user
      password: secure_password
    
    # 生产环境Redis配置
    redis:
      host: 192.168.1.101
      port: 6379
      password: redis_password
      database: 0
    
    # 生产环境服务配置
    services:
      host: 192.168.1.10
      auth:
        port: 8081
      user:
        port: 8082
      file:
        port: 8083
      admin:
        port: 8084
      log:
        port: 8085
    
    # 生产环境安全配置
    security:
      allowed-ip: 192.168.1.0/24
      allowed-ips: 192.168.1.0/24,10.0.0.0/8
```

## 修改配置的步骤

### 1. 修改服务器IP地址

**场景**: 将数据库服务器从`localhost`改为`192.168.1.100`

**步骤**:
1. 修改`common-module/src/main/resources/application.yml`:
   ```yaml
   server:
     config:
       database:
         host: 192.168.1.100  # 修改这里
   ```

2. 重新编译并安装common-module:
   ```bash
   cd common-module
   mvn clean install
   ```

3. 重启所有服务

### 2. 修改服务端口

**场景**: 将用户服务端口从8082改为9082

**步骤**:
1. 修改`common-module/src/main/resources/application.yml`:
   ```yaml
   server:
     config:
       services:
         user:
           port: 9082  # 修改这里
   ```

2. 重新编译并安装common-module
3. 重启所有服务

### 3. 添加新的服务

**步骤**:
1. 在`ServerConfig.java`中添加新的服务配置类
2. 在`ServerConfigUtils.java`中添加获取新服务URL的方法
3. 在配置文件中添加新服务的端口配置
4. 更新其他模块的配置文件引用

## 优势

### 1. 集中管理
- 所有IP和端口配置在一个地方
- 避免配置不一致的问题
- 便于维护和更新

### 2. 环境隔离
- 不同环境使用不同的配置文件
- 支持开发、测试、生产环境配置分离
- 避免环境配置混乱

### 3. 类型安全
- 使用强类型配置类
- IDE自动补全支持
- 编译时类型检查

### 4. 代码复用
- 统一的配置工具类
- 避免重复的配置代码
- 便于扩展和维护

## 注意事项

### 1. 依赖关系
- 其他模块必须依赖`common-module`
- 确保common-module已安装到本地仓库
- 更新common-module后需要重新编译其他模块

### 2. 配置覆盖
- 模块级别的配置可以覆盖通用配置
- 使用`${server.config.xxx:default_value}`语法提供默认值
- 确保配置的优先级正确

### 3. 启动顺序
- 确保common-module先于其他模块编译
- 使用`mvn clean install`安装到本地仓库
- 其他模块启动时会自动加载配置

## 故障排查

### 1. 配置不生效
- 检查配置文件语法是否正确
- 确认common-module已正确安装
- 验证配置路径是否正确

### 2. 服务无法启动
- 检查端口是否被占用
- 验证数据库连接配置
- 查看启动日志中的错误信息

### 3. 服务间通信失败
- 检查服务URL配置是否正确
- 验证网络连通性
- 确认防火墙设置

## 总结

通过IP配置的统一管理，项目实现了：

✅ **配置集中化**: 所有IP和端口配置在一个地方  
✅ **维护简化**: 修改一处配置影响所有模块  
✅ **环境隔离**: 支持不同环境的配置分离  
✅ **类型安全**: 强类型配置类避免配置错误  
✅ **代码复用**: 统一的配置工具类  

现在您只需要在`common-module/src/main/resources/application.yml`中修改配置，就能统一管理所有模块的IP地址和端口配置！
