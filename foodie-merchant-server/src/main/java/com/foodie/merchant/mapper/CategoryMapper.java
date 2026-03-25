package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据分类ID统计菜品数量
     */
    @Select("SELECT COUNT(*) FROM dish WHERE category_id = #{categoryId} AND merchant_id = #{merchantId}")
    Integer countDishByCategoryId(Long categoryId, Long merchantId);

    /**
     * 根据分类ID统计套餐数量
     */
    @Select("SELECT COUNT(*) FROM setmeal WHERE category_id = #{categoryId} AND merchant_id = #{merchantId}")
    Integer countSetmealByCategoryId(Long categoryId, Long merchantId);
}