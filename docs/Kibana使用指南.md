# Kibana 使用指南

## 1. 访问 Kibana

启动 ELK 系统后，访问 Kibana：
- URL: http://localhost:5601
- 默认无需登录（开发环境）

## 2. 创建索引模式

### 2.1 创建索引模式

1. 进入 **Management** → **Stack Management** → **Index Patterns**
2. 点击 **Create index pattern**
3. 输入索引模式：`vibe-logs-*`
4. 选择时间字段：`@timestamp`
5. 点击 **Create index pattern**

### 2.2 验证索引

在 **Discover** 页面中，选择索引模式 `vibe-logs-*`，应该能看到日志数据。

## 3. 常用查询语法

### 3.1 按 TraceId 查询

```
traceId: "abc123def456"
```

### 3.2 按服务名查询

```
service: "order-service"
```

### 3.3 按日志级别查询

```
level: "ERROR"
```

### 3.4 按用户ID查询

```
userId: "123"
```

### 3.5 组合查询

```
service: "order-service" AND level: "ERROR" AND traceId: "abc123"
```

### 3.6 时间范围查询

```
@timestamp:[2024-01-13T00:00:00 TO 2024-01-13T23:59:59]
```

## 4. 创建可视化 Dashboard

### 4.1 错误日志趋势图

1. 进入 **Visualize** → **Create visualization**
2. 选择 **Line** 图表类型
3. 选择索引模式：`vibe-logs-*`
4. 配置：
   - X轴：`@timestamp`（按时间聚合）
   - Y轴：计数（Count）
   - 过滤器：`level: "ERROR"`
5. 保存为：`Error Log Trend`

### 4.2 慢接口统计

1. 创建 **Data Table** 可视化
2. 配置：
   - 行：`operation`（按操作类型分组）
   - 指标：平均 `duration` 字段（如果日志中包含）
   - 过滤器：`logType: "performance"`
3. 保存为：`Slow API Statistics`

### 4.3 日志量统计（按服务）

1. 创建 **Pie Chart** 可视化
2. 配置：
   - 切片：`service`（按服务分组）
   - 大小：计数
3. 保存为：`Log Volume by Service`

### 4.4 日志级别分布

1. 创建 **Pie Chart** 可视化
2. 配置：
   - 切片：`level`（按日志级别分组）
   - 大小：计数
3. 保存为：`Log Level Distribution`

### 4.5 TraceId 链路追踪视图

1. 创建 **Timeline** 可视化
2. 配置：
   - 时间字段：`@timestamp`
   - 分组：`service`、`operation`
   - 过滤器：`traceId: "xxx"`（动态替换）
3. 保存为：`Trace Timeline`

### 4.6 创建 Dashboard

1. 进入 **Dashboard** → **Create dashboard**
2. 添加上述所有可视化组件
3. 调整布局和大小
4. 保存为：`Vibe Ecommerce Logs Dashboard`

## 5. 配置告警规则

### 5.1 错误率告警

1. 进入 **Stack Management** → **Rules and Connectors** → **Create rule**
2. 选择规则类型：**Log threshold**
3. 配置：
   - 索引：`vibe-logs-*`
   - 时间窗口：5 分钟
   - 条件：错误日志数量 > 100
   - 操作：发送通知（邮件/钉钉/Webhook）
4. 保存为：`High Error Rate Alert`

### 5.2 慢接口告警

1. 创建 **Log threshold** 规则
2. 配置：
   - 索引：`vibe-logs-*`
   - 过滤器：`logType: "performance" AND duration > 2000`
   - 条件：数量 > 50（5分钟内）
   - 操作：发送通知
3. 保存为：`Slow API Alert`

### 5.3 日志量异常告警

1. 创建 **Log threshold** 规则
2. 配置：
   - 索引：`vibe-logs-*`
   - 时间窗口：1 分钟
   - 条件：日志总量 > 10000（1分钟内）
   - 操作：发送通知
3. 保存为：`Log Volume Spike Alert`

## 6. 保存的查询（Saved Searches）

### 6.1 错误日志查询

1. 在 **Discover** 页面中，输入查询：`level: "ERROR"`
2. 点击 **Save**，命名为：`Error Logs`

### 6.2 按 TraceId 追踪

1. 创建查询：`traceId: "*"`（使用时替换为实际 TraceId）
2. 保存为：`Trace by TraceId`

### 6.3 业务日志查询

1. 创建查询：`logType: "business"`
2. 保存为：`Business Logs`

## 7. 最佳实践

### 7.1 索引生命周期管理

建议配置索引生命周期策略：
- **Hot 阶段**：7 天，SSD 存储
- **Warm 阶段**：30 天，普通存储
- **Cold 阶段**：90 天，归档存储
- **Delete 阶段**：超过 90 天自动删除

### 7.2 查询性能优化

- 使用时间范围限制查询范围
- 使用字段过滤而非全文搜索
- 避免使用通配符查询（`*`）
- 使用 `keyword` 字段进行精确匹配

### 7.3 监控建议

- 定期检查 Elasticsearch 集群健康状态
- 监控索引大小和增长速度
- 设置磁盘空间告警（建议保留 20% 以上空间）
- 定期清理过期索引

## 8. 故障排查

### 8.1 日志未出现在 Kibana

1. 检查 Filebeat/Logstash 是否正常运行
2. 检查日志文件路径是否正确
3. 检查索引模式是否创建
4. 查看 Logstash 日志：`docker logs vibe-logstash`

### 8.2 查询结果为空

1. 检查时间范围是否正确
2. 检查查询语法是否正确
3. 检查索引模式是否匹配实际索引

### 8.3 性能问题

1. 检查 Elasticsearch 集群状态
2. 检查索引分片数量
3. 考虑增加 Elasticsearch 内存
4. 优化查询语句
