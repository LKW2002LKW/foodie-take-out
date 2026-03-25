package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.merchant.SetmealDTO;
import com.foodie.dto.merchant.SetmealPageQueryDTO;
import com.foodie.entity.Setmeal;
import com.foodie.entity.SetmealDish;
import com.foodie.merchant.mapper.SetmealDishMapper;
import com.foodie.merchant.mapper.SetmealMapper;
import com.foodie.merchant.service.SetmealService;
import com.foodie.vo.merchant.SetmealVO;
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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐（含菜品）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSetmeal(Long merchantId, SetmealDTO setmealDTO) {
        log.info("新增套餐：merchantId={}, setmealDTO={}", merchantId, setmealDTO);

        // 检查套餐名称是否已存在
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getMerchantId, merchantId)
                .eq(Setmeal::getName, setmealDTO.getName());

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.SETMEAL_NAME_ALREADY_EXISTS);
        }

        // 检查套餐是否包含菜品
        if (CollectionUtils.isEmpty(setmealDTO.getSetmealDishes())) {
            throw new BaseException(MessageConstant.SETMEAL_DISH_NOT_NULL);
        }

        // 新增套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setMerchantId(merchantId);
        setmeal.setStatus(StatusConstant.SETMEAL_STOP_SALE); // 默认停售

        this.save(setmeal);

        // 新增套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishes.forEach(setmealDishMapper::insert);

        log.info("套餐添加成功：setmealId={}", setmeal.getId());
    }

    /**
     * 修改套餐（含菜品）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetmeal(Long merchantId, SetmealDTO setmealDTO) {
        log.info("修改套餐：merchantId={}, setmealDTO={}", merchantId, setmealDTO);

        // 查询套餐
        Setmeal setmeal = this.getById(setmealDTO.getId());
        if (setmeal == null || !setmeal.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }

        // 检查套餐名称是否重复（排除自己）
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getMerchantId, merchantId)
                .eq(Setmeal::getName, setmealDTO.getName())
                .ne(Setmeal::getId, setmealDTO.getId());

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.SETMEAL_NAME_ALREADY_EXISTS);
        }

        // 检查套餐是否包含菜品
        if (CollectionUtils.isEmpty(setmealDTO.getSetmealDishes())) {
            throw new BaseException(MessageConstant.SETMEAL_DISH_NOT_NULL);
        }

        // 更新套餐
        BeanUtils.copyProperties(setmealDTO, setmeal);
        this.updateById(setmeal);

        // 删除原有套餐菜品关系
        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        setmealDishMapper.delete(dishWrapper);

        // 新增套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishes.forEach(setmealDishMapper::insert);

        log.info("套餐修改成功：setmealId={}", setmeal.getId());
    }

    /**
     * 删除套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSetmeal(Long merchantId, Long id) {
        log.info("删除套餐：merchantId={}, setmealId={}", merchantId, id);

        // 查询套餐
        Setmeal setmeal = this.getById(id);
        if (setmeal == null || !setmeal.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }

        // 检查套餐状态（起售中不能删除）
        if (setmeal.getStatus().equals(StatusConstant.SETMEAL_ON_SALE)) {
            throw new BaseException(MessageConstant.SETMEAL_ON_SALE);
        }

        // 删除套餐
        this.removeById(id);

        // 删除套餐菜品关系
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        setmealDishMapper.delete(wrapper);

        log.info("套餐删除成功：setmealId={}", id);
    }

    /**
     * 批量删除套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteSetmeal(Long merchantId, List<Long> ids) {
        log.info("批量删除套餐：merchantId={}, ids={}", merchantId, ids);

        for (Long id : ids) {
            deleteSetmeal(merchantId, id);
        }
    }

    /**
     * 根据ID查询套餐详情（含菜品）
     */
    @Override
    public SetmealVO getSetmealDetail(Long merchantId, Long id) {
        log.info("查询套餐详情：merchantId={}, setmealId={}", merchantId, id);

        // 查询套餐基本信息
        SetmealVO setmealVO = this.baseMapper.getSetmealDetail(id, merchantId);
        if (setmealVO == null) {
            throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }

        // 查询套餐菜品
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);
        setmealVO.setSetmealDishes(setmealDishes);

        // 设置状态描述
        setmealVO.setStatusDesc(setmealVO.getStatus().equals(StatusConstant.SETMEAL_ON_SALE)
                ? "起售" : "停售");

        return setmealVO;
    }

    /**
     * 分页查询套餐
     */
    @Override
    public Page<SetmealVO> pageQuery(Long merchantId, SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询套餐：merchantId={}, query={}", merchantId, setmealPageQueryDTO);

        // 设置merchantId
        setmealPageQueryDTO.setMerchantId(merchantId);

        Page<SetmealVO> page = new Page<>(
                setmealPageQueryDTO.getPage(),
                setmealPageQueryDTO.getPageSize()
        );

        page = this.baseMapper.pageQuery(page, setmealPageQueryDTO);

        // 设置状态描述
        page.getRecords().forEach(setmealVO -> {
            setmealVO.setStatusDesc(setmealVO.getStatus().equals(StatusConstant.SETMEAL_ON_SALE)
                    ? "起售" : "停售");
        });

        return page;
    }

    /**
     * 起售/停售套餐
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long merchantId, Long id, Integer status) {
        log.info("修改套餐状态：merchantId={}, setmealId={}, status={}", merchantId, id, status);

        Setmeal setmeal = this.getById(id);
        if (setmeal == null || !setmeal.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.SETMEAL_NOT_FOUND);
        }

        // 如果要起售，检查套餐内是否有停售菜品
        if (status.equals(StatusConstant.SETMEAL_ON_SALE)) {
            Integer stopSaleDishCount = this.baseMapper.countStopSaleDishBySetmealId(id);
            if (stopSaleDishCount > 0) {
                throw new BaseException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        setmeal.setStatus(status);
        this.updateById(setmeal);

        log.info("套餐状态修改成功：setmealId={}, status={}", id, status);
    }

    /**
     * 根据分类ID查询套餐列表
     */
    @Override
    public List<SetmealVO> listByCategoryId(Long merchantId, Long categoryId) {
        log.info("根据分类查询套餐：merchantId={}, categoryId={}", merchantId, categoryId);

        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getMerchantId, merchantId)
                .eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, StatusConstant.SETMEAL_ON_SALE) // 只查询起售的
                .orderByDesc(Setmeal::getCreateTime);

        List<Setmeal> setmeals = this.list(wrapper);

        return setmeals.stream().map(setmeal -> {
            SetmealVO vo = new SetmealVO();
            BeanUtils.copyProperties(setmeal, vo);
            vo.setStatusDesc("起售");
            return vo;
        }).collect(Collectors.toList());
    }
}