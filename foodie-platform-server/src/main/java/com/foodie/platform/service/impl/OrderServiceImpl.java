package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.constant.MessageConstant;

import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.platform.OrderPageQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.entity.OrderDetail;
import com.foodie.entity.Orders;
import com.foodie.entity.User;
import com.foodie.platform.mapper.MerchantMapper;
import com.foodie.platform.mapper.OrderDetailMapper;
import com.foodie.platform.mapper.OrderMapper;
import com.foodie.platform.mapper.UserMapper;
import com.foodie.platform.service.OrderService;
import com.foodie.vo.platform.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现（平台端）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    private final OrderDetailMapper orderDetailMapper;

    private final MerchantMapper merchantMapper;

    private final UserMapper userMapper;

    /**
     * 订单分页查询
     */
    @Override
    public Page<Orders> pageQuery(OrderPageQueryDTO orderPageQueryDTO) {
        Page<Orders> page = new Page<>(orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(orderPageQueryDTO.getOrderNumber()),
                        Orders::getOrderNumber, orderPageQueryDTO.getOrderNumber())
                .eq(orderPageQueryDTO.getMerchantId() != null,
                        Orders::getMerchantId, orderPageQueryDTO.getMerchantId())
                .like(StringUtils.hasText(orderPageQueryDTO.getPhone()),
                        Orders::getPhone, orderPageQueryDTO.getPhone())
                .eq(orderPageQueryDTO.getStatus() != null,
                        Orders::getStatus, orderPageQueryDTO.getStatus())
                .ge(orderPageQueryDTO.getBeginTime() != null,
                        Orders::getOrderTime, orderPageQueryDTO.getBeginTime())
                .le(orderPageQueryDTO.getEndTime() != null,
                        Orders::getOrderTime, orderPageQueryDTO.getEndTime())
                .orderByDesc(Orders::getOrderTime);

        return orderMapper.selectPage(page, queryWrapper);
    }

    /**
     * 订单详情
     */
    @Override
    public OrderDetailVO getDetail(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查询商户信息
        Merchant merchant = merchantMapper.selectById(orders.getMerchantId());

        // 查询用户信息
        User user = userMapper.selectById(orders.getUserId());

        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailWrapper);

        // 组装VO
        OrderDetailVO detailVO = new OrderDetailVO();
        BeanUtils.copyProperties(orders, detailVO);
        detailVO.setMerchantName(merchant != null ? merchant.getMerchantName() : null);
        detailVO.setUserName(orders.getUserName());
        detailVO.setUserPhone(user != null ? user.getPhone() : null);
        detailVO.setStatusText(getStatusText(orders.getStatus()));
        detailVO.setPayMethodText(getPayMethodText(orders.getPayMethod()));
        detailVO.setPayStatusText(getPayStatusText(orders.getPayStatus()));
        detailVO.setOrderDetailList(orderDetails);

        return detailVO;
    }

    /**
     * 获取异常订单列表
     */
    @Override
    public Page<Orders> getAbnormalOrders(Integer page, Integer pageSize) {
        Page<Orders> pageResult = new Page<>(page, pageSize);

        // 异常订单：超过30分钟未接单的订单
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, StatusConstant.ORDER_TO_BE_CONFIRMED)
                .le(Orders::getOrderTime, thirtyMinutesAgo)
                .orderByAsc(Orders::getOrderTime);

        return orderMapper.selectPage(pageResult, queryWrapper);
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待付款";
            case 2: return "待接单";
            case 3: return "已接单";
            case 4: return "配送中";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "已退款";
            default: return "未知";
        }
    }

    /**
     * 获取支付方式文本
     */
    private String getPayMethodText(Integer payMethod) {
        if (payMethod == null) return "未知";
        switch (payMethod) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            default: return "未知";
        }
    }

    /**
     * 获取支付状态文本
     */
    private String getPayStatusText(Integer payStatus) {
        if (payStatus == null) return "未知";
        switch (payStatus) {
            case 0: return "未支付";
            case 1: return "已支付";
            case 2: return "退款中";
            case 3: return "已退款";
            default: return "未知";
        }
    }
}