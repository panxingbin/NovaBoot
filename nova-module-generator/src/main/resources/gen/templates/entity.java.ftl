package ${options.packageName}.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ${options.functionName}<#if table.tableComment?has_content>（${table.tableComment}）</#if>
 *
 * @author ${options.author}
 */
@Data
@TableName("${table.tableName}")
public class ${options.className} {

<#list table.columns as col>
    <#if col.pk>
    @TableId(type = IdType.AUTO)
    <#elseif col.columnName == "created_at">
    @TableField(fill = FieldFill.INSERT)
    <#elseif col.columnName == "updated_at">
    @TableField(fill = FieldFill.INSERT_UPDATE)
    <#elseif col.columnName == "deleted">
    @TableLogic
    </#if>
    /** ${col.comment!col.javaName} */
    private ${col.javaType} ${col.javaName};

</#list>
}
