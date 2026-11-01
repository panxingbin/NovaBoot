package com.novaboot.module.workflow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流表單定義（form_key 同時作為流程鍵）
 */
@Data
@TableName("flow_form")
public class FlowFormDef {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 表單鍵，如 leave */
    private String formKey;

    private String name;

    /** 表單 schema：字段數組 JSON [{key,label,type,required,options,min,max}] */
    private String schemaJson;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
