package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 根据商户ID和状态统计订单数量
     * @param merchantId 商户ID
     * @param status 订单状态
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId} AND status = #{status} AND pay_status = 1")
    Integer countByMerchantIdAndStatus(@Param("merchantId") Long merchantId,
                                       @Param("status") Integer status);
}