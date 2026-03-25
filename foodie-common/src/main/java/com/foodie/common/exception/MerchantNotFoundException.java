package com.foodie.common.exception;

/**
 * Thrown when a merchant cannot be found.
 */
public class MerchantNotFoundException extends BaseException {

    public MerchantNotFoundException(Integer code) {
        super(code);
    }

    public MerchantNotFoundException(String msg) {
        super(msg);
    }

}

