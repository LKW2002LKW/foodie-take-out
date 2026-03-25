package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.merchant.DishDTO;
import com.foodie.dto.merchant.DishPageQueryDTO;
import com.foodie.entity.Dish;
import com.foodie.entity.DishFlavor;
import com.foodie.merchant.mapper.DishFlavorMapper;
import com.foodie.merchant.mapper.DishMapper;
import com.foodie.merchant.service.DishService;
import com.foodie.vo.merchant.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品（含口味）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDish(Long merchantId, DishDTO dishDTO) {
        log.info("新增菜品：merchantId={}, dishDTO={}", merchantId, dishDTO);

        // 检查菜品名称是否已存在
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getName, dishDTO.getName());

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.DISH_NAME_ALREADY_EXISTS);
        }

        // 新增菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setMerchantId(merchantId);
        dish.setStatus(StatusConstant.DISH_STOP_SALE); // 默认停售

        this.save(dish);

        // 新增口味
        if (!CollectionUtils.isEmpty(dishDTO.getFlavors())) {
            List<DishFlavor> flavors = dishDTO.getFlavors();
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            flavors.forEach(dishFlavorMapper::insert);
        }

        log.info("菜品添加成功：dishId={}", dish.getId());
    }

    /**
     * 修改菜品（含口味）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDish(Long merchantId, DishDTO dishDTO) {
        log.info("修改菜品：merchantId={}, dishDTO={}", merchantId, dishDTO);

        // 查询菜品
        Dish dish = this.getById(dishDTO.getId());
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.DISH_NOT_FOUND);
        }

        // 检查菜品名称是否重复（排除自己）
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getName, dishDTO.getName())
                .ne(Dish::getId, dishDTO.getId());

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.DISH_NAME_ALREADY_EXISTS);
        }

        // 更新菜品
        BeanUtils.copyProperties(dishDTO, dish);
        this.updateById(dish);

        // 删除原有口味
        LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
        flavorWrapper.eq(DishFlavor::getDishId, dish.getId());
        dishFlavorMapper.delete(flavorWrapper);

        // 新增口味
        if (!CollectionUtils.isEmpty(dishDTO.getFlavors())) {
            List<DishFlavor> flavors = dishDTO.getFlavors();
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            flavors.forEach(dishFlavorMapper::insert);
        }

        log.info("菜品修改成功：dishId={}", dish.getId());
    }

    /**
     * 删除菜品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDish(Long merchantId, Long id) {
        log.info("删除菜品：merchantId={}, dishId={}", merchantId, id);

        // 查询菜品
        Dish dish = this.getById(id);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.DISH_NOT_FOUND);
        }

        // 检查菜品状态（起售中不能删除）
        if (dish.getStatus().equals(StatusConstant.DISH_ON_SALE)) {
            throw new BaseException(MessageConstant.DISH_ON_SALE);
        }

        // 检查是否被套餐关联
        Integer count = this.baseMapper.countSetmealByDishId(id);
        if (count > 0) {
            throw new BaseException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品
        this.removeById(id);

        // 删除口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, id);
        dishFlavorMapper.delete(wrapper);

        log.info("菜品删除成功：dishId={}", id);
    }

    /**
     * 批量删除菜品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteDish(Long merchantId, List<Long> ids) {
        log.info("批量删除菜品：merchantId={}, ids={}", merchantId, ids);

        for (Long id : ids) {
            deleteDish(merchantId, id);
        }
    }

    /**
     * 根据ID查询菜品详情（含口味）
     */
    @Override
    public DishVO getDishDetail(Long merchantId, Long id) {
        log.info("查询菜品详情：merchantId={}, dishId={}", merchantId, id);

        // 查询菜品基本信息
        DishVO dishVO = this.baseMapper.getDishDetail(id, merchantId);
        if (dishVO == null) {
            throw new BaseException(MessageConstant.DISH_NOT_FOUND);
        }

        // 查询口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorMapper.selectList(wrapper);
        dishVO.setFlavors(flavors);

        // 设置状态描述
        dishVO.setStatusDesc(dishVO.getStatus().equals(StatusConstant.DISH_ON_SALE)
                ? "起售" : "停售");

        return dishVO;
    }

    /**
     * 分页查询菜品
     */
    @Override
    public Page<DishVO> pageQuery(Long merchantId, DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品：merchantId={}, query={}", merchantId, dishPageQueryDTO);

        // 设置merchantId
        dishPageQueryDTO.setMerchantId(merchantId);

        Page<DishVO> page = new Page<>(
                dishPageQueryDTO.getPage(),
                dishPageQueryDTO.getPageSize()
        );

        page = this.baseMapper.pageQuery(page, dishPageQueryDTO);

        // 设置状态描述
        page.getRecords().forEach(dishVO -> {
            dishVO.setStatusDesc(dishVO.getStatus().equals(StatusConstant.DISH_ON_SALE)
                    ? "起售" : "停售");
        });

        return page;
    }

    /**
     * 起售/停售菜品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long merchantId, Long id, Integer status) {
        log.info("修改菜品状态：merchantId={}, dishId={}, status={}", merchantId, id, status);

        Dish dish = this.getById(id);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.DISH_NOT_FOUND);
        }

        dish.setStatus(status);
        this.updateById(dish);

        log.info("菜品状态修改成功：dishId={}, status={}", id, status);
    }

    /**
     * 根据分类ID查询菜品列表
     */
    @Override
    public List<DishVO> listByCategoryId(Long merchantId, Long categoryId) {
        log.info("根据分类查询菜品：merchantId={}, categoryId={}", merchantId, categoryId);

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, StatusConstant.DISH_ON_SALE) // 只查询起售的
                .orderByDesc(Dish::getCreateTime);

        List<Dish> dishes = this.list(wrapper);

        return dishes.stream().map(dish -> {
            DishVO vo = new DishVO();
            BeanUtils.copyProperties(dish, vo);
            vo.setStatusDesc("起售");
            return vo;
        }).collect(Collectors.toList());
    }
}