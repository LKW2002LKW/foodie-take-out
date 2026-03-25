package com.foodie.merchant.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:28
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.MerchantSettlement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户结算Mapper
 */
@Mapper
public interface MerchantSettlementMapper extends BaseMapper<MerchantSettlement> {

}