package ${options.packageName}.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.${options.moduleName}.entity.${options.className};
import com.novaboot.module.${options.moduleName}.mapper.${options.className}Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ${options.functionName} 服務
 *
 * @author ${options.author}
 */
@Service
@RequiredArgsConstructor
public class ${options.className}Service {

    private final ${options.className}Mapper ${options.businessName}Mapper;

<#if table.searchColumn??>
    /**
     * 分頁查詢
     */
    public PageResult<${options.className}> page(int pageNum, int pageSize, String keyword) {
        Page<${options.className}> page = ${options.businessName}Mapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<${options.className}>lambdaQuery()
                        .like(StringUtils.hasText(keyword), ${options.className}::get${table.searchColumn.javaName?cap_first}, keyword)
                        .orderByDesc(${options.className}::getId));
        return PageResult.of(page.getTotal(), page.getRecords());
    }
<#else>
    /**
     * 分頁查詢
     */
    public PageResult<${options.className}> page(int pageNum, int pageSize) {
        Page<${options.className}> page = ${options.businessName}Mapper.selectPage(
                new Page<>(pageNum, pageSize),
                Wrappers.<${options.className}>lambdaQuery().orderByDesc(${options.className}::getId));
        return PageResult.of(page.getTotal(), page.getRecords());
    }
</#if>

    public ${options.className} get(Long id) {
        ${options.className} entity = ${options.businessName}Mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("數據不存在");
        }
        return entity;
    }

    @Transactional
    public void create(${options.className} entity) {
        ${options.businessName}Mapper.insert(entity);
    }

    @Transactional
    public void update(Long id, ${options.className} entity) {
        entity.setId(id);
        ${options.businessName}Mapper.updateById(entity);
    }

    @Transactional
    public void delete(Long id) {
        ${options.businessName}Mapper.deleteById(id);
    }

    /**
     * 按 ID 集合批量刪除
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ${options.businessName}Mapper.deleteByIds(ids);
    }
}
