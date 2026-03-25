package com.foodie.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.merchant.OrderOperationDTO;
import com.foodie.dto.merchant.OrderPageQueryDTO;
import com.foodie.entity.Orders;
import com.foodie.vo.merchant.OrderDetailVO;
import com.foodie.vo.merchant.OrderStatisticsVO;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 订单分页查询
     * @param orderPageQueryDTO 查询条件
     * @return 分页结果
     */
    Page<Orders> pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    /**
     * 订单统计
     * @return 统计数据
     */
    OrderStatisticsVO statistics();

    /**
     * 订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderDetailVO details(Long id);

    /**
     * 接单
     * @param orderOperationDTO 操作DTO
     */
    void confirm(OrderOperationDTO orderOperationDTO);

    /**
     * 拒单
     * @param orderOperationDTO 操作DTO
     */
    void rejection(OrderOperationDTO orderOperationDTO);

    /**
     * 派送订单
     * @param orderOperationDTO 操作DTO
     */
    void delivery(OrderOperationDTO orderOperationDTO);

    /**
     * 完成订单
     * @param orderOperationDTO 操作DTO
     */
    void complete(OrderOperationDTO orderOperationDTO);

    /**
     * 取消订单
     * @param orderOperationDTO 操作DTO
     */
    void cancel(OrderOperationDTO orderOperationDTO);
}