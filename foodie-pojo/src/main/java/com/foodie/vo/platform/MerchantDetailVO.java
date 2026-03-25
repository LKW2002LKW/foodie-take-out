package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 13:39
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetailVO implements Serializable {

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
    private String businessHours;
    private BigDecimal minDeliveryAmount;
    private BigDecimal deliveryFee;
    private BigDecimal commissionRate;
    private Integer status;
    private String statusText;
    private Integer auditStatus;
    private String auditStatusText;
    private String auditReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 统计数据
     */
    private Integer totalOrderCount;
    private Integer completedOrderCount;
    private BigDecimal totalRevenue;
    private BigDecimal platformIncome;
}
