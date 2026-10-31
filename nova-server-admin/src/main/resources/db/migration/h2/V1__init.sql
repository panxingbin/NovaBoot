-- =====================================================================
-- NovaBoot M0 初始化腳本（H2，MySQL 兼容模式，本地開發用）
-- 由 Flyway 執行：classpath:db/migration/h2/V1__init.sql
-- =====================================================================

CREATE TABLE sys_dept (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(50)  NOT NULL,
    sort_no     INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50)  NOT NULL,
    dept_id     BIGINT       NULL,
    email       VARCHAR(100) NULL,
    phone       VARCHAR(20)  NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    sort_no     INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_code UNIQUE (code)
);

CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(50)  NOT NULL,
    menu_type   CHAR(1)      NOT NULL,
    path        VARCHAR(200) NULL,
    component   VARCHAR(200) NULL,
    perm        VARCHAR(100) NULL,
    icon        VARCHAR(100) NULL,
    sort_no     INT          NOT NULL DEFAULT 0,
    visible     TINYINT      NOT NULL DEFAULT 1,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_role_menu UNIQUE (role_id, menu_id)
);

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
