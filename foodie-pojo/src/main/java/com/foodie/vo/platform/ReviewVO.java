package com.foodie.vo.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:48
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO implements Serializable {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long merchantId;
    private String merchantName;
    private Integer rating;
    private String content;
    private String images;
    private String merchantReply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}