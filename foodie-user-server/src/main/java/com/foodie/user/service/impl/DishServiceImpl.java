package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.entity.Dish;
import com.foodie.entity.DishFlavor;

import com.foodie.user.mapper.DishFlavorMapper;
import com.foodie.user.mapper.DishMapper;
import com.foodie.user.service.DishService;
import com.foodie.vo.user.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 根据商户ID和分类ID查询菜品
     */
    @Override
    public List<DishVO> listByMerchantAndCategory(Long merchantId, Long categoryId) {
        log.info("查询菜品列表：merchantId={}, categoryId={}", merchantId, categoryId);

        List<DishVO> dishVOList = this.baseMapper.listByMerchantAndCategory(merchantId, categoryId);

        // 查询每个菜品的口味
        dishVOList.forEach(dishVO -> {
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, dishVO.getId());
            List<DishFlavor> flavors = dishFlavorMapper.selectList(wrapper);
            dishVO.setFlavors(flavors);
        });

        return dishVOList;
    }

    /**
     * 查询菜品详情（含口味）
     */
    @Override
    public DishVO getDishDetail(Long dishId) {
        log.info("查询菜品详情：dishId={}", dishId);

        DishVO dishVO = this.baseMapper.getDishDetail(dishId);

        if (dishVO != null) {
            // 查询口味
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> flavors = dishFlavorMapper.selectList(wrapper);
            dishVO.setFlavors(flavors);
        }

        return dishVO;
    }
}