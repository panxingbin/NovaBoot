package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.ConfigBody;
import com.novaboot.module.system.dto.ConfigQuery;
import com.novaboot.module.system.dto.ConfigVO;
import com.novaboot.module.system.entity.SysConfig;
import com.novaboot.module.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系統參數服務
 */
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public PageResult<ConfigVO> page(ConfigQuery query) {
        Page<SysConfig> page = sysConfigMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysConfig>lambdaQuery()
                        .and(StringUtils.hasText(query.getKeyword()), w -> w
                                .like(SysConfig::getName, query.getKeyword())
                                .or().like(SysConfig::getConfigKey, query.getKeyword()))
                        .orderByAsc(SysConfig::getId));
        List<ConfigVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    @Transactional
    public void create(ConfigBody body) {
        checkKeyUnique(body.getKey(), null);
        SysConfig config = new SysConfig();
        copyBody(config, body);
        sysConfigMapper.insert(config);
    }

    @Transactional
    public void update(Long id, ConfigBody body) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("參數不存在");
        }
        if (config.getBuiltin() != null && config.getBuiltin() == 1 && !config.getConfigKey().equals(body.getKey())) {
            throw new BusinessException("內置參數不允許修改鍵名");
        }
        checkKeyUnique(body.getKey(), id);
        copyBody(config, body);
        sysConfigMapper.updateById(config);
    }

    @Transactional
    public void delete(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("參數不存在");
        }
        if (config.getBuiltin() != null && config.getBuiltin() == 1) {
            throw new BusinessException("內置參數不允許刪除");
        }
        sysConfigMapper.deleteById(id);
    }

    /**
     * 按鍵名取參數值（不存在返回 null）
     */
    public String valueOfKey(String key) {
        SysConfig config = sysConfigMapper.selectOne(
                Wrappers.<SysConfig>lambdaQuery().eq(SysConfig::getConfigKey, key));
        return config == null ? null : config.getConfigValue();
    }

    private void checkKeyUnique(String key, Long excludeId) {
        Long count = sysConfigMapper.selectCount(
                Wrappers.<SysConfig>lambdaQuery()
                        .eq(SysConfig::getConfigKey, key)
                        .ne(excludeId != null, SysConfig::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException("參數鍵名已存在");
        }
    }

    private void copyBody(SysConfig config, ConfigBody body) {
        config.setName(body.getName());
        config.setConfigKey(body.getKey());
        config.setConfigValue(body.getValue());
        config.setBuiltin(body.getBuiltin() == null ? 0 : body.getBuiltin());
        config.setRemark(body.getRemark());
    }

    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        vo.setId(config.getId());
        vo.setName(config.getName());
        vo.setKey(config.getConfigKey());
        vo.setValue(config.getConfigValue());
        vo.setBuiltin(config.getBuiltin());
        vo.setRemark(config.getRemark());
        vo.setCreatedAt(config.getCreatedAt());
        return vo;
    }
}
