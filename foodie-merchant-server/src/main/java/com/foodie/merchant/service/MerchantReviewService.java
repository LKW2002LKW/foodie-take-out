package com.foodie.merchant.service;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:45
 */
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.merchant.ReviewQueryDTO;
import com.foodie.dto.merchant.ReviewReplyDTO;
import com.foodie.vo.merchant.MerchantReviewVO;


/**
 * 商家评价服务接口
 */
public interface MerchantReviewService {

    /**
     * 评价分页查询
     */
    Page<MerchantReviewVO> pageQuery(ReviewQueryDTO reviewQueryDTO);

    /**
     * 商户回复评价
     */
    void replyReview(ReviewReplyDTO reviewReplyDTO);

    /**
     * 差评列表
     */
    Page<MerchantReviewVO> getBadReviews(Integer page, Integer pageSize);
}