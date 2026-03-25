package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户表
 * C端用户（普通消费者）
 */

@TableName("`user`") // ⚠️ user 是 MySQL 关键字，必须加反引号
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 微信 OpenID */
    private String openid;

    /** 手机号 */
    private String phone;

    /** 密码（加密存储） */
    private String password;

    /** 用户昵称 */
    private String nickname;

    /** 头像路径 */
    private String avatar;

    /**
     * 性别
     * 0 未知
     * 1 男
     * 2 女
     */
    private Integer sex;

    /** 生日 */
    private LocalDate birthday;

    /**
     * 账号状态
     * 1 正常
     * 0 禁用
     */
    private Integer status;

    /** 注册时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private boolean hasPhone;
    private boolean hasPassword;
    private boolean hasWechat;
}