package com.foodie.merchant.handler;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.ResultCodeConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> exceptionHandler(BaseException e) {
        log.error("异常信息：{}", e.getMessage());
        return Result.error(
                e.getCode() != null ? e.getCode() : ResultCodeConstant.FAIL,
                e.getMessage()
        );
    }

    /**
     * 捕获其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace();
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}