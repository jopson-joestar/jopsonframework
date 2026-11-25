# 后台站点的业务与运行

本目录继承[仓库工程规范](../AGENTS.md)。jopson-admin-website 是可独立部署的消费站点，负责启动、环境配置、组合公共能力以及本站拥有的业务。复用后台通过 admin 引入，基础能力随之从 core 接入。

## 放置站点代码

- 启动类 `AdminWebsiteApplication` 置于站点根包 `com.jopsonframework.website.admin`。业务在 `com.jopsonframework.website.admin.modules.<业务域>` 内纵向组织。例如，本站单据的审核、明细和审核权限判断归单据域。本站多个域共同使用的配置和组件归 `com.jopsonframework.website.admin.common`。只有形成明确的跨站点契约时才上移到相应 starter。
- 建立其他站点时，在自己的根包保留默认扫描，按需求引用 core 或 admin。站点自行选择启用哪些能力、如何持有数据与会话。权限资源、菜单和初始化数据由对应站点配置。

## 环境与站点验收

- 在站点环境配置中选择文档、指标及管理端点的可见性，并配置所需访问控制。本站 `dev` 开启 OpenAPI 和 Swagger UI。连接信息及凭据由目标环境注入。测试使用隔离资源或外部系统替身。
- 使用本站真实启动类和随机端口验证路由装配、配置覆盖与公开契约。当前入口见 [AdminReuseTest](src/test/java/com/jopsonframework/website/admin/AdminReuseTest.java) 和 [DevApiDocsTest](src/test/java/com/jopsonframework/website/admin/DevApiDocsTest.java)。涉及打包或启动配置时，从可执行 JAR 核对对应环境下的业务入口、健康状态及文档可见性。
- 新增业务用例时，从调用方发起请求并核对最终业务结果。以单据审核为例，还要从本站详情或列表回读审核状态。重复提交、并发冲突或无权访问时核对客户端结果及持久化数据。异步受理的任务按其结果查询或回执契约验收。
