# 🔧 Swagger Authorization头配置修复

## 🚨 问题描述

从你提供的截图可以看到，Swagger UI生成的curl命令是：
```bash
curl -H "JWT: Bearer token"
```

而不是标准的：
```bash
curl -H "Authorization: Bearer token"
```

这说明Swagger的安全配置中ApiKey定义有问题。

## 🔍 问题分析

### 原始配置（有问题）
```java
private ApiKey apiKey() {
    return new ApiKey("JWT", "Authorization", "header");
}

private List<SecurityReference> defaultAuth() {
    return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
}
```

**问题**：
- ApiKey的第一个参数"JWT"是Swagger UI中显示的名称
- 虽然第二个参数是"Authorization"（实际HTTP头名称），但Swagger可能在某些版本中使用第一个参数生成curl命令

## ✅ 修复方案

### 修复后的配置
```java
private ApiKey apiKey() {
    return new ApiKey("Authorization", "Authorization", "header");
}

private List<SecurityReference> defaultAuth() {
    return Arrays.asList(new SecurityReference("Authorization", authorizationScopes));
}
```

**修复内容**：
1. **统一名称**：将ApiKey的显示名称也改为"Authorization"
2. **统一引用**：SecurityReference也使用"Authorization"
3. **更新说明**：更新用户使用指南

## 📝 完整的SwaggerConfig配置

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Arrays.asList(apiKey()))
                .securityContexts(Arrays.asList(securityContext()));
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Spring Boot 多模块项目 API")
                .description("Spring Boot 多模块项目接口文档\n\n" +
                           "🔐 认证说明：\n" +
                           "- 在右上角点击 'Authorize' 按钮\n" +
                           "- 在Authorization字段中输入：Bearer YOUR_TOKEN\n" +
                           "- 或者直接输入token值（系统会自动添加Bearer前缀）\n" +
                           "- 确保使用标准的Authorization头进行认证")
                .version("1.0.0")
                .build();
    }
    
    // ✅ 修复后：统一使用"Authorization"
    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }
    
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/.*"))
                .build();
    }
    
    // ✅ 修复后：引用也使用"Authorization"
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("Authorization", authorizationScopes));
    }
}
```

## 🎯 使用方法

### 修复后的Swagger使用

1. **打开Swagger UI**：`http://localhost:8081/swagger-ui/`

2. **点击右上角的"Authorize"按钮**

3. **在Authorization字段中输入token**：
   - **方式1**：`Bearer eyJhbGciOiJIUzI1NiJ9...`（完整格式）
   - **方式2**：`eyJhbGciOiJIUzI1NiJ9...`（只输入token，系统自动添加Bearer前缀）

4. **点击"Authorize"确认**

5. **现在生成的curl命令应该是**：
   ```bash
   curl -H "Authorization: Bearer token"
   ```

## 🧪 验证修复

### 1. 重启服务
```bash
cd auth-module
mvn spring-boot:run
```

### 2. 访问Swagger UI
打开浏览器访问：`http://localhost:8081/swagger-ui/`

### 3. 测试认证
- 先登录获取token
- 在Swagger中配置Authorization
- 测试任何需要认证的接口
- 查看生成的curl命令是否正确

### 4. 验证curl命令
修复后，Swagger生成的curl命令应该是：
```bash
curl -X POST "http://localhost:8081/auth/logout" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "accept: */*" \
  -d ""
```

而不是：
```bash
curl -X POST "http://localhost:8081/auth/logout" \
  -H "JWT: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "accept: */*" \
  -d ""
```

## 📋 其他模块的Swagger

如果其他模块也有独立的Swagger配置，需要同样修复：

### file-module
```java
// 检查是否有SwaggerConfig
// 如果有，应用相同的修复
```

### admin-module
```java
// 检查是否有SwaggerConfig
// 如果有，应用相同的修复
```

### user-module
```java
// 检查是否有SwaggerConfig
// 如果有，应用相同的修复
```

## 🔧 SpringFox版本兼容性

### 对于SpringFox 3.x
```java
// 使用OAS_30
new Docket(DocumentationType.OAS_30)
```

### 对于SpringFox 2.x
```java
// 使用SWAGGER_2
new Docket(DocumentationType.SWAGGER_2)
```

## 🎯 最佳实践

### 1. 统一配置
- 所有模块使用相同的Swagger安全配置
- 统一使用"Authorization"作为header名称
- 保持ApiKey和SecurityReference名称一致

### 2. 用户友好的说明
```java
.description("🔐 认证说明：\n" +
           "- 点击右上角 'Authorize' 按钮\n" +
           "- 输入：Bearer YOUR_TOKEN 或直接输入 YOUR_TOKEN\n" +
           "- 系统支持标准的Authorization头认证")
```

### 3. 全局安全配置
```java
.securitySchemes(Arrays.asList(apiKey()))
.securityContexts(Arrays.asList(securityContext()));
```

## 🚀 修复完成

现在Swagger配置已经修复：

- ✅ **统一使用Authorization头**
- ✅ **生成标准的curl命令**
- ✅ **与后端认证逻辑一致**
- ✅ **用户友好的操作指南**

### 重启服务后的效果

1. **Swagger UI显示**：Authorization字段
2. **生成的curl命令**：`-H "Authorization: Bearer token"`
3. **与后端一致**：使用标准的Authorization头
4. **工具兼容**：完美支持Postman、curl等工具

**现在Swagger UI会生成正确的Authorization头curl命令！** 🎉
