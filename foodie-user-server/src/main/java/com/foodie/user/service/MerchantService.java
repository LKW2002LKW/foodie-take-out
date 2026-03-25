package com.foodie.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foodie.dto.user.MerchantQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.vo.user.CategoryVO;
import com.foodie.vo.user.MerchantVO;

import java.util.List;

public interface MerchantService extends IService<Merchant> {

    /**
     * 分页查询商户列表
     */
    Page<MerchantVO> pageQuery(MerchantQueryDTO merchantQueryDTO);

    /**
     * 查询商户详情
     */
    MerchantVO getMerchantDetail(Long merchantId);

    /**
     * 查询商户的分类列表
     */
    List<CategoryVO> getMerchantCategories(Long merchantId, Integer type);
}