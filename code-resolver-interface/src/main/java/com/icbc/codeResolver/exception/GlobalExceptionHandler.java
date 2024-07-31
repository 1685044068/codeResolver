package com.icbc.codeResolver.exception;

import com.icbc.codeResolver.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常处理，会捕获 controller 所抛出的异常。
 * 捕获到的异常会被处理成结果集返回给前端。
 * @author zmq
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {

        if (e instanceof GlobalException globalException) {
            log.error(globalException.getErrorMsg());
            return Result.failed(globalException.getErrorCode(), globalException.getErrorMsg());
        }
        log.error(e.getMessage());
        return Result.serverFailed(e.getMessage());
    }
}
