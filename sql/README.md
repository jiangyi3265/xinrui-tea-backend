# 数据库文件与安全导入

- `ry_20250522.sql`：若依基础表、系统菜单、字典与初始化配置；含 DROP TABLE，只能用于全新空库。
- `tea_business.sql`：茶叶共享状态、审计、凭证唯一性及后台业务菜单/权限。
- `tea_seed.public.json`：新部署专用公开数据，包含两个明确标注的零库存示例商品、分类、初始化公告；会员、会话、订单、余额、出价和收款信息均不导入。

推荐通过 app 仓库的 [一键部署](https://github.com/jiangyi3265/xinrui-tea-app/blob/main/deploy/README.md) 执行。独立初始化类 `com.ruoyi.web.tea.DeploymentInitializer` 会验证新库、设置随机管理员密码并标记完成，重复执行不重置数据，非空/中断状态拒绝覆盖。

公开 JSON 不是当前真实业务数据库的备份。真实备份、用户资料和凭据只允许私密迁移，不得提交到这个 Public 仓库。已有数据库的升级不能直接重跑基础初始化 SQL。
