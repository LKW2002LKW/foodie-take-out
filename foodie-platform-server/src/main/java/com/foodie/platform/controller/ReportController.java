package com.foodie.platform.controller;


import com.foodie.common.result.Result;
import com.foodie.dto.platform.ReportQueryDTO;
import com.foodie.platform.service.ReportService;
import com.foodie.vo.platform.HotDishVO;
import com.foodie.vo.platform.OrderReportVO;
import com.foodie.vo.platform.RevenueReportVO;
import com.foodie.vo.platform.UserGrowthReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 报表控制器
 */
@RestController
@RequestMapping("/platform/report")
@Api(tags = "平台端-数据报表接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额报表
     */
    @GetMapping("/revenue")
    @ApiOperation("营业额报表")
    public Result<RevenueReportVO> getRevenueReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询营业额报表，起始日期：{}，结束日期：{}", beginDate, endDate);

        ReportQueryDTO queryDTO = new ReportQueryDTO();
        queryDTO.setBeginDate(beginDate);
        queryDTO.setEndDate(endDate);

        RevenueReportVO report = reportService.getRevenueReport(queryDTO);
        return Result.success(report);
    }

    /**
     * 订单统计报表
     */
    @GetMapping("/order")
    @ApiOperation("订单统计报表")
    public Result<OrderReportVO> getOrderReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询订单统计报表，起始日期：{}，结束日期：{}", beginDate, endDate);

        ReportQueryDTO queryDTO = new ReportQueryDTO();
        queryDTO.setBeginDate(beginDate);
        queryDTO.setEndDate(endDate);

        OrderReportVO report = reportService.getOrderReport(queryDTO);
        return Result.success(report);
    }

    /**
     * 用户增长报表
     */
    @GetMapping("/user-growth")
    @ApiOperation("用户增长报表")
    public Result<UserGrowthReportVO> getUserGrowthReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询用户增长报表，起始日期：{}，结束日期：{}", beginDate, endDate);

        ReportQueryDTO queryDTO = new ReportQueryDTO();
        queryDTO.setBeginDate(beginDate);
        queryDTO.setEndDate(endDate);

        UserGrowthReportVO report = reportService.getUserGrowthReport(queryDTO);
        return Result.success(report);
    }

    /**
     * 热销商品TOP10
     */
    @GetMapping("/hot-dishes")
    @ApiOperation("热销商品TOP10")
    public Result<List<HotDishVO>> getHotDishes() {
        log.info("查询热销商品TOP10");
        List<HotDishVO> hotDishes = reportService.getHotDishes();
        return Result.success(hotDishes);
    }
}