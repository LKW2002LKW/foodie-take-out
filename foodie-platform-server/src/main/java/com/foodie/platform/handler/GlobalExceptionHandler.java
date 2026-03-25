package com.foodie.platform.handler;

import com.foodie.common.constant.MessageConstant;
import com.foodie.common.exception.AccountLockedException;
import com.foodie.common.exception.AccountNotFoundException;
import com.foodie.common.exception.BaseException;
import com.foodie.common.exception.PasswordErrorException;
import com.foodie.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获账号不存在异常
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public Result<Void> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("账号不存在异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获密码错误异常
     */
    @ExceptionHandler(PasswordErrorException.class)
    public Result<Void> handlePasswordErrorException(PasswordErrorException ex) {
        log.error("密码错误异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获账号锁定异常
     */
    @ExceptionHandler(AccountLockedException.class)
    public Result<Void> handleAccountLockedException(AccountLockedException ex) {
        log.error("账号锁定异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBaseException(BaseException ex) {
        log.error("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        log.error("参数校验异常：{}", ex.getMessage());
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(message);
    }

    /**
     * 捕获通用异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常：", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}