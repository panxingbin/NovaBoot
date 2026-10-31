package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.novaboot.common.core.domain.PageQuery;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.annotation.BusinessType;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.dto.FileVO;
import com.novaboot.module.system.entity.SysFileInfo;
import com.novaboot.module.system.service.SysFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 檔案管理接口
 */
@RestController
@RequestMapping("/api/system/files")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService sysFileService;

    @PostMapping("/upload")
    @SaCheckPermission("system:file:upload")
    @OperLog(title = "檔案管理", businessType = BusinessType.UPLOAD)
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.ok(sysFileService.upload(file));
    }

    @GetMapping
    @SaCheckPermission("system:file:list")
    public Result<PageResult<FileVO>> page(PageQuery query, String keyword) {
        return Result.ok(sysFileService.page(query, keyword));
    }

    /**
     * 下載（放在登入攔截白名單，支持 a 標籤直接下載）
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
        SysFileInfo info = sysFileService.getEntity(id);
        Resource resource = new FileSystemResource(sysFileService.resolvePath(info));
        String encodedName = URLEncoder.encode(info.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentType = info.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : info.getContentType();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:file:delete")
    @OperLog(title = "檔案管理", businessType = BusinessType.DELETE)
    public Result<Void> delete(@PathVariable Long id) throws IOException {
        sysFileService.delete(id);
        return Result.ok();
    }
}
