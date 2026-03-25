package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.foodie.dto.platform.ReportQueryDTO;
import com.foodie.entity.User;
import com.foodie.platform.mapper.ReportMapper;
import com.foodie.platform.mapper.UserMapper;

import com.foodie.platform.service.ReportService;
import com.foodie.vo.platform.HotDishVO;
import com.foodie.vo.platform.OrderReportVO;
import com.foodie.vo.platform.RevenueReportVO;
import com.foodie.vo.platform.UserGrowthReportVO;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表服务实现
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额报表
     */
    @Override
    public RevenueReportVO getRevenueReport(ReportQueryDTO reportQueryDTO) {
        LocalDate beginDate = reportQueryDTO.getBeginDate();
        LocalDate endDate = reportQueryDTO.getEndDate();

        List<String> dateList = new ArrayList<>();
        List<BigDecimal> revenueList = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        // 遍历日期范围
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dateList.add(date.toString());

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            BigDecimal revenue = reportMapper.sumRevenue(beginTime, endTime);
            revenueList.add(revenue);
            totalRevenue = totalRevenue.add(revenue);
        }

        return RevenueReportVO.builder()
                .dateList(dateList)
                .revenueList(revenueList)
                .totalRevenue(totalRevenue)
                .build();
    }

    /**
     * 订单统计报表
     */
    @Override
    public OrderReportVO getOrderReport(ReportQueryDTO reportQueryDTO) {
        LocalDate beginDate = reportQueryDTO.getBeginDate();
        LocalDate endDate = reportQueryDTO.getEndDate();

        List<String> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        // 遍历日期范围
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dateList.add(date.toString());

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer orderCount = reportMapper.countOrders(beginTime, endTime);
            Integer validCount = reportMapper.countValidOrders(beginTime, endTime);

            orderCountList.add(orderCount);
            validOrderCountList.add(validCount);
            totalOrderCount += orderCount;
            validOrderCount += validCount;
        }

        // 计算订单完成率
        Double orderCompletionRate = totalOrderCount > 0
                ? (double) validOrderCount / totalOrderCount * 100
                : 0.0;
        orderCompletionRate = Math.round(orderCompletionRate * 100.0) / 100.0;

        return OrderReportVO.builder()
                .dateList(dateList)
                .orderCountList(orderCountList)
                .validOrderCountList(validOrderCountList)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 用户增长报表
     */
    @Override
    public UserGrowthReportVO getUserGrowthReport(ReportQueryDTO reportQueryDTO) {
        LocalDate beginDate = reportQueryDTO.getBeginDate();
        LocalDate endDate = reportQueryDTO.getEndDate();

        List<String> dateList = new ArrayList<>();
        List<Integer> newUserCountList = new ArrayList<>();
        List<Integer> totalUserCountList = new ArrayList<>();
        Integer totalNewUserCount = 0;

        // 遍历日期范围
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dateList.add(date.toString());

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 当天新增用户数
            Integer newUserCount = reportMapper.countNewUsers(beginTime, endTime);
            newUserCountList.add(newUserCount);
            totalNewUserCount += newUserCount;

            // 截止当天的总用户数
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.le(User::getCreateTime, endTime);
            Integer totalCount = Math.toIntExact(userMapper.selectCount(queryWrapper));
            totalUserCountList.add(totalCount);
        }

        return UserGrowthReportVO.builder()
                .dateList(dateList)
                .newUserCountList(newUserCountList)
                .totalUserCountList(totalUserCountList)
                .totalNewUserCount(totalNewUserCount)
                .build();
    }

    /**
     * 热销商品TOP10
     */
    @Override
    public List<HotDishVO> getHotDishes() {
        List<Map<String, Object>> hotDishes = reportMapper.getHotDishes();

        // 计算总销量
        Integer totalSales = hotDishes.stream()
                .mapToInt(map -> ((Number) map.get("sales_count")).intValue())
                .sum();

        // 转换为VO
        return hotDishes.stream().map(map -> {
            Integer salesCount = ((Number) map.get("sales_count")).intValue();
            Double salesRatio = totalSales > 0
                    ? new BigDecimal(salesCount).divide(new BigDecimal(totalSales), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue()
                    : 0.0;

            return HotDishVO.builder()
                    .dishId(((Number) map.get("dish_id")).longValue())
                    .dishName((String) map.get("dish_name"))
                    .dishImage((String) map.get("dish_image"))
                    .merchantId(map.get("merchant_id") != null ? ((Number) map.get("merchant_id")).longValue() : null)
                    .merchantName((String) map.get("merchant_name"))
                    .salesCount(salesCount)
                    .salesRatio(Math.round(salesRatio * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }
}