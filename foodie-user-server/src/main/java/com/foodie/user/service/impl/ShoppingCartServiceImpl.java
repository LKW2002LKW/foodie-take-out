package com.foodie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.StatusConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.user.ShoppingCartDTO;
import com.foodie.entity.Dish;
import com.foodie.entity.Merchant;
import com.foodie.entity.Setmeal;
import com.foodie.entity.ShoppingCart;
import com.foodie.user.mapper.DishMapper;
import com.foodie.user.mapper.MerchantMapper;
import com.foodie.user.mapper.SetmealMapper;
import com.foodie.user.mapper.ShoppingCartMapper;
import com.foodie.user.service.ShoppingCartService;
import com.foodie.vo.user.ShoppingCartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 添加购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long userId, ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：userId={}, dto={}", userId, shoppingCartDTO);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        // 判断是菜品还是套餐
        if (shoppingCartDTO.getDishId() != null) {
            // 添加菜品
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new BaseException("菜品不存在");
            }
            if (!dish.getStatus().equals(StatusConstant.DISH_ON_SALE)) {
                throw new BaseException(MessageConstant.DISH_NOT_ON_SALE);
            }

            shoppingCart.setMerchantId(dish.getMerchantId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());

        } else if (shoppingCartDTO.getSetmealId() != null) {
            // 添加套餐
            Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new BaseException("套餐不存在");
            }
            if (!setmeal.getStatus().equals(StatusConstant.SETMEAL_ON_SALE)) {
                throw new BaseException(MessageConstant.SETMEAL_NOT_ON_SALE);
            }

            shoppingCart.setMerchantId(setmeal.getMerchantId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());

        } else {
            throw new BaseException("请选择菜品或套餐");
        }

        // 检查购物车中是否已有其他商户的商品
        LambdaQueryWrapper<ShoppingCart> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(ShoppingCart::getUserId, userId)
                .ne(ShoppingCart::getMerchantId, shoppingCart.getMerchantId());
        if (this.count(merchantWrapper) > 0) {
            throw new BaseException(MessageConstant.DIFFERENT_MERCHANT);
        }

        // 查询购物车中是否已存在该商品（菜品需要匹配口味）
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getMerchantId, shoppingCart.getMerchantId());

        if (shoppingCartDTO.getDishId() != null) {
            wrapper.eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId());
            // 口味相同才算同一商品
            if (shoppingCartDTO.getDishFlavor() != null) {
                wrapper.eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
            } else {
                wrapper.isNull(ShoppingCart::getDishFlavor);
            }
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId());
        }

        ShoppingCart existCart = this.getOne(wrapper);

        if (existCart != null) {
            // 已存在，数量+1
            existCart.setNumber(existCart.getNumber() + shoppingCartDTO.getNumber());
            this.updateById(existCart);
        } else {
            // 不存在，新增
            shoppingCart.setNumber(shoppingCartDTO.getNumber());
            this.save(shoppingCart);
        }

        log.info("购物车操作成功");
    }

    /**
     * 查看购物车
     */
    @Override
    public List<ShoppingCartVO> listCart(Long userId) {
        log.info("查看购物车：userId={}", userId);

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> cartList = this.list(wrapper);

        // 转换为VO，并补充商户名称
        return cartList.stream().map(cart -> {
            ShoppingCartVO vo = new ShoppingCartVO();
            BeanUtils.copyProperties(cart, vo);

            // 查询商户名称
            Merchant merchant = merchantMapper.selectById(cart.getMerchantId());
            if (merchant != null) {
                vo.setMerchantName(merchant.getMerchantName());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 减少购物车商品数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void subCart(Long userId, ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车商品：userId={}, dto={}", userId, shoppingCartDTO);

        // 查询购物车中的商品
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        if (shoppingCartDTO.getDishId() != null) {
            wrapper.eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId());
            if (shoppingCartDTO.getDishFlavor() != null) {
                wrapper.eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
            } else {
                wrapper.isNull(ShoppingCart::getDishFlavor);
            }
        } else if (shoppingCartDTO.getSetmealId() != null) {
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId());
        } else {
            throw new BaseException("请指定要减少的商品");
        }

        ShoppingCart cart = this.getOne(wrapper);

        if (cart == null) {
            throw new BaseException(MessageConstant.CART_ITEM_NOT_FOUND);
        }

        // 如果数量为1，直接删除；否则数量-1
        if (cart.getNumber() == 1) {
            this.removeById(cart.getId());
        } else {
            cart.setNumber(cart.getNumber() - 1);
            this.updateById(cart);
        }

        log.info("购物车商品减少成功");
    }

    /**
     * 清空购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanCart(Long userId) {
        log.info("清空购物车：userId={}", userId);

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        this.remove(wrapper);

        log.info("购物车已清空");
    }

    /**
     * 清空指定商户的购物车
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanCartByMerchant(Long userId, Long merchantId) {
        log.info("清空指定商户购物车：userId={}, merchantId={}", userId, merchantId);

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getMerchantId, merchantId);

        this.remove(wrapper);

        log.info("指定商户购物车已清空");
    }
}
