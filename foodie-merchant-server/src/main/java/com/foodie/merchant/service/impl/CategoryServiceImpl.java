package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.merchant.CategoryDTO;
import com.foodie.dto.merchant.CategoryPageQueryDTO;
import com.foodie.entity.Category;
import com.foodie.merchant.mapper.CategoryMapper;
import com.foodie.merchant.service.CategoryService;
import com.foodie.vo.merchant.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 新增分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(Long merchantId, CategoryDTO categoryDTO) {
        log.info("新增分类：merchantId={}, categoryDTO={}", merchantId, categoryDTO);

        // 检查分类名称是否已存在（同一商户、同一类型下不能重复）
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getMerchantId, merchantId)
                .eq(Category::getName, categoryDTO.getName())
                .eq(Category::getType, categoryDTO.getType());

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS);
        }

        // 创建分类
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setMerchantId(merchantId);
        category.setStatus(StatusConstant.ENABLE); // 默认启用

        this.save(category);

        log.info("分类添加成功：categoryId={}", category.getId());
    }

    /**
     * 修改分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long merchantId, CategoryDTO categoryDTO) {
        log.info("修改分类：merchantId={}, categoryDTO={}", merchantId, categoryDTO);

        // 查询分类
        Category category = this.getById(categoryDTO.getId());
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 检查分类名称是否重复（排除自己）
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getMerchantId, merchantId)
                .eq(Category::getName, categoryDTO.getName())
                .eq(Category::getType, categoryDTO.getType())
                .ne(Category::getId, categoryDTO.getId());

        if (this.count(wrapper) > 0) {
            throw new BaseException(MessageConstant.CATEGORY_NAME_ALREADY_EXISTS);
        }

        // 更新分类
        BeanUtils.copyProperties(categoryDTO, category);
        this.updateById(category);

        log.info("分类修改成功：categoryId={}", category.getId());
    }

    /**
     * 删除分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long merchantId, Long id) {
        log.info("删除分类：merchantId={}, categoryId={}", merchantId, id);

        // 查询分类
        Category category = this.getById(id);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        // 检查是否关联菜品
        Integer dishCount = this.baseMapper.countDishByCategoryId(id, merchantId);
        if (dishCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 检查是否关联套餐
        Integer setmealCount = this.baseMapper.countSetmealByCategoryId(id, merchantId);
        if (setmealCount > 0) {
            throw new BaseException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 删除分类
        this.removeById(id);

        log.info("分类删除成功：categoryId={}", id);
    }

    /**
     * 根据ID查询分类
     */
    @Override
    public CategoryVO getCategoryById(Long merchantId, Long id) {
        log.info("查询分类：merchantId={}, categoryId={}", merchantId, id);

        Category category = this.getById(id);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        return convertToVO(category);
    }

    /**
     * 分页查询分类
     */
    @Override
    public Page<CategoryVO> pageQuery(Long merchantId, CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询分类：merchantId={}, query={}", merchantId, categoryPageQueryDTO);

        // 构建查询条件
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getMerchantId, merchantId);

        // 分类名称模糊查询
        if (StringUtils.hasText(categoryPageQueryDTO.getName())) {
            wrapper.like(Category::getName, categoryPageQueryDTO.getName());
        }

        // 分类类型
        if (categoryPageQueryDTO.getType() != null) {
            wrapper.eq(Category::getType, categoryPageQueryDTO.getType());
        }

        // 排序
        wrapper.orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime);

        // 分页查询
        Page<Category> page = new Page<>(
                categoryPageQueryDTO.getPage(),
                categoryPageQueryDTO.getPageSize()
        );
        this.page(page, wrapper);

        // 转换为VO
        Page<CategoryVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        voPage.setRecords(
                page.getRecords().stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList())
        );

        return voPage;
    }

    /**
     * 根据类型查询分类列表
     */
    @Override
    public List<CategoryVO> listByType(Long merchantId, Integer type) {
        log.info("查询分类列表：merchantId={}, type={}", merchantId, type);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getMerchantId, merchantId)
                .eq(Category::getStatus, StatusConstant.ENABLE); // 只查询启用的分类

        if (type != null) {
            wrapper.eq(Category::getType, type);
        }

        wrapper.orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime);

        List<Category> categories = this.list(wrapper);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 启用/禁用分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long merchantId, Long id, Integer status) {
        log.info("修改分类状态：merchantId={}, categoryId={}, status={}", merchantId, id, status);

        Category category = this.getById(id);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        category.setStatus(status);
        this.updateById(category);

        log.info("分类状态修改成功：categoryId={}, status={}", id, status);
    }

    /**
     * 转换为VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);

        // 设置类型描述
        vo.setTypeDesc(category.getType().equals(StatusConstant.CATEGORY_TYPE_DISH)
                ? "菜品分类" : "套餐分类");

        // 设置状态描述
        vo.setStatusDesc(category.getStatus().equals(StatusConstant.ENABLE)
                ? "启用" : "禁用");

        return vo;
    }
}