package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.constant.StatusConstant;
import com.foodie.common.context.BaseContext;
import com.foodie.dto.merchant.IncomeQueryDTO;
import com.foodie.dto.merchant.SettlementQueryDTO;
import com.foodie.entity.MerchantSettlement;
import com.foodie.entity.Orders;
import com.foodie.merchant.mapper.MerchantSettlementMapper;

import com.foodie.merchant.mapper.OrdersMapper;
import com.foodie.merchant.service.MerchantFinanceService;

import com.foodie.vo.merchant.IncomeDetailVO;
import com.foodie.vo.merchant.SettlementRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家财务服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantFinanceServiceImpl implements MerchantFinanceService {

    private final MerchantSettlementMapper settlementMapper;

    private final OrdersMapper orderMapper;



    /**
     * 结算记录分页查询
     */
    @Override
    public Page<SettlementRecordVO> pageQuerySettlement(SettlementQueryDTO settlementQueryDTO) {
        Long merchantId = BaseContext.getCurrentId();

        Page<MerchantSettlement> page = new Page<>(settlementQueryDTO.getPage(), settlementQueryDTO.getPageSize());

        LambdaQueryWrapper<MerchantSettlement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantSettlement::getMerchantId, merchantId)
                .eq(StringUtils.hasText(settlementQueryDTO.getSettlementCycle()),
                        MerchantSettlement::getSettlementCycle, settlementQueryDTO.getSettlementCycle())
                .eq(settlementQueryDTO.getStatus() != null,
                        MerchantSettlement::getStatus, settlementQueryDTO.getStatus())
                .orderByDesc(MerchantSettlement::getCreateTime);

        Page<MerchantSettlement> settlementPage = settlementMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<SettlementRecordVO> result = new Page<>();
        result.setCurrent(settlementPage.getCurrent());
        result.setSize(settlementPage.getSize());
        result.setTotal(settlementPage.getTotal());
        result.setPages(settlementPage.getPages());

        List<SettlementRecordVO> voList = settlementPage.getRecords().stream().map(settlement -> {
            SettlementRecordVO vo = new SettlementRecordVO();
            BeanUtils.copyProperties(settlement, vo);
            vo.setStatusText(getSettlementStatusText(settlement.getStatus()));
            return vo;
        }).collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }

    /**
     * 收入明细分页查询
     */
    @Override
    public Page<IncomeDetailVO> pageQueryIncome(IncomeQueryDTO incomeQueryDTO) {
        Long merchantId = BaseContext.getCurrentId();

        Page<Orders> page = new Page<>(incomeQueryDTO.getPage(), incomeQueryDTO.getPageSize());

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getMerchantId, merchantId)
                .eq(Orders::getStatus, StatusConstant.ORDER_COMPLETED)  // 只查询已完成订单
                .like(StringUtils.hasText(incomeQueryDTO.getOrderNumber()),
                        Orders::getOrderNumber, incomeQueryDTO.getOrderNumber())
                .orderByDesc(Orders::getOrderTime);

        // 时间范围查询
        if (incomeQueryDTO.getBeginDate() != null) {
            LocalDateTime beginTime = LocalDateTime.of(incomeQueryDTO.getBeginDate(), LocalTime.MIN);
            queryWrapper.ge(Orders::getOrderTime, beginTime);
        }
        if (incomeQueryDTO.getEndDate() != null) {
            LocalDateTime endTime = LocalDateTime.of(incomeQueryDTO.getEndDate(), LocalTime.MAX);
            queryWrapper.le(Orders::getOrderTime, endTime);
        }

        Page<Orders> orderPage = orderMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<IncomeDetailVO> result = new Page<>();
        result.setCurrent(orderPage.getCurrent());
        result.setSize(orderPage.getSize());
        result.setTotal(orderPage.getTotal());
        result.setPages(orderPage.getPages());

        List<IncomeDetailVO> voList = orderPage.getRecords().stream().map(order ->
                IncomeDetailVO.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .orderTime(order.getOrderTime())
                        .totalAmount(order.getTotalAmount())
                        .platformCommission(order.getPlatformCommission())
                        .merchantAmount(order.getMerchantAmount())
                        .payStatus(order.getPayStatus())
                        .payStatusText(getPayStatusText(order.getPayStatus()))
                        .build()
        ).collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }


    /**
     * 获取结算状态文本
     */
    private String getSettlementStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待结算";
            case 2: return "已结算";
            case 3: return "已取消";
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

    /**
     * 获取账户类型文本
     */
    private String getAccountTypeText(Integer accountType) {
        if (accountType == null) return "未知";
        switch (accountType) {
            case 1: return "银行卡";
            case 2: return "支付宝";
            case 3: return "微信";
            default: return "未知";
        }
    }

    /**
     * 获取提现状态文本
     */
    private String getWithdrawalStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待审核";
            case 2: return "已通过";
            case 3: return "已拒绝";
            case 4: return "已完成";
            default: return "未知";
        }
    }


}