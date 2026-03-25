package com.foodie.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细Mapper接口
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

}