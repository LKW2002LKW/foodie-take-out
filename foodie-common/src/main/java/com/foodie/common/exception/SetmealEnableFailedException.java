package com.foodie.common.exception;

/**
 * 套餐启用失败异常
 */
public class SetmealEnableFailedException extends BaseException {

    public SetmealEnableFailedException(Integer code){super(code);}

    public SetmealEnableFailedException(String msg){
        super(msg);
    }
}
