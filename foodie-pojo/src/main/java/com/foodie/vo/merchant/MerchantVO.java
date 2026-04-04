package com.foodie.vo.merchant;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantVO implements Serializable {

    private Long id;
    private String merchantName;
    private String merchantCode;
    private String businessLicense;
    private String legalPerson;
    private String contactName;
    private String contactPhone;
    private String provinceCode;
    private String provinceName;
    private String cityCode;
    private String cityName;
    private String districtCode;
    private String districtName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String logo;
    private String description;
    private Long bizCategoryId;
    private String businessHours;
    private BigDecimal minDeliveryAmount;
    private BigDecimal deliveryFee;
    private BigDecimal commissionRate;
    private Integer status;
    private Integer auditStatus;
    private String auditReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 状态描述
    private String statusDesc;
    private String auditStatusDesc;
}