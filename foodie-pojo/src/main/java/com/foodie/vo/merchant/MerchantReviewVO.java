package com.foodie.vo.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:26
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户评价VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantReviewVO implements Serializable {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userName;
    private Integer rating;
    private String content;
    private String images;
    private String merchantReply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;

    /**
     * 是否已回复
     */
    private Boolean replied;

    /**
     * 是否差评（评分<=2）
     */
    private Boolean isBadReview;
}