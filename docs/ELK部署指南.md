# ELK 系统部署指南

## 一、系统要求

### 1.1 硬件要求

- **CPU**：至少 4 核（推荐 8 核）
- **内存**：至少 8GB（推荐 16GB）
- **磁盘**：至少 100GB SSD（推荐 500GB+）
- **网络**：千兆网络

### 1.2 软件要求

- Docker 20.10+
- Docker Compose 1.29+
- 操作系统：Linux / macOS / Windows（WSL2）

## 二、快速启动

### 2.1 启动 ELK 系统

```bash
# 进入 ELK 目录
cd docker/elk

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 2.2 验证服务

- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Logstash**: http://localhost:9600

### 2.3 停止服务

```bash
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v
```

## 三、配置说明

### 3.1 Elasticsearch 配置

配置文件：`docker/elk/elasticsearch/`（通过环境变量配置）

主要配置项：
- `discovery.type=single-node`：单节点模式（生产环境建议集群模式）
- `ES_JAVA_OPTS=-Xms512m -Xmx512m`：JVM 堆内存（根据服务器内存调整）
- `xpack.security.enabled=false`：禁用安全认证（开发环境，生产环境建议启用）

### 3.2 Logstash 配置

Pipeline 配置：`docker/elk/logstash/pipeline/logstash.conf`

主要功能：
- 从 Filebeat 接收日志（端口 5044）
- 解析 JSON 格式日志
- 提取 TraceId、UserId、IP 等字段
- 按服务名和日期创建索引
- 输出到 Elasticsearch

### 3.3 Filebeat 配置

配置文件：`docker/elk/filebeat/filebeat.yml`

主要配置：
- 日志文件路径：`/var/log/vibe-ecommerce/**/*-json.log`
- 输出到 Logstash：`logstash:5044`

### 3.4 Kibana 配置

通过环境变量配置：
- `ELASTICSEARCH_HOSTS=http://elasticsearch:9200`：Elasticsearch 地址
- `xpack.security.enabled=false`：禁用安全认证（开发环境）

## 四、日志文件路径配置

### 4.1 应用日志路径

确保应用日志输出到以下路径（或修改 Filebeat 配置）：
```
/var/log/vibe-ecommerce/
├── order-service-json.log
├── order-service-error-json.log
├── order-service-business-json.log
├── inventory-service-json.log
└── ...
```

### 4.2 环境变量配置

在应用启动时设置日志目录：
```bash
export LOG_HOME=/var/log/vibe-ecommerce
```

或在 `application.yml` 中配置：
```yaml
logging:
  file:
    path: /var/log/vibe-ecommerce
```

## 五、索引管理

### 5.1 索引命名规则

索引按以下规则命名：
```
vibe-logs-{service}-{logType}-{date}
```

示例：
- `vibe-logs-order-service-application-2024.01.13`
- `vibe-logs-order-service-error-2024.01.13`
- `vibe-logs-order-service-business-2024.01.13`

### 5.2 索引模板

系统已配置索引模板 `vibe-logs-template`，自动应用到所有 `vibe-logs-*` 索引。

### 5.3 索引生命周期管理

建议配置索引生命周期策略（ILM）：
- **Hot 阶段**：7 天，SSD 存储
- **Warm 阶段**：30 天，普通存储
- **Cold 阶段**：90 天，归档存储
- **Delete 阶段**：超过 90 天自动删除

## 六、生产环境部署

### 6.1 集群模式

生产环境建议使用 Elasticsearch 集群模式：

```yaml
# docker-compose.yml
elasticsearch:
  environment:
    - discovery.type=zen
    - cluster.name=vibe-elk-cluster
    - node.name=es-node-1
    - network.host=0.0.0.0
    - http.port=9200
    - transport.port=9300
    - discovery.seed_hosts=es-node-1,es-node-2,es-node-3
    - cluster.initial_master_nodes=es-node-1,es-node-2,es-node-3
```

