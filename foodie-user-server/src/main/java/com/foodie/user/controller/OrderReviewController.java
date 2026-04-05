package com.foodie.user.controller;

import com.foodie.common.result.Result;
import com.foodie.dto.user.ReviewQueryDTO;
import com.foodie.user.service.OrderReviewService;
import com.foodie.vo.user.OrderReviewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 订单评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/review")
@Api(tags = "用户端-订单评价")
@RequiredArgsConstructor
public class OrderReviewController {

    private final OrderReviewService orderReviewService;
    /**
     * 分页查询商户评价列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询商户评价列表")
    public Result<Map<String, Object>> getReviewPage(
            @ApiParam("商户ID") @RequestParam Long merchantId,
            @ApiParam("评分筛选(0-全部 5-好评(4-5星) 3-中评(3星) 1-差评(1-2星))") @RequestParam(required = false) Integer ratingFilter,
            @ApiParam("是否只看有图") @RequestParam(required = false, defaultValue = "false") Boolean hasImage,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("查询商户评价列表, merchantId={}, ratingFilter={}, hasImage={}, page={}, pageSize={}",
                merchantId, ratingFilter, hasImage, page, pageSize);

        // 构建查询参数
        ReviewQueryDTO query = new ReviewQueryDTO();
        query.setMerchantId(merchantId);
        query.setRatingFilter(ratingFilter);
        query.setHasImage(hasImage);
        query.setPage(page);
        query.setPageSize(pageSize);

        // 查询数据
        Map<String, Object> result = orderReviewService.getReviewPage(query);

        return Result.success(result);
    }

    /**
     * 获取商户评价统计信息
     */
    @GetMapping("/stats/{merchantId}")
    @ApiOperation("获取商户评价统计")
    public Result<Map<String, Object>> getReviewStats(
            @ApiParam("商户ID") @PathVariable Long merchantId) {

        log.info("查询商户评价统计, merchantId={}", merchantId);

        Map<String, Object> stats = orderReviewService.getReviewStats(merchantId);

        return Result.success(stats);
    }

    /**
     * 查询评价详情
     */
    @GetMapping("/{reviewId}")
    @ApiOperation("查询评价详情")
    public Result<OrderReviewVO> getReviewById(
            @ApiParam("评价ID") @PathVariable Long reviewId) {

        log.info("查询评价详情, reviewId={}", reviewId);

        OrderReviewVO review = orderReviewService.getReviewById(reviewId);

        return Result.success(review);
    }

    /**
     * 查询用户自己的评价
     */
    @GetMapping("/my")
    @ApiOperation("查询我的评价列表")
    public Result<Map<String, Object>> getMyReviews(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        log.info("查询用户评价列表, userId={}, page={}, pageSize={}", userId, page, pageSize);

        Map<String, Object> result = orderReviewService.getMyReviewPage(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 删除用户自己的评价
     */
    @DeleteMapping("/{reviewId}")
    @ApiOperation("删除我的评价")
    public Result<Void> deleteMyReview(
            @ApiParam("评价ID") @PathVariable Long reviewId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        log.info("删除用户评价, userId={}, reviewId={}", userId, reviewId);

        orderReviewService.deleteMyReview(userId, reviewId);
        return Result.success();
    }

}