package com.foodie.dto.merchant;

/**
 * @Author: wanderer
 * @Date: 2026/1/22 11:24
 */

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价查询DTO
 */
@Data
public class ReviewQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 是否回复（0未回复 1已回复）
     */
    private Integer replied;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}