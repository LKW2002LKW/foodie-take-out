package com.foodie.user.service.impl;

import cn.hutool.json.ObjectMapper;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.context.BaseContext;
import com.foodie.common.exception.BaseException;
import com.foodie.common.exception.OrderBusinessException;
import com.foodie.dto.user.*;
import com.foodie.entity.*;
import com.foodie.user.mapper.*;
import com.foodie.user.service.OrderService;
import com.foodie.user.service.ShoppingCartService;
import com.foodie.vo.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    private final ShoppingCartService shoppingCartService;

    private final ShoppingCartMapper shoppingCartMapper;

    private final AddressMapper addressMapper;

    private final MerchantMapper merchantMapper;

    private final OrderDetailMapper orderDetailMapper;

    private final OrdersMapper orderMapper;

    private final OrderReviewMapper orderReviewMapper;


    /**
     * 提交订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(Long userId, OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单：userId={}, dto={}", userId, ordersSubmitDTO);

        // 1. 检查地址
        AddressBook addressBook = addressMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null || !addressBook.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_EMPTY);
        }

        Long merchantId = ordersSubmitDTO.getMerchantId();
        if (merchantId == null) {
            throw new BaseException("商户ID不能为空");
        }

        // 2. 查询该商户购物车
        LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getMerchantId, merchantId);
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(cartWrapper);

        if (cartList == null || cartList.isEmpty()) {
            throw new BaseException(MessageConstant.CART_EMPTY);
        }

        // 3. 检查商户状态
        Merchant merchant = merchantMapper.selectById(merchantId);

        if (merchant == null || !merchant.getStatus().equals(StatusConstant.MERCHANT_OPEN)) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_OPEN);
        }

        // 4. 计算订单金额
        BigDecimal amount = cartList.stream()
                .map(cart -> cart.getAmount().multiply(new BigDecimal(cart.getNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 检查起送金额
        if (amount.compareTo(merchant.getMinDeliveryAmount()) < 0) {
            throw new BaseException(MessageConstant.AMOUNT_NOT_ENOUGH);
        }

        BigDecimal deliveryFee = merchant.getDeliveryFee();
        BigDecimal packAmount = ordersSubmitDTO.getPackAmount() != null
                ? ordersSubmitDTO.getPackAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = amount.add(deliveryFee).add(packAmount);

        // 5. 计算平台抽成
        BigDecimal commissionRate = merchant.getCommissionRate();
        BigDecimal platformCommission = amount.multiply(commissionRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal merchantAmount = amount.subtract(platformCommission);

        // 6. 创建订单
        String fullAddress =
                addressBook.getProvinceName()
                        + addressBook.getCityName()
                        + addressBook.getDistrictName()
                        + addressBook.getDetail();

        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);

        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setAddressBookId(addressBook.getId());
        order.setStatus(StatusConstant.ORDER_PENDING_PAYMENT);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(StatusConstant.PAY_STATUS_UNPAID);
        order.setAmount(amount);
        order.setDeliveryFee(deliveryFee);
        order.setPackAmount(packAmount);
        order.setPlatformCommission(platformCommission);
        order.setMerchantAmount(merchantAmount);
        order.setTotalAmount(totalAmount);
        order.setPhone(addressBook.getPhone());
        //order.setAddress(addressBook.getFullAddress());
        order.setAddress(fullAddress);
        order.setConsignee(addressBook.getConsignee());
        order.setUserName(addressBook.getConsignee());

        this.save(order);

        // 7. 创建订单明细
        List<OrderDetail> orderDetails = cartList.stream().map(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetails.forEach(orderDetailMapper::insert);

        // 8. 清空当前商户购物车
        shoppingCartService.cleanCartByMerchant(userId, merchantId);

        // 9. 返回结果
        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(totalAmount)
                .orderTime(order.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentVO payment(Long userId, OrdersPaymentDTO ordersPaymentDTO) {
        log.info("订单支付：userId={}, dto={}", userId, ordersPaymentDTO);

        // 1. 查询订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNumber, ordersPaymentDTO.getOrderNumber())
                .eq(Orders::getUserId, userId);

        Orders order = this.getOne(wrapper);

        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (!order.getStatus().equals(StatusConstant.ORDER_PENDING_PAYMENT)) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 2. 调用支付接口（这里模拟）
        // TODO: 实际项目中需要对接微信支付/支付宝API

        OrderPaymentVO paymentVO = OrderPaymentVO.builder()
                .orderNumber(order.getOrderNumber())
                .nonceStr("mock_nonce_str")
                .paySign("mock_pay_sign")
                .signType("MD5")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .packageStr("prepay_id=mock_prepay_id")
                .build();

        // 3. 模拟支付成功（实际应该在支付回调中处理）
        // 这里为了测试方便，直接调用支付成功方法
        log.info("模拟支付成功，订单号：{}", order.getOrderNumber());
         paySuccess(order.getOrderNumber());  // 实际环境注释掉，等待真实支付回调

        return paymentVO;
    }

    /**
     * 支付成功回调
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNumber) {
        log.info("支付成功回调：orderNumber={}", orderNumber);

        // 1. 查询订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNumber, orderNumber);

        Orders order = this.getOne(wrapper);

        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 2. 更新订单状态
        order.setStatus(StatusConstant.ORDER_TO_BE_CONFIRMED);
        order.setPayStatus(StatusConstant.PAY_STATUS_PAID);
        order.setCheckoutTime(LocalDateTime.now());

        this.updateById(order);

        log.info("订单支付成功，等待商户接单");


    }

    /**
     * 查询订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long userId, String orderNumber) {
        log.info("查询订单详情：userId={}, orderNumber={}", userId, orderNumber);

        // 1. 查询订单基本信息
        OrderVO orderVO = this.baseMapper.getOrderDetail(orderNumber, userId);

        if (orderVO == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 2. 查询订单明细
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getOrderId, orderVO.getId());
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);
        orderVO.setOrderDetails(orderDetails);

        // 3. 设置状态描述
        orderVO.setStatusDesc(getStatusDesc(orderVO.getStatus()));

        return orderVO;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        // 格式：当前时间戳 + 随机6位数
        return System.currentTimeMillis() + String.format("%06d", (int)(Math.random() * 1000000));
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 1: return "待付款";
            case 2: return "待接单";
            case 3: return "已接单";
            case 4: return "派送中";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "已退款";
            default: return "未知";
        }
    }
    private String getPayMethodText(Integer payMethod) {
        if (payMethod == null) {
            return "未知支付方式";
        }
        switch (payMethod) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "银行卡";
            default: return "未知支付方式";
        }
    }



    /**
     * 订单分页查询
     */
    @Override
    public Page<OrderVO> pageQuery(OrdersPageQueryDTO orderPageQueryDTO) {
        Long userId = BaseContext.getCurrentId();

        Page<OrderVO> page = new Page<>(orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize());
        Page<OrderVO> ordersPage = orderMapper.pageQuery(page, orderPageQueryDTO, userId);

        // 转换为VO
        Page<OrderVO> result = new Page<>();
        result.setCurrent(ordersPage.getCurrent());
        result.setSize(ordersPage.getSize());
        result.setTotal(ordersPage.getTotal());
        result.setPages(ordersPage.getPages());

        List<Long> orderIds = ordersPage.getRecords().stream()
                .map(OrderVO::getId)
                .collect(Collectors.toList());

        Set<Long> reviewedOrderIds = new HashSet<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderReview> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.in(OrderReview::getOrderId, orderIds);
            List<OrderReview> reviews = orderReviewMapper.selectList(reviewWrapper);
            reviewedOrderIds = reviews.stream()
                    .map(OrderReview::getOrderId)
                    .collect(Collectors.toSet());
        }

        Set<Long> finalReviewedOrderIds = reviewedOrderIds;
        List<OrderVO> orderVOList = ordersPage.getRecords().stream().map(orderItem -> {
            OrderVO orderVO = OrderVO.builder()
                    .id(orderItem.getId())
                    .orderNumber(orderItem.getOrderNumber())
                    .merchantId(orderItem.getMerchantId())
                    .status(orderItem.getStatus())
                    .statusText(getStatusDesc(orderItem.getStatus()))
                    .canReview(orderItem.getStatus().equals(StatusConstant.ORDER_COMPLETED)
                            && !finalReviewedOrderIds.contains(orderItem.getId()))
                    .orderTime(orderItem.getOrderTime())
                    .totalAmount(orderItem.getTotalAmount())
                    .build();

            // 查询订单明细
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(OrderDetail::getOrderId, orderItem.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailWrapper);
            orderVO.setOrderDetails(orderDetails);
            orderVO.setOrderDetailCount(orderDetails.size());

            // ✅ 查询商户信息
            Merchant merchant = merchantMapper.selectById(orderItem.getMerchantId());
            if (merchant != null) {
                orderVO.setMerchantName(merchant.getMerchantName());
                //orderVO.setLogo(merchant.getLogo());
                //orderVO.setMerchantAddress(merchant.getAddress());
            }

            return orderVO;
        }).collect(Collectors.toList());

        result.setRecords(orderVOList);

        return result;
    }

    /**
     * 订单详情
     */
    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        // 查询订单
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证订单是否属于当前用户
        Long userId = BaseContext.getCurrentId();
        if (!orders.getUserId().equals(userId)) {
            throw new OrderBusinessException("该订单不属于当前用户");
        }

        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailWrapper);

        // 查询订单评价
        LambdaQueryWrapper<OrderReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(OrderReview::getOrderId, id);
        OrderReview orderReview = orderReviewMapper.selectOne(reviewWrapper);

        // 组装VO
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtils.copyProperties(orders, orderDetailVO);
        orderDetailVO.setStatusText(getStatusDesc(orders.getStatus()));
        orderDetailVO.setPayMethodText(getPayMethodText(orders.getPayMethod()));
        orderDetailVO.setOrderDetailList(orderDetails);
        orderDetailVO.setOrderReview(orderReview);

        // 判断是否可以取消（待付款、待接单、已接单状态可以取消）
        boolean canCancel = orders.getStatus().equals(StatusConstant.ORDER_PENDING_PAYMENT) ||
                orders.getStatus().equals(StatusConstant.ORDER_TO_BE_CONFIRMED) ||
                orders.getStatus().equals(StatusConstant.ORDER_CONFIRMED);
        orderDetailVO.setCanCancel(canCancel);

        // 判断是否可以评价（已完成且未评价）
        boolean canReview = orders.getStatus().equals(StatusConstant.ORDER_COMPLETED) && orderReview == null;
        orderDetailVO.setCanReview(canReview);

        return orderDetailVO;
    }

    /**
     * 订单状态跟踪
     */
    @Override
    public OrderTrackVO trackOrder(Long id) {
        // 查询订单
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证订单是否属于当前用户
        Long userId = BaseContext.getCurrentId();
        if (!orders.getUserId().equals(userId)) {
            throw new OrderBusinessException("该订单不属于当前用户");
        }

        // 构建跟踪节点
        List<OrderTrackVO.TrackNode> trackNodes = new ArrayList<>();

        // 下单节点
        trackNodes.add(OrderTrackVO.TrackNode.builder()
                .title("订单已提交")
                .description("您的订单已提交成功")
                .time(orders.getCreateTime())
                .completed(true)
                .build());

        // 支付节点
        if (orders.getPayStatus().equals(StatusConstant.PAY_STATUS_PAID)) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("支付成功")
                    .description("您已成功支付订单")
                    .time(orders.getCheckoutTime())
                    .completed(true)
                    .build());
        }

        // 商户接单节点
        if (orders.getStatus() >= StatusConstant.ORDER_CONFIRMED) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("商户已接单")
                    .description("商户已接受订单，正在准备中")
                    .time(orders.getUpdateTime())
                    .completed(true)
                    .build());
        } else if (orders.getStatus().equals(StatusConstant.ORDER_TO_BE_CONFIRMED)) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("等待商户接单")
                    .description("商户正在确认订单")
                    .time(null)
                    .completed(false)
                    .build());
        }

        // 配送中节点
        if (orders.getStatus() >= StatusConstant.ORDER_DELIVERY_IN_PROGRESS) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("配送中")
                    .description("订单正在配送中，请耐心等待")
                    .time(orders.getUpdateTime())
                    .completed(true)
                    .build());
        } else if (orders.getStatus().equals(StatusConstant.ORDER_CONFIRMED)) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("准备配送")
                    .description("商户正在准备配送")
                    .time(null)
                    .completed(false)
                    .build());
        }

        // 完成节点
        if (orders.getStatus().equals(StatusConstant.ORDER_COMPLETED)) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("订单完成")
                    .description("订单已送达，感谢您的支持")
                    .time(orders.getDeliveryTime())
                    .completed(true)
                    .build());
        } else if (orders.getStatus().equals(StatusConstant.ORDER_CANCELLED)) {
            trackNodes.add(OrderTrackVO.TrackNode.builder()
                    .title("订单已取消")
                    .description("订单已取消：" + (StringUtils.hasText(orders.getCancelReason())
                            ? orders.getCancelReason()
                            : (StringUtils.hasText(orders.getRejectionReason()) ? orders.getRejectionReason() : "未知原因")))
                    .time(orders.getCancelTime())
                    .completed(true)
                    .build());
        }

        return OrderTrackVO.builder()
                .orderId(orders.getId())
                .orderNumber(orders.getOrderNumber())
                .status(orders.getStatus())
                .statusText(getStatusDesc(orders.getStatus()))
                .estimatedDeliveryTime(orders.getEstimatedDeliveryTime())
                .trackNodes(trackNodes)
                .build();
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional
    public void cancelOrder(OrdersCancelDTO orderCancelDTO) {
        Orders orders = orderMapper.selectById(orderCancelDTO.getId());

        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证订单是否属于当前用户
        Long userId = BaseContext.getCurrentId();
        if (!orders.getUserId().equals(userId)) {
            throw new OrderBusinessException("该订单不属于当前用户");
        }

        // 校验订单状态（只有待付款、待接单、已接单状态可以取消）
        if (!orders.getStatus().equals(StatusConstant.ORDER_PENDING_PAYMENT) &&
                !orders.getStatus().equals(StatusConstant.ORDER_TO_BE_CONFIRMED) &&
                !orders.getStatus().equals(StatusConstant.ORDER_CONFIRMED)) {
            throw new OrderBusinessException("订单状态不允许取消");
        }

        // 检查支付状态，如果已支付需要退款
        if (StatusConstant.PAY_STATUS_PAID.equals(orders.getPayStatus())) {
            // TODO: 调用退款接口
            log.info("订单{}需要退款", orderCancelDTO.getId());
        }

        // 更新订单状态
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, orderCancelDTO.getId())
                .set(Orders::getStatus, StatusConstant.ORDER_CANCELLED)
                .set(Orders::getCancelReason, orderCancelDTO.getCancelReason())
                .set(Orders::getCancelTime, LocalDateTime.now());

        orderMapper.update(null, updateWrapper);

        log.info("用户{}取消订单成功，订单ID：{}", userId, orderCancelDTO.getId());
    }

    /**
     * 评价订单
     */
    @Override
    @Transactional
    public void reviewOrder(OrderReviewDTO orderReviewDTO) {
        Orders orders = orderMapper.selectById(orderReviewDTO.getOrderId());

        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证订单是否属于当前用户
        Long userId = BaseContext.getCurrentId();
        if (!orders.getUserId().equals(userId)) {
            throw new OrderBusinessException("该订单不属于当前用户");
        }

        // 校验订单状态（只有已完成的订单可以评价）
        if (!orders.getStatus().equals(StatusConstant.ORDER_COMPLETED)) {
            throw new OrderBusinessException("订单未完成，无法评价");
        }

        // 检查是否已评价
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getOrderId, orderReviewDTO.getOrderId());
        Long count = orderReviewMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new OrderBusinessException("订单已评价，不能重复评价");
        }

        OrderReview orderReview = OrderReview.builder()
                .orderId(orderReviewDTO.getOrderId())
                .userId(userId)
                .merchantId(orders.getMerchantId())
                .rating(orderReviewDTO.getRating())
                .content(orderReviewDTO.getContent())
                .images(org.springframework.util.StringUtils.hasText(orderReviewDTO.getImages())
                        ? orderReviewDTO.getImages()
                        : null)
                .build();

        orderReviewMapper.insert(orderReview);

        log.info("用户{}评价订单成功，订单ID：{}, 评分：{}", userId, orderReviewDTO.getOrderId(), orderReviewDTO.getRating());
    }

    /**
     * 删除订单
     */
    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Orders orders = orderMapper.selectById(id);

        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 验证订单是否属于当前用户
        Long userId = BaseContext.getCurrentId();
        if (!orders.getUserId().equals(userId)) {
            throw new OrderBusinessException("该订单不属于当前用户)");
        }
    }
}

