package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.entity.Dish;
import com.foodie.vo.user.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 根据商户ID和分类ID查询菜品
     */
    List<DishVO> listByMerchantAndCategory(Long merchantId, Long categoryId);

    /**
     * 查询菜品详情（含口味）
     */
    DishVO getDishDetail(Long dishId);
}