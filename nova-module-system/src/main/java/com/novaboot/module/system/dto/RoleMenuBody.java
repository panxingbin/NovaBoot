package com.novaboot.module.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色選單分配請求
 */
@Data
public class RoleMenuBody {

    private List<Long> menuIds;
}
