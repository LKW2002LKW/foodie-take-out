package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:47
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务报表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceReportVO implements Serializable {

    /**
     * 总订单数
     */
    private Integer totalOrderCount;

    /**
     * 完成订单数
     */
    private Integer completedOrderCount;

    /**
     * 总交易额
     */
    private BigDecimal totalRevenue;

    /**
     * 平台总收入
     */
    private BigDecimal totalPlatformIncome;

    /**
     * 已结算金额
     */
    private BigDecimal settledAmount;

    /**
     * 待结算金额
     */
    private BigDecimal pendingSettlementAmount;
}