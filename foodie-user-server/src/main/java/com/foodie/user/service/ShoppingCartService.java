package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.user.ShoppingCartDTO;
import com.foodie.entity.ShoppingCart;
import com.foodie.vo.user.ShoppingCartVO;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 添加购物车
     */
    void addCart(Long userId, ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     */
    List<ShoppingCartVO> listCart(Long userId);

    /**
     * 减少购物车商品数量
     */
    void subCart(Long userId, ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     */
    void cleanCart(Long userId);

    /**
     * 清空指定商户的购物车
     */
    void cleanCartByMerchant(Long userId, Long merchantId);
}
