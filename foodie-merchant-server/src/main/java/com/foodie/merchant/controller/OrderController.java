package com.foodie.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.merchant.OrderOperationDTO;
import com.foodie.dto.merchant.OrderPageQueryDTO;
import com.foodie.entity.Orders;
import com.foodie.merchant.service.OrderService;
import com.foodie.vo.merchant.OrderDetailVO;
import com.foodie.vo.merchant.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商户端订单管理控制器
 */
@RestController
@RequestMapping("/merchant/order")
@Api(tags = "商户端-订单管理接口")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 订单分页查询
     */
    @GetMapping("/page")
    @ApiOperation("订单分页查询")
    public Result<Page<Orders>> page(OrderPageQueryDTO orderPageQueryDTO) {
        log.info("商户端订单分页查询：{}", orderPageQueryDTO);
        Page<Orders> page = orderService.pageQuery(orderPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 订单统计
     */
    @GetMapping("/statistics")
    @ApiOperation("订单统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("商户端订单统计");
        OrderStatisticsVO statistics = orderService.statistics();
        return Result.success(statistics);
    }

    /**
     * 订单详情
     */
    @GetMapping("/details/{id}")
    @ApiOperation("订单详情")
    public Result<OrderDetailVO> details(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrderDetailVO orderDetailVO = orderService.details(id);
        return Result.success(orderDetailVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result<Void> confirm(@RequestBody @Validated OrderOperationDTO orderOperationDTO) {
        log.info("接单：{}", orderOperationDTO);
        orderService.confirm(orderOperationDTO);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result<Void> rejection(@RequestBody @Validated OrderOperationDTO orderOperationDTO) {
        log.info("拒单：{}", orderOperationDTO);
        orderService.rejection(orderOperationDTO);
        return Result.success();
    }

    /**
     * 派送订单
     */
    @PutMapping("/delivery")
    @ApiOperation("派送订单")
    public Result<Void> delivery(@RequestBody @Validated OrderOperationDTO orderOperationDTO) {
        log.info("派送订单：{}", orderOperationDTO);
        orderService.delivery(orderOperationDTO);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete")
    @ApiOperation("完成订单")
    public Result<Void> complete(@RequestBody @Validated OrderOperationDTO orderOperationDTO) {
        log.info("完成订单：{}", orderOperationDTO);
        orderService.complete(orderOperationDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@RequestBody @Validated OrderOperationDTO orderOperationDTO) {
        log.info("取消订单：{}", orderOperationDTO);
        orderService.cancel(orderOperationDTO);
        return Result.success();
    }
}