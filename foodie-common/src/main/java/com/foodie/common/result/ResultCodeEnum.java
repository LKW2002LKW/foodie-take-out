package com.foodie.common.result;

/**
 * Result code enumeration used for uniform API responses.
 */
public enum ResultCodeEnum {
    SUCCESS(1, "成功"),
    FAIL(0, "失败");

    private final int code;
    private final String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

