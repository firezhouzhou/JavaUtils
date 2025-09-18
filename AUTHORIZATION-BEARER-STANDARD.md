# 🔐 统一认证标准：Authorization Bearer Token

## 📋 认证标准化

为了遵循HTTP认证标准并保持系统一致性，所有JWT认证都统一使用 `Authorization: Bearer <token>` 头。

## 🎯 标准化原因

### 1. **HTTP标准**
- `Authorization` 是HTTP/1.1标准定义的认证头
- `Bearer` 是RFC 6750定义的OAuth 2.0标准
- 符合RESTful API最佳实践

### 2. **工具兼容性**
- Postman、Swagger、curl等工具原生支持
- 各种HTTP客户端库默认支持
- 自动处理认证头的中间件兼容

### 3. **安全性**
- 标准化的认证方式更安全
- 减少自定义头带来的安全风险
- 便于安全审计和监控

## ✅ 当前实现状态

### 认证接口统一

| 接口 | 认证方式 | 状态 |
|------|---------|------|
| `/auth/refresh` | `Authorization: Bearer <token>` | ✅ 已统一 |
| `/auth/logout` | `Authorization: Bearer <token>` | ✅ 已统一 |
| `/file/upload` | `Authorization: Bearer <token>` | ✅ 已统一 |
| Gateway路由 | `Authorization: Bearer <token>` | ✅ 已统一 |

### 过滤器统一

| 模块 | 过滤器 | Token提取方式 | 状态 |
|------|-------|-------------|------|
| auth-module | AuthController | `Authorization` 头 | ✅ 已统一 |
| file-module | JwtAuthenticationFilter | `Authorization` 头 | ✅ 已统一 |
| gateway-module | AuthenticationFilter | `Authorization` 头 | ✅ 已统一 |

## 📝 使用规范

### 1. **标准格式**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIn0.signature
```

### 2. **curl示例**
```bash
# ✅ 正确方式
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer YOUR_TOKEN"

# ✅ 文件上传
curl -X POST "http://localhost:8083/file/upload" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@example.txt"

# ✅ 通过网关访问
curl -X POST "http://localhost:8080/api/file/upload" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@example.txt"
```

### 3. **JavaScript示例**
```javascript
// ✅ 标准方式
const response = await fetch('/auth/refresh', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${refreshToken}`,
    'Content-Type': 'application/json'
  }
});

// ✅ Axios
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
```

### 4. **Swagger配置**
```java
@ApiOperation(value = "刷新Token", notes = "使用Authorization头传递Bearer token")
@PostMapping("/refresh")
public ApiResponse<Map<String, Object>> refresh(
    @RequestHeader("Authorization") String authHeader) {
    // 处理逻辑
}
```

## 🔧 Token提取逻辑

### 统一的Token提取方法

```java
/**
 * 从Authorization头提取JWT token
 */
private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken)) {
        // 如果包含Bearer前缀，则去除前缀
        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 如果没有Bearer前缀，直接返回（适配某些客户端）
        return bearerToken;
    }
    return null;
}
```

### 特性说明

- ✅ **自动处理Bearer前缀**：支持有/无"Bearer "前缀
- ✅ **空值检查**：安全的null和空字符串处理
- ✅ **客户端兼容**：适配不同客户端的实现差异

## 🧪 测试验证

### 1. **认证服务测试**
```bash
# 登录获取token
TOKEN=$(curl -s -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  jq -r '.data.accessToken')

# 使用token刷新
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer $TOKEN"
```

### 2. **文件服务测试**
```bash
# 使用token上传文件
curl -X POST "http://localhost:8083/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.txt"
```

### 3. **网关服务测试**
```bash
# 通过网关访问文件服务
curl -X POST "http://localhost:8080/api/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.txt"
```

## 🚫 不再支持的方式

以下方式已被移除，不再支持：

### ❌ 自定义JWT头
```bash
# ❌ 不再支持
curl -H "JWT: token" 
```

### ❌ 自定义认证头
```bash
# ❌ 不再支持  
curl -H "X-Auth-Token: token"
curl -H "Token: token"
```

## 📊 迁移指南

### 如果你之前使用自定义头

#### **之前（不推荐）**
```bash
curl -H "JWT: your_token"
curl -H "X-Auth-Token: your_token"
```

#### **现在（标准方式）**
```bash
curl -H "Authorization: Bearer your_token"
```

### 客户端代码迁移

#### **JavaScript/前端**
```javascript
// 之前
headers: { 'JWT': token }

// 现在
headers: { 'Authorization': `Bearer ${token}` }
```

#### **Java/Android**
```java
// 之前
request.setHeader("JWT", token);

// 现在
request.setHeader("Authorization", "Bearer " + token);
```

## 🔒 安全最佳实践

### 1. **Token存储**
```javascript
// ✅ 安全存储
localStorage.setItem('accessToken', token);

// ✅ 使用时自动添加Bearer前缀
const authHeader = `Bearer ${localStorage.getItem('accessToken')}`;
```

### 2. **Token传输**
```javascript
// ✅ HTTPS传输
const config = {
  headers: {
    'Authorization': `Bearer ${token}`
  }
};
```

### 3. **Token验证**
```java
// ✅ 服务端验证
if (authHeader != null && authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7);
    // 验证token...
}
```

## 📋 接口文档更新

### Swagger注解标准化

```java
@ApiOperation(value = "接口名称")
@ApiImplicitParam(name = "Authorization", value = "Bearer token", 
                  required = true, dataType = "string", paramType = "header")
@PostMapping("/endpoint")
public ApiResponse<?> endpoint(@RequestHeader("Authorization") String authHeader) {
    // 实现逻辑
}
```

## 🎯 好处总结

### 1. **标准化**
- 遵循HTTP/OAuth 2.0标准
- 与行业最佳实践一致
- 提高API的专业性

### 2. **兼容性**
- 所有HTTP客户端原生支持
- 工具链完美兼容
- 减少集成成本

### 3. **安全性**
- 标准化的安全传输
- 减少自定义实现的风险
- 便于安全审计

### 4. **维护性**
- 代码更简洁
- 逻辑更统一
- 降低维护成本

## 🎉 标准化完成

现在整个系统的JWT认证已完全标准化：

- ✅ **统一使用** `Authorization: Bearer <token>`
- ✅ **移除所有自定义头支持**
- ✅ **兼容有/无Bearer前缀**
- ✅ **符合HTTP/OAuth 2.0标准**
- ✅ **提高系统一致性和专业性**

**所有JWT认证现在都使用标准的Authorization Bearer方式！** 🚀
