package com.foodie.merchant.controller;

/**
 * @Author: wanderer
 * @Date: 2026/1/20 12:00
 */

import com.foodie.common.result.Result;
import com.foodie.dto.merchant.OverviewStatisticsVO;
import com.foodie.dto.merchant.TodayStatisticsVO;
import com.foodie.dto.merchant.TrendStatisticsVO;
import com.foodie.merchant.service.MerchantStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家统计控制器
 */
@RestController
@RequestMapping("/merchant/statistics")
@Api(tags = "商家端-数据统计接口")
@Slf4j
public class MerchantStatisticsController {

    @Autowired
    private MerchantStatisticsService statisticsService;

    /**
     * 获取今日统计数据
     */
    @GetMapping("/today")
    @ApiOperation("获取今日统计数据")
    public Result<TodayStatisticsVO> getTodayStatistics() {
        log.info("获取今日统计数据");
        TodayStatisticsVO statistics = statisticsService.getTodayStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取趋势统计数据
     */
    @GetMapping("/trend")
    @ApiOperation("获取趋势统计数据")
    public Result<TrendStatisticsVO> getTrendStatistics(
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("获取趋势统计数据，天数：{}", days);
        TrendStatisticsVO statistics = statisticsService.getTrendStatistics(days);
        return Result.success(statistics);
    }

    /**
     * 获取概览统计数据
     */
    @GetMapping("/overview")
    @ApiOperation("获取概览统计数据")
    public Result<OverviewStatisticsVO> getOverviewStatistics() {
        log.info("获取概览统计数据");
        OverviewStatisticsVO statistics = statisticsService.getOverviewStatistics();
        return Result.success(statistics);
    }
}