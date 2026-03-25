package com.foodie.common.exception;

public class UserNotLoginException extends BaseException {

    public UserNotLoginException(Integer code) {
        super(code);
    }

    public UserNotLoginException(String msg) {
        super(msg);
    }

}
