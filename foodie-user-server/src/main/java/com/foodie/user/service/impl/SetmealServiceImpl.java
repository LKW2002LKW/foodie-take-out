package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.entity.Setmeal;
import com.foodie.entity.SetmealDish;
import com.foodie.user.mapper.SetmealDishMapper;
import com.foodie.user.mapper.SetmealMapper;
import com.foodie.user.service.SetmealService;
import com.foodie.vo.user.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    private final SetmealDishMapper setmealDishMapper;

    /**
     * 根据商户ID和分类ID查询套餐
     */
    @Override
    public List<SetmealVO> listByMerchantAndCategory(Long merchantId, Long categoryId) {
        log.info("查询套餐列表：merchantId={}, categoryId={}", merchantId, categoryId);

        return this.baseMapper.listByMerchantAndCategory(merchantId, categoryId);
    }

    /**
     * 查询套餐详情（含菜品）
     */
    @Override
    public SetmealVO getSetmealDetail(Long setmealId) {
        log.info("查询套餐详情：setmealId={}", setmealId);

        SetmealVO setmealVO = this.baseMapper.getSetmealDetail(setmealId);

        if (setmealVO != null) {
            // 查询套餐菜品
            LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);
            setmealVO.setSetmealDishes(setmealDishes);
        }

        return setmealVO;
    }
}
