package com.novaboot.module.system.annotation;

/**
 * 操作日誌業務類型
 */
public enum BusinessType {

    /** 其它 */
    OTHER(0),
    /** 新增 */
    INSERT(1),
    /** 修改 */
    UPDATE(2),
    /** 刪除 */
    DELETE(3),
    /** 授權 */
    GRANT(4),
    /** 強制下線 */
    FORCE_LOGOUT(5),
    /** 上傳 */
    UPLOAD(6);

    private final int value;

    BusinessType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
