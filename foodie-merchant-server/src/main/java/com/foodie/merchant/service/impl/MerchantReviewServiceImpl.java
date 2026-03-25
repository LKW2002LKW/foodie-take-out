package com.foodie.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.foodie.common.context.BaseContext;
import com.foodie.common.exception.BaseException;

import com.foodie.dto.merchant.ReviewQueryDTO;
import com.foodie.dto.merchant.ReviewReplyDTO;
import com.foodie.entity.OrderReview;
import com.foodie.entity.Orders;
import com.foodie.entity.User;
import com.foodie.merchant.mapper.OrderReviewMapper;

import com.foodie.merchant.mapper.OrdersMapper;
import com.foodie.merchant.mapper.UserMapper;
import com.foodie.merchant.service.MerchantReviewService;

import com.foodie.vo.merchant.MerchantReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家评价服务实现
 */
@Service
@Slf4j
public class MerchantReviewServiceImpl implements MerchantReviewService {

    @Autowired
    private OrderReviewMapper reviewMapper;

    @Autowired
    private OrdersMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 评价分页查询
     */
    @Override
    public Page<MerchantReviewVO> pageQuery(ReviewQueryDTO reviewQueryDTO) {
        Long merchantId = BaseContext.getCurrentId();

        Page<OrderReview> page = new Page<>(reviewQueryDTO.getPage(), reviewQueryDTO.getPageSize());

        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getMerchantId, merchantId)
                .eq(reviewQueryDTO.getRating() != null,
                        OrderReview::getRating, reviewQueryDTO.getRating())
                .ge(reviewQueryDTO.getBeginTime() != null,
                        OrderReview::getCreateTime, reviewQueryDTO.getBeginTime())
                .le(reviewQueryDTO.getEndTime() != null,
                        OrderReview::getCreateTime, reviewQueryDTO.getEndTime())
                .orderByDesc(OrderReview::getCreateTime);

        // 是否回复筛选
        if (reviewQueryDTO.getReplied() != null) {
            if (reviewQueryDTO.getReplied() == 1) {
                queryWrapper.isNotNull(OrderReview::getMerchantReply);
            } else {
                queryWrapper.isNull(OrderReview::getMerchantReply);
            }
        }

        Page<OrderReview> reviewPage = reviewMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<MerchantReviewVO> result = new Page<>();
        result.setCurrent(reviewPage.getCurrent());
        result.setSize(reviewPage.getSize());
        result.setTotal(reviewPage.getTotal());
        result.setPages(reviewPage.getPages());

        List<MerchantReviewVO> voList = reviewPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }

    /**
     * 商户回复评价
     */
    @Override
    @Transactional
    public void replyReview(ReviewReplyDTO reviewReplyDTO) {
        Long merchantId = BaseContext.getCurrentId();

        // 查询评价
        OrderReview review = reviewMapper.selectById(reviewReplyDTO.getId());
        if (review == null) {
            throw new BaseException("评价不存在");
        }

        // 验证评价是否属于当前商户
        if (!review.getMerchantId().equals(merchantId)) {
            throw new BaseException("该评价不属于当前商户");
        }

        // 检查是否已回复
        if (StringUtils.hasText(review.getMerchantReply())) {
            throw new BaseException("该评价已回复，不能重复回复");
        }

        // 更新回复
        LambdaUpdateWrapper<OrderReview> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderReview::getId, reviewReplyDTO.getId())
                .set(OrderReview::getMerchantReply, reviewReplyDTO.getMerchantReply())
                .set(OrderReview::getReplyTime, LocalDateTime.now());

        reviewMapper.update(null, updateWrapper);
        log.info("商户{}回复评价成功，评价ID：{}", merchantId, reviewReplyDTO.getId());
    }

    /**
     * 差评列表
     */
    @Override
    public Page<MerchantReviewVO> getBadReviews(Integer page, Integer pageSize) {
        Long merchantId = BaseContext.getCurrentId();

        Page<OrderReview> reviewPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getMerchantId, merchantId)
                .le(OrderReview::getRating, 2)  // 评分小于等于2星为差评
                .orderByDesc(OrderReview::getCreateTime);

        reviewPage = reviewMapper.selectPage(reviewPage, queryWrapper);

        // 转换为VO
        Page<MerchantReviewVO> result = new Page<>();
        result.setCurrent(reviewPage.getCurrent());
        result.setSize(reviewPage.getSize());
        result.setTotal(reviewPage.getTotal());
        result.setPages(reviewPage.getPages());

        List<MerchantReviewVO> voList = reviewPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        result.setRecords(voList);
        return result;
    }

    /**
     * 转换为VO
     */
    private MerchantReviewVO convertToVO(OrderReview review) {
        // 查询订单信息
        Orders order = orderMapper.selectById(review.getOrderId());

        // 查询用户信息
        User user = userMapper.selectById(review.getUserId());

        return MerchantReviewVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .userId(review.getUserId())
                .userName(user != null ? user.getNickname() : "匿名用户")
                .rating(review.getRating())
                .content(review.getContent())
                .images(review.getImages())
                .merchantReply(review.getMerchantReply())
                .replyTime(review.getReplyTime())
                .createTime(review.getCreateTime())
                .replied(StringUtils.hasText(review.getMerchantReply()))
                .isBadReview(review.getRating() <= 2)
                .build();
    }
}