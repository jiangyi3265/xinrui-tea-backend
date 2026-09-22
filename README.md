# 鑫芮茶业后端服务

基于 RuoYi 改造的 Java/Spring Boot 后端，为管理后台和用户端提供认证、系统管理、数据访问及业务 API 能力。

## 项目简介

本仓库是鑫芮茶业项目的服务端工程。它以 Maven 多模块方式组织后端代码，包含用户、角色、菜单、部门、字典、日志、文件、定时任务和代码生成等管理能力，并通过 REST API、MySQL 和 Redis 为前端提供统一服务。当前目录中的 `ruoyi-admin` 是 Spring Boot 启动模块，其他 `ruoyi-*` 模块提供公共、框架、系统、生成器和任务调度能力。

## 技术栈

- Java 8、Maven、多模块构建
- Spring Boot 2.5.15、Spring MVC、Spring Security
- MyBatis、PageHelper、MySQL Connector/J
- Redis、Lettuce、Druid 数据源与连接池监控
- JWT（jjwt）、Swagger/Springfox、Quartz
- Fastjson2、Apache POI、Velocity、OSHI、Kaptcha

## 关联仓库

| 项目 | 说明 | GitHub |
| --- | --- | --- |
| xinrui-tea-backend | 后端服务 | [xinrui-tea-backend](https://github.com/jiangyi3265/xinrui-tea-backend) |
| xinrui-tea-admin | 管理后台 | [xinrui-tea-admin](https://github.com/jiangyi3265/xinrui-tea-admin) |
| xinrui-tea-app | 用户端 | [xinrui-tea-app](https://github.com/jiangyi3265/xinrui-tea-app) |

## 快速启动

环境要求：JDK 8、Maven 3.6+、MySQL 8（或兼容版本）和 Redis。

1. 创建数据库并执行 [`sql/ry_20250522.sql`](sql/ry_20250522.sql)。
2. 通过环境变量提供本地配置（不要把真实值写回仓库）：

   ```bash
   export DB_URL='jdbc:mysql://localhost:3306/ruoyi?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8'
   export DB_USERNAME='root'
   export DB_PASSWORD='在本机设置'
   export REDIS_PASSWORD='没有密码时留空'
   export JWT_SECRET='至少 32 位随机字符串'
   export DRUID_PASSWORD='本机 Druid 控制台密码'
   ```

3. 构建并启动：

   ```bash
   mvn clean package -DskipTests
   java -jar ruoyi-admin/target/ruoyi-admin-3.9.1.jar
   ```

   开发环境也可以使用 `mvn spring-boot:run -pl ruoyi-admin -am`。服务默认监听 `http://localhost:8080/`，Swagger 前缀为 `/dev-api`。

## 项目结构

| 路径 | 用途 |
| --- | --- |
| `ruoyi-admin/` | Spring Boot 启动模块、Web 控制器、应用配置 |
| `ruoyi-common/` | 通用实体、工具、异常、Redis 和安全基础能力 |
| `ruoyi-framework/` | Spring 配置、JWT、权限、数据源、日志和文件服务 |
| `ruoyi-system/` | 用户、角色、菜单、部门、字典和系统日志业务 |
| `ruoyi-generator/` | 基于数据库表的 Java/Vue/XML/SQL 代码生成 |
| `ruoyi-quartz/` | Quartz 定时任务及任务日志模块 |
| `sql/` | 数据库初始化脚本 |

## 简历描述示例

负责基于 Spring Boot 的管理系统后端模块开发与维护，完善用户权限、日志审计、文件上传、代码生成、缓存及定时任务等通用能力。通过 MyBatis + MySQL/Redis 提供稳定的 REST API，并为管理后台和用户端提供统一认证与数据服务。

## 安全说明

数据库、Redis、Druid 和 JWT 配置均通过环境变量注入；仓库不会提交 `.env`、构建产物、IDE 配置或真实凭据。生产部署前请替换所有本地默认配置并使用独立随机密钥。
