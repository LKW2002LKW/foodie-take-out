package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.entity.Setmeal;
import com.foodie.vo.merchant.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    /**
     * 分页查询套餐（关联分类表）
     */
    Page<SetmealVO> pageQuery(Page<SetmealVO> page, @Param("query") com.foodie.dto.merchant.SetmealPageQueryDTO query);

    /**
     * 根据套餐ID查询套餐详情（关联分类表）
     */
    SetmealVO getSetmealDetail(@Param("setmealId") Long setmealId, @Param("merchantId") Long merchantId);

    /**
     * 根据套餐ID查询停售菜品数量
     */
    Integer countStopSaleDishBySetmealId(@Param("setmealId") Long setmealId);
}