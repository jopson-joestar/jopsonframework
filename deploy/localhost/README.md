# Jorsy 本地 Docker 联调

宿主机使用 JDK 21、Maven 3.9.6、Node.js 22.18+（22.x）或 24.x、pnpm 10.33.4 构建。Docker Desktop 运行 Java 和 MySQL，Spring Boot 从 JAR 提供后台页面和同源 API。

## 构建整包

在仓库根目录运行以下 PowerShell 命令。确认 `mvn -v` 显示 Java 21。

```powershell
Push-Location .\jorsy-frontend-monorepo
pnpm install --frozen-lockfile
pnpm build:embedded
Pop-Location
mvn clean verify -Pwith-admin-web
```

一体 JAR 位于 `jorsy-admin-website/target/with-admin-web/jorsy-admin-website-with-web-1.0.0.jar`。前端编译产物位于 JAR 内的 `BOOT-INF/classes/static/admin-web/`。日常 Java 验证可从仓库根目录运行 `mvn verify`。

## 启动与登录

首次启动时，在仓库根目录运行以下命令，先启动 MySQL。

```powershell
Set-Location .\deploy\localhost
docker compose config --quiet
docker compose up -d --wait mysql
```

使用 MySQL 客户端连接 `127.0.0.1:13306`，库名、账号和密码见 [`.env`](.env)。在空库中按以下顺序完成初始化。

1. [admin-mysql-schema.sql](../../_database_schema/admin-mysql-schema.sql) 建表。
2. 在同一个 MySQL 客户端会话设置 `@admin_code` 和 `@admin_password_hash`，再执行 [admin-mysql-initdata.sql](../../_database_schema/admin-mysql-initdata.sql)。密码参数使用对应登录密码的 `{bcrypt}` 摘要。

执行完成后，仍在 `deploy/localhost` 启动 Java 服务。后台默认启用，连接参数由 Compose 从 `.env` 传入。已有初始化完成的数据卷可以直接执行这一步。

```powershell
docker compose up -d --wait admin
docker compose logs -f admin
```

看到 Java 应用启动后，打开 [Jorsy Admin](http://127.0.0.1:18085/)，使用初始化时指定的管理员账号登录。此前初始化的演示库可继续使用原演示账号和密码。[健康接口](http://127.0.0.1:18085/actuator/health) 预期返回 `UP`。按 `Ctrl+C` 退出日志跟踪。

新库首次登录后，在工作台点击「导入菜单资源」，选择 [admin-resource-tree.json](../../_database_schema/admin-resource-tree.json)。导入成功后页面会刷新菜单。

已有库从「资源管理」的「导入 JSON」入口选择同一文件。只需补齐缺失资源时，选择「增量」并只勾选「新增」。全量替换会同时清除原资源和角色授权。

为普通角色开放角色授权页时，同时授予角色查看权限，以加载角色树。权限身份维护使用用户、组织和角色的查看权限加载选择项。

## 选择运行模式

现有 Compose 默认启动完整后台。以宿主机上的一体 JAR 调试其他模式时，可在运行环境添加下面的 YAML 对应属性。Docker 中可将对应环境变量传给 `admin` 服务。

```yaml
# 完整后台，默认配置
jorsy:
  admin:
    enabled: true
    mode: full
```

```yaml
# 固定账号登录，默认无库
jorsy:
  admin:
    enabled: true
    mode: simple
    simple:
      username: ${JORSY_ADMIN_SIMPLE_USERNAME}
      password-hash: ${JORSY_ADMIN_SIMPLE_PASSWORD_HASH}
```

```yaml
# 关闭后台，独立启用业务数据库与 Ehcache
jorsy:
  admin:
    enabled: false
  datasource:
    enabled: true
spring:
  cache:
    type: jcache
    cache-names: documents
```

简易登录使用环境提供的 `{bcrypt}` 密码摘要。需要业务数据库时配置已有连接。省略 `jorsy.datasource.enabled` 时，简易登录和关闭后台均可无库启动。缓存选项和连接要求见[仓库配置说明](../../README.md#后台模式数据库和缓存)。

## 更新与停止

前端改动后先重新运行 `pnpm build:embedded`。从仓库根目录重新构建一体 JAR 后，在 `deploy/localhost` 重建 Java 容器。

```powershell
docker compose up -d --no-deps --force-recreate admin
docker compose logs -f admin
```

在 `deploy/localhost` 执行 `docker compose down` 可停止 Jorsy 并保留 MySQL 命名卷。GwCloud 有独立的 Compose 项目和端口。
