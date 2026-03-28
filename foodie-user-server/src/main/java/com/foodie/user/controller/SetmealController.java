package com.foodie.user.controller;

import com.foodie.common.result.Result;
import com.foodie.user.service.SetmealService;
import com.foodie.vo.user.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐浏览（用户端）
 */
@RestController
@RequestMapping("/user/setmeal")
@Api(tags = "套餐浏览")
@Slf4j
@RequiredArgsConstructor
public class SetmealController {

    private final SetmealService setmealService;

    /**
     * 根据商户ID和分类ID查询套餐
     */
    @GetMapping("/list")
    @ApiOperation("查询套餐列表")
    public Result<List<SetmealVO>> listByMerchantAndCategory(@RequestParam Long merchantId,
                                                             @RequestParam(required = false) Long categoryId) {
        log.info("查询套餐列表：merchantId={}, categoryId={}", merchantId, categoryId);

        List<SetmealVO> list = setmealService.listByMerchantAndCategory(merchantId, categoryId);
        return Result.success(list);
    }

    /**
     * 查询套餐详情
     */
    @GetMapping("/{id}")
    @ApiOperation("查询套餐详情")
    public Result<SetmealVO> getSetmealDetail(@PathVariable Long id) {
        log.info("查询套餐详情：setmealId={}", id);
        SetmealVO setmealVO = setmealService.getSetmealDetail(id);
        return Result.success(setmealVO);

    }


}