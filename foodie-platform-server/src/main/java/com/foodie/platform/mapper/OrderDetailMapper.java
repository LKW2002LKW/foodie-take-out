package com.foodie.platform.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 14:38
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细Mapper
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

}