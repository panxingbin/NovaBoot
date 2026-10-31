package com.novaboot.module.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色自訂部門數據範圍請求
 */
@Data
public class RoleDeptBody {

    private List<Long> deptIds;
}
