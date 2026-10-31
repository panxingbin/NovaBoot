-- =====================================================================
-- NovaBoot M1 初始化腳本（H2，MySQL 兼容模式，本地開發用）
-- =====================================================================

ALTER TABLE sys_role ADD COLUMN data_scope TINYINT NOT NULL DEFAULT 1;

ALTER TABLE sys_dept ADD COLUMN leader VARCHAR(50) NULL;
ALTER TABLE sys_dept ADD COLUMN phone VARCHAR(20) NULL;

CREATE TABLE sys_role_dept (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_dept UNIQUE (role_id, dept_id)
);

CREATE TABLE sys_post (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    sort_no     INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_post_code UNIQUE (code)
);

CREATE TABLE sys_user_post (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_post UNIQUE (user_id, post_id)
);

CREATE TABLE sys_dict_type (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(100) NOT NULL,
    remark      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_dict_type UNIQUE (type)
);

CREATE TABLE sys_dict_data (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    dict_type   VARCHAR(100) NOT NULL,
    label       VARCHAR(100) NOT NULL,
    item_value  VARCHAR(100) NOT NULL,
    sort_no     INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sys_dict_data_type ON sys_dict_data (dict_type);

CREATE TABLE sys_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    config_key   VARCHAR(100) NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    builtin      TINYINT      NOT NULL DEFAULT 0,
    remark       VARCHAR(255) NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_config_key UNIQUE (config_key)
);

