package com.novaboot.module.generator.service;

import com.novaboot.module.generator.dto.GenOptions;
import com.novaboot.module.generator.meta.ColumnMeta;
import com.novaboot.module.generator.meta.TableMeta;
import com.novaboot.module.generator.meta.TableMetaService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代碼生成：FreeMarker 模板渲染 → 預覽文本 / zip 下載
 * 生成物：後端 entity/mapper/service/controller + 前端 api/列表頁 + 選單 SQL
 */
@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private static final String[] TEMPLATES = {
            "entity.java.ftl", "mapper.java.ftl", "service.java.ftl", "controller.java.ftl",
            "api.ts.ftl", "index.vue.ftl", "menu.sql.ftl",
    };

    private final TableMetaService tableMetaService;

    private Configuration freemarker;

    @PostConstruct
    void init() {
        freemarker = new Configuration(Configuration.VERSION_2_3_32);
        freemarker.setClassForTemplateLoading(CodeGeneratorService.class, "/gen/templates");
        freemarker.setDefaultEncoding("UTF-8");
        // 防止數字插值出現千分位（如選單 ID 1,000）
        freemarker.setNumberFormat("0");
    }

    /**
     * 預覽：文件相對路徑 → 內容
     */
    public Map<String, String> preview(GenOptions options) {
        TableMeta meta = tableMetaService.metaOf(options.getTableName());
        Map<String, String> files = new LinkedHashMap<>();
        for (String template : TEMPLATES) {
            files.put(filePath(template, options), render(template, model(options, meta)));
        }
        return files;
    }

    /**
     * 打包 zip
     */
    public byte[] zip(GenOptions options) {
        Map<String, String> files = preview(options);
        String root = options.getBusinessName() + "-gen/";
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(bos)) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(root + entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            zip.finish();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("打包生成代碼失敗: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> model(GenOptions options, TableMeta meta) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("options", options);
        model.put("table", meta);
        return model;
    }

    private String render(String templateName, Map<String, Object> model) {
        try {
            Template template = freemarker.getTemplate(templateName);
            StringWriter out = new StringWriter();
            template.process(model, out);
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("渲染模板 " + templateName + " 失敗: " + e.getMessage(), e);
        }
    }

    private String filePath(String template, GenOptions options) {
        String pkgPath = options.getPackageName().replace('.', '/');
        String pageDir = options.getModuleName() + "/" + options.getBusinessName();
        return switch (template) {
            case "entity.java.ftl" -> "backend/src/main/java/" + pkgPath + "/entity/" + options.getClassName() + ".java";
            case "mapper.java.ftl" -> "backend/src/main/java/" + pkgPath + "/mapper/" + options.getClassName() + "Mapper.java";
            case "service.java.ftl" -> "backend/src/main/java/" + pkgPath + "/service/" + options.getClassName() + "Service.java";
            case "controller.java.ftl" -> "backend/src/main/java/" + pkgPath + "/controller/" + options.getClassName() + "Controller.java";
            case "api.ts.ftl" -> "frontend/src/api/" + options.getBusinessName() + ".ts";
            case "index.vue.ftl" -> "frontend/src/views/" + pageDir + "/index.vue";
            case "menu.sql.ftl" -> "sql/menu.sql";
            default -> throw new IllegalStateException("未知模板: " + template);
        };
    }

    /** 供模板使用的列判斷保持簡單；此處僅保留 ColumnMeta 引用以防收窄 */
    @SuppressWarnings("unused")
    private static void touch(ColumnMeta col) {
        // no-op
    }
}
