package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/28 17:46
 */

import lombok.Data;

import java.io.Serializable;

/**
 * 员工查询DTO
 */
@Data
public class EmployeeQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 姓名/用户名（模糊查询）
     */
    private String keyword;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 获取分页起始位置
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}