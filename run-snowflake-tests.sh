#!/bin/bash

# 雪花算法ID生成器测试工具启动脚本

echo "🎯 雪花算法ID生成器测试工具"
echo "=============================="
echo ""

# 检查是否在正确的目录
if [ ! -d "common-module" ]; then
    echo "❌ 请在项目根目录下运行此脚本"
    exit 1
fi

# 进入common-module目录
cd common-module

echo "📋 可用的测试工具:"
echo "  1. 简单测试 (SimpleSnowflakeTest) - 自动运行所有测试"
echo "  2. 交互式演示 (SnowflakeDemo) - 菜单选择功能"
echo "  3. 完整测试 (SnowflakeIdTestTool) - 详细测试报告"
echo ""

read -p "请选择要运行的测试工具 (1-3): " choice

case $choice in
    1)
        echo "🚀 启动简单测试..."
        echo ""
        mvn compile exec:java -Dexec.mainClass="com.example.common.util.SimpleSnowflakeTest"
        ;;
    2)
        echo "🚀 启动交互式演示..."
        echo ""
        mvn compile exec:java -Dexec.mainClass="com.example.common.util.SnowflakeDemo"
        ;;
    3)
        echo "🚀 启动完整测试..."
        echo ""
        mvn compile exec:java -Dexec.mainClass="com.example.common.util.SnowflakeIdTestTool"
        ;;
    *)
        echo "❌ 无效选择，退出程序"
        exit 1
        ;;
esac

echo ""
echo "✅ 测试完成！"
