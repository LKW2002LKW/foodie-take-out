package com.foodie.vo.user;

import com.foodie.entity.OrderDetail;
import com.foodie.entity.OrderReview;
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
    private String merchantName;
    private Long addressBookId;
    private Integer status;
    private String statusText;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private String payMethodText;
    private Integer payStatus;
    private BigDecimal amount;
    private BigDecimal deliveryFee;
    private BigDecimal packAmount;
    private BigDecimal totalAmount;
    private String remark;
    private String phone;
    private String address;
    private String consignee;
    private String cancelReason;
    private String rejectionReason;
    private LocalDateTime cancelTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveryTime;
    private Integer tablewareNumber;
    private LocalDateTime createTime;

    /**
     * 订单明细列表
     */
    private List<OrderDetail> orderDetailList;

    /**
     * 订单评价
     */
    private OrderReview orderReview;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否可以评价
     */
    private Boolean canReview;
}