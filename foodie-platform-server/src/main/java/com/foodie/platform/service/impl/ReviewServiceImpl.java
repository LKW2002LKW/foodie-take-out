package com.foodie.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.foodie.common.exception.BaseException;
import com.foodie.dto.platform.ReviewPageQueryDTO;
import com.foodie.entity.Merchant;
import com.foodie.entity.OrderReview;
import com.foodie.entity.Orders;
import com.foodie.entity.User;
import com.foodie.platform.mapper.MerchantMapper;
import com.foodie.platform.mapper.OrderMapper;
import com.foodie.platform.mapper.OrderReviewMapper;
import com.foodie.platform.mapper.UserMapper;
import com.foodie.platform.service.ReviewService;
import com.foodie.vo.platform.ReviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现（平台端）
 */
@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private OrderReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 评价分页查询
     */
    @Override
    public Page<ReviewVO> pageQuery(ReviewPageQueryDTO reviewPageQueryDTO) {
        Page<OrderReview> reviewPage = new Page<>(reviewPageQueryDTO.getPage(), reviewPageQueryDTO.getPageSize());

        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(reviewPageQueryDTO.getMerchantId() != null,
                        OrderReview::getMerchantId, reviewPageQueryDTO.getMerchantId())
                .eq(reviewPageQueryDTO.getRating() != null,
                        OrderReview::getRating, reviewPageQueryDTO.getRating())
                .ge(reviewPageQueryDTO.getBeginTime() != null,
                        OrderReview::getCreateTime, reviewPageQueryDTO.getBeginTime())
                .le(reviewPageQueryDTO.getEndTime() != null,
                        OrderReview::getCreateTime, reviewPageQueryDTO.getEndTime())
                .orderByDesc(OrderReview::getCreateTime);

        reviewPage = reviewMapper.selectPage(reviewPage, queryWrapper);

        // 转换为VO
        Page<ReviewVO> result = new Page<>();
        result.setCurrent(reviewPage.getCurrent());
        result.setSize(reviewPage.getSize());
        result.setTotal(reviewPage.getTotal());
        result.setPages(reviewPage.getPages());

        List<ReviewVO> voList = reviewPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        result.setRecords(voList);

        return result;
    }

    /**
     * 差评预警列表
     */
    @Override
    public Page<ReviewVO> getBadReviews(Integer page, Integer pageSize) {
        Page<OrderReview> reviewPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(OrderReview::getRating, 2)  // 评分小于等于2星
                .orderByDesc(OrderReview::getCreateTime);

        reviewPage = reviewMapper.selectPage(reviewPage, queryWrapper);

        // 转换为VO
        Page<ReviewVO> result = new Page<>();
        result.setCurrent(reviewPage.getCurrent());
        result.setSize(reviewPage.getSize());
        result.setTotal(reviewPage.getTotal());
        result.setPages(reviewPage.getPages());

        List<ReviewVO> voList = reviewPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        result.setRecords(voList);

        return result;
    }

    /**
     * 删除评价
     */
    @Override
    @Transactional
    public void deleteReview(Long id) {
        OrderReview review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BaseException("评价不存在");
        }

        reviewMapper.deleteById(id);
        log.info("删除评价成功，评价ID：{}", id);
    }

    /**
     * 隐藏评价
     */
    @Override
    @Transactional
    public void hideReview(Long id) {
        // 预留接口，可以在order_review表增加is_hidden字段
        log.info("隐藏评价（暂未实现），评价ID：{}", id);
    }

    /**
     * 转换为VO
     */
    private ReviewVO convertToVO(OrderReview review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);

        // 查询订单信息
        Orders order = orderMapper.selectById(review.getOrderId());
        vo.setOrderNumber(order != null ? order.getOrderNumber() : null);

        // 查询用户信息
        User user = userMapper.selectById(review.getUserId());
        vo.setUserName(user != null ? user.getNickname() : null);
        vo.setUserPhone(user != null ? user.getPhone() : null);

        // 查询商户信息
        Merchant merchant = merchantMapper.selectById(review.getMerchantId());
        vo.setMerchantName(merchant != null ? merchant.getMerchantName() : null);

        return vo;
    }
}