package com.novaboot.module.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.generator.dto.GenOptions;
import com.novaboot.module.generator.meta.TableMeta;
import com.novaboot.module.generator.meta.TableMetaService;
import com.novaboot.module.generator.service.CodeGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 代碼生成接口
 */
@RestController
@RequestMapping("/api/gen")
@RequiredArgsConstructor
public class GenController {

    private final TableMetaService tableMetaService;
    private final CodeGeneratorService codeGeneratorService;

    public record GenTable(String tableName, String tableComment) {
    }

    @GetMapping("/tables")
    @SaCheckPermission("tool:gen:list")
    public Result<List<GenTable>> tables() {
        return Result.ok(tableMetaService.listTables().stream()
                .map(t -> new GenTable(t.tableName(), t.tableComment()))
                .toList());
    }

    @GetMapping("/meta/{tableName}")
    @SaCheckPermission("tool:gen:list")
    public Result<TableMeta> meta(@PathVariable String tableName) {
        return Result.ok(tableMetaService.metaOf(tableName));
    }

    @PostMapping("/preview")
    @SaCheckPermission("tool:gen:preview")
    public Result<Map<String, String>> preview(@RequestBody GenOptions options) {
        return Result.ok(codeGeneratorService.preview(options));
    }

    @PostMapping("/download")
    @SaCheckPermission("tool:gen:download")
    public ResponseEntity<byte[]> download(@RequestBody GenOptions options) {
        byte[] zip = codeGeneratorService.zip(options);
        String fileName = URLEncoder.encode(options.getBusinessName() + "-gen.zip", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
