#!/bin/bash

echo "🔧 ===== 刷新Token调试测试 ====="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 你的refresh token
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInVzZXJJZCI6Mywic3ViIjoiamVycnkiLCJpYXQiOjE3NTgxMjc1MDksImV4cCI6MTc1ODczMjMwOX0.crF_XeZM88bydYXSnNHaIQ3wffczrR50Pu4RkO1WrPk"

echo -e "${BLUE}🔍 验证Token格式...${NC}"
echo "Token: ${REFRESH_TOKEN:0:50}..."
echo "Token部分数: $(echo "$REFRESH_TOKEN" | awk -F. '{print NF}')"

# 解码payload查看内容
echo -e "\n${BLUE}📋 Token内容:${NC}"
echo "$REFRESH_TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq '.' 2>/dev/null || echo "无法解码"

echo -e "\n${BLUE}1️⃣ 测试方式1：通过Authorization头传递${NC}"
echo "执行命令:"
echo "curl -X POST \"http://localhost:8081/auth/refresh\" \\"
echo "  -H \"Authorization: Bearer \$REFRESH_TOKEN\""
echo ""

RESPONSE1=$(curl -s -X POST "http://localhost:8081/auth/refresh" \
  -H "Authorization: Bearer $REFRESH_TOKEN")

echo "响应:"
echo "$RESPONSE1" | jq '.' 2>/dev/null || echo "$RESPONSE1"

if echo "$RESPONSE1" | grep -q '"code":200'; then
    echo -e "${GREEN}✅ 方式1成功${NC}"
else
    echo -e "${RED}❌ 方式1失败${NC}"
fi

echo -e "\n${BLUE}2️⃣ 测试方式2：通过请求体传递${NC}"
echo "执行命令:"
echo "curl -X POST \"http://localhost:8081/auth/refresh\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"refreshToken\": \"\$REFRESH_TOKEN\"}'"
echo ""

RESPONSE2=$(curl -s -X POST "http://localhost:8081/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")

echo "响应:"
echo "$RESPONSE2" | jq '.' 2>/dev/null || echo "$RESPONSE2"

if echo "$RESPONSE2" | grep -q '"code":200'; then
    echo -e "${GREEN}✅ 方式2成功${NC}"
else
    echo -e "${RED}❌ 方式2失败${NC}"
fi

echo -e "\n${BLUE}3️⃣ 测试错误的Basic认证方式（用于对比）${NC}"
echo "执行命令:"
echo "curl -u admin:admin123 \"http://localhost:8081/auth/refresh\""
echo ""

RESPONSE3=$(curl -s -u admin:admin123 "http://localhost:8081/auth/refresh")

echo "响应:"
echo "$RESPONSE3" | jq '.' 2>/dev/null || echo "$RESPONSE3"

if echo "$RESPONSE3" | grep -q '"code":400'; then
    echo -e "${GREEN}✅ Basic认证正确被拒绝（符合预期）${NC}"
else
    echo -e "${YELLOW}⚠️ Basic认证响应异常${NC}"
fi

echo -e "\n${GREEN}🎯 ===== 调试总结 =====${NC}"
echo ""
echo -e "${BLUE}📋 问题分析:${NC}"
echo "  - 从截图看到bearerToken是'Basic YWRtaW46YWRtaW4xMjM='"
echo "  - 这是HTTP Basic认证，不是JWT Bearer token"
echo "  - Basic认证会被解析为1部分，导致JWT格式验证失败"
echo ""
echo -e "${BLUE}✅ 解决方案:${NC}"
echo "  1. 确保使用 -H \"Authorization: Bearer JWT_TOKEN\""
echo "  2. 不要使用 -u username:password"
echo "  3. 确保JWT token是完整的3部分格式"
echo ""
echo -e "${BLUE}🔧 正确的curl命令:${NC}"
echo "  curl -H \"Authorization: Bearer \$REFRESH_TOKEN\" /auth/refresh"
echo "  或者"
echo "  curl -d '{\"refreshToken\": \"\$REFRESH_TOKEN\"}' /auth/refresh"
