-- ${options.functionName} 選單 SQL（目錄起始 ID：${options.menuStartId?c}）
-- 執行前請確認 ID 未被佔用；執行後超級管理員角色可直接看到新選單
INSERT INTO sys_menu (id, parent_id, name, menu_type, path, component, perm, icon, sort_no, visible, status) VALUES
(${options.menuStartId?c}, 0, '${options.functionName}管理', 'D', '/${options.moduleName}', NULL, NULL, 'Grid', 10, 1, 1),
(${(options.menuStartId + 1)?c}, ${options.menuStartId?c}, '${options.functionName}', 'M', '${options.routePath}', '${options.moduleName}/${options.businessName}/index', '${options.moduleName}:${options.businessName}:list', '${options.menuIcon}', 1, 1, 1),
(${(options.menuStartId + 2)?c}, ${options.menuStartId?c}, '${options.functionName}新增', 'B', NULL, NULL, '${options.moduleName}:${options.businessName}:add', NULL, 1, 1, 1),
(${(options.menuStartId + 3)?c}, ${options.menuStartId?c}, '${options.functionName}修改', 'B', NULL, NULL, '${options.moduleName}:${options.businessName}:edit', NULL, 2, 1, 1),
(${(options.menuStartId + 4)?c}, ${options.menuStartId?c}, '${options.functionName}刪除', 'B', NULL, NULL, '${options.moduleName}:${options.businessName}:delete', NULL, 3, 1, 1);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, ${options.menuStartId?c}),
(1, ${(options.menuStartId + 1)?c}),
(1, ${(options.menuStartId + 2)?c}),
(1, ${(options.menuStartId + 3)?c}),
(1, ${(options.menuStartId + 4)?c});
