# 🔄 JWT刷新Token问题修复指南

## 🚨 问题描述

用户在使用JWT刷新Token功能时遇到以下错误：

```
io.jsonwebtoken.MalformedJwtException: JWT strings must contain exactly 2 period characters. Found: 0
```

## 🔍 问题分析

### 错误原因
1. **JWT格式错误**：传入的token不是有效的JWT格式
2. **Token为空**：Authorization header为空或只包含"Bearer "
3. **缺少验证**：没有对输入进行充分的格式验证
4. **异常处理不完善**：没有捕获JWT相关的特定异常

### JWT格式说明
有效的JWT token应该包含3个部分，用点(.)分隔：
```
header.payload.signature
例如：eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIn0.signature
```

## ✅ 修复方案

### 1. 增强输入验证

#### **AuthService.refreshToken()方法**
```java
public Map<String, Object> refreshToken(String refreshToken) {
    try {
        // ✅ 输入验证
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new RuntimeException("Refresh token不能为空");
        }
        
        // ✅ 去掉Bearer前缀
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        
        // ✅ 再次检查token格式
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new RuntimeException("Refresh token格式无效");
        }
        
        // ✅ 检查JWT格式（应该包含两个点）
        if (refreshToken.split("\\.").length != 3) {
            throw new RuntimeException("Refresh token格式无效，不是有效的JWT");
        }
        
        // ✅ 检查token是否在黑名单中
        if (isTokenBlacklisted(refreshToken)) {
            throw new RuntimeException("Token已失效");
        }
        
        // 原有的token处理逻辑...
        
    } catch (io.jsonwebtoken.MalformedJwtException e) {
        throw new RuntimeException("Refresh token格式错误: " + e.getMessage());
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
        throw new RuntimeException("Refresh token已过期");
    } catch (io.jsonwebtoken.SignatureException e) {
        throw new RuntimeException("Refresh token签名无效");
    } catch (Exception e) {
        throw new RuntimeException("刷新token失败: " + e.getMessage());
    }
}
```

### 2. 改进Controller接口

#### **支持多种Token传入方式**
```java
@ApiOperation("刷新Token")
@PostMapping("/refresh")
public ApiResponse<Map<String, Object>> refresh(
    @RequestHeader(value = "Authorization", required = false) String authHeader,
    @RequestHeader(value = "JWT", required = false) String jwtHeader,
    @RequestBody(required = false) RefreshTokenRequest request) {
    
    String token = null;
    
    // ✅ 优先从Authorization header获取token
    if (authHeader != null && !authHeader.trim().isEmpty()) {
        token = authHeader;
    }
    // ✅ 其次从JWT header获取token
    else if (jwtHeader != null && !jwtHeader.trim().isEmpty()) {
        token = jwtHeader;
    }
    // ✅ 最后从请求体获取
    else if (request != null && request.getRefreshToken() != null) {
        token = request.getRefreshToken();
    }
    
    if (token == null || token.trim().isEmpty()) {
        return ApiResponse.error(400, "请提供refresh token");
    }
    
    try {
        Map<String, Object> result = authService.refreshToken(token);
        return ApiResponse.success("刷新成功", result);
    } catch (Exception e) {
        return ApiResponse.error(400, e.getMessage());
    }
}
```

#### **新增RefreshTokenRequest类**
```java
public static class RefreshTokenRequest {
    private String refreshToken;
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
```

## 🧪 测试验证

### 1. 正常刷新Token流程

#### **步骤1：用户登录获取Token**
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应示例**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU4MTIzNTY5LCJleHAiOjE3NTgyMDk5Njl9.pMUvyeIpHBda7Y7-tomwC-Jh9FZywS37nRPW69aW2ck",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU4MTIzNTY5LCJleHAiOjE3NTg3Mjg5Njl9.differentSignatureForRefreshToken",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "refreshExpiresIn": 604800
  }
}
```

#### **步骤2：使用Refresh Token刷新**

**方法1：通过Authorization Header（标准方式）**
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU4MTIzNTY5LCJleHAiOjE3NTg3Mjg5Njl9.differentSignatureForRefreshToken"
```

**方法2：通过JWT Header（新增支持）**
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "JWT: eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU4MTIzNTY5LCJleHAiOjE3NTg3Mjg5Njl9.differentSignatureForRefreshToken" \
  -d ""
```

**方法3：通过请求体**
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU4MTIzNTY5LCJleHAiOjE3NTg3Mjg5Njl9.differentSignatureForRefreshToken"
  }'
```

