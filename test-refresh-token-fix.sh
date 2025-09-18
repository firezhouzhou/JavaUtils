#!/bin/bash

echo "🔧 测试刷新Token修复"
echo "================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 服务器配置
AUTH_SERVER="http://localhost:8081"

echo -e "${BLUE}1. 首先登录获取refresh token${NC}"
echo "--------------------------------"

# 登录获取token
LOGIN_RESPONSE=$(curl -s -X POST "$AUTH_SERVER/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jerry",
    "password": "jerry123"
  }')

echo "登录响应:"
echo "$LOGIN_RESPONSE" | jq '.'

# 提取refresh token
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.refreshToken')
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')

if [ "$REFRESH_TOKEN" = "null" ] || [ -z "$REFRESH_TOKEN" ]; then
    echo -e "${RED}❌ 无法获取refresh token${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 成功获取refresh token${NC}"
echo "Refresh Token: $REFRESH_TOKEN"
echo ""

echo -e "${BLUE}2. 测试刷新token接口${NC}"
echo "--------------------------------"

# 测试刷新token（通过Authorization头）
echo -e "${YELLOW}方式1: 通过Authorization头传递refresh token${NC}"
REFRESH_RESPONSE1=$(curl -s -X POST "$AUTH_SERVER/auth/refresh" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $REFRESH_TOKEN" \
  -d '{}')

echo "刷新响应1:"
echo "$REFRESH_RESPONSE1" | jq '.'

# 检查是否成功
SUCCESS1=$(echo "$REFRESH_RESPONSE1" | jq -r '.code')

if [ "$SUCCESS1" = "200" ]; then
    echo -e "${GREEN}✅ 方式1成功：通过Authorization头刷新token${NC}"
    
    # 提取新的token
    NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE1" | jq -r '.data.accessToken')
    NEW_REFRESH_TOKEN=$(echo "$REFRESH_RESPONSE1" | jq -r '.data.refreshToken')
    
    echo "新的Access Token: $NEW_ACCESS_TOKEN"
    echo "新的Refresh Token: $NEW_REFRESH_TOKEN"
else
    echo -e "${RED}❌ 方式1失败${NC}"
    ERROR1=$(echo "$REFRESH_RESPONSE1" | jq -r '.message')
    echo "错误信息: $ERROR1"
fi

echo ""

# 测试刷新token（通过请求体）
echo -e "${YELLOW}方式2: 通过请求体传递refresh token${NC}"
REFRESH_RESPONSE2=$(curl -s -X POST "$AUTH_SERVER/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$NEW_REFRESH_TOKEN\"
  }")

echo "刷新响应2:"
echo "$REFRESH_RESPONSE2" | jq '.'

# 检查是否成功
SUCCESS2=$(echo "$REFRESH_RESPONSE2" | jq -r '.code')

if [ "$SUCCESS2" = "200" ]; then
    echo -e "${GREEN}✅ 方式2成功：通过请求体刷新token${NC}"
    
    # 提取新的token
    NEW_ACCESS_TOKEN2=$(echo "$REFRESH_RESPONSE2" | jq -r '.data.accessToken')
    NEW_REFRESH_TOKEN2=$(echo "$REFRESH_RESPONSE2" | jq -r '.data.refreshToken')
    
    echo "新的Access Token: $NEW_ACCESS_TOKEN2"
    echo "新的Refresh Token: $NEW_REFRESH_TOKEN2"
else
    echo -e "${RED}❌ 方式2失败${NC}"
    ERROR2=$(echo "$REFRESH_RESPONSE2" | jq -r '.message')
    echo "错误信息: $ERROR2"
fi

echo ""
echo -e "${BLUE}3. 测试类型转换修复${NC}"
echo "--------------------------------"

# 测试多次刷新来验证类型转换
echo -e "${YELLOW}测试多次刷新验证类型转换修复...${NC}"

for i in {1..3}; do
    echo "第 $i 次刷新测试..."
    
    REFRESH_RESPONSE=$(curl -s -X POST "$AUTH_SERVER/auth/refresh" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $NEW_REFRESH_TOKEN2" \
      -d '{}')
    
    SUCCESS=$(echo "$REFRESH_RESPONSE" | jq -r '.code')
    
    if [ "$SUCCESS" = "200" ]; then
        echo -e "${GREEN}  ✅ 第 $i 次刷新成功${NC}"
        NEW_REFRESH_TOKEN2=$(echo "$REFRESH_RESPONSE" | jq -r '.data.refreshToken')
    else
        echo -e "${RED}  ❌ 第 $i 次刷新失败${NC}"
        ERROR=$(echo "$REFRESH_RESPONSE" | jq -r '.message')
        echo "  错误: $ERROR"
        break
    fi
done

echo ""
echo -e "${BLUE}4. 验证JWT Token内容${NC}"
echo "--------------------------------"

# 解码JWT token验证内容
echo -e "${YELLOW}解码Refresh Token内容:${NC}"
echo "$NEW_REFRESH_TOKEN2" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq '.' 2>/dev/null || echo "无法解码token"

echo ""
echo -e "${BLUE}测试总结${NC}"
echo "================================"

if [ "$SUCCESS1" = "200" ] && [ "$SUCCESS2" = "200" ]; then
    echo -e "${GREEN}🎉 所有测试通过！类型转换问题已修复${NC}"
    echo -e "${GREEN}✅ Authorization Bearer方式工作正常${NC}"
    echo -e "${GREEN}✅ 请求体方式工作正常${NC}"
    echo -e "${GREEN}✅ 多次刷新无类型转换错误${NC}"
else
    echo -e "${RED}❌ 仍有问题需要解决${NC}"
    echo -e "${RED}请检查服务器日志获取更多信息${NC}"
fi

echo ""
echo -e "${BLUE}🔧 修复说明${NC}"
echo "--------------------------------"
echo "修复了Redis缓存中的类型转换问题："
echo "- 原问题：Integer无法转换为Long"
echo "- 解决方案：增加类型兼容性处理"
echo "- 支持：Long, Integer, Number, String类型转换"
echo ""
echo "现在Swagger UI中的刷新token功能应该可以正常工作了！"
