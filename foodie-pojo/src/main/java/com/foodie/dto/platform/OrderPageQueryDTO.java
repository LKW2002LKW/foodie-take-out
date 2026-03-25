package com.foodie.dto.platform;

/**
 * @Author: wanderer
 * @Date: 2026/1/15 13:34
 */
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单分页查询DTO（平台端）
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
     * 商户ID
     */
    private Long merchantId;

    /**
     * 用户手机号
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