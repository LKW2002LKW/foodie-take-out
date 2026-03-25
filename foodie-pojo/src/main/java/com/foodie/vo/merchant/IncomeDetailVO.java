package com.foodie.vo.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:26
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收入明细VO（基于订单）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDetailVO implements Serializable {

    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private BigDecimal platformCommission;
    private BigDecimal merchantAmount;
    private Integer payStatus;
    private String payStatusText;
}
