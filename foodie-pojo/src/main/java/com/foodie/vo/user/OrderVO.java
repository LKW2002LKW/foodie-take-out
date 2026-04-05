package com.foodie.vo.user;

import com.foodie.entity.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {

    private Long id;
    private String orderNumber;
    private Long userId;
    private Long merchantId;
    private String merchantName;
    private Integer status;
    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private Integer payMethod;
    private Integer payStatus;
    private BigDecimal amount;
    private BigDecimal deliveryFee;
    private BigDecimal packAmount;
    private BigDecimal totalAmount;
    private String remark;
    private String phone;
    private String address;
    private String consignee;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveryTime;
    private Integer tablewareNumber;
    private String statusDesc;


    private String statusText;

    private Boolean canReview;

    private Integer orderDetailCount;
    private List<OrderDetail> orderDetails;  // 订单明细
}