### 6.2 安全配置

生产环境必须启用安全认证：

```yaml
elasticsearch:
  environment:
    - xpack.security.enabled=true
    - xpack.security.transport.ssl.enabled=true
```

### 6.3 资源限制

为容器设置资源限制：

```yaml
elasticsearch:
  deploy:
    resources:
      limits:
        cpus: '4'
        memory: 8G
      reservations:
        cpus: '2'
        memory: 4G
```

### 6.4 数据持久化

使用命名卷或绑定挂载确保数据持久化：

```yaml
volumes:
  es_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/elasticsearch
```

## 七、监控与维护

### 7.1 健康检查

```bash
# Elasticsearch 健康状态
curl http://localhost:9200/_cluster/health

# Logstash 状态
curl http://localhost:9600/_node/stats

# 查看索引状态
curl http://localhost:9200/_cat/indices?v
```

### 7.2 日志查看

```bash
# Elasticsearch 日志
docker logs vibe-elasticsearch

# Logstash 日志
docker logs vibe-logstash

# Filebeat 日志
docker logs vibe-filebeat

# Kibana 日志
docker logs vibe-kibana
```

### 7.3 性能优化

- **Elasticsearch**：
  - 调整 JVM 堆内存（建议不超过物理内存的 50%）
  - 优化分片数量（建议每个索引 1-3 个分片）
  - 定期清理过期索引

- **Logstash**：
  - 调整 JVM 堆内存
  - 优化 Pipeline 配置，减少不必要的处理

- **Filebeat**：
  - 调整批量大小
  - 优化采集路径，避免重复采集

## 八、故障排查

### 8.1 Elasticsearch 无法启动

- 检查内存是否足够
- 检查端口是否被占用
- 查看日志：`docker logs vibe-elasticsearch`

### 8.2 Logstash 无法连接 Elasticsearch

- 检查 Elasticsearch 是否正常运行
- 检查网络连接
- 查看 Logstash 日志：`docker logs vibe-logstash`

### 8.3 日志未出现在 Kibana

- 检查 Filebeat 是否正常运行
- 检查日志文件路径是否正确
- 检查 Logstash Pipeline 配置
- 查看索引是否创建：`curl http://localhost:9200/_cat/indices?v`

### 8.4 索引创建失败

- 检查 Elasticsearch 磁盘空间
- 检查索引模板是否正确
- 查看 Elasticsearch 日志

## 九、备份与恢复

### 9.1 备份索引

```bash
# 创建快照仓库
curl -X PUT "localhost:9200/_snapshot/backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/backup/elasticsearch"
  }
}'

# 创建快照
curl -X PUT "localhost:9200/_snapshot/backup/snapshot_1?wait_for_completion=true"
```

### 9.2 恢复索引

```bash
# 恢复快照
curl -X POST "localhost:9200/_snapshot/backup/snapshot_1/_restore"
```

## 十、升级指南

### 10.1 升级前准备

1. 备份所有数据
2. 查看当前版本
3. 阅读升级文档

### 10.2 升级步骤

1. 停止服务：`docker-compose down`
2. 备份数据卷
3. 更新镜像版本
4. 启动服务：`docker-compose up -d`
5. 验证服务正常

## 十一、常见问题

### Q1: 如何修改日志采集路径？

修改 `docker/elk/filebeat/filebeat.yml` 中的 `paths` 配置，然后重启 Filebeat。

### Q2: 如何调整 Elasticsearch 内存？

修改 `docker-compose.yml` 中的 `ES_JAVA_OPTS` 环境变量。

### Q3: 如何清理过期索引？

使用 Elasticsearch 的索引生命周期管理（ILM）或手动删除：
```bash
curl -X DELETE "localhost:9200/vibe-logs-*"
```

### Q4: 如何查看索引大小？

```bash
curl "localhost:9200/_cat/indices?v&s=store.size:desc"
```
