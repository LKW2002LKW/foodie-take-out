package com.foodie.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.merchant.SetmealDTO;
import com.foodie.dto.merchant.SetmealPageQueryDTO;
import com.foodie.entity.Setmeal;
import com.foodie.vo.merchant.SetmealVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐（含菜品）
     */
    void addSetmeal(Long merchantId, SetmealDTO setmealDTO);

    /**
     * 修改套餐（含菜品）
     */
    void updateSetmeal(Long merchantId, SetmealDTO setmealDTO);

    /**
     * 删除套餐
     */
    void deleteSetmeal(Long merchantId, Long id);

    /**
     * 批量删除套餐
     */
    void batchDeleteSetmeal(Long merchantId, List<Long> ids);

    /**
     * 根据ID查询套餐详情（含菜品）
     */
    SetmealVO getSetmealDetail(Long merchantId, Long id);

    /**
     * 分页查询套餐
     */
    Page<SetmealVO> pageQuery(Long merchantId, SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 起售/停售套餐
     */
    void updateStatus(Long merchantId, Long id, Integer status);

    /**
     * 根据分类ID查询套餐列表
     */
    List<SetmealVO> listByCategoryId(Long merchantId, Long categoryId);
}