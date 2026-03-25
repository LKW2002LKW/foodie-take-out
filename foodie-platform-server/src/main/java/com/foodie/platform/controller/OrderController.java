package com.foodie.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.platform.OrderPageQueryDTO;
import com.foodie.entity.Orders;
import com.foodie.platform.service.OrderService;
import com.foodie.vo.platform.OrderDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单监控控制器
 */
@RestController
@RequestMapping("/platform/order")
@Api(tags = "平台端-订单监控接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单分页查询
     */
    @GetMapping("/page")
    @ApiOperation("订单分页查询")
    public Result<Page<Orders>> page(OrderPageQueryDTO orderPageQueryDTO) {
        log.info("平台端订单分页查询：{}", orderPageQueryDTO);
        Page<Orders> page = orderService.pageQuery(orderPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    @ApiOperation("订单详情")
    public Result<OrderDetailVO> getDetail(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrderDetailVO detailVO = orderService.getDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 异常订单列表
     */
    @GetMapping("/abnormal")
    @ApiOperation("异常订单列表")
    public Result<Page<Orders>> getAbnormalOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询异常订单列表");
        Page<Orders> abnormalOrders = orderService.getAbnormalOrders(page, pageSize);
        return Result.success(abnormalOrders);
    }
}