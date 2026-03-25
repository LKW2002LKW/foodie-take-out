package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 11:55
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 今日统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatisticsVO implements Serializable {

    /**
     * 今日订单总数
     */
    private Integer totalOrderCount;

    /**
     * 今日待接单数
     */
    private Integer pendingOrderCount;

    /**
     * 今日已完成数
     */
    private Integer completedOrderCount;

    /**
     * 今日已取消数
     */
    private Integer cancelledOrderCount;

    /**
     * 今日营业额（已完成且已支付的订单总额）
     */
    private BigDecimal todayRevenue;

    /**
     * 今日实收金额（扣除平台抽成）
     */
    private BigDecimal todayIncome;

    /**
     * 今日平台抽成
     */
    private BigDecimal todayCommission;

    /**
     * 今日订单完成率（%）
     */
    private Double completionRate;
}