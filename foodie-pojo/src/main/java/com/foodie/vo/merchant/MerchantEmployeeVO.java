package com.foodie.vo.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:45
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户员工VO
 */
@Data
public class MerchantEmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    private Long id;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号（脱敏）
     */
    private String idCard;

    /**
     * 角色 1管理员 2经理 3普通员工
     */
    private Integer role;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态 1正常 0禁用
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}