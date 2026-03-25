package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.entity.Setmeal;
import com.foodie.vo.user.SetmealVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 根据商户ID和分类ID查询套餐
     */
    List<SetmealVO> listByMerchantAndCategory(Long merchantId, Long categoryId);

    /**
     * 查询套餐详情（含菜品）
     */
    SetmealVO getSetmealDetail(Long setmealId);
}