package com.foodie.dto.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 13:31
 */
import lombok.Data;
import java.io.Serializable;

/**
 * 商户分页查询DTO
 */
@Data
public class MerchantPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 商户状态 1营业中 2休息中 3已关闭 0待审核
     */
    private Integer status;

    /**
     * 审核状态 1已通过 2审核中 3已拒绝
     */
    private Integer auditStatus;
}