#!/bin/bash

# 文件模块数据库初始化脚本
# 使用方法: ./setup-file-database.sh

echo "🚀 开始创建文件模块数据库表..."

# 数据库连接配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="multi_module_dev"
DB_USER="root"
DB_PASSWORD="19990626ZYX"

# 检查MySQL是否可连接
echo "📡 检查数据库连接..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD -e "SELECT 1;" > /dev/null 2>&1

if [ $? -ne 0 ]; then
    echo "❌ 无法连接到数据库，请检查MySQL服务是否启动以及连接参数是否正确"
    echo "   主机: $DB_HOST:$DB_PORT"
    echo "   用户: $DB_USER"
    echo "   数据库: $DB_NAME"
    exit 1
fi

echo "✅ 数据库连接成功"

# 创建数据库（如果不存在）
echo "📝 创建数据库 $DB_NAME（如果不存在）..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行建表SQL
echo "🏗️  创建 file_metadata 表..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME < create-file-metadata-table.sql

if [ $? -eq 0 ]; then
    echo "✅ file_metadata 表创建成功！"
    
    # 查看表结构
    echo "📋 表结构如下："
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME -e "DESCRIBE file_metadata;"
    
    echo ""
    echo "🎉 文件模块数据库初始化完成！"
    echo ""
    echo "📌 你现在可以重新测试文件上传功能了："
    echo "   curl -X POST \"http://localhost:8083/file/upload\" \\"
    echo "     -H \"Authorization: Bearer YOUR_TOKEN\" \\"
    echo "     -F \"file=@test-file.txt\" \\"
    echo "     -F \"businessType=test\""
else
    echo "❌ 创建表时发生错误"
    exit 1
fi
