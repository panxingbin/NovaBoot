package com.novaboot.module.system.datascope;

/**
 * 數據範圍 ThreadLocal 持有器：AOP 計算後放入，查詢方法讀取，最後必須清理
 */
public final class DataScopeHolder {

    private static final ThreadLocal<DataScopeInfo> CONTEXT = new ThreadLocal<>();

    private DataScopeHolder() {
    }

    public static void set(DataScopeInfo info) {
        CONTEXT.set(info);
    }

    public static DataScopeInfo get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
