package com.foodie.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.result.Result;
import com.foodie.dto.merchant.CategoryDTO;
import com.foodie.dto.merchant.CategoryPageQueryDTO;
import com.foodie.merchant.service.CategoryService;
import com.foodie.vo.merchant.CategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/merchant/category")
@Api(tags = "分类管理")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> addCategory(@RequestBody CategoryDTO categoryDTO,
                                      HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("新增分类：merchantId={}, categoryDTO={}", merchantId, categoryDTO);

        categoryService.addCategory(merchantId, categoryDTO);
        return Result.success(MessageConstant.CATEGORY_ADD_SUCCESS);
    }

    /**
     * 修改分类
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> updateCategory(@RequestBody CategoryDTO categoryDTO,
                                         HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改分类：merchantId={}, categoryDTO={}", merchantId, categoryDTO);

        categoryService.updateCategory(merchantId, categoryDTO);
        return Result.success(MessageConstant.CATEGORY_UPDATE_SUCCESS);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public Result<String> deleteCategory(@PathVariable Long id,
                                         HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("删除分类：merchantId={}, categoryId={}", merchantId, id);

        categoryService.deleteCategory(merchantId, id);
        return Result.success(MessageConstant.CATEGORY_DELETE_SUCCESS);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询分类")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id,
                                              HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("查询分类：merchantId={}, categoryId={}", merchantId, id);

        CategoryVO categoryVO = categoryService.getCategoryById(merchantId, id);
        return Result.success(categoryVO);
    }

    /**
     * 分页查询分类
     */
    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result<Page<CategoryVO>> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO,
                                              HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("分页查询分类：merchantId={}, query={}", merchantId, categoryPageQueryDTO);

        Page<CategoryVO> page = categoryService.pageQuery(merchantId, categoryPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据类型查询分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类列表")
    public Result<List<CategoryVO>> listByType(@RequestParam(required = false) Integer type,
                                               HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("查询分类列表：merchantId={}, type={}", merchantId, type);

        List<CategoryVO> list = categoryService.listByType(merchantId, type);
        return Result.success(list);
    }

    /**
     * 启用/禁用分类
     */
    @PutMapping("/status/{id}/{status}")
    @ApiOperation("启用/禁用分类")
    public Result<String> updateStatus(@PathVariable Long id,
                                       @PathVariable Integer status,
                                       HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("merchantId");
        log.info("修改分类状态：merchantId={}, categoryId={}, status={}", merchantId, id, status);

        categoryService.updateStatus(merchantId, id, status);
        return Result.success(MessageConstant.CATEGORY_STATUS_UPDATE_SUCCESS);
    }
}