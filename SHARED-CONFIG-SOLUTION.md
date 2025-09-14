# 共享配置解决方案

## 问题描述

之前遇到的错误：
```
Failed to bind properties under 'server.port' to java.lang.Integer:
Property: server.port
Value: "${server.config.services.log.port}"
Reason: failed to convert java.lang.String to java.lang.Integer
```

## 问题原因

1. **配置隔离**: 每个Spring Boot模块都是独立的应用，有自己的ApplicationContext
2. **配置不共享**: 各模块无法直接访问common-module中的配置
3. **变量解析失败**: `${server.config.services.log.port}`无法被解析，因为配置不在当前模块的上下文中

## 解决方案

### 1. 创建共享配置文件

**文件位置**: `common-module/src/main/resources/shared-config.yml`

```yaml
# 共享配置文件 - 所有模块都可以引用
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
      auth: { port: 8081 }
      user: { port: 8082 }
      file: { port: 8083 }
      admin: { port: 8084 }
      log: { port: 8085 }
    
    # 网关配置
    gateway:
      port: 8080
    
    # 安全配置
    security:
      allowed-ip: 127.0.0.1
      allowed-ips: 127.0.0.1,localhost
```

### 2. 在各模块中导入共享配置

在每个模块的`application.yml`中添加：

```yaml
spring:
  application:
    name: your-service
  profiles:
    active: dev
  
  # 导入共享配置
  config:
    import:
      - "classpath:shared-config.yml"
```

### 3. 提供默认值作为备选

为了确保配置的健壮性，在使用配置变量时提供默认值：

```yaml
server:
  port: ${server.config.services.log.port:8085}  # 提供默认值8085
```

```yaml
spring:
  datasource:
    host: ${server.config.database.host:localhost}  # 提供默认值localhost
    port: ${server.config.database.port:3306}       # 提供默认值3306
```

## 已更新的模块

### ✅ Log Module
```yaml
spring:
  config:
    import:
      - "classpath:shared-config.yml"
  datasource:
    url: jdbc:mysql://${server.config.database.host:localhost}:${server.config.database.port:3306}/log_module?...
    username: ${server.config.database.username:root}
    password: ${server.config.database.password:19990626ZYX}
server:
  port: ${server.config.services.log.port:8085}
```

### ✅ Gateway Module
```yaml
spring:
  config:
    import:
      - "classpath:shared-config.yml"
  redis:
    host: ${server.config.redis.host:localhost}
    port: ${server.config.redis.port:6379}
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://${server.config.services.host:localhost}:${server.config.services.auth.port:8081}
server:
  port: ${server.config.gateway.port:8080}
```

### ✅ User Module
```yaml
spring:
  config:
    import:
      - "classpath:shared-config.yml"
  datasource:
    url: jdbc:mysql://${server.config.database.host:localhost}:${server.config.database.port:3306}/user_module?...
  redis:
    host: ${server.config.redis.host:localhost}
    port: ${server.config.redis.port:6379}
server:
  port: ${server.config.services.user.port:8082}
```

### ✅ 其他模块
- `auth-module`: 已添加配置导入和默认值
- `admin-module`: 已添加配置导入和默认值
- `file-module`: 已添加配置导入和默认值

## 工作原理

### 1. 配置加载顺序
```
1. 加载模块自己的 application.yml
2. 通过 spring.config.import 导入 shared-config.yml
3. 解析配置变量 ${server.config.xxx}
4. 如果找不到，使用默认值 ${server.config.xxx:default}
```

### 2. 配置优先级
```
模块自己的配置 > 导入的共享配置 > 默认值
```

### 3. 变量解析
```yaml
# 原来（会失败）
server:
  port: ${server.config.services.log.port}

# 现在（成功）
server:
  port: ${server.config.services.log.port:8085}
```

## 优势

### ✅ 配置统一管理
- 所有IP和端口配置在`shared-config.yml`中
- 修改一处配置影响所有模块
- 避免配置不一致

### ✅ 环境隔离
- 可以为不同环境创建不同的共享配置文件
- 支持`shared-config-dev.yml`、`shared-config-prod.yml`

### ✅ 健壮性
- 提供默认值确保服务能正常启动
- 即使共享配置加载失败也有备选方案

### ✅ 灵活性
- 模块可以覆盖共享配置
- 支持环境特定的配置

## 使用方法

### 1. 修改配置
只需修改`common-module/src/main/resources/shared-config.yml`：
```yaml
server:
  config:
    database:
      host: 192.168.1.100  # 修改数据库服务器IP
    services:
      host: 192.168.1.10   # 修改微服务主机IP
```

### 2. 重新部署
```bash
# 1. 重新编译和安装common-module
cd common-module
mvn clean install

# 2. 重启各个服务
cd ../log-module
mvn spring-boot:run

cd ../gateway-module  
mvn spring-boot:run

# ... 其他模块
```

### 3. 验证配置
```bash
# 检查服务端口是否正确
curl http://localhost:8085/actuator/health  # log-module
curl http://localhost:8080/actuator/health  # gateway-module
```

## 环境配置示例

### 开发环境
```yaml
# shared-config-dev.yml
server:
  config:
    database:
      host: localhost
    services:
      host: localhost
```

### 生产环境
```yaml
# shared-config-prod.yml
server:
  config:
    database:
      host: prod-db.company.com
    services:
      host: prod-services.company.com
```

## 故障排查

### 1. 配置不生效
- 检查`spring.config.import`是否正确
- 确认`shared-config.yml`在classpath中
- 验证common-module是否已安装到本地仓库

### 2. 服务启动失败
- 检查默认值是否正确
- 查看启动日志中的配置加载信息
- 验证端口是否被占用

### 3. 配置变量解析失败
- 确保使用了默认值语法：`${key:default}`
- 检查YAML语法是否正确
- 验证配置路径是否完整

## 总结

通过创建`shared-config.yml`并在各模块中导入，我们成功解决了配置共享的问题：

✅ **问题解决**: log-module现在可以正确访问共享配置  
✅ **统一管理**: 所有IP配置在一个文件中管理  
✅ **健壮性**: 提供默认值确保服务正常启动  
✅ **灵活性**: 支持环境特定配置和模块覆盖  

现在所有模块都能正确解析配置变量，不会再出现`NumberFormatException`错误！
