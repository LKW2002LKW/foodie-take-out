package com.foodie.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.platform.OrderPageQueryDTO;
import com.foodie.entity.Orders;
import com.foodie.vo.platform.OrderDetailVO;


/**
 * 订单服务接口（平台端）
 */
public interface OrderService {

    /**
     * 订单分页查询
     * @param orderPageQueryDTO 查询条件
     * @return 分页结果
     */
    Page<Orders> pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    /**
     * 订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderDetailVO getDetail(Long id);

    /**
     * 获取异常订单列表
     * @return 异常订单
     */
    Page<Orders> getAbnormalOrders(Integer page, Integer pageSize);
}