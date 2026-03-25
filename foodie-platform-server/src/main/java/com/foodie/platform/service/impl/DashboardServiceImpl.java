package com.foodie.platform.service.impl;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 17:26
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.foodie.entity.User;
import com.foodie.platform.mapper.MerchantMapper;
import com.foodie.platform.mapper.OrderMapper;
import com.foodie.platform.mapper.UserMapper;
import com.foodie.platform.service.DashboardService;
import com.foodie.vo.platform.DashboardVO;
import com.foodie.vo.platform.TrendDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据统计服务实现
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 获取数据统计面板数据
     */
    @Override
    public DashboardVO getDashboardData() {
        // 今日订单统计
        Integer todayOrderCount = orderMapper.countToday();
        Integer todayCompletedOrderCount = orderMapper.countTodayCompleted();
        Integer todayCancelledOrderCount = orderMapper.countTodayCancelled();

        // 今日交易额统计
        BigDecimal todayRevenue = orderMapper.sumTodayRevenue();
        BigDecimal todayPlatformIncome = orderMapper.sumTodayPlatformIncome();

        // 今日新增统计
        Integer todayNewUserCount = userMapper.countTodayNew();
        Integer todayNewMerchantCount = merchantMapper.countTodayNew();

        // 待处理数据
        Integer pendingMerchantCount = merchantMapper.countPending();
        Integer pendingOrderCount = orderMapper.countPending();

        // 总数统计
        Integer totalUserCount = Math.toIntExact(userMapper.selectCount(null));
        Integer totalMerchantCount = Math.toIntExact(merchantMapper.selectCount(null));
        Integer openMerchantCount = merchantMapper.countOpen();

        return DashboardVO.builder()
                .todayOrderCount(todayOrderCount)
                .todayCompletedOrderCount(todayCompletedOrderCount)
                .todayCancelledOrderCount(todayCancelledOrderCount)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .todayPlatformIncome(todayPlatformIncome != null ? todayPlatformIncome : BigDecimal.ZERO)
                .todayNewUserCount(todayNewUserCount)
                .todayNewMerchantCount(todayNewMerchantCount)
                .pendingMerchantCount(pendingMerchantCount)
                .pendingOrderCount(pendingOrderCount)
                .totalUserCount(totalUserCount)
                .totalMerchantCount(totalMerchantCount)
                .openMerchantCount(openMerchantCount)
                .build();
    }

    /**
     * 获取趋势图表数据
     */
    @Override
    public TrendDataVO getTrendData(Integer days) {
        if (days == null || (days != 7 && days != 30)) {
            days = 7;
        }

        List<String> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<BigDecimal> revenueList = new ArrayList<>();
        List<Integer> userCountList = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dateList.add(date.toString());

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 统计订单数
            Integer orderCount = orderMapper.countByTimePeriod(beginTime, endTime);
            orderCountList.add(orderCount);

            // 统计交易额
            BigDecimal revenue = orderMapper.sumRevenueByTimePeriod(beginTime, endTime);
            revenueList.add(revenue != null ? revenue : BigDecimal.ZERO);

            // 统计用户数（当天新增）
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(User::getCreateTime, beginTime, endTime);
            Integer userCount = Math.toIntExact(userMapper.selectCount(queryWrapper));
            userCountList.add(userCount);
        }

        return TrendDataVO.builder()
                .dateList(dateList)
                .orderCountList(orderCountList)
                .revenueList(revenueList)
                .userCountList(userCountList)
                .build();
    }
}
