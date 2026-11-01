-- =====================================================================
-- NovaBoot M3 工作流腳本（MySQL 8.x / H2 通用）
-- 可配置表單定義表 + 請假單示例表單 + 工作流菜單
-- 注意：Flowable 引擎自身的 ACT_ 表由引擎自動創建（僅 mysql profile）
-- =====================================================================

CREATE TABLE flow_form (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主鍵',
    form_key    VARCHAR(64)  NOT NULL COMMENT '表單鍵（同時作為流程鍵）',
    name        VARCHAR(100) NOT NULL COMMENT '表單名稱',
    schema_json TEXT         NOT NULL COMMENT '表單 schema（字段數組 JSON）',
    remark      VARCHAR(255) NULL COMMENT '備註',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '邏輯刪除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_form_key (form_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '工作流表單定義表';

-- 請假單示例表單（form_key = leave，對應流程 leave）
INSERT INTO flow_form (form_key, name, schema_json, remark) VALUES
('leave', '請假申請',
 '[{"key":"leaveType","label":"請假類型","type":"select","required":true,"options":["事假","病假","年假","调休"]},{"key":"startDate","label":"開始日期","type":"date","required":true},{"key":"days","label":"請假天數","type":"number","required":true,"min":0.5,"max":30},{"key":"reason","label":"請假事由","type":"textarea","required":true}]',
 '示例：請假單（主管審批後結束）');

-- 工作流菜單（請假申請 / 我的申請 / 待辦審批 / 表單管理）
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(7, 0, '工作流', 'D', '/workflow', NULL, NULL, 'Connection', 4, 1, 1),
(70, 7, '請假申請', 'M', '/workflow/apply', 'workflow/apply/index', 'workflow:apply', 'EditPen', 1, 1, 1),
(71, 7, '我的申請', 'M', '/workflow/mine', 'workflow/mine/index', 'workflow:mine', 'Tickets', 2, 1, 1),
(72, 7, '待辦審批', 'M', '/workflow/todo', 'workflow/todo/index', 'workflow:todo', 'Checked', 3, 1, 1),
(73, 7, '表單管理', 'M', '/workflow/forms', 'workflow/forms/index', 'workflow:form', 'Document', 4, 1, 1),
(730, 73, '表單新增', 'B', NULL, NULL, 'workflow:form:add', NULL, 1, 1, 1),
(731, 73, '表單修改', 'B', NULL, NULL, 'workflow:form:edit', NULL, 2, 1, 1),
(732, 73, '表單刪除', 'B', NULL, NULL, 'workflow:form:delete', NULL, 3, 1, 1);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 7), (1, 70), (1, 71), (1, 72), (1, 73),
(1, 730), (1, 731), (1, 732);
