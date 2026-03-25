package com.foodie.dto.merchant;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单分页查询DTO
 */
@Data
public class OrderPageQueryDTO implements Serializable {

    private Integer page = 1;

    private Integer pageSize = 10;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
