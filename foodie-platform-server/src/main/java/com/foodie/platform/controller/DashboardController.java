package com.foodie.platform.controller;

import com.foodie.common.result.Result;

import com.foodie.platform.service.DashboardService;
import com.foodie.vo.platform.DashboardVO;
import com.foodie.vo.platform.TrendDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计控制器
 */
@RestController
@RequestMapping("/platform/admin/dashboard")
@Api(tags = "平台端-数据统计接口")
@Slf4j
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取数据统计面板数据
     */
    @GetMapping("/data")
    @ApiOperation("获取数据统计面板数据")
    public Result<DashboardVO> getDashboardData() {
        log.info("获取数据统计面板数据");
        DashboardVO dashboardVO = dashboardService.getDashboardData();
        return Result.success(dashboardVO);
    }

    /**
     * 获取趋势图表数据
     */
    @GetMapping("/trend")
    @ApiOperation("获取趋势图表数据")
    public Result<TrendDataVO> getTrendData(@RequestParam(defaultValue = "7") Integer days) {
        log.info("获取趋势图表数据，天数：{}", days);
        TrendDataVO trendDataVO = dashboardService.getTrendData(days);
        return Result.success(trendDataVO);
    }
}