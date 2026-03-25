package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO implements Serializable {

    private Long addressBookId;              // 地址ID
    private Integer payMethod;               // 支付方式：1-微信 2-支付宝
    private String remark;                   // 备注
    private LocalDateTime estimatedDeliveryTime; // 预计送达时间
    private Integer deliveryStatus;          // 配送状态：1-立即送出 0-选择具体时间
    private Integer tablewareNumber;         // 餐具数量
    private Integer tablewareStatus;         // 餐具数量状态：1-按餐量提供 0-选择具体数量
    private BigDecimal packAmount;           // 打包费
}
