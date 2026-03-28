package com.foodie.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.result.Result;
import com.foodie.dto.merchant.SetmealDTO;
import com.foodie.dto.merchant.SetmealPageQueryDTO;
import com.foodie.merchant.service.SetmealService;
import com.foodie.vo.merchant.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/merchant/setmeal")
@Api(tags = "套餐管理")
@Slf4j
@RequiredArgsConstructor
public class SetmealController {

    private final SetmealService setmealService;

    /**
     * 新增套餐
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result<String> addSetmeal(@RequestBody SetmealDTO setmealDTO,
                                     HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("新增套餐：merchantId={}, setmealDTO={}", merchantId, setmealDTO);

        setmealService.addSetmeal(merchantId, setmealDTO);
        return Result.success(MessageConstant.SETMEAL_ADD_SUCCESS);
    }

    /**
     * 修改套餐
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result<String> updateSetmeal(@RequestBody SetmealDTO setmealDTO,
                                        HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改套餐：merchantId={}, setmealDTO={}", merchantId, setmealDTO);

        setmealService.updateSetmeal(merchantId, setmealDTO);
        return Result.success(MessageConstant.SETMEAL_UPDATE_SUCCESS);
    }

    /**
     * 删除套餐
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除套餐")
    public Result<String> deleteSetmeal(@PathVariable Long id,
                                        HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("删除套餐：merchantId={}, setmealId={}", merchantId, id);

        setmealService.deleteSetmeal(merchantId, id);
        return Result.success(MessageConstant.SETMEAL_DELETE_SUCCESS);
    }

    /**
     * 批量删除套餐
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除套餐")
    public Result<String> batchDeleteSetmeal(@RequestParam List<Long> ids,
                                             HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("批量删除套餐：merchantId={}, ids={}", merchantId, ids);

        setmealService.batchDeleteSetmeal(merchantId, ids);
        return Result.success(MessageConstant.SETMEAL_DELETE_SUCCESS);
    }

    /**
     * 根据ID查询套餐详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐详情")
    public Result<SetmealVO> getSetmealDetail(@PathVariable Long id,
                                              HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("查询套餐详情：merchantId={}, setmealId={}", merchantId, id);

        SetmealVO setmealVO = setmealService.getSetmealDetail(merchantId, id);
        return Result.success(setmealVO);
    }

    /**
     * 分页查询套餐
     */
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<Page<SetmealVO>> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO,
                                             HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("分页查询套餐：merchantId={}, query={}", merchantId, setmealPageQueryDTO);

        Page<SetmealVO> page = setmealService.pageQuery(merchantId, setmealPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 起售/停售套餐
     */
    @PutMapping("/status/{id}/{status}")
    @ApiOperation("起售/停售套餐")
    public Result<String> updateStatus(@PathVariable Long id,
                                       @PathVariable Integer status,
                                       HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改套餐状态：merchantId={}, setmealId={}, status={}", merchantId, id, status);

        setmealService.updateStatus(merchantId, id, status);
        return Result.success(MessageConstant.SETMEAL_STATUS_UPDATE_SUCCESS);
    }

    /**
     * 根据分类ID查询套餐列表
     */
    @GetMapping("/list/{categoryId}")
    @ApiOperation("根据分类ID查询套餐列表")
    public Result<List<SetmealVO>> listByCategoryId(@PathVariable Long categoryId,
                                                    HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("根据分类查询套餐：merchantId={}, categoryId={}", merchantId, categoryId);

        List<SetmealVO> list = setmealService.listByCategoryId(merchantId, categoryId);
        return Result.success(list);
    }
}