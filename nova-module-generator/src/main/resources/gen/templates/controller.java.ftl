package ${options.packageName}.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.${options.moduleName}.entity.${options.className};
import com.novaboot.module.${options.moduleName}.service.${options.className}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ${options.functionName} 接口
 *
 * @author ${options.author}
 */
@RestController
@RequestMapping("${options.apiPath}")
@RequiredArgsConstructor
public class ${options.className}Controller {

    private final ${options.className}Service ${options.businessName}Service;

    @GetMapping
    @SaCheckPermission("${options.moduleName}:${options.businessName}:list")
    public Result<PageResult<${options.className}>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize<#if table.searchColumn??>,
            @RequestParam(required = false) String keyword</#if>) {
        return Result.ok(${options.businessName}Service.page(pageNum, pageSize<#if table.searchColumn??>, keyword</#if>));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("${options.moduleName}:${options.businessName}:list")
    public Result<${options.className}> get(@PathVariable Long id) {
        return Result.ok(${options.businessName}Service.get(id));
    }

    @PostMapping
    @SaCheckPermission("${options.moduleName}:${options.businessName}:add")
    public Result<Void> create(@RequestBody ${options.className} entity) {
        ${options.businessName}Service.create(entity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("${options.moduleName}:${options.businessName}:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody ${options.className} entity) {
        ${options.businessName}Service.update(id, entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("${options.moduleName}:${options.businessName}:delete")
    public Result<Void> delete(@PathVariable Long id) {
        ${options.businessName}Service.delete(id);
        return Result.ok();
    }

    @DeleteMapping("/batch")
    @SaCheckPermission("${options.moduleName}:${options.businessName}:delete")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        ${options.businessName}Service.deleteBatch(ids);
        return Result.ok();
    }
}
