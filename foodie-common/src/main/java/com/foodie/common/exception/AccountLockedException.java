package com.foodie.common.exception;

public class AccountLockedException extends BaseException {

    public AccountLockedException(Integer code) {
        super(code);
    }

    public AccountLockedException(String msg) {
        super(msg);
    }
}