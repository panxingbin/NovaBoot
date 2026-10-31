-- =====================================================================
-- NovaBoot M0 初始化腳本（MySQL 8.x）
-- 由 Flyway 執行：classpath:db/migration/mysql/V1__init.sql
-- =====================================================================

CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '上級部門ID',
    name        VARCHAR(50)  NOT NULL COMMENT '部門名稱',
    sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '狀態：1啟用 0停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '邏輯刪除',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部門表';

CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    username    VARCHAR(50)  NOT NULL COMMENT '用戶名',
    password    VARCHAR(100) NOT NULL COMMENT '密碼（BCrypt）',
    nickname    VARCHAR(50)  NOT NULL COMMENT '暱稱',
    dept_id     BIGINT       NULL COMMENT '部門ID',
    email       VARCHAR(100) NULL COMMENT '郵箱',
    phone       VARCHAR(20)  NULL COMMENT '手機號',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '狀態：1啟用 0停用',
    remark      VARCHAR(255) NULL COMMENT '備註',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '邏輯刪除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用戶表';

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    name        VARCHAR(50)  NOT NULL COMMENT '角色名稱',
    code        VARCHAR(50)  NOT NULL COMMENT '角色編碼',
    sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '狀態：1啟用 0停用',
    remark      VARCHAR(255) NULL COMMENT '備註',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '邏輯刪除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色表';

CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '上級選單ID',
    name        VARCHAR(50)  NOT NULL COMMENT '名稱',
    menu_type   CHAR(1)      NOT NULL COMMENT '類型：D目錄 M選單 B按鈕',
    path        VARCHAR(200) NULL COMMENT '路由地址',
    component   VARCHAR(200) NULL COMMENT '前端組件路徑',
    perm        VARCHAR(100) NULL COMMENT '權限標識',
    icon        VARCHAR(100) NULL COMMENT '圖標',
    sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否顯示：1是 0否',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '狀態：1啟用 0停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '選單表';

CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用戶角色關聯表';

CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '選單ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色選單關聯表';

-- ---------------------------------------------------------------------
-- 初始化數據
-- ---------------------------------------------------------------------

INSERT INTO sys_dept (id, parent_id, name, sort_no, status)
VALUES (1, 0, '總部', 0, 1);

-- 默認管理員：admin / admin123（BCrypt）
INSERT INTO sys_user (id, username, password, nickname, dept_id, status, remark)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系統管理員', 1, 1, '內置管理員');

INSERT INTO sys_role (id, name, code, sort_no, status, remark)
VALUES (1, '超級管理員', 'admin', 1, 1, '內置超級管理員');

INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(1,  0, '系統管理', 'D', '/system',       NULL,                NULL,               'Setting',    1, 1, 1),
(2,  1, '用戶管理', 'M', '/system/user',  'system/user/index', 'system:user:list', 'User',       1, 1, 1),
(3,  1, '角色管理', 'M', '/system/role',  'system/role/index', 'system:role:list', 'UserFilled', 2, 1, 1),
(4,  1, '選單管理', 'M', '/system/menu',  'system/menu/index', 'system:menu:list', 'Menu',       3, 1, 1),
(11, 2, '用戶新增', 'B', NULL, NULL, 'system:user:add',      NULL, 1, 1, 1),
(12, 2, '用戶修改', 'B', NULL, NULL, 'system:user:edit',     NULL, 2, 1, 1),
(13, 2, '用戶刪除', 'B', NULL, NULL, 'system:user:delete',   NULL, 3, 1, 1),
(14, 2, '重置密碼', 'B', NULL, NULL, 'system:user:resetPwd', NULL, 4, 1, 1),
(21, 3, '角色新增', 'B', NULL, NULL, 'system:role:add',      NULL, 1, 1, 1),
(22, 3, '角色修改', 'B', NULL, NULL, 'system:role:edit',     NULL, 2, 1, 1),
(23, 3, '角色刪除', 'B', NULL, NULL, 'system:role:delete',   NULL, 3, 1, 1),
(31, 4, '選單新增', 'B', NULL, NULL, 'system:menu:add',      NULL, 1, 1, 1),
(32, 4, '選單修改', 'B', NULL, NULL, 'system:menu:edit',     NULL, 2, 1, 1),
(33, 4, '選單刪除', 'B', NULL, NULL, 'system:menu:delete',   NULL, 3, 1, 1);

INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4),
(5, 1, 11), (6, 1, 12), (7, 1, 13), (8, 1, 14),
(9, 1, 21), (10, 1, 22), (11, 1, 23),
(12, 1, 31), (13, 1, 32), (14, 1, 33);
