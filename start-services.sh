#!/bin/bash

# Spring Boot 多模块项目启动脚本
# 各服务端口分配：
# Gateway: 8080 (网关入口)
# Auth: 8081 (认证服务)
# User: 8082 (用户服务)
# File: 8083 (文件服务)
# Admin: 8084 (管理服务)
# Log: 8085 (日志服务)

echo "Spring Boot 多模块项目启动脚本"
echo "================================="
echo "端口分配："
echo "  Gateway服务: 8080"
echo "  Auth服务:    8081"
echo "  User服务:    8082"
echo "  File服务:    8083"
echo "  Admin服务:   8084"
echo "  Log服务:     8085"
echo "================================="

function start_service() {
    local module=$1
    local port=$2
    echo "启动 $module 服务 (端口: $port)..."
    cd $module
    mvn spring-boot:run &
    echo "$module 服务启动中，PID: $!"
    cd ..
    sleep 2
}

function start_all() {
    echo "启动所有服务..."
    start_service "gateway-module" "8080"
    start_service "auth-module" "8081"
    start_service "user-module" "8082"
    start_service "file-module" "8083"
    start_service "admin-module" "8084"
    start_service "log-module" "8085"
    echo "所有服务启动完成！"
}

function stop_all() {
    echo "停止所有服务..."
    pkill -f "spring-boot:run"
    echo "所有服务已停止！"
}

case "$1" in
    "gateway")
        start_service "gateway-module" "8080"
        ;;
    "auth")
        start_service "auth-module" "8081"
        ;;
    "user")
        start_service "user-module" "8082"
        ;;
    "file")
        start_service "file-module" "8083"
        ;;
    "admin")
        start_service "admin-module" "8084"
        ;;
    "log")
        start_service "log-module" "8085"
        ;;
    "all")
        start_all
        ;;
    "stop")
        stop_all
        ;;
    *)
        echo "使用方法: $0 {gateway|auth|user|file|admin|log|all|stop}"
        echo "示例："
        echo "  $0 gateway  # 启动网关服务"
        echo "  $0 log      # 启动日志服务"
        echo "  $0 all      # 启动所有服务"
        echo "  $0 stop     # 停止所有服务"
        exit 1
        ;;
esac
