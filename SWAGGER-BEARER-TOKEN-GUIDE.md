# 🔐 Swagger Bearer Token使用指南

## 🚨 问题解决

### 问题描述
在Swagger UI中进行/auth/refresh请求时，调试显示收到的是：
```
Basic YWRtaW46YWRtaW4xMjM=
```
而不是期望的JWT Bearer token。

### 问题原因
这说明在Swagger UI中输入的是**用户名密码**而不是**JWT token**，导致Swagger发送了HTTP Basic认证而不是Bearer认证。

## ✅ 正确的Swagger使用方法

### 1. 重启服务
首先重启auth服务以应用新的Swagger配置：
```bash
cd auth-module
mvn spring-boot:run
```

### 2. 访问Swagger UI
打开浏览器访问：
```
http://localhost:8081/swagger-ui/
```

### 3. 获取JWT Token
首先通过登录接口获取token：

#### 步骤1：找到登录接口
在Swagger UI中找到 `POST /auth/login` 接口

#### 步骤2：执行登录
```json
{
  "username": "admin",
  "password": "admin123"
}
```

#### 步骤3：复制返回的token
从响应中复制 `accessToken` 或 `refreshToken`：
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 4. 配置Bearer认证

#### 步骤1：点击Authorize按钮
在Swagger UI右上角找到 🔒 "Authorize" 按钮并点击

#### 步骤2：输入JWT Token
在弹出的认证对话框中：

**✅ 正确方式**：
- 在 **Bearer** 字段中输入JWT token
- **格式**：直接粘贴token（无需添加"Bearer "前缀）
- **示例**：`eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIn0...`

**❌ 错误方式**：
- 不要在任何字段输入用户名密码
- 不要输入 `admin:admin123`
- 不要选择Basic认证

#### 步骤3：点击Authorize
输入token后点击"Authorize"按钮确认

### 5. 测试刷新接口

现在测试 `POST /auth/refresh` 接口：

#### 对于刷新接口的特殊说明：
- **刷新接口需要refresh token**，不是access token
- 如果你在Authorize中配置的是access token，刷新可能失败
- **解决方案**：在Authorize中输入refresh token，或者通过请求体传递

#### 方式1：通过Authorize配置refresh token
1. 点击Authorize
2. 输入refresh token（不是access token）
3. 测试/auth/refresh接口

#### 方式2：通过请求体传递refresh token
1. Authorize中可以配置access token（用于其他接口）
2. 在/auth/refresh接口的请求体中输入：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCJ9..."
}
```

## 🔧 修复后的Swagger配置

### 新的配置特点：
```java
// 明确指定Bearer认证
private SecurityScheme apiKey() {
    return new ApiKey("Bearer", "Authorization", "header");
}

// 引用名称统一
private List<SecurityReference> defaultAuth() {
    return Arrays.asList(new SecurityReference("Bearer", authorizationScopes));
}
```

### 用户指南更新：
```
🔐 Bearer Token认证说明：
1. 点击右上角 '🔒 Authorize' 按钮
2. 在Bearer字段中输入JWT token
3. 格式：直接输入token（无需Bearer前缀）
4. 示例：eyJhbGciOiJIUzI1NiJ9...
5. 点击Authorize确认

⚠️ 注意：请勿输入用户名密码，只输入JWT token
```

## 🎯 验证方法

### 1. 检查生成的curl命令
配置正确后，Swagger生成的curl命令应该是：
```bash
curl -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "accept: */*"
```

而不是：
```bash
curl -u admin:admin123 "http://localhost:8081/auth/refresh"
```

### 2. 调试验证
在调试时，`bearerToken` 应该显示：
```
Bearer eyJhbGciOiJIUzI1NiJ9...
```

而不是：
```
Basic YWRtaW46YWRtaW4xMjM=
```

## 📋 常见问题

### Q1: 为什么还是收到Basic认证？
**A**: 可能原因：
1. 服务没有重启，仍使用旧配置
2. 在Swagger中输入了用户名密码而不是JWT token
3. 浏览器缓存了旧的Swagger配置

**解决方案**：
1. 重启auth服务
2. 刷新浏览器页面
3. 确保只输入JWT token

### Q2: 刷新token时提示"Token已失效"？
**A**: 可能原因：
1. 使用了access token而不是refresh token
2. refresh token确实已过期
3. token被加入黑名单

**解决方案**：
1. 确保使用refresh token进行刷新
2. 重新登录获取新的token
3. 检查token的过期时间

### Q3: 如何区分access token和refresh token？
**A**: 解码JWT payload查看：
- **Access token**: 没有`type`字段
- **Refresh token**: 包含`"type": "refresh"`字段

## 🎊 修复完成

重启服务后，Swagger UI现在会：

1. ✅ **显示Bearer认证字段**
2. ✅ **生成正确的Authorization头**
3. ✅ **发送Bearer JWT token**
4. ✅ **与后端认证逻辑完美配合**

**记住：在Swagger中只输入JWT token，不要输入用户名密码！** 🔑
