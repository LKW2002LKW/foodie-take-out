package com.foodie.merchant.service;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 11:58
 */

import com.foodie.dto.merchant.OverviewStatisticsVO;
import com.foodie.dto.merchant.TodayStatisticsVO;
import com.foodie.dto.merchant.TrendStatisticsVO;

/**
 * 商家统计服务接口
 */
public interface MerchantStatisticsService {

    /**
     * 获取今日统计数据
     */
    TodayStatisticsVO getTodayStatistics();

    /**
     * 获取趋势统计数据
     * @param days 天数（7或30）
     */
    TrendStatisticsVO getTrendStatistics(Integer days);

    /**
     * 获取概览统计数据
     */
    OverviewStatisticsVO getOverviewStatistics();
}