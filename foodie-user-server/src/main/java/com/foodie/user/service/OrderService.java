package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.user.*;
import com.foodie.entity.Orders;
import com.foodie.vo.user.*;

public interface OrderService extends IService<Orders> {

    /**
     * 提交订单
     */
    OrderSubmitVO submitOrder(Long userId, OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     */
    OrderPaymentVO payment(Long userId, OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 支付成功回调
     */
    void paySuccess(String orderNumber);

    /**
     * 查询订单详情
     */
    OrderVO getOrderDetail(Long userId, String orderNumber);

    /**
     * 订单分页查询
     * @param orderPageQueryDTO 查询条件
     * @return 分页结果
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO orderPageQueryDTO);

    /**
     * 订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(Long id);

    /**
     * 订单状态跟踪
     * @param id 订单ID
     * @return 订单跟踪信息
     */
    OrderTrackVO trackOrder(Long id);

    /**
     * 取消订单
     * @param orderCancelDTO 取消订单DTO
     */
    void cancelOrder(OrdersCancelDTO orderCancelDTO);

    /**
     * 评价订单
     * @param orderReviewDTO 评价DTO
     */
    void reviewOrder(OrderReviewDTO orderReviewDTO);

    /**
     * 删除订单（仅限已完成或已取消的订单）
     * @param id 订单ID
     */
    void deleteOrder(Long id);
}