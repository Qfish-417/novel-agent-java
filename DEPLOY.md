# Docker 部署说明

## 快速启动

### 1. 配置 API Key

复制环境变量模板文件：
```bash
cp .env.template .env
```

编辑 `.env` 文件，填入您的阿里云 DashScope API Key：
```env
DASHSCOPE_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxx
```

### 2. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down
```

### 3. 验证服务

访问健康检查端点：
```bash
curl http://localhost:8080/actuator/health
```

## 服务说明

### 端口映射
| 服务 | 端口 | 说明 |
|------|------|------|
| app | 8080 | 应用服务 |
| postgres | 5432 | PostgreSQL 数据库 |

### 环境变量

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| DASHSCOPE_API_KEY | 是 | - | 阿里云 DashScope API Key |
| POSTGRES_USER | 否 | postgres | 数据库用户名 |
| POSTGRES_PASSWORD | 否 | postgres123 | 数据库密码 |

### 数据持久化

PostgreSQL 数据存储在 Docker Volume 中：
```bash
# 查看 Volume
docker volume ls | grep novel_agent

# 备份数据
docker-compose exec postgres pg_dump -U postgres novel_agent > backup.sql

# 恢复数据
docker-compose exec -T postgres psql -U postgres novel_agent < backup.sql
```

## 开发模式

本地开发时使用 `dev` profile：
```bash
# 启动 PostgreSQL
docker-compose up -d postgres

# 运行应用
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 构建优化

### 仅构建镜像
```bash
docker-compose build
```

### 多架构构建
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t novel-agent:latest .
```