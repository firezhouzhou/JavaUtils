# 🔐 JWT Token认证使用指南

## 📋 问题说明

访问`/file/upload`时出现403 Forbidden错误，这是因为网关的认证过滤器要求JWT token，但请求中没有提供有效的token。

## 🔑 默认用户账号

系统中预置了一个管理员账号供测试使用：

**用户名**: `admin`  
**密码**: `admin123`

> 💡 **提示**: 这个账号在 `auth-module/src/main/java/com/example/auth/service/CustomUserDetailsService.java` 中硬编码定义。

## 🚀 解决方案

### 方案1：获取JWT Token进行认证访问

#### 1. **通过认证服务获取Token**

首先需要登录获取JWT token：

```bash
# 方法1：直接访问认证服务
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# 方法2：通过网关访问认证服务
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin", 
    "password": "admin123"
  }'
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzI2MzI2MTg0LCJleHAiOjE3MjY0MTI1ODR9.abc123...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInR5cGUiOiJyZWZyZXNoIiwic3ViIjoiYWRtaW4iLCJpYXQiOjE3MjYzMjYxODQsImV4cCI6MTcyNjkzMDk4NH0.def456...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com"
    }
  }
}
```

#### 2. **在Swagger中配置Token**

**方法A：使用Swagger UI的认证功能**
1. 打开Swagger UI页面
2. 点击右上角的 🔓 **Authorize** 按钮
3. 在弹出的对话框中输入：`Bearer eyJhbGciOiJIUzI1NiJ9...`（注意Bearer后面有空格）
4. 点击 **Authorize**
5. 现在所有请求都会自动带上这个token

**方法B：手动在请求头中添加**
在Swagger的请求界面中，找到 **Parameters** 部分，添加Header：
- **Name**: `Authorization`
- **Value**: `Bearer eyJhbGciOiJIUzI1NiJ9...`

#### 3. **使用curl命令测试**

```bash
# 获取token后，使用token访问文件上传接口
curl -X POST "http://localhost:8080/api/file/upload" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/file.txt"
```

### 方案2：将文件接口加入白名单（已实施）

我已经将以下路径加入了认证白名单，现在可以无需token直接访问：
- `/file/upload` - 文件上传
- `/file/download` - 文件下载

## 🔍 当前认证白名单

```java
private static final List<String> EXCLUDED_PATHS = Arrays.asList(
    "/auth/login",           // 登录接口
    "/auth/register",        // 注册接口
    "/auth/refresh",         // 刷新token接口
    "/user/check-username",  // 检查用户名
    "/user/check-email",     // 检查邮箱
    "/file/upload",          // 文件上传接口 ✅ 新增
    "/file/download",        // 文件下载接口 ✅ 新增
    "/swagger-ui",           // Swagger UI
    "/v2/api-docs",          // API文档
    "/swagger-resources",    // Swagger资源
    "/webjars"              // Web资源
);
```

## 🛠️ 测试步骤

### 测试方案1：使用Token认证
```bash
# 1. 登录获取token
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  jq -r '.data.token')

# 2. 使用token上传文件
curl -X POST "http://localhost:8080/api/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.txt"
```

### 测试方案2：直接访问（无需token）
```bash
# 直接上传文件，无需token
curl -X POST "http://localhost:8080/api/file/upload" \
  -F "file=@test.txt"
```

## 📊 Token信息说明

### Token结构
JWT token包含三个部分：
- **Header**: 算法和类型信息
- **Payload**: 用户信息和过期时间
- **Signature**: 签名验证

### Token有效期
- **Access Token**: 24小时（86400秒）
- **Refresh Token**: 7天

### Token刷新
当access token过期时，可以使用refresh token获取新的token：

```bash
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"your-refresh-token"}'
```

## 🚨 安全建议

### 生产环境建议
1. **文件上传应该需要认证** - 建议在生产环境中移除文件接口的白名单配置
2. **文件访问权限控制** - 实现基于用户的文件访问权限
3. **文件大小限制** - 设置合理的文件上传大小限制
4. **文件类型验证** - 验证上传文件的类型和内容

### 开发环境配置
当前配置适合开发和测试环境，方便调试和测试文件上传功能。

## 🔧 故障排除

### 常见错误

#### 1. 401 Unauthorized
- **原因**: Token无效或已过期
- **解决**: 重新登录获取新token

#### 2. 403 Forbidden  
- **原因**: Token有效但权限不足，或路径不在白名单中
- **解决**: 检查用户权限或添加路径到白名单

#### 3. Token格式错误
- **正确格式**: `Bearer eyJhbGciOiJIUzI1NiJ9...`
- **注意**: Bearer后面必须有空格

### 调试命令
```bash
# 检查token是否有效
curl -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer your-token"

# 查看网关日志
tail -f logs/app-dev.log | grep -i auth
```

现在你可以选择使用哪种方案来解决文件上传的认证问题！🚀
