# Jorsy Framework

Jorsy 1.0.0 使用 Java 21 和 Spring Boot 4.2.0。根 POM 聚合 `jorsy-starter-parent` 和 `jorsy-admin-website`，parent 再聚合其目录下的两个 starter。后台按单体部署，运行时依赖方向为 `jorsy-admin-website → jorsy-starter-admin → jorsy-starter-core`。后台前端位于 `jorsy-frontend-monorepo/apps/jorsy-admin-web/`。[前端启动与联调说明](jorsy-frontend-monorepo/README.zh-CN.md#安装使用)

| 模块 | 职责 |
| --- | --- |
| [`jorsy-starter-parent`](jorsy-starter-parent/pom.xml) | 构建父 POM，统一管理框架依赖版本、Java 编译、注解处理器和打包约定，也支持单独导入依赖管理。 |
| [`jorsy-starter-core`](jorsy-starter-parent/jorsy-starter-core/pom.xml) | 普通 JAR，提供响应、分页、校验、JSON、缓存、日志和 Actuator，并引入 Hutool Core、MapStruct 注解和 OpenAPI 文档能力。 |
| [`jorsy-starter-admin`](jorsy-starter-parent/jorsy-starter-admin/pom.xml) | 普通 JAR，提供完整后台和固定账号登录，默认启用完整后台。 |
| [`jorsy-admin-website`](jorsy-admin-website/pom.xml) | 可独立启动的后台站点。启动类为 `com.jorsyframework.website.admin.AdminWebsiteApplication`，同包下的 `common` 存放本站共用的配置与组件，`modules` 按本站业务功能建包。 |

## 本地构建和启动

使用 Docker Desktop 调试完整后台时，按[本地 Docker 联调说明](deploy/localhost/README.md)构建前后端一体 JAR 并启动 MySQL 和 Java 容器。

准备 JDK 21 和 Maven 3.9.6，在仓库根目录运行 `mvn -v`，确认输出中的 Java 主版本为 21。在 PowerShell 中可以按实际安装路径切换当前会话的 JDK。

```powershell
$env:JAVA_HOME = 'D:\ProgramFiles\Java\jdk21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -v
```

构建会运行 core 和后台站点的测试，并把根 POM 和各模块安装到 Maven 本地仓库，供其他工程按坐标引用。用 `dev` 模式可在无 MySQL 的环境中验证启动和接口文档。

```powershell
mvn clean install
java -jar .\jorsy-admin-website\target\jorsy-admin-website-1.0.0.jar --spring.profiles.active=dev
```

在另一终端请求健康接口，预期返回 HTTP 200，且 JSON 中的 `status` 为 `UP`。在服务终端按 `Ctrl+C` 停止。

```powershell
curl.exe -i http://localhost:8080/actuator/health
```

## 启用后台登录

后台站点默认启用登录、权限和基础数据。首次部署时，按[基础资料与权限管理](#基础资料与权限管理)在本站 MySQL 空库建表和初始化，并使用部署者指定的管理员账号和 `{bcrypt}` 密码摘要。

在运行环境注入 `JORSY_DB_URL`、`JORSY_DB_USER`、`JORSY_DB_PASSWORD`（也支持标准 `SPRING_DATASOURCE_*`），然后运行以下命令。仅本地 HTTP 联调时在当前终端设置 `$env:JORSY_COOKIE_SECURE = 'false'`。生产环境通过 HTTPS 访问同源 `/api`。

```powershell
java -jar .\jorsy-admin-website\target\jorsy-admin-website-1.0.0.jar
```

浏览器获取 `GET /auth/csrf` 返回的 `headerName` 和 `token`，在写请求头中带上令牌。以表单编码向 `POST /auth/login` 提交 `username`、`password`，成功后重新取 CSRF。认证依靠 HttpOnly 会话 Cookie，空闲 30 分钟或服务重启后需要重新登录。同一来源连续产生的失败尝试在五分钟内达到 20 次时返回 429。

| 会话接口 | 结果 |
| --- | --- |
| `GET /admin/sys-login/current` | 当前用户、身份列表、当前角色权限、数据范围和额外组织。首次登录要求账号已审核且存在有效默认身份。 |
| `GET /admin/sys-login/menu` | 当前角色的菜单树，含必要的上级目录。初始化后可从工作台、个人中心开始使用。 |
| `POST /admin/sys-login/switch-rights` | JSON 传 `sysRightsId`，仅可选择本人启用的身份。返回新的当前用户信息。 |
| `POST /auth/logout` | 销毁会话并清除 Cookie。退出后重新获取 CSRF 供下次登录使用。 |

角色授权的增减在下次受保护请求生效。登录失效返回 401，缺少功能权限或无有效默认身份返回 403。Servlet 站点引入 `jorsy-starter-admin` 后，配置 MySQL 驱动和数据源即可启用整套后台。可参考[网站 POM](jorsy-admin-website/pom.xml)及[网站配置](jorsy-admin-website/src/main/resources/application.yml)。

### 后台模式、数据库和缓存

| 配置 | 后台行为 | 数据源默认 |
| --- | --- | --- |
| 省略后台配置，或 `jorsy.admin.enabled=true` 且 `jorsy.admin.mode=full` | 使用数据库账号、权限和完整后台 | 开启，启动时检查 Admin 表 |
| `jorsy.admin.enabled=true` 且 `jorsy.admin.mode=simple` | 使用固定账号保护业务页面和接口，使用站点前端定义的业务路由 | 关闭 |
| `jorsy.admin.enabled=false` | 关闭后台登录、接口和内嵌页面入口，忽略 `mode` 和固定账号配置 | 关闭 |

简易登录在服务端配置 `jorsy.admin.simple.username` 和 `jorsy.admin.simple.password-hash`，也可通过环境变量 `JORSY_ADMIN_SIMPLE_USERNAME` 和 `JORSY_ADMIN_SIMPLE_PASSWORD_HASH` 注入。账号为 1 至 32 个字符，摘要使用 `{bcrypt}` 格式。浏览器继续使用相同的登录、退出和 CSRF 接口。当前用户响应中的 `mode` 为 `simple`，无组织、角色或可切换身份。

在简易登录或关闭后台时，设置 `jorsy.datasource.enabled=true` 并提供 `spring.datasource.*` 可启用业务数据源。完整后台需要数据源，显式关闭时启动失败。`dev` 关闭后台且默认关闭数据库，可直接使用 Swagger。

缓存独立由 `spring.cache.type` 选择。后台站点 JAR 已包含 Redis 和 Ehcache 运行依赖。

| 缓存配置 | 配套设置 |
| --- | --- |
| `caffeine`（默认） | 使用进程内缓存 |
| `redis` | 配置 `spring.data.redis.*`、缓存键前缀和需要的 `RedisCacheSpec`，见[按需接入 Redis 共享缓存](#按需接入-redis-共享缓存) |
| `jcache` | 使用 Ehcache，可通过 `spring.cache.cache-names` 声明缓存，或用 `spring.cache.jcache.config` 指定配置。多个 JCache 实现共存时用 `spring.cache.jcache.provider` 选择 `org.ehcache.jsr107.EhcacheCachingProvider` |
| `none` | 关闭业务缓存 |

引入 Admin Starter 的站点仅在选用 Redis 缓存时自动装配 Redis 连接。缓存选择独立于登录会话和登录限流。

## 接入构建父 POM

Jorsy 的独立父 POM 在 `dependencyManagement` 中导入 `spring-boot-dependencies`。属性 `spring-boot.version` 同时指定 Boot BOM 和打包插件的版本。Jorsy 管理 Java 21 的 `--release`、`-parameters`、UTF-8 和测试插件。构建时只对 `application*.yml`、`application*.yaml`、`application*.properties` 替换 `@...@`，其中的 `${...}` 留给 Spring 在运行时解析。

新站点可以继承 `jorsy-starter-parent`，使用统一的 Java 21、Lombok 和 MapStruct 编译配置。站点可声明自己的项目版本，core 和 admin 依赖的版本由父 POM 管理。原来继承 `com.jorsyframework:jorsyframework` 的工程，将 `<parent>` 更新为以下坐标。

```xml
<parent>
    <groupId>com.jorsyframework</groupId>
    <artifactId>jorsy-starter-parent</artifactId>
    <version>1.0.0</version>
    <relativePath/>
</parent>
```

已有企业父 POM 的站点，在自己的 `dependencyManagement` 中导入同一坐标即可取得依赖版本。Java 编译级别、注解处理器和打包插件由站点的构建体系配置。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.jorsyframework</groupId>
            <artifactId>jorsy-starter-parent</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 只接入 core

完成本地安装后，在 Spring Boot 4.2.0 MVC 工程中添加以下依赖。示例显式指定版本，继承或导入 Jorsy 父 POM 的站点可省略依赖的 `<version>`。站点沿用自己的 `@SpringBootApplication` 和包扫描范围。

```xml
<dependency>
    <groupId>com.jorsyframework</groupId>
    <artifactId>jorsy-starter-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

| 能力 | 使用方式 |
| --- | --- |
| 响应与分页 | JSON 接口显式返回 `R.success(data)`。对 `@ModelAttribute PageQuery` 或 `@RequestBody PageQuery` 使用 `@Valid`。页码和每页条数均须为正数，默认为第 1 页、每页 20 条。具体接口可在业务请求对象上声明页大小上限。空页用 `new PageResult<>(List.of(), total, currentPage, pageSize)`。 |
| 异常 | 参数校验失败和非法 JSON 分别返回 HTTP 400、业务码 2001 和 2002。抛出 `BusinessException` 可向客户端返回业务提示与业务码 3001。未知异常返回 HTTP 500 和业务码 5001。 |
| JSON | Jackson 3 读写 `yyyy-MM-dd` 和 `yyyy-MM-dd HH:mm:ss`，带偏移的时间保留偏移。`BigDecimal` 输出去掉末尾零的普通十进制字符串。 |
| 缓存 | 在由 Spring 代理的 Bean 上使用 `@Cacheable`、`@CacheEvict`。默认每个 Caffeine 缓存最多 1,000 条，写入后 30 分钟过期。多实例站点可[按需接入 Redis 共享缓存](#按需接入-redis-共享缓存)。 |
| 请求日志 | 响应头 `X-Request-Id` 与 MDC 的 `requestId` 对应。接收 1–64 个 ASCII 字母、数字、点、下划线或连字符，其他值自动换为 UUID。Spring Boot 默认任务执行器可传递该标识；自建执行器可使用 `RequestIdTaskDecorator`。 |
| 监控 | `GET /actuator/health` 返回健康状态。默认通过 HTTP 开放 health，其他端点由站点使用 `management.*` 配置并设置访问控制。 |

分页查询成功时返回以下 JSON。

```json
{"code":0,"message":"操作成功","data":{"items":[],"total":0,"currentPage":1,"pageSize":20}}
```

### 独立请求类与业务枚举

创建和更新分别声明允许提交的字段，在 Controller 的两个 `@RequestBody` 参数上使用 `@Valid`。例如单据编号在创建时必填，站点通过路径参数确定要更新的单据，审核状态由审核用例维护。

```java
record CreateDocumentParam(@NotBlank String number, @NotBlank @Size(max = 80) String title) {}
record UpdateDocumentParam(@NotBlank @Size(max = 80) String title, @Size(max = 200) String remark) {}
```

固定的业务状态实现 `com.jorsyframework.starter.core.enums.ValueEnum<V>`。同一枚举选择一种类型，每个常量声明唯一的非空字符串或 `Integer` 编码。请求类直接使用枚举类型。

```java
enum DocumentStatus implements ValueEnum<String> {
    DRAFT("draft"), APPROVED("approved");

    private final String code;
    DocumentStatus(String code) { this.code = code; }
    public String getValue() { return code; }
}

record DocumentQuery(DocumentStatus status) {}
```

查询参数 `?status=approved`、路径中的 `approved`、JSON 字段 `"status":"approved"` 都绑定为 `DocumentStatus.APPROVED`。JSON 响应和 OpenAPI 使用相同的编码与类型。整数编码在 JSON 中写作数字，例如 `20`。编码精确匹配，未知编码或 JSON 编码类型不符返回 HTTP 400 和业务码 `2001`，JSON 语法损坏返回 `400 / 2002`。可空、必填由具体接口声明。站点维护的动态字典由请求类接收编码，再由业务层检查存在性、启用状态及访问范围。

### 分页列表批量补齐展示名称

用 `com.jorsyframework.starter.core.convert.ConvertProvider<K,V>` 对接业务域的批量查名方法。先查询调用者可见的一页数据，再用 `ConvertContext.load` 对当前页的非空键去重并查询一次。将上下文作为 MapStruct 的 `@Context` 参数传给列表映射与行映射。

```java
var names = ConvertContext.load(page.items(), Document::reviewerId, reviewerService::findNamesByIds);
var items = documentMapper.toRows(page.items(), names);
return new PageResult<>(items, page.total(), page.currentPage(), page.pageSize());
```

行映射通过 `names.get(reviewerId)` 读取本页结果，找不到的名称为 `null`，保留该行和原顺序。空页跳过查名，查询失败交给现有系统异常处理。每次转换创建自己的上下文，数据权限由 Provider 落实。完整示例见[测试站点](jorsy-starter-parent/jorsy-starter-core/src/test/java/org/example/jorsy/DocumentEndpoints.java)和[列表 Mapper](jorsy-starter-parent/jorsy-starter-core/src/test/java/org/example/jorsy/DocumentRowMapper.java)。

## Hutool、MapStruct 与 Lombok

引用 core 后即可使用 Hutool 5.8.48 的 `StrUtil`、`CollUtil` 等工具。业务对象转换可以声明 `@Mapper(componentModel = "spring")`，再按 Mapper 接口注入生成的 Spring Bean。继承 `jorsy-starter-parent` 的模块已具备所需的 Lombok 和注解处理器配置。

选用其他父 POM 的站点，在**编译 Mapper 的模块**配置注解处理器。以下示例适用于已导入 Jorsy 依赖管理的站点。编译插件从依赖管理中解析 Lombok 处理器版本，使其与编译时的 Lombok 依赖一致。

```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.16.0</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.6.3</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

外部站点在应用模块启用 `spring-boot-maven-plugin` 打可执行 JAR。继承 Jorsy 父 POM 时会沿用其 Lombok 打包排除配置，使用其他父 POM 时可参照同一插件的 `excludes`。MapStruct 生成的实现类随应用打包。

## 接入 admin

需要后台接口的站点直接引用 admin，Maven 会传递引入 core。继承或导入 Jorsy 父 POM 时同样可省略依赖版本。各站点独立配置、部署和管理自己的数据。

```xml
<dependency>
    <groupId>com.jorsyframework</groupId>
    <artifactId>jorsy-starter-admin</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 开发环境接口文档

core 默认关闭 OpenAPI 文档和 Swagger UI。后台站点的 `dev` 配置关闭后台装配并启用两者，可在无 MySQL 的环境中启动。构建后运行以下命令，然后打开 [Swagger UI](http://localhost:8080/swagger-ui/index.html) 或 [OpenAPI JSON](http://localhost:8080/v3/api-docs)。

```powershell
java -jar .\jorsy-admin-website\target\jorsy-admin-website-1.0.0.jar --spring.profiles.active=dev
```

其他站点可在自己的 `application-dev.yml` 中启用相同入口。

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

## 按需接入 MySQL

需要持久化的站点在引用 core 或 admin 的基础上增加以下依赖。版本由继承或导入的 `jorsy-starter-parent` 管理。只使用 Web 能力的站点可以直接启动，无需配置数据源。

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

使用标准数据源配置即可接入 HikariCP。在站点自己的 Mapper 上标记 `@Mapper`，或在站点配置中指定 `@MapperScan`。业务表和迁移脚本由所属站点维护。

```yaml
spring:
  datasource:
    url: ${JORSY_DB_URL} # jdbc:mysql://主机:端口/数据库
    username: ${JORSY_DB_USER}
    password: ${JORSY_DB_PASSWORD}
```

### 查询与写入

- 在数据服务中调用 `MybatisPages.toPage(query)` 创建 MyBatis-Plus 分页对象，再用 `MybatisPages.toResult(page)` 返回公共分页结果。当前页和每页条数沿用 `PageQuery`，查询条件同时作用于数据和总数。
- 实体自行声明 `@TableId`。默认字符串主键使用 `ASSIGN_UUID`，自增表可声明 `@TableId(type = IdType.AUTO)`。
- 继承 `AuditEntity` 的实体映射 `create_id`、`create_time`、`update_id`、`update_time` 四列。业务用例按可信身份填写创建人和修改人，普通实体更新会保留创建人。继承 `CrudEntity` 的表还需提供 `version`（BIGINT）和 `deleted`（0/1）列，用于乐观锁和逻辑删除。普通实体及只继承 `AuditEntity` 的实体仍支持物理删除。
- 升级已有站点时，逐表检查直接或间接继承 `AuditEntity` 的实体。原先仅有两个时间列的表，须先补齐可空的 `create_id CHAR(32)` 和 `update_id CHAR(32)`，再升级应用。历史记录的操作者 ID 可以留空。`sys_user` 的建表基线已包含这两列。
- 带实体的写入由审计处理器填充时间。完整编辑的默认更新策略是 `ALWAYS`，传入的 null 可将允许编辑的列清空。局部命令用显式 `set`，并按用例带上原状态或版本条件。仅使用 Wrapper 的更新须自行设置需要变更的审计时间和版本。单条更新检查影响行数，多表写入由 Facade 的事务统筹。
- 默认插件依次为乐观锁、防全表更新与删除、分页。站点可以分别提供自己的 `MybatisPlusInterceptor` 或 `MetaObjectHandler`。主键和更新策略也可通过 `mybatis-plus.global-config.db-config.id-type`、`update-strategy` 覆盖。

### 系统用户表（starter-admin）

消费站点引入 `jorsy-starter-admin`，并按本节配置 MySQL 驱动和数据源。starter 提供 MyBatis-Plus、分页解析器和 Spring Security，并自动注册 `com.jorsyframework.starter.admin.modules.rights.mapper.SysUserMapper`。站点自己的 Mapper 仍按原有范围扫描。

后台业务按 `Controller → ISys*Facade → ISys*Service → Mapper` 调用。数据库 Mapper 标注 MyBatis 的 `@Mapper`，Service 和 Facade 实现标注 `@Service`，各业务对象的转换器标注 MapStruct 的 `@Mapper(componentModel = "spring")`。新增类放在业务域对应的包中即可自动发现。直接引用旧版具体 Service 的 Java 调用方请改为注入同域 `service` 包中的 `ISys*Service` 接口。

用户表定义位于 [Admin MySQL 建表脚本](_database_schema/admin-mysql-schema.sql)。部署方在空库中显式执行建表脚本；已有同名表时 MySQL 返回错误。

- 登录账号必填，使用 `utf8mb4_0900_ai_ci` 比较。每个账号最多一条未删除记录，锁定或作废期间仍占用账号，逻辑删除后可重新使用。
- `SysUserStatus` 的业务编码为作废 `-100`、未审核 `0`、锁定 `1`、审核通过 `10`。实体以 32 位 UUID 为主键，沿用 core 的时间审计、版本检查和逻辑删除。
- `selectById` 等默认查询省略密码摘要，JSON 也排除此字段。普通实体更新保留登录账号、密码摘要和创建人。管理用例从站点提供的可信身份填写审计字段。

### 系统用户管理接口

用户数据层及业务接口位于 `com.jorsyframework.starter.admin.modules.rights`。升级已有消费代码时，将用户实体、枚举和 Mapper 的 import 切到该权限域。后台启用后可注入 `ISysUserService` 和 `ISysUserFacade`，管理接口从内置会话读取可信操作者。

完整后台从已验证的会话提供操作者，并校验 CSRF。权限使用完整的 `Admin:SysUser:操作名`。关闭 Jorsy 后台的站点自行负责业务认证与授权。

以下操作均使用 `POST /admin/sys-user/<动作>`。与 gwcloud 管理页对接时，将删除请求改为 POST，创建请求传每个账号的 `initialPassword`，编辑、状态变更和删除传上次读取的 `version`。头像 ID 只读。成功时读取 `code=0` 和 `data`。

| 动作 | 输入 | 权限及结果 |
| --- | --- | --- |
| `page` | JSON，例如 `{"currentPage":1,"pageSize":20,"sysUserCode":"ops","sysUserStatus":10}` | `View`。返回 `items/total/currentPage/pageSize`；支持账号、姓名、状态和创建时间范围，结束时间含该秒。 |
| `get-by-id` | 请求参数 `sysUserId` | `View`。返回资料、状态、版本和审计信息。 |
| `create` | JSON 必填 `sysUserCode/sysUserName/initialPassword`，选填 `sysUserMobile/sysUserEmail/sysUserQq` | `Create`。生成未审核的普通账号并返回 ID。初始密码最多 72 个 UTF-8 字节，默认以带算法标识的 bcrypt 保存。 |
| `update-by-id` | JSON 包含 `sysUserId/version/sysUserName` 及可选联系信息 | `Update`。仅可编辑未审核用户，缺省或为 null 的可选联系字段会清空，返回用户 ID。 |
| `update-status` | 请求参数，例如 `?sysUserId=<用户ID>&sysStatus=10&version=0` | 审核 `0→10: Audit`，撤审 `10→0: CancelAudit`，停用 `10→1: Disable`，启用 `1→10: Enable`。 |
| `delete-by-id` | 请求参数 `sysUserId/version` | `Delete`。逻辑删除未审核或作废的用户。 |

撤审、停用和删除会保护本人及内置账号。管理接口已开放时，未登录返回 401，权限不足或账号受保护返回 403，记录缺失返回 404，重名、无效流转及旧版本返回 409。收到 409 后重新查询记录再决定操作。输入校验和非法 JSON 分别沿用 400 下的业务码 2001、2002。

### 基础资料与权限管理

两份 SQL 统一维护在仓库根目录的 `_database_schema/`。手工初始化时，在仓库根目录打开 MySQL 8 客户端并连接目标库。

1. 在空库执行 [admin-mysql-schema.sql](_database_schema/admin-mysql-schema.sql)，建立用户、基础资料和权限共 11 张表。
2. 在同一个客户端会话设置 `@admin_code` 和 `@admin_password_hash`，再执行 [admin-mysql-initdata.sql](_database_schema/admin-mysql-initdata.sql)。账号由部署者指定，摘要使用 Spring Security 的 `{bcrypt}` 格式。执行账号需具备创建、执行和删除存储过程的权限。

初始数据包含内置用户、根组织 `0001`、角色 `administrator` 和已启用的默认身份。用相同账号重跑时保留原密码摘要，普通账号占用指定名称时管理员初始化回滚。

后台资源统一维护在 [admin-resource-tree.json](_database_schema/admin-resource-tree.json)，文件用 `children` 表示资源层级。首次登录后，内置管理员从工作台进入「导入菜单资源」，选择该文件并提交。导入后可在「资源管理」维护菜单。已有库补齐缺失资源时，选择「增量」并只勾选「新增」。

组织、角色和资源树按查询顺序展示从根可达的节点。父级缺失或形成闭环的分支会被略过，其余分支照常展示。角色授权树先按可授予资源过滤，再按相同规则组树；已勾选的资源 ID 从 `checkedKeys` 读取。

后台启用后，可通过 HTTP 或直接注入对应的 `ISys*Facade` 调用。所有路由均以 `/admin/` 开头。下面的权限后缀与资源名组合，例如 `Admin:SysDict:Sync`。常规动作 `page/tree/get-by-id` 需要 `View`，`create/update-by-id/delete-by-id` 分别需要 `Create/Update/Delete`。

| 路由与权限资源 | 提供的动作 | 业务约定 |
| --- | --- | --- |
| `sys-config` · `SysConfig` | `page/get-by-id/create/update-by-id/delete-by-id/get-value-by-code` | 按 `sysConfigCode` 取本站配置值，需要 `View`。 |
| `sys-serial` · `SysSerial` | `page/get-by-id/create/update-by-id/delete-by-id/next-value` | 按 `serialCode` 取号，需要 `NextValue`。独立事务锁行分配，支持日期分段、前后缀及 1～18 位序号；业务事务回滚后已分配的号仍被占用。`SNOWFLAKE` 和缺省规则使用雪花号，耗尽或取号异常返回备用 ObjectId。 |
| `sys-dict` · `SysDict` | `page/get-by-id/create/update-by-id/delete-by-id/options/sync-by-enum` | `options` 按 `sysDictCode` 查询，需要 `View`。`sync-by-enum` 需要 `Sync`，请求示例 `{"operations":["ADD","UPDATE"]}`，可选 `DELETE`。同步范围为已登记的用户状态、组织类型、资源类型和数据范围。 |
| `sys-dict-item` · `SysDictItem` | `page/get-by-id/create/update-by-id/delete-by-id` | 锁定字典保护项值。删除普通字典时，其明细一起逻辑删除。 |
| `sys-org` · `SysOrg` | `tree/get-by-id/create/update-by-id/delete-by-id/get-name-by-code/get-names-by-codes` | 两个查名动作需要 `View`。单个使用 `GET ?sysOrgCode=0001`，批量提交 `{"codes":["0001"]}`。下级组织编码在父编码后追加四位序号。 |
| `sys-res` · `SysRes` | `tree/get-by-id/create/update-by-id/delete-by-id/import-json` | 导入需要 `Import`。传 `mode` 与嵌套的 `resources`。`FULL` 在同一事务替换整棵树，并清理旧授权；`INCREMENTAL` 按 `routeName` 匹配，传 `operations` 选择 `ADD/UPDATE/DELETE`。勾选删除会移除输入树中缺少的节点及其授权。 |
| `sys-role` · `SysRole` | `page/tree/get-by-id/create/update-by-id/delete-by-id` | 在指定父角色下创建子角色。内置管理员角色受保护。 |
| `sys-role-auth` · `SysRoleAuth` | `list-auth-res/auth-res` | 读取需要 `View`，返回 `rows/checkedKeys/assignableKeys/version`；保存需要 `AuthRes`，JSON 传 `sysRoleId/version/sysResIds`。子角色只能取得父角色可授予的资源。撤销时先收回下级持有的授权。 |
| `sys-rights` · `SysRights` | `page/get-by-id/create/update-by-id/delete-by-id/update-enabled` | 身份整单提交用户、组织、角色、数据范围及 `extraOrgIds`。范围用字符串 `"1"/"2"/"3"/"9"` 表示本人、本组织、本组织及下级、全部。`update-enabled` 传 `sysRightsId/version/enabled`，启用需要 `Enable`，停用需要 `Disable`。 |

除组织单个查名使用 GET，其余动作使用 POST。分页接收 JSON，返回 `data.items/total/currentPage/pageSize`。详情通过查询参数传对应主键，删除还需传 `version`；编辑、启停和授权保存使用上次读到的版本。身份编辑同时改变启用状态时还需对应的启用或停用权限。提交成功后重新读取版本。越权返回 403，版本过期、业务键重复或仍有引用时返回 409。配置读取、流水取号和组织查名可分别通过 `ISysConfigFacade`、`ISysSerialFacade`、`ISysOrgFacade` 在单体内调用。

### Druid 与 P6Spy

切换连接池时，在站点增加 `com.alibaba:druid-spring-boot-4-starter`，保留原数据源的 URL 和凭据，并增加以下配置。Druid 监控页面默认关闭。

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      max-active: 16
```

启用 SQL 日志时，增加运行时依赖 `p6spy:p6spy`，将数据源 URL 前缀改为 `jdbc:p6spy:mysql:`，设置 `spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver`。HikariCP 和 Druid 均可使用。将以下内容放在**消费站点**的 `src/main/resources/spy.properties` 中。

```properties
appender=com.p6spy.engine.spy.appender.Slf4JLogger
logMessageFormat=com.jorsyframework.starter.core.mybatis.log.JorsySqlFormat
excludecategories=info,debug,result,resultset,commit,rollback
```

`p6spy` 日志记录请求标识、SQL 操作类型和毫秒耗时。简单参数化语句记录保留 `?` 的模板。含字面值或注释、超长和无法判定的语句只记录摘要。展开后的绑定值和连接 URL 均排除在输出之外。

## 按需接入 Redis 共享缓存

需要多个实例共享查询结果时，在消费站点引入以下依赖，版本沿用 Jorsy parent 的 Spring Boot BOM。只引用 core 或 admin 的站点默认使用 Caffeine。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  cache:
    type: redis
    redis:
      key-prefix: "orders:prod:"
      time-to-live: 5m
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      # 有认证的实例在部署环境提供 spring.data.redis.password
```

同一站点、同一环境的实例使用同一个前缀，其他站点或环境使用独立前缀。上例中，ID 为 42 的详情键为 `orders:prod:documents.detail::42`。启用 Redis 时必须显式指定非空前缀并保留 `use-key-prefix=true`。默认写入后 5 分钟过期，读取命中不会续期；覆盖值至少为 1 毫秒。站点声明自己的 `CacheManager` 时，由站点决定这些策略。

### 声明值类型

在站点配置类中为每个缓存名声明一个 `com.jorsyframework.starter.core.cache.redis.RedisCacheSpec` Bean。同一个缓存名对应同一种值类型；启动时检查重名，使用未声明的缓存名时会报错。泛型值可以使用 Spring 的 `ParameterizedTypeReference`。

```java
@Bean
RedisCacheSpec documentDetail() {
    return new RedisCacheSpec("documents.detail", DocumentView.class);
}

@Bean
RedisCacheSpec documentList() {
    return new RedisCacheSpec("documents.list", new ParameterizedTypeReference<List<DocumentView>>() {}.getType());
}
```

键使用字符串，值由站点的 Jackson 3 mapper 按声明的类型读写。业务枚举、时间及十进制金额沿用站点的 JSON 契约。不同实例部署兼容的 DTO；若缓存值格式发生不兼容变更，可以使用新缓存名并让旧键自然过期。

### 查询、事务与故障

以下方法分别位于受 Spring 管理的查询 Service、写入 Service 和 Facade。示例详情在站点内共享，涉及租户或用户数据范围时，业务域须将相应范围纳入缓存键，按权限读取数据。

```java
// 查询 Service，空结果跳过缓存
@Cacheable(cacheNames = "documents.detail", key = "#id", unless = "#result == null")
public DocumentView find(long id) {
    return documents.findVisibleDetail(id);
}

// 写入 Service，Facade 通过注入的 Bean 调用
@CacheEvict(cacheNames = "documents.detail", key = "#id")
public void changeTitle(long id, String title) {
    documents.updateTitle(id, title);
}

// Facade
@Transactional
public void edit(long id, String title) {
    edits.changeTitle(id, title);
    notes.recordChange(id, title);
}
```

- 正常提交后执行缓存失效，其他实例下次回源读取新值。明细写入失败导致事务回滚时，事务中排队的缓存填充和失效操作一同取消。涉及多个查询缓存时，写用例同时失效受影响的键。
- Redis 连接失败或超时时，读取回源，填充失败仍返回查询结果。提交后的失效失败保留业务成功结果，记录缓存名、操作与 `requestId`。Redis 恢复后仍可能读到旧条目，直至剩余 TTL 到期。配置错误、JSON 错误和数据库错误进入现有异常处理。
- 这套策略适合允许短时旧值的展示数据。权限判断与状态流转读取所属域的权威数据。缓存注解使用普通读取及成功返回后的 `@CacheEvict`；`beforeInvocation=true` 等立即执行的操作有单独的事务语义。

## 可选配置与后续适配

使用 Spring Boot 配置调整 Caffeine 容量与过期时间。站点也可以声明自己的 `CacheManager` Bean。

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=2000,expireAfterWrite=10m
```

下表是后续批次的设计范围。实现时将第三方适配放入 core 对应包，通过可选依赖和条件装配接入。每项随实现补充站点依赖、配置与启用示例，并验证启用和未启用两种运行状态。

| 批次 | 计划能力 | 验收重点 |
| --- | --- | --- |
| 2 · 后续 | 通用约束与工具、ID 生成、SQL Server 适配，以及 Redisson、JetCache 和分布式锁 | 使用隔离 SQL Server 验证方言、分页与事务，使用隔离 Redis 验证两级缓存失效和锁竞争。 |
| 3 | Spring Security、OAuth2/OIDC、数据权限；Forest、OpenFeign 与开放接口认证 | 认证/授权错误、行级隔离、调用上下文、超时和防重放。后台用户、角色和菜单由 admin 管理。 |
| 4 | 文件存储、Excel、WebSocket/STOMP、短信、XXL-JOB；Forest/Feign/RabbitMQ 日志、Arthas、管理端点访问策略和 Sentinel | 临时存储及供应商替身验收，验证异步追踪、限流与管理权限。 |

可选配置解密随站点需求接入，第三方依赖版本由 `jorsy-starter-parent` 统一管理。发布每项适配前核对 Java 21、Boot 4.2.0 和 Jackson 3 的兼容性。

## 验收

在仓库根目录运行以下命令。core 测试在随机端口验证公共 MVC 能力和未注册路径的 404 响应。`AdminReuseTest` 验证后台站点默认启动后 `/actuator/health` 返回 HTTP 200 和状态 `UP`。

```powershell
mvn clean verify
```

单独构建父 POM 和两个 starter 时，在仓库根目录执行 `mvn -f jorsy-starter-parent/pom.xml clean verify`。

可用以下命令检查 starter 产物。输出中应包含 `com/jorsyframework/starter/admin/common/autoconfigure/JorsyAdminAutoConfiguration.class` 和 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

```powershell
jar tf .\jorsy-starter-parent\jorsy-starter-admin\target\jorsy-starter-admin-1.0.0.jar
```
