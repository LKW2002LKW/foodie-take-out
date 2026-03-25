package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.constant.MessageConstant;

import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;

import com.foodie.dto.platform.MerchantAuditDTO;
import com.foodie.dto.platform.MerchantPageQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.entity.Orders;
import com.foodie.platform.mapper.MerchantMapper;
import com.foodie.platform.mapper.OrderMapper;
import com.foodie.platform.service.MerchantService;
import com.foodie.vo.platform.MerchantDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 商户服务实现
 */
@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 商户分页查询
     */
    @Override
    public Page<Merchant> pageQuery(MerchantPageQueryDTO merchantPageQueryDTO) {
        Page<Merchant> page = new Page<>(merchantPageQueryDTO.getPage(), merchantPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(merchantPageQueryDTO.getMerchantName()),
                        Merchant::getMerchantName, merchantPageQueryDTO.getMerchantName())
                .like(StringUtils.hasText(merchantPageQueryDTO.getCityName()),
                        Merchant::getCityName, merchantPageQueryDTO.getCityName())
                .eq(merchantPageQueryDTO.getStatus() != null,
                        Merchant::getStatus, merchantPageQueryDTO.getStatus())
                .eq(merchantPageQueryDTO.getAuditStatus() != null,
                        Merchant::getAuditStatus, merchantPageQueryDTO.getAuditStatus())
                .orderByDesc(Merchant::getCreateTime);

        return merchantMapper.selectPage(page, queryWrapper);
    }

    /**
     * 商户详情
     */
    @Override
    public MerchantDetailVO getDetail(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        MerchantDetailVO detailVO = new MerchantDetailVO();
        BeanUtils.copyProperties(merchant, detailVO);

        // 设置状态文本
        detailVO.setStatusText(getStatusText(merchant.getStatus()));
        detailVO.setAuditStatusText(getAuditStatusText(merchant.getAuditStatus()));

        // 统计商户订单数据
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Orders::getMerchantId, id);
        Integer totalOrderCount = Math.toIntExact(orderMapper.selectCount(orderWrapper));

        LambdaQueryWrapper<Orders> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Orders::getMerchantId, id)
                .eq(Orders::getStatus, StatusConstant.ORDER_COMPLETED);
        Integer completedOrderCount = Math.toIntExact(orderMapper.selectCount(completedWrapper));

        // 统计营收
        LambdaQueryWrapper<Orders> revenueWrapper = new LambdaQueryWrapper<>();
        revenueWrapper.eq(Orders::getMerchantId, id)
                .eq(Orders::getStatus, StatusConstant.ORDER_COMPLETED)
                .eq(Orders::getPayStatus, StatusConstant.PAY_STATUS_PAID);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal platformIncome = BigDecimal.ZERO;

        orderMapper.selectList(revenueWrapper).forEach(order -> {
        });

        detailVO.setTotalOrderCount(totalOrderCount);
        detailVO.setCompletedOrderCount(completedOrderCount);
        detailVO.setTotalRevenue(totalRevenue);
        detailVO.setPlatformIncome(platformIncome);

        return detailVO;
    }

    /**
     * 商户审核
     */
    @Override
    @Transactional
    public void audit(MerchantAuditDTO merchantAuditDTO) {
        Merchant merchant = merchantMapper.selectById(merchantAuditDTO.getId());
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        // 只有审核中的商户可以审核
        if (!StatusConstant.AUDIT_PENDING.equals(merchant.getAuditStatus())) {
            throw new BaseException("商户审核状态错误");
        }

        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId, merchantAuditDTO.getId())
                .set(Merchant::getAuditStatus, merchantAuditDTO.getAuditStatus())
                .set(Merchant::getAuditReason, merchantAuditDTO.getAuditReason());

        // 如果审核通过，设置商户状态为营业中
        if (StatusConstant.AUDIT_PASSED.equals(merchantAuditDTO.getAuditStatus())) {
            updateWrapper.set(Merchant::getStatus, StatusConstant.MERCHANT_OPEN);
        }

        merchantMapper.update(null, updateWrapper);

        log.info("商户审核成功，商户ID：{}，审核状态：{}", merchantAuditDTO.getId(), merchantAuditDTO.getAuditStatus());
    }

    /**
     * 启用商户
     */
    @Override
    @Transactional
    public void enable(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        // 必须审核通过才能启用
        if (!StatusConstant.AUDIT_PASSED.equals(merchant.getAuditStatus())) {
            throw new BaseException("商户未通过审核，无法启用");
        }

        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId, id)
                .set(Merchant::getStatus, StatusConstant.MERCHANT_OPEN);

        merchantMapper.update(null, updateWrapper);
        log.info("启用商户成功，商户ID：{}", id);
    }

    /**
     * 禁用商户
     */
    @Override
    @Transactional
    public void disable(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BaseException(MessageConstant.MERCHANT_NOT_FOUND);
        }

        LambdaUpdateWrapper<Merchant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Merchant::getId, id)
                .set(Merchant::getStatus, StatusConstant.MERCHANT_CLOSED);

        merchantMapper.update(null, updateWrapper);
        log.info("禁用商户成功，商户ID：{}", id);
    }

    /**
     * 获取商户状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "营业中";
            case 2: return "休息中";
            case 3: return "已关闭";
            default: return "未知";
        }
    }

    /**
     * 获取审核状态文本
     */
    private String getAuditStatusText(Integer auditStatus) {
        if (auditStatus == null) return "未知";
        switch (auditStatus) {
            case 1: return "已通过";
            case 2: return "审核中";
            case 3: return "已拒绝";
            default: return "未知";
        }
    }
}