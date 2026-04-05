package com.foodie.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.foodie.dto.user.ReviewQueryDTO;
import com.foodie.entity.OrderReview;
import com.foodie.vo.user.OrderReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单评价Mapper
 */
@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {

    /**
     * 分页查询商户评价列表
     */
    List<OrderReviewVO> selectReviewPage(@Param("query") ReviewQueryDTO query);

    /**
     * 查询商户评价总数
     */
    Long countReviews(@Param("query") ReviewQueryDTO query);

    /**
     * 查询商户评价统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "AVG(rating) as avgRating, " +
            "SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) as fiveStarCount, " +
            "SUM(CASE WHEN rating >= 3 AND rating < 5 THEN 1 ELSE 0 END) as mediumCount, " +
            "SUM(CASE WHEN rating < 3 THEN 1 ELSE 0 END) as badCount, " +
            "SUM(CASE WHEN images IS NOT NULL AND images != '' THEN 1 ELSE 0 END) as hasImageCount " +
            "FROM order_review WHERE merchant_id = #{merchantId}")
    Map<String, Object> selectReviewStats(@Param("merchantId") Long merchantId);

    /**
     * 查询用户自己的评价列表
     */
    List<OrderReviewVO> selectMyReviewPage(@Param("userId") Long userId,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    /**
     * 查询用户自己的评价总数
     */
    Long countMyReviews(@Param("userId") Long userId);
}