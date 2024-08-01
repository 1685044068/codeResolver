package com.icbc.codeResolver.exception;

import com.icbc.codeResolver.entity.Result;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，会捕获 controller 所抛出的异常。
 * 捕获到的异常会被处理成结果集返回给前端。
 * @author zmq
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {

        if (e instanceof GlobalException globalException) {
            log.error(globalException.getErrorMsg());
            return Result.error(globalException.getErrorCode(), globalException.getErrorMsg());
        }
        if (e instanceof ServletException && e instanceof ErrorResponse) {
            log.error(e.getMessage());
            HttpStatusCode statusCode = ((ErrorResponse) e).getStatusCode();
            if (statusCode.is4xxClientError()) {
                return Result.clientErrorWithCode(statusCode.value(),e.getMessage());
            }
            if (statusCode.is5xxServerError()) {
                return Result.serverErrorWithCode(statusCode.value(),e.getMessage());
            }
        }
        log.error(e.getMessage());
        return Result.serverError(e.getMessage());
    }
}
