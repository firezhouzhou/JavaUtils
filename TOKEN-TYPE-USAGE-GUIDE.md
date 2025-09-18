# 🔄 Token类型使用指南

## 🚨 常见问题：Token类型混用

### 问题描述

用户遇到的错误：
```json
{
  "code": 400,
  "message": "刷新token失败: Token已失效",
  "timestamp": 1758127250348
}
```

### 问题分析

用户的请求：
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "REFRESH_TOKEN"}'
```

**问题根源**：
1. **Authorization头**：包含的是 **access token** (用于普通API访问)
2. **请求体**：包含的是 **refresh token** (用于刷新token)
3. **优先级逻辑**：系统优先使用Authorization头中的token
4. **结果**：系统尝试用access token来刷新，导致失败

## 🎯 Token类型说明

### 1. Access Token (访问令牌)
```json
{
  "userId": 1,
  "sub": "admin",
  "iat": 1758123569,
  "exp": 1758209969
}
```

**特点**：
- ✅ 用于访问受保护的API接口
- ✅ 有效期短（通常15分钟-24小时）
- ✅ 用于日常API调用的认证
- ❌ **不能用于刷新token**

### 2. Refresh Token (刷新令牌)
```json
{
  "type": "refresh",
  "userId": 3,
  "sub": "jerry",
  "iat": 1758127151,
  "exp": 1758731951
}
```

**特点**：
- ✅ 用于获取新的access token
- ✅ 有效期长（通常7天-30天）
- ✅ 包含`"type": "refresh"`标识
- ❌ **不能用于普通API调用**

## ✅ 正确的使用方法

### 1. 普通API调用
```bash
# ✅ 使用access token
curl -X GET "http://localhost:8081/auth/users" \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

### 2. 刷新token
有两种正确方式：

#### **方式1：通过Authorization头（推荐）**
```bash
# ✅ 在Authorization头中使用refresh token
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer REFRESH_TOKEN"
```

#### **方式2：通过请求体**
```bash
# ✅ 在请求体中使用refresh token
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "REFRESH_TOKEN"}'
```

## 🚫 错误的使用方法

### ❌ Token类型混用
```bash
# ❌ 错误：在Authorization头中使用access token来刷新
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "REFRESH_TOKEN"}'
```

**问题**：系统会优先使用Authorization头中的access token，导致刷新失败。

### ❌ 使用access token刷新
```bash
# ❌ 错误：尝试用access token刷新
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

**问题**：Access token不能用于刷新操作。

### ❌ 使用refresh token访问API
```bash
# ❌ 错误：用refresh token访问普通API
curl -X GET "http://localhost:8081/auth/users" \
  -H "Authorization: Bearer REFRESH_TOKEN"
```

**问题**：Refresh token不能用于普通API访问。

## 🔄 完整的认证流程

### 1. 用户登录
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...ACCESS_TOKEN",
    "refreshToken": "eyJ...REFRESH_TOKEN",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "refreshExpiresIn": 604800
  }
}
```

### 2. 使用access token访问API
```bash
curl -X GET "http://localhost:8081/auth/users" \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

### 3. access token即将过期时刷新
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer REFRESH_TOKEN"
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...NEW_ACCESS_TOKEN",
    "refreshToken": "eyJ...NEW_REFRESH_TOKEN",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "refreshExpiresIn": 604800
  }
}
```

### 4. 使用新的access token继续访问
```bash
curl -X GET "http://localhost:8081/auth/users" \
  -H "Authorization: Bearer NEW_ACCESS_TOKEN"
```

## 🛠️ 客户端最佳实践

### JavaScript示例
```javascript
class TokenManager {
  constructor() {
    this.accessToken = localStorage.getItem('accessToken');
    this.refreshToken = localStorage.getItem('refreshToken');
  }
  
  // 普通API调用
  async apiCall(url, options = {}) {
    return fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${this.accessToken}`
      }
    });
  }
  
  // 刷新token
  async refreshTokens() {
    const response = await fetch('/auth/refresh', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.refreshToken}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      const data = await response.json();
      this.accessToken = data.data.accessToken;
      this.refreshToken = data.data.refreshToken;
      
      localStorage.setItem('accessToken', this.accessToken);
      localStorage.setItem('refreshToken', this.refreshToken);
    }
    
    return response;
  }
}
```

### 自动刷新机制
```javascript
// 拦截器：自动处理token过期
async function apiCallWithAutoRefresh(url, options = {}) {
  let response = await tokenManager.apiCall(url, options);
  
  // 如果access token过期
  if (response.status === 401) {
    // 尝试刷新token
    const refreshResponse = await tokenManager.refreshTokens();
    
    if (refreshResponse.ok) {
      // 重新发起原始请求
      response = await tokenManager.apiCall(url, options);
    } else {
      // refresh token也过期，重定向到登录页
      window.location.href = '/login';
    }
  }
  
  return response;
}
```

## 📋 Token识别方法

### 通过JWT payload识别
```bash
# 解码token查看内容
echo "TOKEN" | cut -d'.' -f2 | base64 -d
```

**Access Token特征**：
```json
{
  "userId": 1,
  "sub": "username",
  "iat": 1758123569,
  "exp": 1758209969
}
```

**Refresh Token特征**：
```json
{
  "type": "refresh",  // ← 关键标识
  "userId": 1,
  "sub": "username",
  "iat": 1758123569,
  "exp": 1758731951   // ← 过期时间更长
}
```

## 🔧 故障排除

### 1. "Token已失效"错误
**可能原因**：
- 使用了access token来刷新
- Token已被加入黑名单
- Token已过期
- Token签名无效

**解决方案**：
- 确保使用refresh token刷新
- 检查token是否过期
- 重新登录获取新token

### 2. "用户名或密码错误"
**可能原因**：
- 使用了refresh token访问普通API
- Token类型不匹配

**解决方案**：
- 使用access token访问普通API
- 确保token类型正确

## 🎯 总结

### Token使用原则

| 操作类型 | 使用的Token | 传递方式 |
|---------|------------|---------|
| 普通API访问 | Access Token | `Authorization: Bearer ACCESS_TOKEN` |
| 刷新Token | Refresh Token | `Authorization: Bearer REFRESH_TOKEN` |
| 文件上传 | Access Token | `Authorization: Bearer ACCESS_TOKEN` |
| 用户登录 | 无需Token | 用户名密码 |

### 关键要点

1. **Access Token**：短期有效，用于API访问
2. **Refresh Token**：长期有效，仅用于刷新
3. **不要混用**：刷新时用refresh token，访问时用access token
4. **优先级**：Authorization头 > 请求体
5. **安全性**：定期刷新，及时更新存储的token

**记住：正确的token用于正确的场景！** 🎯
