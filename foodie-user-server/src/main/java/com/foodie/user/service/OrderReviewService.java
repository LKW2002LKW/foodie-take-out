package com.foodie.user.service;


import com.foodie.dto.user.ReviewQueryDTO;
import com.foodie.vo.user.OrderReviewVO;

import java.util.Map;

/**
 * 订单评价服务接口
 */
public interface OrderReviewService {

    /**
     * 分页查询商户评价列表
     */
    Map<String, Object> getReviewPage(ReviewQueryDTO query);

    /**
     * 获取商户评价统计信息
     */
    Map<String, Object> getReviewStats(Long merchantId);

    /**
     * 根据ID查询评价详情
     */
    OrderReviewVO getReviewById(Long reviewId);

    /**
     * 查询用户自己的评价列表
     */
    Map<String, Object> getMyReviewPage(Long userId, Integer page, Integer pageSize);

    /**
     * 删除用户自己的评价
     */
    void deleteMyReview(Long userId, Long reviewId);

}