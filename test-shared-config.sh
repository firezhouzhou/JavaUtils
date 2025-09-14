#!/bin/bash

# 测试共享配置是否生效的脚本

echo "🧪 测试共享配置解决方案"
echo "=========================="

# 测试各个模块的编译
modules=("common-module" "log-module" "gateway-module" "user-module" "auth-module" "admin-module" "file-module")

for module in "${modules[@]}"; do
    echo ""
    echo "📦 测试模块: $module"
    echo "------------------------"
    
    if [ -d "$module" ]; then
        cd "$module"
        
        # 编译测试
        echo "编译中..."
        if mvn clean compile -q; then
            echo "✅ $module 编译成功"
        else
            echo "❌ $module 编译失败"
        fi
        
        cd ..
    else
        echo "❌ 模块目录不存在: $module"
    fi
done

echo ""
echo "🔍 检查共享配置文件"
echo "------------------------"
if [ -f "common-module/src/main/resources/shared-config.yml" ]; then
    echo "✅ shared-config.yml 存在"
    echo "配置内容预览:"
    head -10 "common-module/src/main/resources/shared-config.yml"
else
    echo "❌ shared-config.yml 不存在"
fi

echo ""
echo "📋 检查各模块的配置导入"
echo "------------------------"
for module in "${modules[@]}"; do
    if [ "$module" != "common-module" ] && [ -f "$module/src/main/resources/application.yml" ]; then
        if grep -q "shared-config.yml" "$module/src/main/resources/application.yml"; then
            echo "✅ $module 已导入共享配置"
        else
            echo "❌ $module 未导入共享配置"
        fi
    fi
done

echo ""
echo "🎯 测试完成!"
echo "如果所有模块都编译成功且已导入共享配置，说明问题已解决。"
