package com.foodie.common.exception;

public class BaseException extends RuntimeException {
    private final Integer code;



    public BaseException(Integer code) {
        this.code = code;
    }



    public BaseException(String message) {
        super(message);
        this.code = 400;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }


}