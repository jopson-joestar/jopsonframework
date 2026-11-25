-- Initialize the Jopson Admin tables in an empty MySQL database.
SET NAMES utf8mb4;

CREATE TABLE sys_user (
    sys_user_id     CHAR(32)     NOT NULL COMMENT '用户主键',
    sys_user_code   VARCHAR(50)  NOT NULL COMMENT '登录账号',
    sys_user_pwd    VARCHAR(500) DEFAULT NULL COMMENT '密码摘要',
    sys_user_name   VARCHAR(100) DEFAULT NULL COMMENT '用户名称',
    sys_user_mobile VARCHAR(30)  DEFAULT NULL COMMENT '手机',
    sys_user_email  VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    sys_user_qq     VARCHAR(30)  DEFAULT NULL COMMENT 'QQ号',
    avatar_file_id  CHAR(32)     DEFAULT NULL COMMENT '头像文件主键',
    sys_user_status TINYINT      DEFAULT NULL COMMENT '用户状态',
    sys_data        BIT(1)       DEFAULT NULL COMMENT '系统数据',
    create_id       CHAR(32)     DEFAULT NULL COMMENT '创建人主键',
    create_time     DATETIME(3)  DEFAULT NULL COMMENT '创建时间',
    update_id       CHAR(32)     DEFAULT NULL COMMENT '修改人主键',
    update_time     DATETIME(3)  DEFAULT NULL COMMENT '修改时间',
    version         BIGINT       NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted         BIT(1)       NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_user_id),
    CONSTRAINT chk_sys_user_code_nonblank CHECK (CHAR_LENGTH(TRIM(sys_user_code)) > 0),
    UNIQUE KEY uk_sys_user_active_code ((CASE WHEN deleted = b'0' THEN sys_user_code END)),
    KEY idx_sys_user_code_deleted (sys_user_code, deleted),
    KEY idx_sys_user_mobile_deleted (sys_user_mobile, deleted),
    KEY idx_sys_user_deleted_create_time (deleted, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='[系统] 系统用户';

CREATE TABLE sys_config (
    sys_config_id       CHAR(32)      NOT NULL COMMENT '配置主键',
    sys_config_code     VARCHAR(100)  NOT NULL COMMENT '配置代码',
    sys_config_name     VARCHAR(100)  DEFAULT NULL COMMENT '配置名称',
    sys_config_value    VARCHAR(1000) DEFAULT NULL COMMENT '配置值',
    remark              VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_config_id),
    KEY idx_sys_config_code_deleted (sys_config_code, deleted),
    KEY idx_sys_config_deleted_create_time (deleted, create_time),
    UNIQUE KEY uk_sys_config_active ((CASE WHEN deleted = b'0' THEN sys_config_code END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 系统配置' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_dict_item (
    sys_dict_item_id    CHAR(32)      NOT NULL COMMENT '字典明细主键',
    sys_dict_id         CHAR(32)      NOT NULL COMMENT '字典主键',
    dict_item_value     VARCHAR(50)   NOT NULL COMMENT '明细值',
    dict_item_label     VARCHAR(100)  DEFAULT NULL COMMENT '明细标签',
    sort_no             INT           DEFAULT NULL COMMENT '排序序号',
    dict_item_extra     JSON          NULL COMMENT '明细扩展数据',
    label_color         VARCHAR(50)   DEFAULT NULL COMMENT '标签颜色',
    label_class         VARCHAR(100)  DEFAULT NULL COMMENT '标签样式',
    disabled            BIT(1)        NULL DEFAULT b'0' COMMENT '是否禁用',
    remark              VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_dict_item_id),
    KEY idx_sys_dict_item_dict_value_deleted (sys_dict_id, dict_item_value, deleted),
    KEY idx_sys_dict_item_dict_sort_deleted (sys_dict_id, sort_no, deleted),
    KEY idx_sys_dict_item_value_deleted (dict_item_value, deleted),
    UNIQUE KEY uk_sys_dict_item_active ((CASE WHEN deleted = b'0' THEN sys_dict_id END), (CASE WHEN deleted = b'0' THEN dict_item_value END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 字典明细' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_dict (
    sys_dict_id         CHAR(32)      NOT NULL COMMENT '字典主键',
    sys_dict_code       VARCHAR(50)   NOT NULL COMMENT '字典代码',
    sys_dict_name       VARCHAR(100)  DEFAULT NULL COMMENT '字典名称',
    locked              BIT(1)        NULL DEFAULT b'0' COMMENT '是否锁定',
    remark              VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_dict_id),
    KEY idx_sys_dict_code_deleted (sys_dict_code, deleted),
    KEY idx_sys_dict_deleted_create_time (deleted, create_time),
    UNIQUE KEY uk_sys_dict_active ((CASE WHEN deleted = b'0' THEN sys_dict_code END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 数据字典' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_serial (
    sys_serial_id       CHAR(32)      NOT NULL COMMENT '序列主键',
    serial_code         VARCHAR(50)   NOT NULL COMMENT '序列代码',
    serial_name         VARCHAR(100)  DEFAULT NULL COMMENT '序列名称',
    serial_prefix       VARCHAR(50)   DEFAULT NULL COMMENT '序列前缀',
    serial_suffix       VARCHAR(50)   DEFAULT NULL COMMENT '序列后缀',
    serial_digit        INT           DEFAULT NULL COMMENT '序列位数',
    date_format         VARCHAR(50)   DEFAULT NULL COMMENT '日期格式化',
    last_serial         JSON          NULL COMMENT '最后序号',
    sys_data            BIT(1)        DEFAULT NULL COMMENT '系统数据',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_serial_id),
    KEY idx_sys_serial_code_deleted (serial_code, deleted),
    KEY idx_sys_serial_deleted_create_time (deleted, create_time),
    UNIQUE KEY uk_sys_serial_active ((CASE WHEN deleted = b'0' THEN serial_code END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 系统流水号' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_org (
    sys_org_id          CHAR(32)      NOT NULL COMMENT '组织主键',
    parent_id           CHAR(32)      DEFAULT NULL COMMENT '父级主键',
    sys_org_code        VARCHAR(50)   NOT NULL COMMENT '组织代码',
    sys_org_name        VARCHAR(100)  DEFAULT NULL COMMENT '组织名称',
    sys_org_type        CHAR(1)       DEFAULT NULL COMMENT '类型代码',
    sort_no             INT           DEFAULT NULL COMMENT '排序序号',
    sys_data            BIT(1)        DEFAULT NULL COMMENT '系统数据',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_org_id),
    KEY idx_sys_org_code_deleted (sys_org_code, deleted),
    KEY idx_sys_org_parent_sort_deleted (parent_id, sort_no, deleted),
    KEY idx_sys_org_sort_deleted (sort_no, deleted),
    UNIQUE KEY uk_sys_org_active ((CASE WHEN deleted = b'0' THEN sys_org_code END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 系统组织' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_res (
    sys_res_id          CHAR(32)      NOT NULL COMMENT '资源主键',
    parent_id           CHAR(32)      DEFAULT NULL COMMENT '父级主键，根节点为空',
    sort_no             INT           DEFAULT NULL COMMENT '排序序号',
    sys_res_type        VARCHAR(10)   DEFAULT NULL COMMENT '资源类型',
    permission          VARCHAR(100)  DEFAULT NULL COMMENT '权限标识',
    route_name          VARCHAR(100)  NOT NULL COMMENT '路由名称',
    route_path          VARCHAR(500)  DEFAULT NULL COMMENT '路由路径',
    component           VARCHAR(500)  DEFAULT NULL COMMENT 'Vue组件',
    redirect            VARCHAR(500)  DEFAULT NULL COMMENT '路由重定向',
    route_meta          JSON          NULL COMMENT 'Vben路由元数据',
    remark              VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_res_id),
    KEY idx_sys_res_sort_deleted (sort_no, deleted),
    KEY idx_sys_res_parent_deleted (parent_id, deleted),
    KEY idx_sys_res_type_sort_deleted (sys_res_type, sort_no, deleted),
    UNIQUE KEY uk_sys_res_active ((CASE WHEN deleted = b'0' THEN route_name END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 系统资源' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_rights_extra (
    sys_rights_extra_id CHAR(32)      NOT NULL COMMENT '权限额外组织主键',
    sys_rights_id       CHAR(32)      NOT NULL COMMENT '权限身份主键',
    sys_org_id          CHAR(32)      NOT NULL COMMENT '额外可查看组织主键',
    sys_org_code        VARCHAR(50)   DEFAULT NULL COMMENT '额外可查看组织编码',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_rights_extra_id),
    KEY idx_sys_rights_extra_rights_org_deleted (sys_rights_id, sys_org_id, deleted),
    KEY idx_sys_rights_extra_rights (sys_rights_id, deleted),
    KEY idx_sys_rights_extra_org_code (sys_org_code, deleted),
    UNIQUE KEY uk_sys_rights_extra_active ((CASE WHEN deleted = b'0' THEN sys_rights_id END), (CASE WHEN deleted = b'0' THEN sys_org_id END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 权限额外组织' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_rights (
    sys_rights_id       CHAR(32)      NOT NULL COMMENT '权限身份主键',
    sys_user_id         CHAR(32)      NOT NULL COMMENT '用户主键',
    sys_user_code       VARCHAR(50)   DEFAULT NULL COMMENT '用户账号',
    sys_org_id          CHAR(32)      NOT NULL COMMENT '组织主键',
    sys_org_code        VARCHAR(50)   DEFAULT NULL COMMENT '组织编码',
    sys_role_id         CHAR(32)      NOT NULL COMMENT '角色主键',
    sys_role_code       VARCHAR(50)   DEFAULT NULL COMMENT '角色编码',
    data_scope          CHAR(1)       DEFAULT NULL COMMENT '基础数据权限：1仅本人 2本组织 3本组织及下级 9全部',
    enabled             BIT(1)        NULL DEFAULT b'1' COMMENT '是否启用',
    defaulted           BIT(1)        NULL DEFAULT b'0' COMMENT '是否默认',
    sys_data            BIT(1)        NULL DEFAULT b'0' COMMENT '系统数据',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_rights_id),
    KEY idx_sys_rights_user_org_role_deleted (sys_user_id, sys_org_id, sys_role_id, deleted),
    KEY idx_sys_rights_user_code (sys_user_code, deleted),
    KEY idx_sys_rights_user_enabled (sys_user_id, enabled, deleted),
    KEY idx_sys_rights_user_defaulted (sys_user_id, defaulted, enabled, deleted),
    KEY idx_sys_rights_org_code (sys_org_code, deleted),
    KEY idx_sys_rights_role_code (sys_role_code, deleted),
    KEY idx_sys_rights_role_enabled_deleted (sys_role_id, enabled, deleted),
    UNIQUE KEY uk_sys_rights_active ((CASE WHEN deleted = b'0' THEN sys_user_id END), (CASE WHEN deleted = b'0' THEN sys_org_id END), (CASE WHEN deleted = b'0' THEN sys_role_id END)),
    UNIQUE KEY uk_sys_rights_default ((CASE WHEN deleted = b'0' AND defaulted = b'1' THEN sys_user_id END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 权限身份' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_role_auth (
    sys_role_auth_id    CHAR(32)      NOT NULL COMMENT '授权主键',
    sys_role_id         CHAR(32)      NOT NULL COMMENT '角色主键',
    sys_res_id          CHAR(32)      NOT NULL COMMENT '资源主键',
    PRIMARY KEY (sys_role_auth_id),
    KEY idx_sys_role_auth_role (sys_role_id),
    KEY idx_sys_role_auth_res (sys_res_id),
    UNIQUE KEY uk_sys_role_auth_pair (sys_role_id, sys_res_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 角色授权' ROW_FORMAT=DYNAMIC;

CREATE TABLE sys_role (
    sys_role_id         CHAR(32)      NOT NULL COMMENT '角色主键',
    parent_id           CHAR(32)      DEFAULT NULL COMMENT '父级角色主键',
    sys_role_code       VARCHAR(50)   NOT NULL COMMENT '角色代码',
    sys_role_name       VARCHAR(100)  DEFAULT NULL COMMENT '角色名称',
    remark              VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    sys_data            BIT(1)        DEFAULT NULL COMMENT '系统数据',
    create_id           CHAR(32)      DEFAULT NULL COMMENT '创建人主键',
    create_time         DATETIME(3)   DEFAULT NULL COMMENT '创建时间',
    update_id           CHAR(32)      DEFAULT NULL COMMENT '修改人主键',
    update_time         DATETIME(3)   DEFAULT NULL COMMENT '修改时间',
    version             BIGINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    deleted             BIT(1)        NOT NULL DEFAULT b'0' COMMENT '逻辑删除',
    PRIMARY KEY (sys_role_id),
    KEY idx_sys_role_parent (parent_id, deleted),
    KEY idx_sys_role_code_deleted (sys_role_code, deleted),
    KEY idx_sys_role_deleted_create_time (deleted, create_time),
    UNIQUE KEY uk_sys_role_active ((CASE WHEN deleted = b'0' THEN sys_role_code END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='[系统] 系统角色' ROW_FORMAT=DYNAMIC;
