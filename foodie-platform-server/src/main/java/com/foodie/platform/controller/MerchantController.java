package com.foodie.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.platform.MerchantAuditDTO;
import com.foodie.dto.platform.MerchantPageQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.platform.service.MerchantService;
import com.foodie.vo.platform.MerchantDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商户管理控制器
 */
@RestController
@RequestMapping("/platform/merchant")
@Api(tags = "平台端-商户管理接口")
@Slf4j
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 商户分页查询
     */
    @GetMapping("/page")
    @ApiOperation("商户分页查询")
    public Result<Page<Merchant>> page(MerchantPageQueryDTO merchantPageQueryDTO) {
        log.info("商户分页查询：{}", merchantPageQueryDTO);
        Page<Merchant> page = merchantService.pageQuery(merchantPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 商户详情
     */
    @GetMapping("/{id}")
    @ApiOperation("商户详情")
    public Result<MerchantDetailVO> getDetail(@PathVariable Long id) {
        log.info("查询商户详情：{}", id);
        MerchantDetailVO detailVO = merchantService.getDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 商户审核
     */
    @PutMapping("/audit")
    @ApiOperation("商户审核")
    public Result<Void> audit(@RequestBody @Validated MerchantAuditDTO merchantAuditDTO) {
        log.info("商户审核：{}", merchantAuditDTO);
        merchantService.audit(merchantAuditDTO);
        return Result.success();
    }

    /**
     * 启用商户
     */
    @PutMapping("/enable/{id}")
    @ApiOperation("启用商户")
    public Result<Void> enable(@PathVariable Long id) {
        log.info("启用商户：{}", id);
        merchantService.enable(id);
        return Result.success();
    }

    /**
     * 禁用商户
     */
    @PutMapping("/disable/{id}")
    @ApiOperation("禁用商户")
    public Result<Void> disable(@PathVariable Long id) {
        log.info("禁用商户：{}", id);
        merchantService.disable(id);
        return Result.success();
    }
}