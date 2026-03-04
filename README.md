# 电商微服务系统

基于 Spring Cloud Alibaba 的分布式电商微服务系统，严格遵守阿里巴巴Java开发手册规范。

## 技术栈

- **框架**: Spring Boot 2.7.18, Spring Cloud 2021.0.8, Spring Cloud Alibaba 2021.0.5.0
- **服务注册与配置**: Nacos
- **消息队列**: RocketMQ
- **分布式事务**: Seata
- **流量控制**: Sentinel
- **RPC框架**: Dubbo
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus
- **连接池**: Druid

## 项目结构

```
java-vibe-coding/
├── common-core/              # 通用核心模块
│   ├── result/              # 统一响应格式
│   ├── exception/           # 异常处理
│   ├── util/                # 工具类
│   └── constant/            # 常量类
├── common-mq/                # 通用消息队列模块
│   ├── producer/            # 消息生产者
│   ├── consumer/            # 消息消费者
│   └── config/              # 消息队列配置
├── gateway-service/          # API网关服务
├── user-service/            # 用户服务
├── product-service/          # 商品服务
├── order-service/            # 订单服务
├── payment-service/          # 支付服务
├── inventory-service/        # 库存服务
├── coupon-service/           # 优惠券服务
└── sql/                     # 数据库脚本
```

## 模块说明

### common-core
通用核心模块，提供：
- 统一响应格式 `Result<T>`
- 全局异常处理 `GlobalExceptionHandler`
- 业务异常类 `BusinessException`
- 工具类：`DateUtils`、`StringUtils`
- 常量类：`CommonConstants`

### common-mq
通用消息队列模块，提供：
- 消息生产者接口 `MessageProducer`
- RocketMQ 实现 `RocketMQProducer`
- 消息消费者抽象类 `AbstractMessageConsumer`
- Topic 和 Tag 配置（`RocketMQTopicConfig`）
- 本地消息表（`LocalMessage` + `LocalMessageSendTask`）实现 Outbox 模式
- 死信查询与人工重试服务 `DeadLetterService`

### 服务模块
各服务模块遵循统一的结构：
- `entity/` - 实体类
- `dto/` - 数据传输对象
- `mapper/` - MyBatis Mapper
- `service/` - 服务接口
- `service/impl/` - 服务实现
- `controller/` - 控制器
- `api/` - Dubbo API接口（如需要）

## 数据库设计

所有表都包含以下字段：
- `id` - 主键，自增
- `create_time` - 创建时间
- `update_time` - 更新时间
- `is_deleted` - 逻辑删除标记

详细表结构请参考 `sql/init.sql`。

## 启动说明

### 前置条件

1. **MySQL 8.0**
   - 创建数据库：`vibe_ecommerce`
   - 执行 `sql/init.sql` 初始化表结构

2. **Nacos**
   - 启动 Nacos Server（默认端口：8848）
   - 配置命名空间和配置中心

3. **RocketMQ**
   - 启动 RocketMQ NameServer（默认端口：9876）
   - 启动 RocketMQ Broker

4. **Seata**
   - 启动 Seata Server
   - 配置 Nacos 注册中心和配置中心

### 启动顺序

1. 启动基础设施：
   - Nacos
   - RocketMQ
   - Seata

2. 启动服务（无顺序要求）：
   - user-service (8081)
   - product-service (8082)
   - order-service (8083)
   - payment-service (8084)
   - inventory-service (8085)
   - coupon-service (8086)

3. 启动网关：
   - gateway-service (8080)

## API 接口

### 网关地址
- 网关端口：8080
- 路由前缀：`/api/{service-name}/**`

### 用户服务
- 注册：`POST /api/user/user/register`
- 登录：`POST /api/user/user/login`
- 查询用户：`GET /api/user/user/{id}`

### 订单服务
- 创建订单：`POST /api/order/order/create`
- 查询订单：`GET /api/order/order/{orderNo}`
- 取消订单：`POST /api/order/order/cancel/{orderNo}`

### 库存服务
- 扣减库存：`POST /api/inventory/inventory/deduct`
- 回滚库存：`POST /api/inventory/inventory/rollback`
- 查询库存：`GET /api/inventory/inventory/{productId}`

## 代码规范

本项目严格遵守《阿里巴巴Java开发手册》规范：

- **命名规范**：类名 UpperCamelCase，方法名/变量名 lowerCamelCase，常量 UPPER_SNAKE_CASE
- **代码风格**：4个空格缩进，单行不超过120字符
- **注释规范**：类和方法必须包含完整的 JavaDoc 注释
- **异常处理**：使用具体的异常类型，异常信息包含上下文
- **日志规范**：使用 SLF4J，禁止使用 System.out.println
- **数据库规范**：表名和字段名使用小写字母和下划线

## 分布式组件使用

### Nacos
- 服务注册与发现
- 配置中心

### RocketMQ
- 订单创建消息
- 库存变更消息
- 支付消息

### Seata
- 订单创建（分布式事务）
- 库存扣减（分布式事务）

### Sentinel
- API网关限流
- 服务熔断降级

### Dubbo
- 订单服务调用库存服务
- 订单服务调用优惠券服务

## 开发规范

1. 所有代码必须通过阿里巴巴代码规范检查
2. 提交代码前必须通过编译和基本测试
3. 新增功能必须添加相应的单元测试
4. 数据库变更必须提供 SQL 脚本

## 注意事项

1. 生产环境需要修改数据库连接配置
2. 需要配置 Nacos、RocketMQ、Seata 的实际地址
3. JWT Token 生成需要完善实现
4. 商品服务、优惠券服务、支付服务的具体业务逻辑需要进一步完善

## 作者

vibe

## 日期

2024-01-13
