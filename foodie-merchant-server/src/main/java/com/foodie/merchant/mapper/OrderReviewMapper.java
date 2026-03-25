package com.foodie.merchant.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:29
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单评价Mapper
 */
@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {

}
