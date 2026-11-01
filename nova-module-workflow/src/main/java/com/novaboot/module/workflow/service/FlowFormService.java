package com.novaboot.module.workflow.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.service.SysConfigService;
import com.novaboot.module.workflow.dto.FormFieldDef;
import com.novaboot.module.workflow.entity.FlowFormDef;
import com.novaboot.module.workflow.mapper.FlowFormDefMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 可配置表單服務：schema 的存儲、解析與申請數據校驗
 */
@Service
@RequiredArgsConstructor
public class FlowFormService {

    private final FlowFormDefMapper formMapper;

    public PageResult<FlowFormDef> page(int pageNum, int pageSize, String keyword) {
        Page<FlowFormDef> page = formMapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<FlowFormDef>lambdaQuery()
                        .like(StringUtils.hasText(keyword), FlowFormDef::getName, keyword)
                        .or(StringUtils.hasText(keyword))
                        .like(StringUtils.hasText(keyword), FlowFormDef::getFormKey, keyword)
                        .orderByDesc(FlowFormDef::getId));
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    public List<FlowFormDef> listAll() {
        return formMapper.selectList(Wrappers.<FlowFormDef>lambdaQuery().orderByAsc(FlowFormDef::getId));
    }

    public FlowFormDef getByKey(String formKey) {
        FlowFormDef def = formMapper.selectOne(
                Wrappers.<FlowFormDef>lambdaQuery().eq(FlowFormDef::getFormKey, formKey));
        if (def == null) {
            throw new BusinessException("表單不存在：" + formKey);
        }
        return def;
    }

    public FlowFormDef requireByKey(String formKey) {
        return getByKey(formKey);
    }

    @Transactional
    public void create(FlowFormDef def) {
        checkKeyUnique(def.getFormKey(), null);
        parseSchema(def.getSchemaJson());
        formMapper.insert(def);
    }

    @Transactional
    public void update(Long id, FlowFormDef def) {
        FlowFormDef existing = formMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("表單不存在");
        }
        checkKeyUnique(def.getFormKey(), id);
        parseSchema(def.getSchemaJson());
        def.setId(id);
        formMapper.updateById(def);
    }

    @Transactional
    public void delete(Long id) {
        formMapper.deleteById(id);
    }

    private void checkKeyUnique(String formKey, Long excludeId) {
        Long count = formMapper.selectCount(Wrappers.<FlowFormDef>lambdaQuery()
                .eq(FlowFormDef::getFormKey, formKey)
                .ne(excludeId != null, FlowFormDef::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException("表單鍵已存在：" + formKey);
        }
    }

    /**
     * 校驗 schema JSON：字段數組，key/label/type 必填，type 限枚舉，select 需 options
     */
    public List<FormFieldDef> parseSchema(String schemaJson) {
        if (!StringUtils.hasText(schemaJson)) {
            throw new BusinessException("表單 schema 不能為空");
        }
        List<FormFieldDef> fields;
        try {
            fields = JSONUtil.toList(schemaJson, FormFieldDef.class);
        } catch (Exception e) {
            throw new BusinessException("表單 schema 不是合法的 JSON 數組");
        }
        if (fields == null || fields.isEmpty()) {
            throw new BusinessException("表單至少需要一個字段");
        }
        List<String> allowed = List.of("input", "textarea", "number", "date", "select");
        for (FormFieldDef field : fields) {
            if (!StringUtils.hasText(field.getKey()) || !field.getKey().matches("[a-zA-Z][a-zA-Z0-9]{0,31}")) {
                throw new BusinessException("字段 key 不合法：" + field.getKey() + "（字母開頭的駝峰）");
            }
            if (!StringUtils.hasText(field.getLabel())) {
                throw new BusinessException("字段 " + field.getKey() + " 缺少 label");
            }
            if (!allowed.contains(field.getType())) {
                throw new BusinessException("字段 " + field.getKey() + " 的 type 僅支持 " + allowed);
            }
            if ("select".equals(field.getType())
                    && (field.getOptions() == null || field.getOptions().isEmpty())) {
                throw new BusinessException("字段 " + field.getKey() + " 為下拉類型，需要配置 options");
            }
        }
        return fields;
    }

    /**
     * 按 schema 校驗申請數據（必填/數字範圍）
     */
    public Map<String, Object> validateData(String schemaJson, Map<String, Object> data) {
        List<FormFieldDef> fields = parseSchema(schemaJson);
        Map<String, Object> cleaned = new java.util.LinkedHashMap<>();
        for (FormFieldDef field : fields) {
            Object value = data == null ? null : data.get(field.getKey());
            boolean blank = value == null || (value instanceof String s && s.isBlank());
            if (Boolean.TRUE.equals(field.getRequired()) && blank) {
                throw new BusinessException(field.getLabel() + " 不能為空");
            }
            if (blank) {
                continue;
            }
            if ("number".equals(field.getType())) {
                Number number;
                try {
                    number = cn.hutool.core.convert.Convert.toNumber(value);
                } catch (Exception e) {
                    throw new BusinessException(field.getLabel() + " 必須是數字");
                }
                if (field.getMin() != null && number.doubleValue() < field.getMin()) {
                    throw new BusinessException(field.getLabel() + " 不能小於 " + field.getMin());
                }
                if (field.getMax() != null && number.doubleValue() > field.getMax()) {
                    throw new BusinessException(field.getLabel() + " 不能大於 " + field.getMax());
                }
                cleaned.put(field.getKey(), number);
            } else if ("select".equals(field.getType())
                    && field.getOptions() != null && !field.getOptions().contains(String.valueOf(value))) {
                throw new BusinessException(field.getLabel() + " 的值不在候選範圍內");
            } else {
                cleaned.put(field.getKey(), String.valueOf(value));
            }
        }
        return cleaned;
    }
}
