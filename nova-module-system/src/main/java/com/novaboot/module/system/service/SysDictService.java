package com.novaboot.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaboot.common.core.domain.PageResult;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.module.system.dto.DictDataBody;
import com.novaboot.module.system.dto.DictDataQuery;
import com.novaboot.module.system.dto.DictDataVO;
import com.novaboot.module.system.dto.DictTypeBody;
import com.novaboot.module.system.dto.DictTypeQuery;
import com.novaboot.module.system.dto.DictTypeVO;
import com.novaboot.module.system.entity.SysDictData;
import com.novaboot.module.system.entity.SysDictType;
import com.novaboot.module.system.mapper.SysDictDataMapper;
import com.novaboot.module.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典服務（類型 + 數據）
 */
@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;

    // ---------------------------------- 字典類型 ----------------------------------

    public PageResult<DictTypeVO> typePage(DictTypeQuery query) {
        Page<SysDictType> page = sysDictTypeMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysDictType>lambdaQuery()
                        .and(StringUtils.hasText(query.getKeyword()), w -> w
                                .like(SysDictType::getName, query.getKeyword())
                                .or().like(SysDictType::getType, query.getKeyword()))
                        .orderByAsc(SysDictType::getId));
        List<DictTypeVO> records = page.getRecords().stream().map(this::toTypeVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    @Transactional
    public void typeCreate(DictTypeBody body) {
        checkTypeUnique(body.getType(), null);
        SysDictType dictType = new SysDictType();
        dictType.setName(body.getName());
        dictType.setType(body.getType());
        dictType.setRemark(body.getRemark());
        sysDictTypeMapper.insert(dictType);
    }

    @Transactional
    public void typeUpdate(Long id, DictTypeBody body) {
        SysDictType dictType = sysDictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典類型不存在");
        }
        checkTypeUnique(body.getType(), id);
        String oldType = dictType.getType();
        dictType.setName(body.getName());
        dictType.setType(body.getType());
        dictType.setRemark(body.getRemark());
        sysDictTypeMapper.updateById(dictType);
        if (!oldType.equals(body.getType())) {
            SysDictData update = new SysDictData();
            update.setDictType(body.getType());
            sysDictDataMapper.update(update, Wrappers.<SysDictData>lambdaUpdate()
                    .eq(SysDictData::getDictType, oldType));
        }
    }

    @Transactional
    public void typeDelete(Long id) {
        SysDictType dictType = sysDictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典類型不存在");
        }
        Long dataCount = sysDictDataMapper.selectCount(
                Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictType, dictType.getType()));
        if (dataCount != null && dataCount > 0) {
            throw new BusinessException("該類型下存在字典數據，不允許刪除");
        }
        sysDictTypeMapper.deleteById(id);
    }

    // ---------------------------------- 字典數據 ----------------------------------

    public PageResult<DictDataVO> dataPage(DictDataQuery query) {
        Page<SysDictData> page = sysDictDataMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                Wrappers.<SysDictData>lambdaQuery()
                        .eq(StringUtils.hasText(query.getDictType()), SysDictData::getDictType, query.getDictType())
                        .like(StringUtils.hasText(query.getKeyword()), SysDictData::getLabel, query.getKeyword())
                        .orderByAsc(SysDictData::getSortNo));
        List<DictDataVO> records = page.getRecords().stream().map(this::toDataVO).toList();
        return PageResult.of(page.getTotal(), records);
    }

    @Transactional
    public void dataCreate(DictDataBody body) {
        SysDictData data = new SysDictData();
        copyDataBody(data, body);
        sysDictDataMapper.insert(data);
    }

    @Transactional
    public void dataUpdate(Long id, DictDataBody body) {
        SysDictData data = sysDictDataMapper.selectById(id);
        if (data == null) {
            throw new BusinessException("字典數據不存在");
        }
        copyDataBody(data, body);
        sysDictDataMapper.updateById(data);
    }

    @Transactional
    public void dataDelete(Long id) {
        sysDictDataMapper.deleteById(id);
    }

    /**
     * 按類型取全部啟用的字典項（下拉用）
     */
    public List<DictDataVO> listByType(String type) {
        return sysDictDataMapper.selectList(Wrappers.<SysDictData>lambdaQuery()
                        .eq(SysDictData::getDictType, type)
                        .eq(SysDictData::getStatus, 1)
                        .orderByAsc(SysDictData::getSortNo))
                .stream().map(this::toDataVO).toList();
    }

    private void checkTypeUnique(String type, Long excludeId) {
        Long count = sysDictTypeMapper.selectCount(
                Wrappers.<SysDictType>lambdaQuery()
                        .eq(SysDictType::getType, type)
                        .ne(excludeId != null, SysDictType::getId, excludeId));
        if (count != null && count > 0) {
            throw new BusinessException("字典類型已存在");
        }
    }

    private void copyDataBody(SysDictData data, DictDataBody body) {
        data.setDictType(body.getDictType());
        data.setLabel(body.getLabel());
        data.setItemValue(body.getValue());
        data.setSortNo(body.getSortNo() == null ? 0 : body.getSortNo());
        data.setStatus(body.getStatus());
        data.setRemark(body.getRemark());
    }

    private DictTypeVO toTypeVO(SysDictType dictType) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(dictType.getId());
        vo.setName(dictType.getName());
        vo.setType(dictType.getType());
        vo.setRemark(dictType.getRemark());
        vo.setCreatedAt(dictType.getCreatedAt());
        return vo;
    }

    private DictDataVO toDataVO(SysDictData data) {
        DictDataVO vo = new DictDataVO();
        vo.setId(data.getId());
        vo.setDictType(data.getDictType());
        vo.setLabel(data.getLabel());
        vo.setValue(data.getItemValue());
        vo.setSortNo(data.getSortNo());
        vo.setStatus(data.getStatus());
        vo.setRemark(data.getRemark());
        vo.setCreatedAt(data.getCreatedAt());
        return vo;
    }
}
