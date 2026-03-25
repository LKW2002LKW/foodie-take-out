package com.foodie.common.exception;

/**
 * 订单业务异常
 */
public class OrderBusinessException extends RuntimeException {
    public OrderBusinessException(String message) {
        super(message);
    }
}
