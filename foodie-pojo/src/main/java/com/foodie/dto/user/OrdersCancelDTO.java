package com.foodie.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 取消订单DTO
 */
@Data
public class OrdersCancelDTO implements Serializable {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long id;

    /**
     * 取消原因
     */
    private String cancelReason;
}