package com.novaboot.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.idev.excel.FastExcel;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.core.domain.Result;
import com.novaboot.module.system.dto.ResetPasswordBody;
import com.novaboot.module.system.dto.UserBody;
import com.novaboot.module.system.dto.UserExcelVO;
import com.novaboot.module.system.dto.UserQuery;
import com.novaboot.module.system.dto.UserVO;
import com.novaboot.module.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用戶管理接口
 */
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    @SaCheckPermission("system:user:list")
    public Result<PageResult<UserVO>> page(UserQuery query) {
        return Result.ok(sysUserService.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:user:list")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.ok(sysUserService.get(id));
    }

    @PostMapping
    @SaCheckPermission("system:user:add")
    public Result<Void> create(@RequestBody UserBody body) {
        sysUserService.create(body);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserBody body) {
        sysUserService.update(id, body);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @SaCheckPermission("system:user:resetPwd")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordBody body) {
        sysUserService.resetPassword(id, body.getPassword());
        return Result.ok();
    }

    /**
     * 導出用戶 Excel（同過濾條件，遵循資料權限）
     */
    @GetMapping("/export")
    @SaCheckPermission("system:user:export")
    public ResponseEntity<byte[]> export(UserQuery query) throws IOException {
        List<UserExcelVO> rows = sysUserService.exportList(query);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FastExcel.write(out, UserExcelVO.class).sheet("用戶").doWrite(rows);
        String fileName = URLEncoder.encode("用戶列表.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }

    /**
     * 導入用戶 Excel（逐行校驗，返回成功行數與失敗原因）
     */
    @PostMapping("/import")
    @SaCheckPermission("system:user:import")
    public Result<SysUserService.ImportResult> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail("上傳文件不能為空");
        }
        List<UserExcelVO> rows = FastExcel.read(file.getInputStream())
                .head(UserExcelVO.class)
                .sheet()
                .doReadSync();
        return Result.ok(sysUserService.importUsers(rows));
    }
}
