package com.foodie.common.handler;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.constant.ResultCodeConstant;
import com.foodie.common.exception.BaseException;
import com.foodie.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
public abstract class AbstractGlobalExceptionHandler {

    protected String getGenericErrorMessage() {
        return MessageConstant.UNKNOWN_ERROR;
    }

    @ExceptionHandler(BaseException.class)
    public Result<String> handleBaseException(BaseException e) {
        log.error("异常信息：{}", e.getMessage());
        return Result.error(
                e.getCode() != null ? e.getCode() : ResultCodeConstant.FAIL,
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace();
        return Result.error(getGenericErrorMessage());
    }
}
