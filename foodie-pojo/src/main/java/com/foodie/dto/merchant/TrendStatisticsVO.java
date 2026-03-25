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
import java.util.List;

/**
 * 趋势统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendStatisticsVO implements Serializable {

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 订单数列表
     */
    private List<Integer> orderCountList;

    /**
     * 营业额列表
     */
    private List<BigDecimal> revenueList;

    /**
     * 实收金额列表
     */
    private List<BigDecimal> incomeList;

    /**
     * 统计周期内订单总数
     */
    private Integer totalOrderCount;

    /**
     * 统计周期内营业额总计
     */
    private BigDecimal totalRevenue;

    /**
     * 统计周期内实收总计
     */
    private BigDecimal totalIncome;
}