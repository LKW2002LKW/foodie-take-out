package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 11:56
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 概览统计VO（包含累计数据）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewStatisticsVO implements Serializable {

    /**
     * 累计订单总数
     */
    private Integer totalOrders;

    /**
     * 累计完成订单数
     */
    private Integer completedOrders;

    /**
     * 累计营业额
     */
    private BigDecimal totalRevenue;

    /**
     * 累计实收金额
     */
    private BigDecimal totalIncome;

    /**
     * 平均客单价
     */
    private BigDecimal avgOrderAmount;

    /**
     * 商户评分
     */
    private BigDecimal merchantRating;

    /**
     * 评价总数
     */
    private Integer totalReviews;
}