package com.foodie.merchant.controller;

import com.foodie.common.properties.JwtProperties;
import com.foodie.common.result.Result;
import com.foodie.dto.merchant.MerchantLoginDTO;
import com.foodie.dto.merchant.MerchantRegisterDTO;
import com.foodie.merchant.service.MerchantAdminService;
import com.foodie.vo.merchant.MerchantLoginVO;
import com.foodie.vo.merchant.MerchantRegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant/admin")
@Api(tags = "商户管理员接口")
@Slf4j
public class MerchantAdminController {

    @Autowired
    private MerchantAdminService merchantAdminService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 商户注册
     */
    @PostMapping("/register")
    @ApiOperation("商户注册")
    public Result<MerchantRegisterVO> register(@RequestBody MerchantRegisterDTO merchantRegisterDTO) {
        log.info("商户注册：{}", merchantRegisterDTO);
        MerchantRegisterVO merchantRegisterVO = merchantAdminService.register(merchantRegisterDTO);
        return Result.success(merchantRegisterVO);
    }


    /**
     * 商户登录
     */
    @PostMapping("/login")
    @ApiOperation("商户登录")
    public Result<MerchantLoginVO> login(@RequestBody MerchantLoginDTO merchantLoginDTO) {
        log.info("商户登录：{}", merchantLoginDTO);
        MerchantLoginVO merchantLoginVO = merchantAdminService.login(merchantLoginDTO);
        System.out.println(merchantLoginVO);
        return Result.success(merchantLoginVO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result<String> logout() {
        return Result.success();
    }
}