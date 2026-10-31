package com.novaboot.module.system.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日誌註解：標註在 Controller 方法上，由 AOP 異步記錄操作行為
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /** 模塊標題，如「用戶管理」 */
    String title();

    /** 業務類型 */
    BusinessType businessType() default BusinessType.OTHER;

    /** 是否記錄請求參數 */
    boolean saveParam() default true;
}
