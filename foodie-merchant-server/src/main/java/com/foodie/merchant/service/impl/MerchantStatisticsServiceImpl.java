package com.foodie.merchant.service.impl;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 11:59
 */

import com.foodie.common.context.BaseContext;
import com.foodie.dto.merchant.OverviewStatisticsVO;
import com.foodie.dto.merchant.TodayStatisticsVO;
import com.foodie.dto.merchant.TrendStatisticsVO;
import com.foodie.merchant.mapper.MerchantStatisticsMapper;
import com.foodie.merchant.service.MerchantStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家统计服务实现
 */
@Service
@Slf4j
public class MerchantStatisticsServiceImpl implements MerchantStatisticsService {

    @Autowired
    private MerchantStatisticsMapper statisticsMapper;

    /**
     * 获取今日统计数据
     */
    @Override
    public TodayStatisticsVO getTodayStatistics() {
        Long merchantId = BaseContext.getCurrentId();

        // 统计今日订单数据
        Integer totalOrderCount = statisticsMapper.countTodayOrders(merchantId);
        Integer pendingOrderCount = statisticsMapper.countTodayPendingOrders(merchantId);
        Integer completedOrderCount = statisticsMapper.countTodayCompletedOrders(merchantId);
        Integer cancelledOrderCount = statisticsMapper.countTodayCancelledOrders(merchantId);

        // 统计今日金额数据
        BigDecimal todayRevenue = statisticsMapper.sumTodayRevenue(merchantId);
        BigDecimal todayIncome = statisticsMapper.sumTodayIncome(merchantId);
        BigDecimal todayCommission = statisticsMapper.sumTodayCommission(merchantId);

        // 计算订单完成率
        Double completionRate = 0.0;
        if (totalOrderCount > 0) {
            completionRate = new BigDecimal(completedOrderCount)
                    .divide(new BigDecimal(totalOrderCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
            completionRate = Math.round(completionRate * 100.0) / 100.0;
        }

        return TodayStatisticsVO.builder()
                .totalOrderCount(totalOrderCount)
                .pendingOrderCount(pendingOrderCount)
                .completedOrderCount(completedOrderCount)
                .cancelledOrderCount(cancelledOrderCount)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .todayIncome(todayIncome != null ? todayIncome : BigDecimal.ZERO)
                .todayCommission(todayCommission != null ? todayCommission : BigDecimal.ZERO)
                .completionRate(completionRate)
                .build();
    }

    /**
     * 获取趋势统计数据
     */
    @Override
    public TrendStatisticsVO getTrendStatistics(Integer days) {
        if (days == null || (days != 7 && days != 30)) {
            days = 7;
        }

        Long merchantId = BaseContext.getCurrentId();
        LocalDate today = LocalDate.now();

        List<String> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<BigDecimal> revenueList = new ArrayList<>();
        List<BigDecimal> incomeList = new ArrayList<>();

        Integer totalOrderCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        // 遍历日期范围
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.toString());

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 统计当天订单数
            Integer orderCount = statisticsMapper.countOrdersByTime(merchantId, beginTime, endTime);
            orderCountList.add(orderCount);
            totalOrderCount += orderCount;

            // 统计当天营业额
            BigDecimal revenue = statisticsMapper.sumRevenueByTime(merchantId, beginTime, endTime);
            revenue = revenue != null ? revenue : BigDecimal.ZERO;
            revenueList.add(revenue);
            totalRevenue = totalRevenue.add(revenue);

            // 统计当天实收金额
            BigDecimal income = statisticsMapper.sumIncomeByTime(merchantId, beginTime, endTime);
            income = income != null ? income : BigDecimal.ZERO;
            incomeList.add(income);
            totalIncome = totalIncome.add(income);
        }

        return TrendStatisticsVO.builder()
                .dateList(dateList)
                .orderCountList(orderCountList)
                .revenueList(revenueList)
                .incomeList(incomeList)
                .totalOrderCount(totalOrderCount)
                .totalRevenue(totalRevenue)
                .totalIncome(totalIncome)
                .build();
    }

    /**
     * 获取概览统计数据
     */
    @Override
    public OverviewStatisticsVO getOverviewStatistics() {
        Long merchantId = BaseContext.getCurrentId();

        // 统计累计数据
        Integer totalOrders = statisticsMapper.countTotalOrders(merchantId);
        Integer completedOrders = statisticsMapper.countTotalCompletedOrders(merchantId);
        BigDecimal totalRevenue = statisticsMapper.sumTotalRevenue(merchantId);
        BigDecimal totalIncome = statisticsMapper.sumTotalIncome(merchantId);

        // 计算平均客单价
        BigDecimal avgOrderAmount = BigDecimal.ZERO;
        if (completedOrders > 0) {
            avgOrderAmount = totalRevenue.divide(new BigDecimal(completedOrders), 2, RoundingMode.HALF_UP);
        }

        // 统计评价数据
        BigDecimal merchantRating = statisticsMapper.getAverageRating(merchantId);
        merchantRating = merchantRating.setScale(2, RoundingMode.HALF_UP);
        Integer totalReviews = statisticsMapper.countTotalReviews(merchantId);

        return OverviewStatisticsVO.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .avgOrderAmount(avgOrderAmount)
                .merchantRating(merchantRating)
                .totalReviews(totalReviews)
                .build();
    }
}
