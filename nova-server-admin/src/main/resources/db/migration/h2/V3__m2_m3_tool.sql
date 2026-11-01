-- =====================================================================
-- NovaBoot M2/M3 選單腳本（MySQL 8.x / H2 通用）
-- 代碼生成、Excel 導入導出、AI 對話、郵件發送權限
-- =====================================================================

-- 系統工具目錄 + 代碼生成頁面與按鈕
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(6, 0, '系統工具', 'D', '/tool', NULL, NULL, 'SetUp', 3, 1, 1),
(60, 6, '代碼生成', 'M', '/tool/gen', 'tool/gen/index', 'tool:gen:list', 'MagicStick', 1, 1, 1),
(61, 60, '生成預覽', 'B', NULL, NULL, 'tool:gen:preview', NULL, 1, 1, 1),
(62, 60, '生成下載', 'B', NULL, NULL, 'tool:gen:download', NULL, 2, 1, 1);

-- 用戶管理下：Excel 導出/導入按鈕
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(15, 2, '用戶導出', 'B', NULL, NULL, 'system:user:export', NULL, 5, 1, 1),
(16, 2, '用戶導入', 'B', NULL, NULL, 'system:user:import', NULL, 6, 1, 1);

-- AI 對話 / 郵件發送（M3 Starter 演示接口權限）
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(63, 6, 'AI 對話', 'B', NULL, NULL, 'ai:chat', NULL, 3, 1, 1),
(64, 6, '郵件發送', 'B', NULL, NULL, 'mail:send', NULL, 4, 1, 1);

-- 綁定超級管理員角色（ID 列自增，不顯式指定）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 6), (1, 60), (1, 61), (1, 62),
(1, 15), (1, 16),
(1, 63), (1, 64);
