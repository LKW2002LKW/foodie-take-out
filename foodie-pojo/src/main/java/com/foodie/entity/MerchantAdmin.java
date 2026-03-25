package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


/**
 * 商户管理员表
 */

@TableName("merchant_admin")
public class MerchantAdmin implements Serializable {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 用户名 */
    private String username;

    /** 密码（加密） */
    private String password;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    private String idCard;

    /**
     * 角色类型
     * 1 管理员
     * 2 员工
     */
    private Integer role;

    /**
     * 账号状态
     * 1 正常
     * 0 禁用
     */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}


