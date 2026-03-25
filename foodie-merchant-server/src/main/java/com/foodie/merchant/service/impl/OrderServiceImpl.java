package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.constant.StatusConstant;
import com.foodie.common.context.BaseContext;
import com.foodie.common.exception.OrderBusinessException;
import com.foodie.dto.merchant.OrderOperationDTO;
import com.foodie.dto.merchant.OrderPageQueryDTO;
import com.foodie.entity.OrderDetail;
import com.foodie.entity.Orders;
import com.foodie.merchant.mapper.OrderDetailMapper;
import com.foodie.merchant.mapper.OrdersMapper;
import com.foodie.merchant.service.OrderService;
import com.foodie.vo.merchant.OrderDetailVO;
import com.foodie.vo.merchant.OrderStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 订单分页查询
     */
    @Override
    public Page<Orders> pageQuery(OrderPageQueryDTO orderPageQueryDTO) {
        Long merchantId = BaseContext.getCurrentId();

        Page<Orders> page = new Page<>(orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getMerchantId, merchantId)
                .like(StringUtils.hasText(orderPageQueryDTO.getOrderNumber()),
                        Orders::getOrderNumber, orderPageQueryDTO.getOrderNumber())
                .like(StringUtils.hasText(orderPageQueryDTO.getPhone()),
                        Orders::getPhone, orderPageQueryDTO.getPhone())
                .eq(orderPageQueryDTO.getStatus() != null,
                        Orders::getStatus, orderPageQueryDTO.getStatus())
                .ge(orderPageQueryDTO.getBeginTime() != null,
                        Orders::getOrderTime, orderPageQueryDTO.getBeginTime())
                .le(orderPageQueryDTO.getEndTime() != null,
                        Orders::getOrderTime, orderPageQueryDTO.getEndTime())
                .orderByDesc(Orders::getOrderTime);

        return ordersMapper.selectPage(page, queryWrapper);
    }

    /**
     * 订单统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        Long merchantId = BaseContext.getCurrentId();

        // 待接单数量 (状态为2)
        Integer toBeConfirmed = ordersMapper.countByMerchantIdAndStatus(merchantId, StatusConstant.ORDER_TO_BE_CONFIRMED);

        // 待派送数量 (状态为3)
        Integer confirmed = ordersMapper.countByMerchantIdAndStatus(merchantId, StatusConstant.PAY_STATUS_REFUNDED);

        // 派送中数量 (状态为4)
        Integer deliveryInProgress = ordersMapper.countByMerchantIdAndStatus(merchantId, StatusConstant.ORDER_DELIVERY_IN_PROGRESS);

        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }

    /**
     * 订单详情
     */
    @Override
    public OrderDetailVO details(Long id) {
        // 查询订单
        Orders orders = ordersMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证订单是否属于当前商户
        Long merchantId = BaseContext.getCurrentId();
        if (!orders.getMerchantId().equals(merchantId)) {
            throw new OrderBusinessException("该订单不属于当前商户");
        }

        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);

        // 组装VO
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtils.copyProperties(orders, orderDetailVO);
        orderDetailVO.setOrderDetailList(orderDetails);

        return orderDetailVO;
    }

    /**
     * 接单
     */
    @Override
    @Transactional
    public void confirm(OrderOperationDTO orderOperationDTO) {
        Orders orders = ordersMapper.selectById(orderOperationDTO.getId());

        // 校验订单状态
        if (orders == null || !StatusConstant.ORDER_TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单状态错误，无法接单");
        }

        // 验证商户
        Long merchantId = BaseContext.getCurrentId();
        if (!orders.getMerchantId().equals(merchantId)) {
            throw new OrderBusinessException("该订单不属于当前商户");
        }

        // 更新订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderOperationDTO.getId())
                .set(Orders::getStatus, StatusConstant.ORDER_CONFIRMED);

        ordersMapper.update(null, updateWrapper);

        log.info("商户{}接单成功，订单ID：{}", merchantId, orderOperationDTO.getId());
    }

    /**
     * 拒单
     */
    @Override
    @Transactional
    public void rejection(OrderOperationDTO orderOperationDTO) {
        Orders orders = ordersMapper.selectById(orderOperationDTO.getId());

        // 校验订单状态（只有待接单状态可以拒单）
        if (orders == null || !StatusConstant.ORDER_TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单状态错误，无法拒单");
        }

        // 验证商户
        Long merchantId = BaseContext.getCurrentId();
        if (!orders.getMerchantId().equals(merchantId)) {
            throw new OrderBusinessException("该订单不属于当前商户");
        }

        // 检查拒单原因
        if (!StringUtils.hasText(orderOperationDTO.getReason())) {
            throw new OrderBusinessException("请填写拒单原因");
        }

        // 检查支付状态，如果已支付需要退款
        if (StatusConstant.PAY_STATUS_PAID.equals(orders.getPayStatus())) {
            // TODO: 调用退款接口
            log.info("订单{}需要退款", orderOperationDTO.getId());
        }

        // 更新订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderOperationDTO.getId())
                .set(Orders::getStatus, StatusConstant.ORDER_CANCELLED)
                .set(Orders::getRejectionReason, orderOperationDTO.getReason())
                .set(Orders::getCancelTime, LocalDateTime.now());

        ordersMapper.update(null, updateWrapper);

        log.info("商户{}拒单成功，订单ID：{}，原因：{}", merchantId, orderOperationDTO.getId(), orderOperationDTO.getReason());
    }

    /**
     * 派送订单
     */
    @Override
    @Transactional
    public void delivery(OrderOperationDTO orderOperationDTO) {
        Orders orders = ordersMapper.selectById(orderOperationDTO.getId());

        // 校验订单状态（必须是已接单状态）
        if (orders == null || !StatusConstant.ORDER_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单状态错误，无法派送");
        }

        // 验证商户
        Long merchantId = BaseContext.getCurrentId();
        if (!orders.getMerchantId().equals(merchantId)) {
            throw new OrderBusinessException("该订单不属于当前商户");
        }

        // 更新订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderOperationDTO.getId())
                .set(Orders::getStatus, StatusConstant.ORDER_DELIVERY_IN_PROGRESS);

        ordersMapper.update(null, updateWrapper);

        log.info("商户{}派送订单成功，订单ID：{}", merchantId, orderOperationDTO.getId());
    }

    /**
     * 完成订单
     */
    @Override
    @Transactional
    public void complete(OrderOperationDTO orderOperationDTO) {
        Orders orders = ordersMapper.selectById(orderOperationDTO.getId());

        // 校验订单状态（必须是派送中状态）
        if (orders == null || !StatusConstant.ORDER_DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException("订单状态错误，无法完成");
        }

        // 验证商户
        Long merchantId = BaseContext.getCurrentId();
        if (!orders.getMerchantId().equals(merchantId)) {
            throw new OrderBusinessException("该订单不属于当前商户");
        }

        // 更新订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderOperationDTO.getId())
                .set(Orders::getStatus, StatusConstant.ORDER_COMPLETED)
                .set(Orders::getDeliveryTime, LocalDateTime.now());

        ordersMapper.update(null, updateWrapper);

        log.info("商户{}完成订单，订单ID：{}", merchantId, orderOperationDTO.getId());

        // TODO: 记录结算数据
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional
    public void cancel(OrderOperationDTO orderOperationDTO) {
        Orders orders = ordersMapper.selectById(orderOperationDTO.getId());

        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证商户
        Long merchantId = BaseContext.getCurrentId();
        if (!orders.getMerchantId().equals(merchantId)) {
            throw new OrderBusinessException("该订单不属于当前商户");
        }

        // 检查取消原因
        if (!StringUtils.hasText(orderOperationDTO.getReason())) {
            throw new OrderBusinessException("请填写取消原因");
        }

        // 检查支付状态，如果已支付需要退款
        if (StatusConstant.PAY_STATUS_PAID.equals(orders.getPayStatus())) {
            // TODO: 调用退款接口
            log.info("订单{}需要退款", orderOperationDTO.getId());
        }

        // 更新订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderOperationDTO.getId())
                .set(Orders::getStatus, StatusConstant.ORDER_CANCELLED)
                .set(Orders::getCancelReason, orderOperationDTO.getReason())
                .set(Orders::getCancelTime, LocalDateTime.now());

        ordersMapper.update(null, updateWrapper);

        log.info("商户{}取消订单，订单ID：{}，原因：{}", merchantId, orderOperationDTO.getId(), orderOperationDTO.getReason());
    }
}