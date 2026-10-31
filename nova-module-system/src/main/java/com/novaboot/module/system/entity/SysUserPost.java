package com.novaboot.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用戶崗位關聯
 */
@Data
@TableName("sys_user_post")
public class SysUserPost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long postId;
}
