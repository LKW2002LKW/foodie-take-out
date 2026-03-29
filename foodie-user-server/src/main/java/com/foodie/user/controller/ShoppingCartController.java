package com.foodie.user.controller;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.result.Result;
import com.foodie.dto.user.ShoppingCartDTO;
import com.foodie.user.service.ShoppingCartService;
import com.foodie.vo.user.ShoppingCartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 购物车管理
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "购物车管理")
@Slf4j
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result<String> addCart(@RequestBody ShoppingCartDTO shoppingCartDTO,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("添加购物车：userId={}, dto={}", userId, shoppingCartDTO);

        shoppingCartService.addCart(userId, shoppingCartDTO);
        return Result.success(MessageConstant.CART_ADD_SUCCESS);
    }

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCartVO>> listCart(@RequestParam(required = false) String merchantIds, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("查看购物车：userId={},merchantIds={}", userId, merchantIds);

        List<ShoppingCartVO> cartList = shoppingCartService.listCart(merchantIds, userId);
        return Result.success(cartList);
    }

    /**
     * 减少购物车商品
     */
    @PostMapping("/sub")
    @ApiOperation("减少购物车商品")
    public Result<String> subCart(@RequestBody ShoppingCartDTO shoppingCartDTO,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("减少购物车商品：userId={}, dto={}", userId, shoppingCartDTO);

        shoppingCartService.subCart(userId, shoppingCartDTO);
        return Result.success(MessageConstant.CART_UPDATE_SUCCESS);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result<String> cleanCart(@RequestParam Long merchantId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("清空购物车：userId={},merchantId={}", userId,merchantId);

        shoppingCartService.cleanCartByMerchant(userId,merchantId);
        return Result.success(MessageConstant.CART_CLEAR_SUCCESS);
    }
}
