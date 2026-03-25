package com.foodie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

/**
 * 商户表
 */

@TableName("merchant")
public class Merchant implements Serializable {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商户名称 */
    private String merchantName;

    /** 商户编码 */
    private String merchantCode;

    /** 营业执照号 */
    private String businessLicense;

    /** 法人姓名 */
    private String legalPerson;

    /** 联系人姓名 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 省份编码 */
    private String provinceCode;

    /** 省份名称 */
    private String provinceName;

    /** 城市编码 */
    private String cityCode;

    /** 城市名称 */
    private String cityName;

    /** 区县编码 */
    private String districtCode;

    /** 区县名称 */
    private String districtName;

    /** 详细地址 */
    private String address;

    /** 经度 */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    /** 商户Logo */
    private String logo;

    /** 商户描述 */
    private String description;

    /** 营业时间 */
    private String businessHours;

    /** 起送金额 */
    private BigDecimal minDeliveryAmount;

    /** 配送费 */
    private BigDecimal deliveryFee;

    /** 抽成比例 */
    private BigDecimal commissionRate;

    /**
     * 商户状态
     * 0 待审核
     * 1 营业中
     * 2 休息中
     * 3 已关闭
     */
    private Integer status;

    /**
     * 审核状态
     * 1 已通过
     * 2 审核中
     * 3 已拒绝
     */
    private Integer auditStatus;

    /** 审核说明 */
    private String auditReason;

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

