package com.foodie.merchant.service;

import com.foodie.dto.merchant.MerchantLoginDTO;
import com.foodie.dto.merchant.MerchantRegisterDTO;
import com.foodie.entity.MerchantAdmin;
import com.foodie.vo.merchant.MerchantLoginVO;
import com.foodie.vo.merchant.MerchantRegisterVO;

public interface MerchantAdminService {

    /**
     * 商户注册
     */
    MerchantRegisterVO register(MerchantRegisterDTO merchantRegisterDTO);

    /**
     * 商户登录
     */
    MerchantLoginVO login(MerchantLoginDTO merchantLoginDTO);
}