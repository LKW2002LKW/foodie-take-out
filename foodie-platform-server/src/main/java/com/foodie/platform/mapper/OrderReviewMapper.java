package com.foodie.platform.mapper;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:51
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 订单评价Mapper
 */
@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {

    /**
     * 统计差评数量（评分小于等于2星）
     */
    @Select("SELECT COUNT(*) FROM order_review WHERE rating <= 2")
    Integer countBadReviews();
}