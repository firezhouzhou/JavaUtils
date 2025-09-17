# Spring Security 统一配置说明

## 概述

本项目已配置了统一的 Spring Security 配置，所有模块都可以共享相同的用户账号和安全设置，避免重复配置。

## 配置文件

### 1. 统一配置文件
- **位置**: `common-module/src/main/resources/shared-config.yml`
- **作用**: 定义所有模块共享的 Spring Security 配置

### 2. 配置类
- **SecurityProperties**: `common-module/src/main/java/com/example/common/config/SecurityProperties.java`
- **BaseSecurityConfig**: `common-module/src/main/java/com/example/common/config/BaseSecurityConfig.java`

## 默认用户账号

系统预配置了以下用户账号：

| 用户名 | 密码 | 角色 | 权限说明 |
|--------|------|------|----------|
| admin | admin123 | ADMIN | 管理员权限，可访问所有接口 |
| user | user123 | USER | 普通用户权限 |
| developer | dev123 | DEVELOPER | 开发者权限 |

## 无需认证的路径

以下路径无需认证即可访问：

- `/swagger-ui/**` - Swagger UI 界面
- `/v3/api-docs/**` - API 文档
- `/swagger-resources/**` - Swagger 资源
- `/webjars/**` - Web 资源
- `/druid/**` - Druid 监控
- `/actuator/**` - Spring Boot Actuator
- `/auth/login` - 登录接口
- `/auth/register` - 注册接口
- `/user/check-username` - 用户名检查
- `/user/check-email` - 邮箱检查
- `/common/id/**` - ID 生成服务
- `/common/security/public` - 公开测试接口

## 使用方法

### 1. 基础使用（推荐）
对于大多数模块，只需要在 `application.yml` 中导入共享配置即可：

```yaml
spring:
  config:
    import:
      - "classpath:shared-config.yml"
```

### 2. 自定义配置
如果模块需要特殊的安全配置（如 JWT 认证），可以创建自己的 SecurityConfig：

```java
@Configuration
@EnableWebSecurity
@Order(1) // 设置优先级
public class CustomSecurityConfig {
    
    @Autowired
    private SecurityProperties securityProperties;
    
    @Bean
    @Primary
    public SecurityFilterChain customFilterChain(HttpSecurity http) throws Exception {
        // 获取统一配置的无需认证路径
        List<String> permitAllPaths = securityProperties.getPermitAllPaths();
        String[] pathArray = permitAllPaths.toArray(new String[0]);
        
        return http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(pathArray).permitAll()
                // 添加模块特定的配置
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

## 测试接口

系统提供了测试接口来验证 Security 配置：

- `GET /common/security/public` - 公开接口，无需认证
- `GET /common/security/authenticated` - 需要认证的接口
- `GET /common/security/admin` - 需要 ADMIN 角色
- `GET /common/security/user` - 需要 USER 或 ADMIN 角色
- `GET /common/security/developer` - 需要 DEVELOPER 角色

## 认证方式

### 1. HTTP Basic 认证
使用用户名和密码进行 Basic 认证：

```bash
curl -u admin:admin123 http://localhost:8082/common/security/authenticated
```

### 2. JWT 认证（auth-module 和 file-module）
这些模块支持 JWT 认证，需要先登录获取 token：

```bash
# 1. 登录获取 token
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 使用 token 访问接口
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/common/security/authenticated
```

## 自定义配置

### 1. 修改用户账号
在 `shared-config.yml` 中修改用户配置：

```yaml
app:
  security:
    users:
      - username: custom_admin
        password: custom_password
        role: ADMIN
```

### 2. 添加无需认证的路径
在 `shared-config.yml` 中添加路径：

```yaml
app:
  security:
    permit-all-paths:
      - "/your/public/path/**"
```

### 3. 禁用统一配置
如果某个模块不需要使用统一配置：

```yaml
app:
  security:
    enabled: false
```

## 注意事项

1. **优先级**: 模块特定的 SecurityConfig 应该设置 `@Order` 注解来控制优先级
2. **Bean 冲突**: 使用 `@Primary` 注解来解决 Bean 冲突
3. **密码加密**: 系统使用 BCrypt 加密密码
4. **配置更新**: 修改配置后需要重启应用才能生效

## 故障排除

### 1. 认证失败
- 检查用户名和密码是否正确
- 确认用户角色是否有权限访问接口

### 2. 配置不生效
- 确认 `shared-config.yml` 已被正确导入
- 检查 `@Order` 注解的优先级设置

### 3. Bean 冲突
- 使用 `@Primary` 注解标记主要的 Bean
- 检查是否有重复的 Bean 定义

## 示例项目结构

```
JavaUtils/
├── common-module/
│   ├── src/main/java/com/example/common/config/
│   │   ├── SecurityProperties.java
│   │   ├── BaseSecurityConfig.java
│   │   └── SecurityConfigurationInfo.java
│   └── src/main/resources/
│       └── shared-config.yml
├── auth-module/
│   └── src/main/java/com/example/auth/config/
│       └── SecurityConfig.java (自定义JWT配置)
├── file-module/
│   └── src/main/java/com/example/file/config/
│       └── FileSecurityConfig.java (自定义文件服务配置)
└── user-module/ (使用基础配置)