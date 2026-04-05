package com.foodie.user.service.impl;




import com.foodie.common.exception.BusinessException;
import com.foodie.dto.user.ReviewQueryDTO;
import com.foodie.user.mapper.OrderReviewMapper;
import com.foodie.user.service.OrderReviewService;
import com.foodie.vo.user.OrderReviewVO;
import com.foodie.entity.OrderReview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 订单评价服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReviewServiceImpl implements OrderReviewService {

    private final OrderReviewMapper orderReviewMapper;

    @Override
    public Map<String, Object> getReviewPage(ReviewQueryDTO query) {
        // 参数校验
        if (query.getMerchantId() == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询评价列表（返回 VO）
        List<OrderReviewVO> reviews = orderReviewMapper.selectReviewPage(query);
        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        // 处理图片字段：VO 中的 imageList 是 String（单张图片），确保非空或置为 null
        reviews.forEach(review -> {
            if (!StringUtils.hasText(review.getImageList())) {
                // 保持字段为空字符串或 null，根据前端需要，这里设置为 null
                review.setImageList(null);
            }

            // 设置是否有回复
            review.setHasReply(StringUtils.hasText(review.getMerchantReply()));

            // 用户昵称脱敏处理（如需要）
            if (StringUtils.hasText(review.getUserNickname())) {
                String nickname = review.getUserNickname();
                if (nickname.length() > 2) {
                    String masked = nickname.charAt(0) +
                            "*".repeat(nickname.length() - 2) +
                            nickname.charAt(nickname.length() - 1);
                    review.setUserNickname(masked);
                }
            }
        });

        // 查询总数
        Long total = orderReviewMapper.countReviews(query);
        if (total == null) {
            total = 0L;
        }

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / query.getPageSize());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", reviews);
        result.put("total", total);
        result.put("page", query.getPage());
        result.put("pageSize", query.getPageSize());
        result.put("totalPages", totalPages);
        result.put("hasNext", query.getPage() < totalPages);

        return result;
    }

    @Override
    public Map<String, Object> getReviewStats(Long merchantId) {
        if (merchantId == null) {
            throw new BusinessException("商户ID不能为空");
        }

        // 查询统计数据
        Map<String, Object> stats = orderReviewMapper.selectReviewStats(merchantId);

        if (stats == null || stats.isEmpty()) {
            // 没有评价数据，返回默认值
            stats = new HashMap<>();
            stats.put("totalCount", 0L);
            stats.put("avgRating", 5.0);
            stats.put("fiveStarCount", 0L);
            stats.put("mediumCount", 0L);
            stats.put("badCount", 0L);
            stats.put("hasImageCount", 0L);
            stats.put("goodRate", "100.0");
        } else {
            // 计算好评率（4-5星为好评）
            Long totalCount = ((Number) stats.get("totalCount")).longValue();
            Long fiveStarCount = ((Number) stats.get("fiveStarCount")).longValue();
            Long mediumCount = ((Number) stats.get("mediumCount")).longValue();

            if (totalCount > 0) {
                BigDecimal goodRate = BigDecimal.valueOf(fiveStarCount + mediumCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
                stats.put("goodRate", goodRate.toString());

                // 处理平均评分
                Object avgRatingObj = stats.get("avgRating");
                if (avgRatingObj != null) {
                    BigDecimal avgRating = new BigDecimal(avgRatingObj.toString())
                            .setScale(1, RoundingMode.HALF_UP);
                    stats.put("avgRating", avgRating.doubleValue());
                }
            } else {
                stats.put("goodRate", "100.0");
            }
        }

        return stats;
    }

    @Override
    public OrderReviewVO getReviewById(Long reviewId) {
        if (reviewId == null) {
            throw new BusinessException("评价ID不能为空");
        }

        // 查询评价（实体）
        OrderReview review = orderReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 转换为VO
        OrderReviewVO vo = new OrderReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 处理图片字段：实体 images 是单字符串，VO.imageList 也为单字符串
        vo.setImageList(review.getImages());

        // 设置是否有回复
        vo.setHasReply(StringUtils.hasText(review.getMerchantReply()));

        return vo;
    }

    @Override
    public Map<String, Object> getMyReviewPage(Long userId, Integer page, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        int safePage = (page == null || page < 1) ? 1 : page;
        int safePageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        int offset = (safePage - 1) * safePageSize;

        List<OrderReviewVO> reviews = orderReviewMapper.selectMyReviewPage(userId, offset, safePageSize);
        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        reviews.forEach(review -> {
            if (!StringUtils.hasText(review.getImageList())) {
                review.setImageList(null);
            }
            review.setHasReply(StringUtils.hasText(review.getMerchantReply()));
            if (StringUtils.hasText(review.getUserNickname())) {
                String nickname = review.getUserNickname();
                if (nickname.length() > 2) {
                    String masked = nickname.charAt(0) +
                            "*".repeat(nickname.length() - 2) +
                            nickname.charAt(nickname.length() - 1);
                    review.setUserNickname(masked);
                }
            }
        });

        Long total = orderReviewMapper.countMyReviews(userId);
        if (total == null) {
            total = 0L;
        }
        int totalPages = (int) Math.ceil((double) total / safePageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("list", reviews);
        result.put("total", total);
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        result.put("totalPages", totalPages);
        result.put("hasNext", safePage < totalPages);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyReview(Long userId, Long reviewId) {
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        if (reviewId == null) {
            throw new BusinessException("评价ID不能为空");
        }

        OrderReview review = orderReviewMapper.selectById(reviewId);
        if (review == null || !userId.equals(review.getUserId())) {
            throw new BusinessException("评价不存在");
        }

        int affected = orderReviewMapper.deleteById(reviewId);
        if (affected <= 0) {
            throw new BusinessException("评价不存在");
        }
    }

}