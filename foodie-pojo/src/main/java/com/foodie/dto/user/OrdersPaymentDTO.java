package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrdersPaymentDTO implements Serializable {

    private String orderNumber;   // 订单号
    private Integer payMethod;    // 支付方式
}