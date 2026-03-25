package com.foodie.dto.merchant;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单操作DTO
 */
@Data
public class OrderOperationDTO implements Serializable {

    /**
     * 订单ID
     */
    //@NotNull(message = "订单ID不能为空")
    private Long id;

    /**
     * 拒单原因/取消原因
     */
    private String reason;
}