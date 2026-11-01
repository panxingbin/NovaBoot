package com.novaboot.module.workflow.dto;

import lombok.Data;

import java.util.List;

/**
 * 表單字段定義（schema 元素）
 */
@Data
public class FormFieldDef {

    private String key;

    private String label;

    /** input / textarea / number / date / select */
    private String type;

    private Boolean required;

    /** select 類型的候選值 */
    private List<String> options;

    private Double min;

    private Double max;
}
