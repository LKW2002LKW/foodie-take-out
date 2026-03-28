package com.foodie.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.result.Result;
import com.foodie.dto.merchant.DishDTO;
import com.foodie.dto.merchant.DishPageQueryDTO;
import com.foodie.merchant.service.DishService;
import com.foodie.vo.merchant.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/merchant/dish")
@Api(tags = "菜品管理")
@Slf4j
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    /**
     * 新增菜品
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> addDish(@RequestBody DishDTO dishDTO,
                                  HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("新增菜品：merchantId={}, dishDTO={}", merchantId, dishDTO);

        dishService.addDish(merchantId, dishDTO);
        return Result.success(MessageConstant.DISH_ADD_SUCCESS);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result<String> updateDish(@RequestBody DishDTO dishDTO,
                                     HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改菜品：merchantId={}, dishDTO={}", merchantId, dishDTO);

        dishService.updateDish(merchantId, dishDTO);
        return Result.success(MessageConstant.DISH_UPDATE_SUCCESS);
    }

    /**
     * 删除菜品
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除菜品")
    public Result<String> deleteDish(@PathVariable Long id,
                                     HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("删除菜品：merchantId={}, dishId={}", merchantId, id);

        dishService.deleteDish(merchantId, id);
        return Result.success(MessageConstant.DISH_DELETE_SUCCESS);
    }

    /**
     * 批量删除菜品
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除菜品")
    public Result<String> batchDeleteDish(@RequestParam List<Long> ids,
                                          HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("批量删除菜品：merchantId={}, ids={}", merchantId, ids);

        dishService.batchDeleteDish(merchantId, ids);
        return Result.success(MessageConstant.DISH_DELETE_SUCCESS);
    }

    /**
     * 根据ID查询菜品详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品详情")
    public Result<DishVO> getDishDetail(@PathVariable Long id,
                                        HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("查询菜品详情：merchantId={}, dishId={}", merchantId, id);

        DishVO dishVO = dishService.getDishDetail(merchantId, id);
        return Result.success(dishVO);
    }

    /**
     * 分页查询菜品
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<Page<DishVO>> pageQuery(DishPageQueryDTO dishPageQueryDTO,
                                          HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("分页查询菜品：merchantId={}, query={}", merchantId, dishPageQueryDTO);

        Page<DishVO> page = dishService.pageQuery(merchantId, dishPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 起售/停售菜品
     */
    @PutMapping("/status/{id}/{status}")
    @ApiOperation("起售/停售菜品")
    public Result<String> updateStatus(@PathVariable Long id,
                                       @PathVariable Integer status,
                                       HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改菜品状态：merchantId={}, dishId={}, status={}", merchantId, id, status);

        dishService.updateStatus(merchantId, id, status);
        return Result.success(MessageConstant.DISH_STATUS_UPDATE_SUCCESS);
    }

    /**
     * 根据分类ID查询菜品列表
     */
    @GetMapping("/list/{categoryId}")
    @ApiOperation("根据分类ID查询菜品列表")
    public Result<List<DishVO>> listByCategoryId(@PathVariable Long categoryId,
                                                 HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("根据分类查询菜品：merchantId={}, categoryId={}", merchantId, categoryId);

        List<DishVO> list = dishService.listByCategoryId(merchantId, categoryId);
        return Result.success(list);
    }
}
