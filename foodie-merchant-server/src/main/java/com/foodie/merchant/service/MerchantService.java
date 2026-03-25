package com.foodie.merchant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.merchant.MerchantBusinessHoursDTO;
import com.foodie.dto.merchant.MerchantStatusDTO;
import com.foodie.dto.merchant.MerchantUpdateDTO;
import com.foodie.entity.Merchant;
import com.foodie.vo.merchant.MerchantVO;

public interface MerchantService extends IService<Merchant> {

    /**
     * 获取商户信息
     */
    MerchantVO getMerchantInfo(Long merchantId);

    /**
     * 修改商户信息
     */
    void updateMerchantInfo(Long merchantId, MerchantUpdateDTO merchantUpdateDTO);

    /**
     * 修改营业状态
     */
    void updateStatus(Long merchantId, MerchantStatusDTO merchantStatusDTO);

    /**
     * 修改营业时间
     */
    void updateBusinessHours(Long merchantId, MerchantBusinessHoursDTO merchantBusinessHoursDTO);

    void updateLogo(Long merchantId, String logoUrl);
}