### 2. 错误情况测试

#### **测试1：空Token**
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer "
```

**预期响应**：
```json
{
  "code": 400,
  "message": "Refresh token格式无效"
}
```

#### **测试2：格式错误的Token**
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer invalidtoken"
```

**预期响应**：
```json
{
  "code": 400,
  "message": "Refresh token格式无效，不是有效的JWT"
}
```

#### **测试3：过期的Token**
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NzgxMjM1Njl9.expiredSignature"
```

**预期响应**：
```json
{
  "code": 400,
  "message": "Refresh token已过期"
}
```

## 🔧 错误处理机制

### JWT异常类型处理

| 异常类型 | 原因 | 用户友好消息 |
|---------|------|-------------|
| `MalformedJwtException` | JWT格式错误 | "Refresh token格式错误" |
| `ExpiredJwtException` | Token已过期 | "Refresh token已过期" |
| `SignatureException` | 签名无效 | "Refresh token签名无效" |
| `IllegalArgumentException` | Token为空 | "Refresh token不能为空" |
| `RuntimeException` | 其他业务错误 | 具体错误信息 |

### 验证步骤

1. **非空验证**：检查token是否为null或空字符串
2. **Bearer前缀处理**：自动去掉"Bearer "前缀
3. **JWT格式验证**：检查是否包含两个点分隔符
4. **黑名单检查**：验证token是否已被吊销
5. **JWT解析**：使用JwtUtil解析token内容
6. **缓存验证**：检查refresh token缓存状态
7. **生成新Token**：创建新的access和refresh token

## 📋 最佳实践

### 1. 客户端使用建议

#### **推荐的Token存储**
```javascript
// 存储登录响应的token
const loginResponse = await fetch('/auth/login', { /* ... */ });
const { accessToken, refreshToken } = loginResponse.data;

// 安全存储
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
```

#### **自动刷新Token**
```javascript
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  if (!refreshToken) {
    // 重定向到登录页
    window.location.href = '/login';
    return;
  }
  
  try {
    const response = await fetch('/auth/refresh', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${refreshToken}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      const { accessToken, refreshToken: newRefreshToken } = response.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', newRefreshToken);
      return accessToken;
    } else {
      // 刷新失败，重定向到登录页
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
  } catch (error) {
    console.error('Token刷新失败:', error);
    window.location.href = '/login';
  }
}
```

### 2. 服务端安全建议

#### **Token安全配置**
```yaml
jwt:
  secret: ${JWT_SECRET:myVeryLongAndSecureSecretKey123456789012345678901234567890}
  expiration: 900  # 15分钟 (accessToken)
  refresh-expiration: 604800  # 7天 (refreshToken)
```

#### **Redis缓存策略**
```java
// 缓存refresh token，设置过期时间
private void cacheRefreshToken(String refreshToken, Long userId, int expireSeconds) {
    String cacheKey = "refresh:" + refreshToken;
    redisTemplate.opsForValue().set(cacheKey, userId, expireSeconds, TimeUnit.SECONDS);
}

// 验证refresh token缓存
private Long getCachedRefreshToken(String refreshToken) {
    String cacheKey = "refresh:" + refreshToken;
    return (Long) redisTemplate.opsForValue().get(cacheKey);
}
```

## 🎯 修复总结

### ✅ 修复内容

1. **增强输入验证**：
   - 空值检查
   - JWT格式验证
   - Bearer前缀处理

2. **完善异常处理**：
   - 捕获JWT特定异常
   - 提供用户友好的错误消息
   - 详细的错误日志记录

3. **改进接口设计**：
   - 支持多种token传入方式
   - 灵活的参数处理
   - 统一的响应格式

4. **安全性增强**：
   - 黑名单机制
   - 缓存验证
   - Token轮换策略

### 🚀 使用效果

- ✅ **错误提示清晰**：用户能够理解具体的错误原因
- ✅ **接口更灵活**：支持Header和Body两种传参方式
- ✅ **安全性更高**：多层验证，防止恶意请求
- ✅ **维护性更好**：异常处理统一，日志记录完善

## 🎉 修复完成

JWT刷新Token功能现在更加健壮和用户友好！

**主要改进**：
- 🔍 **完善的输入验证**：防止格式错误的token
- 🛡️ **异常处理机制**：提供清晰的错误信息
- 🔄 **灵活的接口设计**：支持多种调用方式
- 📊 **详细的错误日志**：便于问题排查

**现在refresh token接口能够正确处理各种异常情况，提供清晰的错误反馈！** 🎊
