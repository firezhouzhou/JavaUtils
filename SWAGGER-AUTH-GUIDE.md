# 🔐 Swagger认证使用指南

## 📝 概述

现在Swagger支持更灵活的JWT认证方式，你可以直接填入token值，无需手动添加"Bearer "前缀。

## 🚀 使用方法

### 方法1：在Swagger UI界面中认证（推荐）

1. **打开Swagger界面**
   ```
   http://localhost:8083/swagger-ui/index.html
   ```

2. **点击认证按钮**
   - 在页面右上角找到 🔒 **Authorize** 按钮
   - 点击打开认证对话框

3. **填入JWT Token**
   - 在 **Value** 字段中直接粘贴你的JWT token
   - **无需添加 "Bearer " 前缀**
   - 例如：直接填入 `eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU3OTUyMDYzLCJleHAiOjE3NTgwMzg0NjN9.e48KzKCe21olHBjyM8DLqMsBc89wkPpX63GFK4bsaAU`

4. **点击Authorize**
   - 认证成功后，所有API请求都会自动携带认证信息

### 方法2：直接使用curl命令

现在支持两种curl方式：

#### 方式1：直接填入token（新方式）
```bash
curl -X POST "http://localhost:8083/file/upload" \
  -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU3OTUyMDYzLCJleHAiOjE3NTgwMzg0NjN9.e48KzKCe21olHBjyM8DLqMsBc89wkPpX63GFK4bsaAU" \
  -F "file=@test.png"
```

#### 方式2：带Bearer前缀（传统方式）
```bash
curl -X POST "http://localhost:8083/file/upload" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU3OTUyMDYzLCJleHAiOjE3NTgwMzg0NjN9.e48KzKCe21olHBjyM8DLqMsBc89wkPpX63GFK4bsaAU" \
  -F "file=@test.png"
```

#### 方式3：使用JWT头（兼容方式）
```bash
curl -X POST "http://localhost:8083/file/upload" \
  -H "JWT: eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzU3OTUyMDYzLCJleHAiOjE3NTgwMzg0NjN9.e48KzKCe21olHBjyM8DLqMsBc89wkPpX63GFK4bsaAU" \
  -F "file=@test.png"
```

## 🔑 获取JWT Token

### 方法1：通过认证服务直接获取
```bash
curl -X POST "http://localhost:8081/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 方法2：通过网关获取
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 默认用户账号
- **用户名**: `admin`
- **密码**: `admin123`

## ✅ 支持的认证方式

| 方式 | 头部名称 | 格式 | 示例 |
|------|----------|------|------|
| 标准Bearer | Authorization | `Bearer <token>` | `Authorization: Bearer eyJ...` |
| 直接Token | Authorization | `<token>` | `Authorization: eyJ...` |
| JWT头 | JWT | `<token>` | `JWT: eyJ...` |

## 🛠️ 技术实现

系统的JWT认证过滤器会自动处理以下情况：

1. **自动识别Bearer前缀**：如果token包含"Bearer "前缀，自动去除
2. **直接处理token**：如果token没有前缀，直接使用
3. **多头支持**：同时支持Authorization和JWT头
4. **优先级**：Authorization头优先级高于JWT头

## 🎯 使用建议

- **Swagger界面**：推荐直接填入token值（无Bearer前缀）
- **API调用**：推荐使用标准的Bearer方式
- **测试调试**：可以使用JWT头方式

## 🔧 故障排除

### 问题1：403 Forbidden
- **原因**：token无效或已过期
- **解决**：重新获取新的token

### 问题2：认证失败
- **原因**：token格式不正确
- **解决**：确保token完整且没有额外字符

### 问题3：文件上传失败
- **原因**：可能是文件大小超限
- **解决**：检查文件大小是否超过100MB限制

## 📱 Swagger界面截图说明

1. 点击右上角的 🔒 **Authorize** 按钮
2. 在弹出的对话框中：
   - **Name**: JWT
   - **In**: header  
   - **Value**: 直接粘贴你的token值
3. 点击绿色的 **Authorize** 按钮
4. 关闭对话框，开始使用API

现在你可以更方便地在Swagger中进行认证了！🎉
