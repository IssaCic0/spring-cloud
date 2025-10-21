# 智能游戏商城 - 微服务架构版

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/IssaCic0/spring-cloud)

基于 Spring Cloud Alibaba 的智能游戏商城微服务架构项目

## 项目架构

```
game-shop-cloud/
├── game-shop-common/          # 公共模块 (端口: 无)
├── game-shop-api/             # API接口模块 (端口: 无)
├── game-shop-gateway/         # 网关服务 (端口: 8080)
├── game-shop-user/            # 用户服务 (端口: 8081)
├── game-shop-product/         # 商品服务 (端口: 8082)
├── game-shop-shop/            # 店铺服务 (端口: 8083)
├── game-shop-order/           # 订单服务 (端口: 8084)
├── game-shop-payment/         # 支付服务 (端口: 8085)
├── game-shop-system/          # 系统服务 (端口: 8086)
└── game-shop-ai/              # AI服务 (端口: 8087)
```

## 技术栈

- **Spring Boot**: 3.0.2
- **Spring Cloud**: 2022.0.0
- **Spring Cloud Alibaba**: 2022.0.0.0-RC2
- **Nacos**: 2.2.2 (服务注册与配置中心)
- **Gateway**: API网关
- **OpenFeign**: 服务间调用
- **MyBatis-Plus**: 3.5.7
- **MySQL**: 8.0.33
- **Lombok**: 1.18.30
- **JDK**: 17

## 服务说明

### 1. game-shop-common (公共模块)
- 通用响应类 (ApiResponse, AjaxResult)
- 安全认证 (Role, RequireRoles, RequestContext)
- MyBatis-Plus配置
- 工具类

### 2. game-shop-api (API接口模块)
- Feign接口定义
- DTO对象
- 服务间调用契约

### 3. game-shop-gateway (网关服务)
- 统一入口: http://localhost:8080
- 路由转发
- 跨域配置
- 负载均衡

### 4. game-shop-user (用户服务)
- 用户注册、登录
- 用户信息管理
- 管理员用户管理
- 数据库表: users

### 5. game-shop-product (商品服务)
- 商品管理 (CRUD)
- 商品分类管理
- 商品状态管理
- 数据库表: products, categories

### 6. game-shop-shop (店铺服务)
- 店铺开设
- 店铺信息管理
- 店铺状态管理
- 数据库表: shops

### 7. game-shop-order (订单服务)
- 订单创建
- 订单查询
- 订单状态管理
- 数据库表: orders, order_items

### 8. game-shop-payment (支付服务)
- 支付配置
- 支付处理
- 支付状态管理
- 数据库表: payments

### 9. game-shop-system (系统服务)
- 公告管理
- 系统配置
- 数据库表: announcements

### 10. game-shop-ai (AI服务)
- Ollama AI聊天
- Python图表生成
- 数据分析

## 快速开始

### 前置要求

1. **JDK 17**
2. **Maven 3.8.1+**
3. **MySQL 8.0+**
4. **Nacos 2.2.2**
5. **Python 3.10+** (可选，用于AI服务)
6. **Ollama** (可选，用于AI聊天)

### 1. 安装 Nacos

```bash
# 下载 Nacos 2.x
# https://github.com/alibaba/nacos/releases

# Windows 单机模式启动
cd nacos/bin
startup.cmd -m standalone

# 访问 Nacos 控制台
# http://localhost:8848/nacos
# 默认账号密码: nacos/nacos
```

### 2. 配置数据库

```sql
-- 使用原有数据库
USE onlineshop;

-- 确保以下表存在:
-- users, shops, products, categories, orders, order_items, payments, announcements
```

### 3. 修改配置

修改各服务的 `application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/onlineshop?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: 你的密码
```

### 4. 编译项目

```bash
# 在项目根目录执行
cd D:\ai\game-shop-cloud
mvn clean install
```

### 5. 启动服务

**启动顺序**（建议按此顺序启动）：

1. **启动 Nacos** (必须)
2. **启动基础服务**:
   ```bash
   # 用户服务
   cd game-shop-user
   mvn spring-boot:run
   
   # 店铺服务
   cd game-shop-shop
   mvn spring-boot:run
   
   # 商品服务
   cd game-shop-product
   mvn spring-boot:run
   
   # 支付服务
   cd game-shop-payment
   mvn spring-boot:run
   
   # 订单服务
   cd game-shop-order
   mvn spring-boot:run
   
   # 系统服务
   cd game-shop-system
   mvn spring-boot:run
   
   # AI服务 (可选)
   cd game-shop-ai
   mvn spring-boot:run
   ```

3. **启动网关** (最后启动):
   ```bash
   cd game-shop-gateway
   mvn spring-boot:run
   ```

### 6. 验证服务

访问 Nacos 控制台查看服务注册情况：
- http://localhost:8848/nacos

