#!/bin/bash

# 🔐 文件上传测试脚本
# 使用默认管理员账号获取token并测试文件上传

echo "🚀 开始测试文件上传功能..."
echo ""

# 默认用户信息
USERNAME="admin"
PASSWORD="admin123"
GATEWAY_URL="http://localhost:8080"
AUTH_URL="http://localhost:8081"

echo "📋 使用账号信息:"
echo "用户名: $USERNAME"
echo "密码: $PASSWORD"
echo ""

# 方法1: 通过网关登录
echo "🔑 方法1: 通过网关获取Token..."
RESPONSE1=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

echo "登录响应: $RESPONSE1"
echo ""

# 提取token (需要jq工具)
if command -v jq &> /dev/null; then
    TOKEN=$(echo "$RESPONSE1" | jq -r '.data.token // empty')
    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        echo "✅ Token获取成功: ${TOKEN:0:50}..."
        echo ""
        
        # 创建测试文件
        echo "📝 创建测试文件..."
        TEST_FILE="test-upload.txt"
        echo "这是一个测试文件，创建时间: $(date)" > "$TEST_FILE"
        echo "测试文件已创建: $TEST_FILE"
        echo ""
        
        # 测试文件上传 (使用token)
        echo "📤 测试文件上传 (使用Token认证)..."
        UPLOAD_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/file/upload" \
          -H "Authorization: Bearer $TOKEN" \
          -F "file=@$TEST_FILE")
        
        echo "上传响应: $UPLOAD_RESPONSE"
        echo ""
        
        # 清理测试文件
        rm -f "$TEST_FILE"
        echo "🗑️ 测试文件已清理"
        
    else
        echo "❌ Token获取失败，请检查用户名密码或服务状态"
        echo "响应内容: $RESPONSE1"
    fi
else
    echo "⚠️ 未安装jq工具，无法解析JSON响应"
    echo "请安装jq: brew install jq (macOS) 或 apt-get install jq (Ubuntu)"
fi

echo ""
echo "🔄 方法2: 直接访问认证服务..."
RESPONSE2=$(curl -s -X POST "$AUTH_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

echo "直接登录响应: $RESPONSE2"
echo ""

echo "📋 测试完成！"
echo ""
echo "💡 如果遇到问题，请检查："
echo "1. 网关服务是否启动 (端口8080)"
echo "2. 认证服务是否启动 (端口8081)"
echo "3. 文件服务是否启动 (端口8083)"
echo "4. 用户名密码是否正确: admin/admin123"
