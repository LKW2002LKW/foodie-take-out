package com.foodie.dto.user;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单分页查询DTO
 */
@Data
public class OrdersPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;

    /**
     * 订单状态（0表示全部）
     */
    private Integer status;

    /**
     * 是否仅查询待评价订单（已完成且未评价）
     */
    private Boolean pendingReviewOnly;
}