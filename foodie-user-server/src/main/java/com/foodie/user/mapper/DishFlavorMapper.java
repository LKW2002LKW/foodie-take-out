package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for DishFlavor — extend BaseMapper to inherit CRUD methods including selectList(Wrapper)
 */
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
}
