package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodie.entity.Setmeal;
import com.foodie.vo.user.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    /**
     * 根据商户ID和分类ID查询套餐
     */
    List<SetmealVO> listByMerchantAndCategory(@Param("merchantId") Long merchantId,
                                              @Param("categoryId") Long categoryId);

    /**
     * 查询套餐详情
     */
    SetmealVO getSetmealDetail(@Param("setmealId") Long setmealId);
}