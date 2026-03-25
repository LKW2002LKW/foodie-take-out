package com.foodie.vo.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MerchantVO implements Serializable {

    private Long id;
    private String merchantName;
    private String logo;
    private String description;
    private String businessHours;
    private BigDecimal minDeliveryAmount;    // 起送金额
    private BigDecimal deliveryFee;          // 配送费
    private Integer status;                  // 营业状态
    private String statusDesc;               // 状态描述
    private BigDecimal distance;             // 距离（km）
    private Integer salesVolume;             // 销量（可选）
    private BigDecimal rating;               // 评分（可选）
}