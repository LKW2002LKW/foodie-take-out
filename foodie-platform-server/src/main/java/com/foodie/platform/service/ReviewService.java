package com.foodie.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.dto.platform.ReviewPageQueryDTO;
import com.foodie.vo.platform.ReviewVO;


/**
 * 评价服务接口（平台端）
 */
public interface ReviewService {

    /**
     * 评价分页查询
     */
    Page<ReviewVO> pageQuery(ReviewPageQueryDTO reviewPageQueryDTO);

    /**
     * 差评预警列表
     */
    Page<ReviewVO> getBadReviews(Integer page, Integer pageSize);

    /**
     * 删除评价
     */
    void deleteReview(Long id);

    /**
     * 隐藏评价（暂不实现，预留）
     */
    void hideReview(Long id);
}