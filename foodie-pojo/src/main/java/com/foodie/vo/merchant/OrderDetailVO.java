package com.foodie.vo.merchant;

import com.foodie.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO implements Serializable {

    private Long id;
    private String orderNumber;
    private Long userId;
    private Long merchantId;
    private Long addressBookId;
    private Integer status;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private Integer payStatus;
    private BigDecimal amount;
    private BigDecimal deliveryFee;
    private BigDecimal packAmount;
    private BigDecimal platformCommission;
    private BigDecimal merchantAmount;
    private BigDecimal totalAmount;
    private String remark;
    private String phone;
    private String address;
    private String consignee;
    private String userName;
    private String cancelReason;
    private String rejectionReason;
    private LocalDateTime cancelTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveryTime;
    private Integer tablewareNumber;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 订单明细列表
     */
    private List<OrderDetail> orderDetailList;
}