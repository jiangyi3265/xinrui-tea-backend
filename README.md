# xinrui-tea-backend · 鑫芮茶业后端服务

基于若依的 Spring Boot 后端，为 xinrui-tea-admin 管理后台和 xinrui-tea-app H5 用户端提供统一鉴权、权限与业务数据接口。

## 项目简介

采用 Maven 多模块结构，保留若依用户、角色、菜单、字典、日志、定时任务和代码生成能力。茶叶业务入口位于 `ruoyi-admin/src/main/java/com/ruoyi/web/tea/`：Java 承担 JWT/RBAC、请求校验、Redis 限流、MySQL 事务与审计，通过仅限内网的 Node 兼容业务引擎处理恢复版 H5 的商品、订单、拍卖、会员、凭证审核和内容契约。

当前茶叶业务以 MySQL 聚合 JSON 状态存储，不是已经全部迁移为独立关系表的纯 Java 业务系统。私有 Node 引擎源码位于关联用户端仓库。第三方支付、短信、实名、实时物流及部分分销/寄卖流程尚未完成，不应直接视为生产可用。

## 技术栈

- Java：Maven 编译目标 1.8；共享业务联调实际使用 JDK 17。
- Spring Boot 2.5.15、Spring MVC、Spring Security、JWT。
- MyBatis（不是 MyBatis Plus）、PageHelper、MySQL、Druid。
- Redis/Lettuce、Quartz、Swagger/Springfox。
- Fastjson2、Apache POI、Velocity；Maven 多模块构建。

## 关联仓库

| 项目 | 说明 | GitHub |
| --- | --- | --- |
| xinrui-tea-backend | 后端服务 | [xinrui-tea-backend](https://github.com/jiangyi3265/xinrui-tea-backend) |
| xinrui-tea-admin | 管理后台 | [xinrui-tea-admin](https://github.com/jiangyi3265/xinrui-tea-admin) |
| xinrui-tea-app | 用户端 | [xinrui-tea-app](https://github.com/jiangyi3265/xinrui-tea-app) |

## 快速启动

### 推荐：完整本地联调

三个仓库的本地目录名应保持为下面的名称（联调脚本按相邻目录定位）：

```bash
git clone https://github.com/jiangyi3265/xinrui-tea-backend.git RuoYi-Vue
git clone https://github.com/jiangyi3265/xinrui-tea-admin.git RuoYi-Vue3
git clone https://github.com/jiangyi3265/xinrui-tea-app.git 分销茶叶
```

前置：Node.js 22+、npm、JDK 17、Maven、本地 MySQL 与 Redis。数据库账号须允许创建隔离测试库；不要使用生产数据库账号。凭据通过受控环境变量或本机 MySQL defaults 文件提供，不提交到 Git。

```bash
cd RuoYi-Vue3
npm ci
cp .env.development.example .env.development
cd ../分销茶叶
npm ci
npm run local:shared
```

脚本会构建 Java/H5/后台，创建独立 `tea_local_*` 库并启动 Java 8080、H5 5173、静态 H5 5180、后台 5174、私有引擎 8091。测试账户仅用于该隔离环境，禁止部署到公网。配置说明见 [用户端 README](https://github.com/jiangyi3265/xinrui-tea-app#快速启动)。

### 独立构建与手动启动

```bash
mvn clean package
java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=druid,tea
```

启动前在新建空库依次导入 `sql/ry_20250522.sql` 和 `sql/tea_business.sql`，配置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_PASSWORD`、`JWT_SECRET`、`TEA_ENGINE_SECRET`、`TEA_ENGINE_URL`。两个 Secret 必须独立随机生成且各不少于 32 字符，不在 README 或 shell 历史中写入真实值。Node 引擎须在 app 仓库用 `node server/shared-engine.mjs` 启动，并与 Java 使用同一 `TEA_ENGINE_SECRET`。初始化 SQL 禁止对已有业务库重复执行。

`application-tea.yml` 默认绑定 loopback；反向代理、HTTPS、正式数据库及 Redis ACL、备份/恢复、限流与监控均须单独配置和验收。

## 项目结构

| 路径 | 用途 |
| --- | --- |
| ruoyi-admin/ | 启动模块、Web 控制器、茶叶业务适配和应用配置 |
| ruoyi-framework/ | Spring Security、JWT、数据源和基础框架 |
| ruoyi-system/ | 用户、角色、菜单、字典与系统业务 |
| ruoyi-common/ | 通用模型、工具、异常和缓存能力 |
| ruoyi-quartz/ | 定时任务与任务日志 |
| ruoyi-generator/ | 代码生成与模板 |
| sql/ | 若依新库初始化和茶叶业务迁移/菜单脚本 |

## 测试与发布边界

在 app 仓库运行 `npm run verify:nonpay`，执行 Node 回归、三套构建、真实 Java HTTP/SQL、权限、并发、重启和隔离恢复检查。最近业务验收记录见 [复查报告](https://github.com/jiangyi3265/xinrui-tea-app/blob/main/docs/NONPAY-RECHECK-20260927.md)。该命令当前仍因未完成业务/外部环境而退出 1；不能把本地子检查通过当作发布通过。

## 简历描述示例

参与若依茶叶业务后端开发，建设会员/管理端权限隔离、业务事务与审计、订单履约和拍卖接口，使用 MySQL 与 Redis 支持 H5 和管理后台共享状态。补充并发、幂等、跨用户权限及恢复场景的接口验收。

## 安全与来源

真实配置、数据库备份、日志、上传文件、私钥及构建目录不纳入版本管理。SQL 中若依公开初始化账户仅供空库开发初始化，生产必须重建凭据并清除示例账号。尊重原若依开源许可证，保留仓库 LICENSE；本次重建 Git 历史不改变上游代码的权利归属。
