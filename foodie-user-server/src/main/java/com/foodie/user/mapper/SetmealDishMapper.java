package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.DishFlavor;
import com.foodie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: wanderer
 * @Date: 2026/1/12 12:04
 */
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {

}
