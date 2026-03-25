package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 13:36
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据统计面板VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO implements Serializable {

    /**
     * 今日订单数
     */
    private Integer todayOrderCount;

    /**
     * 今日完成订单数
     */
    private Integer todayCompletedOrderCount;

    /**
     * 今日取消订单数
     */
    private Integer todayCancelledOrderCount;

    /**
     * 今日交易额
     */
    private BigDecimal todayRevenue;

    /**
     * 今日平台收入
     */
    private BigDecimal todayPlatformIncome;

    /**
     * 今日新增用户数
     */
    private Integer todayNewUserCount;

    /**
     * 今日新增商户数
     */
    private Integer todayNewMerchantCount;

    /**
     * 待审核商户数
     */
    private Integer pendingMerchantCount;

    /**
     * 待处理订单数（待接单）
     */
    private Integer pendingOrderCount;

    /**
     * 总用户数
     */
    private Integer totalUserCount;

    /**
     * 总商户数
     */
    private Integer totalMerchantCount;

    /**
     * 营业中商户数
     */
    private Integer openMerchantCount;
}