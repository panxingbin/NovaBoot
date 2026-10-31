package com.novaboot.module.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageQuery;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.FileVO;
import com.novaboot.module.system.entity.SysFileInfo;
import com.novaboot.module.system.mapper.SysFileInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 檔案服務（本地磁盤存儲；S3/OSS 將以同一接口在 M2+ 擴展）
 */
@Service
@RequiredArgsConstructor
public class SysFileService {

    private final SysFileInfoMapper sysFileInfoMapper;

    @Value("${nova.storage.local-path:./data/files}")
    private String localPath;

    @Transactional
    public FileVO upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上傳文件不能為空");
        }
        String originalName = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "unnamed"));
        String ext = "";
        int idx = originalName.lastIndexOf('.');
        if (idx >= 0) {
            ext = originalName.substring(idx);
        }
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(localPath, dateDir).toAbsolutePath();
        Files.createDirectories(dir);
        Path target = dir.resolve(name);
        file.transferTo(target);

        SysFileInfo info = new SysFileInfo();
        info.setName(name);
        info.setOriginalName(originalName);
        info.setPath(dateDir + "/" + name);
        info.setSize(file.getSize());
        info.setContentType(file.getContentType());
        info.setOperName(currentUsername());
        sysFileInfoMapper.insert(info);
        return toVO(info);
    }

    public PageResult<FileVO> page(PageQuery query, String keyword) {
        Page<SysFileInfo> page = sysFileInfoMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysFileInfo>lambdaQuery()
                        .like(StringUtils.hasText(keyword), SysFileInfo::getOriginalName, keyword)
                        .orderByDesc(SysFileInfo::getId));
        List<FileVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    public SysFileInfo getEntity(Long id) {
        SysFileInfo info = sysFileInfoMapper.selectById(id);
        if (info == null) {
            throw new BusinessException("檔案不存在");
        }
        return info;
    }

    public Path resolvePath(SysFileInfo info) {
        return Paths.get(localPath).toAbsolutePath().resolve(info.getPath());
    }

    @Transactional
    public void delete(Long id) throws IOException {
        SysFileInfo info = getEntity(id);
        sysFileInfoMapper.deleteById(id);
        Files.deleteIfExists(resolvePath(info));
    }

    private String currentUsername() {
        try {
            long loginId = StpUtil.getLoginIdAsLong();
            // 檔案上傳人記錄登入 ID 即可，避免額外查詢
            return String.valueOf(loginId);
        } catch (Exception e) {
            return null;
        }
    }

    private FileVO toVO(SysFileInfo info) {
        FileVO vo = new FileVO();
        vo.setId(info.getId());
        vo.setName(info.getName());
        vo.setOriginalName(info.getOriginalName());
        vo.setSize(info.getSize());
        vo.setContentType(info.getContentType());
        vo.setOperName(info.getOperName());
        vo.setCreatedAt(info.getCreatedAt());
        return vo;
    }
}
