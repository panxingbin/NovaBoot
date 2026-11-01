package com.novaboot.module.system.enums;

import lombok.Getter;

/**
 * 角色數據範圍（資料權限）。命名為 DataScopeType 以避開 @DataScope 註解同名衝突
 */
@Getter
public enum DataScopeType {

    /** 全部數據 */
    ALL(1),
    /** 自訂部門（sys_role_dept） */
    CUSTOM_DEPT(2),
    /** 本部門 */
    DEPT(3),
    /** 本部門及以下 */
    DEPT_AND_CHILD(4),
    /** 僅本人 */
    SELF(5);

    private final int value;

    DataScopeType(int value) {
        this.value = value;
    }

    /**
     * 按 DB 值解析；空值或未知值回退為全部（與舊邏輯一致：默認不過濾）
     */
    public static DataScopeType of(Integer value) {
        for (DataScopeType scope : values()) {
            if (scope.value == (value == null ? ALL.value : value)) {
                return scope;
            }
        }
        return ALL;
    }
}
