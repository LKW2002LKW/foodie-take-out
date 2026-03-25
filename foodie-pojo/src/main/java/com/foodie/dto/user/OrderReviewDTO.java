package com.foodie.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 订单评价DTO
 */
@Data
public class OrderReviewDTO implements Serializable {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 评分（1-5星）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片（多张用逗号分隔）
     */
    private String images;
}