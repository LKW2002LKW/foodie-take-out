package com.foodie.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.result.Result;
import com.foodie.dto.user.MerchantQueryDTO;
import com.foodie.user.service.MerchantService;
import com.foodie.vo.user.CategoryVO;
import com.foodie.vo.user.MerchantVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户浏览（用户端）
 */
@RestController
@RequestMapping("/user/merchant")
@Api(tags = "商户浏览")
@Slf4j
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 分页查询商户列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询商户列表")
    public Result<Page<MerchantVO>> pageQuery(MerchantQueryDTO merchantQueryDTO) {
        log.info("查询商户列表：{}", merchantQueryDTO);

        Page<MerchantVO> page = merchantService.pageQuery(merchantQueryDTO);
        return Result.success(page);
    }

    /**
     * 查询商户详情
     */
    @GetMapping("/{id}")
    @ApiOperation("查询商户详情")
    public Result<MerchantVO> getMerchantDetail(@PathVariable Long id) {
        log.info("查询商户详情：merchantId={}", id);

        MerchantVO merchantVO = merchantService.getMerchantDetail(id);
        return Result.success(merchantVO);
    }

    /**
     * 查询商户的分类列表
     */
    @GetMapping("/{id}/categories")
    @ApiOperation("查询商户的分类列表")
    public Result<List<CategoryVO>> getMerchantCategories(@PathVariable Long id,
                                                          @RequestParam(required = false) Integer type) {
        log.info("查询商户分类：merchantId={}, type={}", id, type);

        List<CategoryVO> categories = merchantService.getMerchantCategories(id, type);
        return Result.success(categories);
    }
}