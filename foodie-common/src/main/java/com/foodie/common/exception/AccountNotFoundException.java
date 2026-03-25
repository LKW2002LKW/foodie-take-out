package com.foodie.common.exception;

public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException(Integer code) {
        super(code);
    }

    public AccountNotFoundException(String msg) {
        super(msg);
    }
}