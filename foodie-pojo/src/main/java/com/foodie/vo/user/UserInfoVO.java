package com.foodie.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 性别 0未知 1男 2女
     */
    private Integer sex;

    /**
     * 性别名称
     */
    private String sexName;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    /**
     * 账号状态 1正常 0禁用
     */
    private Integer status;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 是否绑定手机
     */
    private Boolean hasPhone;

    /**
     * 是否设置密码
     */
    private Boolean hasPassword;

    private Boolean hasWechat;
}