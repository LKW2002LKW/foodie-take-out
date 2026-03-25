package com.foodie.common.exception;

public class PasswordErrorException extends BaseException {

    public PasswordErrorException(Integer code) {
        super(code);
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }
}