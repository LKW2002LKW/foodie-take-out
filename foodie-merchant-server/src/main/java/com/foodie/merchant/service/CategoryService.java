package com.foodie.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.merchant.CategoryDTO;
import com.foodie.dto.merchant.CategoryPageQueryDTO;
import com.foodie.entity.Category;
import com.foodie.vo.merchant.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 新增分类
     */
    void addCategory(Long merchantId, CategoryDTO categoryDTO);

    /**
     * 修改分类
     */
    void updateCategory(Long merchantId, CategoryDTO categoryDTO);

    /**
     * 删除分类
     */
    void deleteCategory(Long merchantId, Long id);

    /**
     * 根据ID查询分类
     */
    CategoryVO getCategoryById(Long merchantId, Long id);

    /**
     * 分页查询分类
     */
    Page<CategoryVO> pageQuery(Long merchantId, CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据类型查询分类列表
     */
    List<CategoryVO> listByType(Long merchantId, Integer type);

    /**
     * 启用/禁用分类
     */
    void updateStatus(Long merchantId, Long id, Integer status);
}