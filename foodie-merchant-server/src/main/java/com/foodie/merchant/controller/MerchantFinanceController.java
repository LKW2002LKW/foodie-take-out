package com.foodie.merchant.controller;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:48
 */
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.merchant.IncomeQueryDTO;
import com.foodie.dto.merchant.SettlementQueryDTO;
import com.foodie.merchant.service.MerchantFinanceService;

import com.foodie.vo.merchant.IncomeDetailVO;
import com.foodie.vo.merchant.SettlementRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商家财务管理控制器
 */
@RestController
@RequestMapping("/merchant/finance")
@Api(tags = "商家端-财务管理接口")
@Slf4j
@RequiredArgsConstructor
public class MerchantFinanceController {

    private final MerchantFinanceService financeService;

    /**
     * 结算记录分页查询
     */
    @GetMapping("/settlement/page")
    @ApiOperation("结算记录分页查询")
    public Result<Page<SettlementRecordVO>> pageQuerySettlement(SettlementQueryDTO settlementQueryDTO) {
        log.info("查询结算记录：{}", settlementQueryDTO);
        Page<SettlementRecordVO> page = financeService.pageQuerySettlement(settlementQueryDTO);
        return Result.success(page);
    }

    /**
     * 收入明细分页查询
     */
    @GetMapping("/income/page")
    @ApiOperation("收入明细分页查询")
    public Result<Page<IncomeDetailVO>> pageQueryIncome(IncomeQueryDTO incomeQueryDTO) {
        log.info("查询收入明细：{}", incomeQueryDTO);
        Page<IncomeDetailVO> page = financeService.pageQueryIncome(incomeQueryDTO);
        return Result.success(page);
    }



}