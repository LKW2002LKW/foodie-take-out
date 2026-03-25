package com.foodie.platform.service;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 17:25
 */

import com.foodie.vo.platform.DashboardVO;
import com.foodie.vo.platform.TrendDataVO;

/**
 * 数据统计服务接口
 */
public interface DashboardService {

    /**
     * 获取数据统计面板数据
     * @return 统计数据
     */
    DashboardVO getDashboardData();

    /**
     * 获取趋势图表数据
     * @param days 天数（7或30）
     * @return 趋势数据
     */
    TrendDataVO getTrendData(Integer days);
}