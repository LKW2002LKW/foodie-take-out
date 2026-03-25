package com.foodie.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.result.Result;
import com.foodie.dto.platform.ReviewPageQueryDTO;
import com.foodie.platform.service.ReviewService;
import com.foodie.vo.platform.ReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评价管理控制器
 */
@RestController
@RequestMapping("/platform/review")
@Api(tags = "平台端-评价管理接口")
@Slf4j
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 评价分页查询
     */
    @GetMapping("/page")
    @ApiOperation("评价分页查询")
    public Result<Page<ReviewVO>> page(ReviewPageQueryDTO reviewPageQueryDTO) {
        log.info("评价分页查询：{}", reviewPageQueryDTO);
        Page<ReviewVO> page = reviewService.pageQuery(reviewPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 差评预警列表
     */
    @GetMapping("/warning")
    @ApiOperation("差评预警列表")
    public Result<Page<ReviewVO>> getBadReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询差评预警列表");
        Page<ReviewVO> result = reviewService.getBadReviews(page, pageSize);
        return Result.success(result);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除评价")
    public Result<Void> deleteReview(@PathVariable Long id) {
        log.info("删除评价：{}", id);
        reviewService.deleteReview(id);
        return Result.success();
    }

    /**
     * 隐藏评价
     */
    @PutMapping("/hide/{id}")
    @ApiOperation("隐藏评价")
    public Result<Void> hideReview(@PathVariable Long id) {
        log.info("隐藏评价：{}", id);
        reviewService.hideReview(id);
        return Result.success();
    }
}