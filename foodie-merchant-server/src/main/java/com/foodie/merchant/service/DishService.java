package com.foodie.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.merchant.DishDTO;
import com.foodie.dto.merchant.DishPageQueryDTO;
import com.foodie.entity.Dish;
import com.foodie.vo.merchant.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 新增菜品（含口味）
     */
    void addDish(Long merchantId, DishDTO dishDTO);

    /**
     * 修改菜品（含口味）
     */
    void updateDish(Long merchantId, DishDTO dishDTO);

    /**
     * 删除菜品
     */
    void deleteDish(Long merchantId, Long id);

    /**
     * 批量删除菜品
     */
    void batchDeleteDish(Long merchantId, List<Long> ids);

    /**
     * 根据ID查询菜品详情（含口味）
     */
    DishVO getDishDetail(Long merchantId, Long id);

    /**
     * 分页查询菜品
     */
    Page<DishVO> pageQuery(Long merchantId, DishPageQueryDTO dishPageQueryDTO);

    /**
     * 起售/停售菜品
     */
    void updateStatus(Long merchantId, Long id, Integer status);

    /**
     * 根据分类ID查询菜品列表
     */
    List<DishVO> listByCategoryId(Long merchantId, Long categoryId);
}