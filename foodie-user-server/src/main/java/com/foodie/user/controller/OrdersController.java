package com.foodie.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.result.Result;
import com.foodie.dto.user.*;
import com.foodie.user.service.OrderService;
import com.foodie.vo.user.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/user/order")
@Api(tags = "订单管理")
@Slf4j
public class  OrdersController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("提交订单：userId={}, dto={}", userId, ordersSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submitOrder(userId, ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     */
    @PostMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("订单支付：userId={}, dto={}", userId, ordersPaymentDTO);

        OrderPaymentVO paymentVO = orderService.payment(userId, ordersPaymentDTO);
        return Result.success(paymentVO);
    }

    /**
     * 支付成功回调（模拟）
     */
    @PostMapping("/paySuccess/{orderNumber}")
    @ApiOperation("支付成功回调")
    public Result<String> paySuccess(@PathVariable String orderNumber) {
        log.info("支付成功回调：orderNumber={}", orderNumber);

        orderService.paySuccess(orderNumber);
        return Result.success(MessageConstant.PAYMENT_SUCCESS);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/detail/by-order-number/{orderNumber}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderNumber,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("查询订单详情：userId={}, orderNumber={}", userId, orderNumber);

        OrderVO orderVO = orderService.getOrderDetail(userId, orderNumber);
        return Result.success(orderVO);
    }

    /**
     * 订单分页查询
     */
    @GetMapping("/page")
    @ApiOperation("订单分页查询")
    public Result<Page<OrderVO>> page(OrdersPageQueryDTO orderPageQueryDTO) {
        log.info("用户端订单分页查询：{}", orderPageQueryDTO);
        Page<OrderVO> page = orderService.pageQuery(orderPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 订单详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("订单详情")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        OrderDetailVO orderDetailVO = orderService.getOrderDetail(id);
        return Result.success(orderDetailVO);
    }

    /**
     * 订单状态跟踪
     */
    @GetMapping("/track/{id}")
    @ApiOperation("订单状态跟踪")
    public Result<OrderTrackVO> track(@PathVariable Long id) {
        log.info("订单状态跟踪：{}", id);
        OrderTrackVO orderTrackVO = orderService.trackOrder(id);
        return Result.success(orderTrackVO);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@RequestBody @Valid OrdersCancelDTO orderCancelDTO) {
        log.info("取消订单：{}", orderCancelDTO);
        orderService.cancelOrder(orderCancelDTO);
        return Result.success();
    }

    /**
     * 评价订单
     */
    @PostMapping("/review")
    @ApiOperation("评价订单")
    public Result<Void> review(@RequestBody @Valid OrderReviewDTO orderReviewDTO) {
        log.info("评价订单：{}", orderReviewDTO);
        orderService.reviewOrder(orderReviewDTO);
        return Result.success();
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除订单：{}", id);
        orderService.deleteOrder(id);
        return Result.success();
    }
}
