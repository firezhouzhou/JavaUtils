# 端口配置文档

## 各模块端口分配

| 模块 | 服务名称 | 端口 | 说明 |
|------|----------|------|------|
| gateway-module | gateway-service | 8080 | 网关入口，统一路由 |
| auth-module | auth-service | 8081 | 认证服务，用户登录注册 |
| user-module | user-service | 8082 | 用户管理服务 |
| file-module | file-service | 8083 | 文件上传下载服务 |
| admin-module | admin-service | 8084 | 系统管理服务 |
| log-module | log-service | 8085 | 日志收集和查询服务 |

## 启动方式

### 1. 单独启动某个服务
```bash
# 进入对应模块目录
cd auth-module
mvn spring-boot:run

# 或使用启动脚本
./start-services.sh auth
```

### 2. 启动所有服务
```bash
./start-services.sh all
```

### 3. 停止所有服务
```bash
./start-services.sh stop
```

## 服务访问地址

- **网关入口**: http://localhost:8080
- **认证服务**: http://localhost:8081
- **用户服务**: http://localhost:8082  
- **文件服务**: http://localhost:8083
- **管理服务**: http://localhost:8084
- **日志服务**: http://localhost:8085

## API文档访问

通过网关访问各服务的Swagger文档：
- http://localhost:8080/swagger-ui/index.html

或直接访问各服务：
- http://localhost:8081/swagger-ui/index.html (认证服务)
- http://localhost:8082/swagger-ui/index.html (用户服务)
- http://localhost:8083/swagger-ui/index.html (文件服务)
- http://localhost:8084/swagger-ui/index.html (管理服务)
- http://localhost:8085/swagger-ui/index.html (日志服务)

## 网关路由配置

网关会将请求根据路径前缀路由到对应的服务：

- `/auth/**` → auth-service (8081)
- `/user/**` → user-service (8082)
- `/file/**` → file-service (8083)
- `/admin/**` → admin-service (8084)
- `/log/**` → log-service (8085)

## 注意事项

1. **启动顺序建议**：先启动基础服务（auth、user），再启动业务服务（file、admin），最后启动网关
2. **端口冲突**：如果端口被占用，请检查其他应用程序或修改配置文件中的端口号
3. **依赖服务**：确保MySQL、Redis等依赖服务已启动
4. **环境配置**：各模块会根据 `spring.profiles.active` 加载对应的配置文件

## 配置文件说明

- `common-module/src/main/resources/application.yml` - 公共配置
- `common-module/src/main/resources/application-dev.yml` - 开发环境配置
- 各模块的 `src/main/resources/application.yml` - 模块特定配置
