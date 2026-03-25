package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.entity.Dish;
import com.foodie.vo.merchant.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 分页查询菜品（关联分类表）
     */
    Page<DishVO> pageQuery(Page<DishVO> page, @Param("query") com.foodie.dto.merchant.DishPageQueryDTO query);

    /**
     * 根据菜品ID查询菜品详情（关联分类表）
     */
    DishVO getDishDetail(@Param("dishId") Long dishId, @Param("merchantId") Long merchantId);

    /**
     * 统计套餐关联的菜品数量
     */
    @Select("SELECT COUNT(*) FROM setmeal_dish WHERE dish_id = #{dishId}")
    Integer countSetmealByDishId(Long dishId);
}