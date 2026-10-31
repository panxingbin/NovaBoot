package com.novaboot.module.system.datascope;

import java.util.List;

/**
 * 數據範圍過濾條件（並集語義）：允許可見 = 部門在 deptIds 中 OR 用戶為 selfUserId
 */
public record DataScopeInfo(List<Long> deptIds, Long selfUserId) {
}
