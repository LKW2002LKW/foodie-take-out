package com.foodie.user.controller;

import com.foodie.common.result.Result;
import com.foodie.user.service.DishService;
import com.foodie.vo.user.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品浏览（用户端）
 */
@RestController
@RequestMapping("/user/dish")
@Api(tags = "菜品浏览")
@Slf4j
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    /**
     * 根据商户ID和分类ID查询菜品
     */
    @GetMapping("/list")
    @ApiOperation("查询菜品列表")
    public Result<List<DishVO>> listByMerchantAndCategory(@RequestParam Long merchantId,
                                                          @RequestParam(required = false) Long categoryId) {
        log.info("查询菜品列表：merchantId={}, categoryId={}", merchantId, categoryId);

        List<DishVO> list = dishService.listByMerchantAndCategory(merchantId, categoryId);
        return Result.success(list);
    }

    /**
     * 查询菜品详情
     */
    @GetMapping("/{id}")
    @ApiOperation("查询菜品详情")
    public Result<DishVO> getDishDetail(@PathVariable Long id) {
        log.info("查询菜品详情：dishId={}", id);

        DishVO dishVO = dishService.getDishDetail(id);
        return Result.success(dishVO);
    }
}
