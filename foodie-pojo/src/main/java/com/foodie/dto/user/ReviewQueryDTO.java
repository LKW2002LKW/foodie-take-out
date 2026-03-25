package com.foodie.dto.user;

/**
 * @Author: wanderer
 * @Date: 2026/1/23 19:14
 */
import lombok.Data;
import java.io.Serializable;

/**
 * 评价查询DTO
 */
@Data
public class ReviewQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商户ID（必填）
     */
    private Long merchantId;

    /**
     * 评分筛选
     * 0-全部 5-好评(5星) 3-中评(3-4星) 1-差评(1-2星)
     */
    private Integer ratingFilter;

    /**
     * 是否只看有图
     */
    private Boolean hasImage;

    /**
     * 当前页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 获取分页起始位置
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}