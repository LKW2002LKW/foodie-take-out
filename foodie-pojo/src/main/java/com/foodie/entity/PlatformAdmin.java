package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台管理员实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("platform_admin")
public class PlatformAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;

    /**
     * 角色类型 1超级管理员 2普通管理员
     */
    private Integer roleType;

    /**
     * 账号状态 1正常 0禁用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime lastLoginTime;
}