应该看到以下服务已注册：
- game-shop-gateway
- game-shop-user
- game-shop-product
- game-shop-shop
- game-shop-order
- game-shop-payment
- game-shop-system
- game-shop-ai (如果启动)

### 7. 测试接口

所有接口通过网关访问：

```bash
# 用户登录
curl -X POST http://localhost:8080/api/account/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 查询商品
curl http://localhost:8080/api/products?page=0&size=10

# 查询店铺
curl http://localhost:8080/api/shops?page=0&size=10
```

## 前端对接

### 修改前端配置

修改前端项目的 API 地址：

```javascript
// 前端代码/src/api/axiosFun.js
const baseURL = 'http://localhost:8080'  // 指向网关地址
```

### 启动前端

```bash
cd 前端代码
npm install
npm run dev
```

访问: http://localhost:8081 (前端端口可能不同)

## 服务间调用示例

### 订单服务调用商品服务

```java
// game-shop-order 中使用 Feign 调用
@Autowired
private ProductFeignClient productFeignClient;

public void createOrder(Long productId) {
    // 通过 Feign 调用商品服务获取商品信息
    ApiResponse<ProductDTO> response = productFeignClient.getProductById(productId);
    ProductDTO product = response.getData();
    // 业务逻辑...
}
```

## 配置说明

### Nacos 配置

每个服务的 `bootstrap.yml`:

```yaml
spring:
  application:
    name: game-shop-xxx  # 服务名
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos地址
      config:
        server-addr: localhost:8848
        file-extension: yml
```

### 网关路由配置

`game-shop-gateway/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://game-shop-user
          predicates:
            - Path=/api/account/**
        - id: product-service
          uri: lb://game-shop-product
          predicates:
            - Path=/api/products/**,/api/categories/**
        # ... 其他路由
```

## 数据库设计

### 共享数据库策略

所有服务共享同一个数据库 `onlineshop`，但各服务只访问自己负责的表：

| 服务 | 负责的表 |
|------|---------|
| user | users |
| shop | shops |
| product | products, categories |
| order | orders, order_items |
| payment | payments |
| system | announcements |

### 跨表查询

通过 Feign 接口实现服务间调用，避免直接跨表查询。

## 常见问题

### 1. 服务启动失败

**问题**: 服务无法连接到 Nacos

**解决**:
- 确保 Nacos 已启动
- 检查 `bootstrap.yml` 中的 Nacos 地址
- 检查防火墙设置

### 2. 网关无法转发请求

**问题**: 404 Not Found

**解决**:
- 确保目标服务已启动并注册到 Nacos
- 检查网关路由配置
- 查看网关日志

### 3. Feign 调用失败

**问题**: Feign 客户端调用超时

**解决**:
- 确保被调用服务已启动
- 检查服务名是否正确
- 增加 Feign 超时时间

### 4. 数据库连接失败

**问题**: 无法连接到 MySQL

**解决**:
- 检查数据库是否启动
- 检查用户名密码
- 检查数据库 URL

## 开发指南

### 添加新服务

1. 在父 POM 中添加模块声明
2. 创建服务目录和 pom.xml
3. 创建启动类，添加必要注解
4. 配置 bootstrap.yml 和 application.yml
5. 实现业务逻辑
6. 在网关中添加路由规则

### 添加 Feign 接口

1. 在 `game-shop-api` 模块定义接口
2. 使用 `@FeignClient` 注解
3. 在调用方注入并使用

### 配置管理

建议使用 Nacos 配置中心统一管理配置：

1. 在 Nacos 控制台创建配置
2. 配置 Data ID: `服务名-环境.yml`
3. 服务启动时自动拉取配置

## 性能优化

1. **数据库连接池**: 配置 HikariCP
2. **缓存**: 引入 Redis
3. **限流降级**: 引入 Sentinel
4. **链路追踪**: 引入 Sleuth + Zipkin
5. **分布式事务**: 引入 Seata (如需要)

## 部署建议

### Docker 部署

```dockerfile
# Dockerfile 示例
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes 部署

使用 K8s 部署时，注意：
- 配置 Service 和 Deployment
- 使用 ConfigMap 管理配置
- 配置健康检查
- 配置资源限制

## 监控与日志

### 推荐工具

- **Prometheus + Grafana**: 监控
- **ELK Stack**: 日志收集
- **Skywalking**: 链路追踪

## 项目结构对比

### 原单体项目
```
基于SpringBoot的智能游戏商城/
└── 后端代码/
    └── src/main/java/com/baidu/
        ├── controller/  (所有Controller)
        ├── service/     (所有Service)
        ├── dao/         (所有Mapper)
        └── pojo/        (所有实体)
```

### 微服务项目
```
game-shop-cloud/
├── game-shop-user/      (用户相关)
├── game-shop-product/   (商品相关)
├── game-shop-shop/      (店铺相关)
└── ...                  (其他服务)
```

## 联系方式

如有问题，请提交 Issue 或联系开发团队。

## 许可证

本项目仅供学习使用。

