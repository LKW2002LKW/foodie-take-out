package com.foodie.merchant.controller;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:50
 */
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.merchant.ReviewQueryDTO;
import com.foodie.dto.merchant.ReviewReplyDTO;
import com.foodie.merchant.service.MerchantReviewService;

import com.foodie.vo.merchant.MerchantReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商家评价管理控制器
 */
@RestController
@RequestMapping("/merchant/review")
@Api(tags = "商家端-评价管理接口")
@Slf4j
public class MerchantReviewController {

    @Autowired
    private MerchantReviewService reviewService;

    /**
     * 评价分页查询
     */
    @GetMapping("/page")
    @ApiOperation("评价分页查询")
    public Result<Page<MerchantReviewVO>> page(ReviewQueryDTO reviewQueryDTO) {
        log.info("查询评价列表：{}", reviewQueryDTO);
        Page<MerchantReviewVO> page = reviewService.pageQuery(reviewQueryDTO);
        return Result.success(page);
    }

    /**
     * 商户回复评价
     */
    @PutMapping("/reply")
    @ApiOperation("商户回复评价")
    public Result<Void> replyReview(@RequestBody @Validated ReviewReplyDTO reviewReplyDTO) {
        log.info("商户回复评价：{}", reviewReplyDTO);
        reviewService.replyReview(reviewReplyDTO);
        return Result.success();
    }

    /**
     * 差评列表
     */
    @GetMapping("/bad-reviews")
    @ApiOperation("差评列表")
    public Result<Page<MerchantReviewVO>> getBadReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询差评列表");
        Page<MerchantReviewVO> result = reviewService.getBadReviews(page, pageSize);
        return Result.success(result);
    }
}