CREATE TABLE sys_notice (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    type        TINYINT      NOT NULL DEFAULT 1,
    content     TEXT         NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE sys_oper_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    title           VARCHAR(100) NOT NULL,
    business_type   TINYINT      NOT NULL DEFAULT 0,
    method          VARCHAR(200) NULL,
    request_method  VARCHAR(10)  NULL,
    url             VARCHAR(255) NULL,
    ip              VARCHAR(64)  NULL,
    param           TEXT         NULL,
    error_msg       TEXT         NULL,
    status          TINYINT      NOT NULL DEFAULT 0,
    cost_ms         INT          NOT NULL DEFAULT 0,
    oper_name       VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sys_oper_log_created ON sys_oper_log (created_at);

CREATE TABLE sys_login_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    ip          VARCHAR(64)  NULL,
    status      TINYINT      NOT NULL DEFAULT 0,
    msg         VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_sys_login_log_created ON sys_login_log (created_at);

CREATE TABLE sys_file_info (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200) NOT NULL,
    original_name VARCHAR(200) NOT NULL,
    path          VARCHAR(500) NOT NULL,
    size          BIGINT       NOT NULL DEFAULT 0,
    content_type  VARCHAR(100) NULL,
    oper_name     VARCHAR(50)  NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 種子數據
INSERT INTO sys_dept (id, parent_id, name, sort_no, status) VALUES
(2, 1, '研發部', 1, 1),
(3, 1, '市場部', 2, 1);

INSERT INTO sys_post (id, code, name, sort_no, status, remark) VALUES
(1, 'CEO', '董事長', 1, 1, NULL),
(2, 'PM',  '項目經理', 2, 1, NULL),
(3, 'DEV', '工程師',   3, 1, NULL),
(4, 'HR',  '人力資源', 4, 1, NULL);

INSERT INTO sys_dict_type (id, name, type, remark) VALUES
(1, '系統狀態', 'sys_normal_status', '系統啟用/停用狀態'),
(2, '用戶性別', 'sys_user_sex',      '用戶性別'),
(3, '公告類型', 'sys_notice_type',   '通知公告類型');

INSERT INTO sys_dict_data (id, dict_type, label, item_value, sort_no, status) VALUES
(1, 'sys_normal_status', '啟用', '1', 1, 1),
(2, 'sys_normal_status', '停用', '0', 2, 1),
(3, 'sys_user_sex',      '未知', '0', 1, 1),
(4, 'sys_user_sex',      '男',   '1', 2, 1),
(5, 'sys_user_sex',      '女',   '2', 3, 1),
(6, 'sys_notice_type',   '通知', '1', 1, 1),
(7, 'sys_notice_type',   '公告', '2', 2, 1);

INSERT INTO sys_config (id, name, config_key, config_value, builtin, remark) VALUES
(1, '是否允許自注冊', 'sys.account.registerUser', 'false',        1, '是否允許自注冊賬號'),
(2, '用戶初始密碼',   'sys.user.initPassword',    'admin123', 1, '初始化用戶密碼');

INSERT INTO sys_notice (id, title, type, content, status, remark) VALUES
(1, 'NovaBoot M1 版本上線通知', 1, '系統模組 M1 已上線：部門、崗位、字典、參數、公告、日誌、在線用戶、檔案管理與資料權限。', 1, NULL),
(2, '系統維護公告',             2, '本週六 22:00-24:00 進行例行維護，期間系統暫停服務。', 1, NULL);

UPDATE sys_role SET data_scope = 1 WHERE id = 1;

INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(40, 1, '部門管理', 'M', '/system/dept',   'system/dept/index',   'system:dept:list',   'OfficeBuilding', 4, 1, 1),
(41, 1, '崗位管理', 'M', '/system/post',   'system/post/index',   'system:post:list',   'Postcard',       5, 1, 1),
(42, 1, '字典管理', 'M', '/system/dict',   'system/dict/index',   'system:dict:list',   'Collection',     6, 1, 1),
(43, 1, '參數設置', 'M', '/system/config', 'system/config/index', 'system:config:list', 'Tools',          7, 1, 1),
(44, 1, '通知公告', 'M', '/system/notice', 'system/notice/index', 'system:notice:list', 'Bell',           8, 1, 1),
(45, 1, '檔案管理', 'M', '/system/file',   'system/file/index',   'system:file:list',   'FolderOpened',   9, 1, 1),
(5,  0, '系統監控', 'D', '/monitor',       NULL,                  NULL,                 'Monitor',        2, 1, 1),
(50, 5, '操作日誌', 'M', '/monitor/operlog',  'monitor/operlog/index',  'monitor:operlog:list',  'Document',     1, 1, 1),
(51, 5, '登入日誌', 'M', '/monitor/loginlog', 'monitor/loginlog/index', 'monitor:loginlog:list', 'Key',          2, 1, 1),
(52, 5, '在線用戶', 'M', '/monitor/online',   'monitor/online/index',   'monitor:online:list',   'UserFilled',   3, 1, 1),
(401, 40, '部門新增', 'B', NULL, NULL, 'system:dept:add',      NULL, 1, 1, 1),
(402, 40, '部門修改', 'B', NULL, NULL, 'system:dept:edit',     NULL, 2, 1, 1),
(403, 40, '部門刪除', 'B', NULL, NULL, 'system:dept:delete',   NULL, 3, 1, 1),
(411, 41, '崗位新增', 'B', NULL, NULL, 'system:post:add',      NULL, 1, 1, 1),
(412, 41, '崗位修改', 'B', NULL, NULL, 'system:post:edit',     NULL, 2, 1, 1),
(413, 41, '崗位刪除', 'B', NULL, NULL, 'system:post:delete',   NULL, 3, 1, 1),
(421, 42, '字典新增', 'B', NULL, NULL, 'system:dict:add',      NULL, 1, 1, 1),
(422, 42, '字典修改', 'B', NULL, NULL, 'system:dict:edit',     NULL, 2, 1, 1),
(423, 42, '字典刪除', 'B', NULL, NULL, 'system:dict:delete',   NULL, 3, 1, 1),
(431, 43, '參數新增', 'B', NULL, NULL, 'system:config:add',    NULL, 1, 1, 1),
(432, 43, '參數修改', 'B', NULL, NULL, 'system:config:edit',   NULL, 2, 1, 1),
(433, 43, '參數刪除', 'B', NULL, NULL, 'system:config:delete', NULL, 3, 1, 1),
(441, 44, '公告新增', 'B', NULL, NULL, 'system:notice:add',    NULL, 1, 1, 1),
(442, 44, '公告修改', 'B', NULL, NULL, 'system:notice:edit',   NULL, 2, 1, 1),
(443, 44, '公告刪除', 'B', NULL, NULL, 'system:notice:delete', NULL, 3, 1, 1),
(451, 45, '檔案上傳', 'B', NULL, NULL, 'system:file:upload',   NULL, 1, 1, 1),
(452, 45, '檔案刪除', 'B', NULL, NULL, 'system:file:delete',   NULL, 2, 1, 1),
(521, 52, '強制下線', 'B', NULL, NULL, 'monitor:online:forceLogout', NULL, 1, 1, 1);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(15, 1, 40), (16, 1, 41), (17, 1, 42), (18, 1, 43), (19, 1, 44), (20, 1, 45),
(21, 1, 5),  (22, 1, 50), (23, 1, 51), (24, 1, 52),
(25, 1, 401), (26, 1, 402), (27, 1, 403),
(28, 1, 411), (29, 1, 412), (30, 1, 413),
(31, 1, 421), (32, 1, 422), (33, 1, 423),
(34, 1, 431), (35, 1, 432), (36, 1, 433),
(37, 1, 441), (38, 1, 442), (39, 1, 443),
(40, 1, 451), (41, 1, 452),
(42, 1, 521);
