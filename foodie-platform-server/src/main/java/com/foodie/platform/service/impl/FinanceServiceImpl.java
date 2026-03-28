package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.platform.CommissionConfigDTO;
import com.foodie.dto.platform.SettlementAuditDTO;
import com.foodie.dto.platform.SettlementGenerateDTO;
import com.foodie.entity.Merchant;
import com.foodie.entity.MerchantSettlement;
import com.foodie.entity.Orders;
import com.foodie.entity.PlatformCommission;
import com.foodie.platform.mapper.*;
import com.foodie.platform.service.FinanceService;
import com.foodie.vo.platform.FinanceReportVO;
import com.foodie.vo.platform.SettlementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final MerchantSettlementMapper settlementMapper;

    private final PlatformCommissionMapper commissionMapper;

    private final MerchantMapper merchantMapper;

    private final OrderMapper orderMapper;

    /**
     * 结算单分页查询
     */
    @Override
    public Page<SettlementVO> pageQuerySettlement(Integer page, Integer pageSize,
                                                  Long merchantId, String settlementCycle, Integer status) {
        Page<MerchantSettlement> settlementPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<MerchantSettlement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(merchantId != null, MerchantSettlement::getMerchantId, merchantId)
                .eq(StringUtils.hasText(settlementCycle), MerchantSettlement::getSettlementCycle, settlementCycle)
                .eq(status != null, MerchantSettlement::getStatus, status)
                .orderByDesc(MerchantSettlement::getCreateTime);

        settlementPage = settlementMapper.selectPage(settlementPage, queryWrapper);

        // 转换为VO
        Page<SettlementVO> result = new Page<>();
        result.setCurrent(settlementPage.getCurrent());
        result.setSize(settlementPage.getSize());
        result.setTotal(settlementPage.getTotal());
        result.setPages(settlementPage.getPages());

        List<SettlementVO> voList = settlementPage.getRecords().stream().map(settlement -> {
            SettlementVO vo = new SettlementVO();
            BeanUtils.copyProperties(settlement, vo);

            // 查询商户名称
            Merchant merchant = merchantMapper.selectById(settlement.getMerchantId());
            vo.setMerchantName(merchant != null ? merchant.getMerchantName() : null);
            vo.setStatusText(getSettlementStatusText(settlement.getStatus()));

            return vo;
        }).collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }

    /**
     * 生成结算单
     */
    @Override
    @Transactional
    public void generateSettlement(SettlementGenerateDTO settlementGenerateDTO) {
        String cycle = settlementGenerateDTO.getSettlementCycle();

        // 解析周期
        YearMonth yearMonth = YearMonth.parse(cycle, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime beginTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 获取商户列表
        List<Merchant> merchants;
        if (settlementGenerateDTO.getMerchantIds() != null && !settlementGenerateDTO.getMerchantIds().isEmpty()) {
            merchants = merchantMapper.selectBatchIds(settlementGenerateDTO.getMerchantIds());
        } else {
            // 查询所有营业中的商户
            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Merchant::getStatus, StatusConstant.MERCHANT_OPEN);
            merchants = merchantMapper.selectList(queryWrapper);
        }

        // 为每个商户生成结算单
        for (Merchant merchant : merchants) {
            // 检查是否已生成
            LambdaQueryWrapper<MerchantSettlement> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(MerchantSettlement::getMerchantId, merchant.getId())
                    .eq(MerchantSettlement::getSettlementCycle, cycle);
            Long count = settlementMapper.selectCount(checkWrapper);
            if (count > 0) {
                log.info("商户{}的{}结算单已存在，跳过", merchant.getMerchantName(), cycle);
                continue;
            }

            // 统计该商户的订单数据
            LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Orders::getMerchantId, merchant.getId())
                    .eq(Orders::getStatus, StatusConstant.ORDER_COMPLETED)
                    .eq(Orders::getPayStatus, StatusConstant.PAY_STATUS_PAID)
                    .between(Orders::getOrderTime, beginTime, endTime);

            List<Orders> orders = orderMapper.selectList(orderWrapper);

            if (orders.isEmpty()) {
                log.info("商户{}在{}无完成订单，跳过", merchant.getMerchantName(), cycle);
                continue;
            }

            // 计算金额
            Integer orderCount = orders.size();
            BigDecimal totalAmount = orders.stream()
                    .map(Orders::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal platformCommission = orders.stream()
                    .map(Orders::getPlatformCommission)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal merchantAmount = orders.stream()
                    .map(Orders::getMerchantAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建结算单
            MerchantSettlement settlement = MerchantSettlement.builder()
                    .merchantId(merchant.getId())
                    .settlementCycle(cycle)
                    .orderCount(orderCount)
                    .totalAmount(totalAmount)
                    .platformCommission(platformCommission)
                    .merchantAmount(merchantAmount)
                    .settlementAmount(merchantAmount)  // 默认实际结算金额=商户应得
                    .status(StatusConstant.STATUS_PENDING)
                    .build();

            settlementMapper.insert(settlement);
            log.info("已为商户{}生成{}结算单", merchant.getMerchantName(), cycle);
        }
    }

    /**
     * 结算审核
     */
    @Override
    @Transactional
    public void auditSettlement(SettlementAuditDTO settlementAuditDTO) {
        MerchantSettlement settlement = settlementMapper.selectById(settlementAuditDTO.getId());
        if (settlement == null) {
            throw new BaseException("结算单不存在");
        }

        if (!StatusConstant.STATUS_PENDING.equals(settlement.getStatus())) {
            throw new BaseException("结算单状态错误");
        }

        LambdaUpdateWrapper<MerchantSettlement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantSettlement::getId, settlementAuditDTO.getId())
                .set(MerchantSettlement::getStatus, StatusConstant.STATUS_SETTLED)
                .set(MerchantSettlement::getSettlementTime, LocalDateTime.now())
                .set(settlementAuditDTO.getSettlementAmount() != null,
                        MerchantSettlement::getSettlementAmount, settlementAuditDTO.getSettlementAmount())
                .set(StringUtils.hasText(settlementAuditDTO.getRemark()),
                        MerchantSettlement::getRemark, settlementAuditDTO.getRemark());

        settlementMapper.update(null, updateWrapper);
        log.info("结算单审核成功，ID：{}", settlementAuditDTO.getId());
    }

    /**
     * 抽成配置分页查询
     */
    @Override
    public Page<PlatformCommission> pageQueryCommission(Integer page, Integer pageSize, Integer configType) {
        Page<PlatformCommission> commissionPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<PlatformCommission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(configType != null, PlatformCommission::getConfigType, configType)
                .orderByDesc(PlatformCommission::getCreateTime);

        return commissionMapper.selectPage(commissionPage, queryWrapper);
    }

    /**
     * 新增抽成配置
     */
    @Override
    @Transactional
    public void addCommission(CommissionConfigDTO commissionConfigDTO) {
        PlatformCommission commission = new PlatformCommission();
        BeanUtils.copyProperties(commissionConfigDTO, commission);
        commission.setStatus(1);

        if (commissionConfigDTO.getEffectiveTime() == null) {
            commission.setEffectiveTime(LocalDateTime.now());
        }

        commissionMapper.insert(commission);
        log.info("新增抽成配置成功");
    }

    /**
     * 修改抽成配置
     */
    @Override
    @Transactional
    public void updateCommission(Long id, CommissionConfigDTO commissionConfigDTO) {
        PlatformCommission commission = commissionMapper.selectById(id);
        if (commission == null) {
            throw new BaseException("抽成配置不存在");
        }

        BeanUtils.copyProperties(commissionConfigDTO, commission);
        commissionMapper.updateById(commission);
        log.info("修改抽成配置成功，ID：{}", id);
    }

    /**
     * 删除抽成配置
     */
    @Override
    @Transactional
    public void deleteCommission(Long id) {
        commissionMapper.deleteById(id);
        log.info("删除抽成配置成功，ID：{}", id);
    }

    /**
     * 财务报表
     */
    @Override
    public FinanceReportVO getFinanceReport() {
        // 统计总订单数
        LambdaQueryWrapper<Orders> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Orders::getPayStatus, StatusConstant.PAY_STATUS_PAID);
        Integer totalOrderCount = Math.toIntExact(orderMapper.selectCount(totalWrapper));

        // 统计完成订单数
        LambdaQueryWrapper<Orders> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Orders::getStatus, StatusConstant.ORDER_COMPLETED)
                .eq(Orders::getPayStatus, StatusConstant.PAY_STATUS_PAID);
        Integer completedOrderCount = Math.toIntExact(orderMapper.selectCount(completedWrapper));

        // 统计总交易额和平台收入
        List<Orders> completedOrders = orderMapper.selectList(completedWrapper);
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPlatformIncome = completedOrders.stream()
                .map(Orders::getPlatformCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 统计已结算金额
        LambdaQueryWrapper<MerchantSettlement> settledWrapper = new LambdaQueryWrapper<>();
        settledWrapper.eq(MerchantSettlement::getStatus, StatusConstant.STATUS_SETTLED);
        List<MerchantSettlement> settledList = settlementMapper.selectList(settledWrapper);
        BigDecimal settledAmount = settledList.stream()
                .map(MerchantSettlement::getSettlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 统计待结算金额
        LambdaQueryWrapper<MerchantSettlement> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(MerchantSettlement::getStatus, StatusConstant.STATUS_PENDING);
        List<MerchantSettlement> pendingList = settlementMapper.selectList(pendingWrapper);
        BigDecimal pendingSettlementAmount = pendingList.stream()
                .map(MerchantSettlement::getSettlementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinanceReportVO.builder()
                .totalOrderCount(totalOrderCount)
                .completedOrderCount(completedOrderCount)
                .totalRevenue(totalRevenue)
                .totalPlatformIncome(totalPlatformIncome)
                .settledAmount(settledAmount)
                .pendingSettlementAmount(pendingSettlementAmount)
                .build();
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
}