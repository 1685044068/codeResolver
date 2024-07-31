package com.icbc.codeResolver.exception;

import com.icbc.codeResolver.entity.ResultCode;

/**
 * 全局异常，包括服务端和客户端异常
 * @author zmq
 */
public class GlobalException extends RuntimeException {

    protected ResultCode errorCode;

    protected String errorMsg;

    public GlobalException(ResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMsg = message;
    }


    public ResultCode getErrorCode() {
        return errorCode;
    }


    public String getErrorMsg() {
        return errorMsg;
    }

}
