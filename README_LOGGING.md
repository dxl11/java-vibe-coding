# 日志系统说明

## 概述

本项目已实现完整的日志系统，包括：

1. **统一日志配置**：基于 Logback 的统一配置，支持多环境
2. **链路追踪**：自动生成和传递 TraceId
3. **敏感信息脱敏**：自动脱敏手机号、身份证、银行卡等敏感信息
4. **性能监控**：自动记录接口、数据库、RPC、消息队列性能
5. **操作审计**：通过注解自动记录操作审计日志
6. **多类型日志**：应用日志、错误日志、业务日志、性能日志、审计日志

## 快速开始

### 1. 日志文件位置

日志文件默认保存在 `logs/` 目录下：

```
logs/
├── application.log              # 应用日志
├── application-error.log        # 错误日志
├── application-business.log     # 业务日志
├── application-performance.log  # 性能日志
├── application-audit.log        # 审计日志
└── archive/                    # 归档日志
```

### 2. 环境变量配置

```bash
# 设置日志目录（可选，默认为 logs）
export LOG_HOME=/var/log/vibe-ecommerce

# 设置应用名称（可选，默认为 application）
export SPRING_APPLICATION_NAME=user-service
```

### 3. 日志级别配置

在 `application.yml` 中配置：

```yaml
logging:
  config: classpath:logback-spring.xml
  level:
    root: INFO
    com.vibe: DEBUG  # 开发环境使用 DEBUG，生产环境使用 INFO
```

## 日志功能

### 1. 自动记录 HTTP 请求

所有 HTTP 请求会自动记录：
- 请求方法、URI、参数
- 响应状态、耗时
- TraceId 自动生成和传递

### 2. 业务日志

使用 `LogUtils.businessLog()` 记录业务操作：

```java
LogUtils.businessLog("USER_REGISTER", "用户注册成功", userId, username);
```

### 3. 操作审计

使用 `@LogOperation` 注解自动记录操作审计：

```java
@LogOperation(operation = "ORDER_CREATE", resource = "ORDER", action = "CREATE")
public OrderDTO createOrder(OrderCreateDTO createDTO) {
    // 业务逻辑
}
```

### 4. 性能监控

自动记录：
- 接口响应时间（慢接口自动告警）
- 数据库查询时间（慢查询自动告警）
- RPC 调用时间
- 消息队列处理时间

### 5. 敏感信息脱敏

自动脱敏：
- 手机号：138****5678
- 身份证：110101********1234
- 银行卡：6222****1234
- 密码：******
- 邮箱：u***@example.com

## 日志查询

### 按 TraceId 查询

```bash
grep "abc123def456" logs/application.log
```

### 查询错误日志

```bash
tail -f logs/application-error.log
```

### 查询慢接口

```bash
grep "SLOW" logs/application-performance.log
```

## 详细文档

更多使用说明请参考：[docs/日志系统使用说明.md](docs/日志系统使用说明.md)
