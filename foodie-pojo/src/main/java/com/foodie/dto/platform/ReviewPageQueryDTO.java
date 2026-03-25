package com.foodie.dto.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/16 16:45
 */
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价分页查询DTO
 */
@Data
public class ReviewPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}