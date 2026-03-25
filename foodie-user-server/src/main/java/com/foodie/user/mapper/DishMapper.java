package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.Dish;
import com.foodie.vo.user.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 根据商户ID和分类ID查询菜品
     */
    List<DishVO> listByMerchantAndCategory(@Param("merchantId") Long merchantId,
                                           @Param("categoryId") Long categoryId);

    /**
     * 查询菜品详情
     */
    DishVO getDishDetail(@Param("dishId") Long dishId);
}