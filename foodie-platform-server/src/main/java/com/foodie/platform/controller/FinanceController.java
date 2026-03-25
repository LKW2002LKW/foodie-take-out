package com.foodie.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.platform.CommissionConfigDTO;
import com.foodie.dto.platform.SettlementAuditDTO;
import com.foodie.dto.platform.SettlementGenerateDTO;
import com.foodie.entity.PlatformCommission;
import com.foodie.platform.service.FinanceService;
import com.foodie.vo.platform.FinanceReportVO;
import com.foodie.vo.platform.SettlementVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 财务管理控制器
 */
@RestController
@RequestMapping("/platform/finance")
@Api(tags = "平台端-财务管理接口")
@Slf4j
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    /**
     * 结算单分页查询
     */
    @GetMapping("/settlement/page")
    @ApiOperation("结算单分页查询")
    public Result<Page<SettlementVO>> pageQuerySettlement(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String settlementCycle,
            @RequestParam(required = false) Integer status) {
        log.info("结算单分页查询");
        Page<SettlementVO> result = financeService.pageQuerySettlement(page, pageSize, merchantId, settlementCycle, status);
        return Result.success(result);
    }

    /**
     * 生成结算单
     */
    @PostMapping("/settlement/generate")
    @ApiOperation("生成结算单")
    public Result<Void> generateSettlement(@RequestBody @Validated SettlementGenerateDTO settlementGenerateDTO) {
        log.info("生成结算单：{}", settlementGenerateDTO);
        financeService.generateSettlement(settlementGenerateDTO);
        return Result.success();
    }

    /**
     * 结算审核
     */
    @PutMapping("/settlement/audit")
    @ApiOperation("结算审核")
    public Result<Void> auditSettlement(@RequestBody @Validated SettlementAuditDTO settlementAuditDTO) {
        log.info("结算审核：{}", settlementAuditDTO);
        financeService.auditSettlement(settlementAuditDTO);
        return Result.success();
    }

    /**
     * 抽成配置分页查询
     */
    @GetMapping("/commission/page")
    @ApiOperation("抽成配置分页查询")
    public Result<Page<PlatformCommission>> pageQueryCommission(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer configType) {
        log.info("抽成配置分页查询");
        Page<PlatformCommission> result = financeService.pageQueryCommission(page, pageSize, configType);
        return Result.success(result);
    }

    /**
     * 新增抽成配置
     */
    @PostMapping("/commission")
    @ApiOperation("新增抽成配置")
    public Result<Void> addCommission(@RequestBody @Validated CommissionConfigDTO commissionConfigDTO) {
        log.info("新增抽成配置：{}", commissionConfigDTO);
        financeService.addCommission(commissionConfigDTO);
        return Result.success();
    }

    /**
     * 修改抽成配置
     */
    @PutMapping("/commission/{id}")
    @ApiOperation("修改抽成配置")
    public Result<Void> updateCommission(@PathVariable Long id,
                                         @RequestBody @Validated CommissionConfigDTO commissionConfigDTO) {
        log.info("修改抽成配置，ID：{}，数据：{}", id, commissionConfigDTO);
        financeService.updateCommission(id, commissionConfigDTO);
        return Result.success();
    }

    /**
     * 删除抽成配置
     */
    @DeleteMapping("/commission/{id}")
    @ApiOperation("删除抽成配置")
    public Result<Void> deleteCommission(@PathVariable Long id) {
        log.info("删除抽成配置，ID：{}", id);
        financeService.deleteCommission(id);
        return Result.success();
    }

    /**
     * 财务报表
     */
    @GetMapping("/report")
    @ApiOperation("财务报表")
    public Result<FinanceReportVO> getFinanceReport() {
        log.info("获取财务报表");
        FinanceReportVO report = financeService.getFinanceReport();
        return Result.success(report);
    }
}